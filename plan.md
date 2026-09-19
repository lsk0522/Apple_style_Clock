# Apple Style Clock (Android StandBy) — 구현 계획서

> iPhone의 **StandBy 모드**(무선 충전 중 가로 거치 시 나타나는 시계/위젯 화면)를
> Android에서 재현하는 프로젝트.
> 최종 목표: **Google Play 스토어 정식 배포**

| | |
|---|---|
| **문서 버전** | v0.3 (2026-09-19) |
| **상태** | 주요 결정 확정 · Phase 0 착수 대기 |
| **앱 이름** | **Nightstand** |
| **패키지명** | `com.lsk0522.nightstand` (배포 후 변경 불가) |
| **저장소** | https://github.com/lsk0522/Apple_style_Clock |
| **개발 브랜치** | `main_code` → 기능 브랜치 → `main` 병합 |
| **테스트 기기** | **Galaxy S25 Ultra** (One UI 7 / Android 15) |
| **v0.1 MVP 범위** | **Phase 0 ~ 5** (약 3주) |

---

## 0. 한눈에 보는 요약

| 항목 | 결정 |
|---|---|
| 언어 / UI | Kotlin + Jetpack Compose |
| 최소 지원 | **Android 10 (API 29)** — 확정 |
| 타깃 | API 36 |
| 아키텍처 | MVVM + Clean Architecture, 멀티 모듈 |
| DI | Hilt |
| 저장소 | DataStore (Preferences + Proto) |
| 화면 진입 | **오버레이 권한 + DreamService 하이브리드** |
| 위젯 | **AppWidgetHost — 기기에 설치된 진짜 위젯 호스팅** (갤럭시 위젯 포함) |
| 후원 | Google Play Billing v7 (소모성 상품) |
| CI | GitHub Actions (빌드 · APK 자동 업로드) |

---

## 1. 제품 정의

### 1-1. 동작 방식 (요청하신 시나리오)

```
[앱 실행]
   └─ 메인 화면 (하단 5개 섹션 탭바 · 전부 이미지 아이콘)
        ├─ 1. 위젯        : 폰에 설치된 위젯 추가 / 정렬 / 자동넘김 설정
        ├─ 2. 충전 판별   : "무선만 / 유선만 / 둘 다 / 도크" 중 선택
        ├─ 3. 메인 메뉴   : 시계 스타일, 테마, 미리보기, 전체 설정
        ├─ 4. 개발자 모드 : 로그, 센서 실시간값, 충전 이벤트 시뮬레이션
        └─ 5. 후원        : Play 결제로 개발자 후원

[충전 케이블 / 무선 패드 연결]
   └─ 충전 방식 감지 (무선 / 유선 / 도크)
        └─ 2번 탭에서 사용자가 선택한 조건과 일치?
             ├─ NO  → 아무 일도 일어나지 않음
             └─ YES → 2초 대기 → 서서히 페이드인 (StandBy 켜짐)
                        └─ 충전 해제 → 서서히 페이드아웃 → 종료
```

### 1-2. 핵심 가치

1. **애플다운 디테일** — 곡률, 여백, 타이밍 함수, 관성 스크롤까지 재현
2. **안드로이드다운 자유도** — 아이폰과 달리 **폰에 깔린 모든 위젯**을 올릴 수 있음
3. **거슬리지 않음** — 배터리 소모 최소, 번인 방지, 야간 자동 어두워짐
4. **누구나 5분 안에 설정 완료** — 권한 온보딩을 손잡고 안내

---

## 2. 기술 설계

### 2-1. 충전 방식 판별 (유선 / 무선 / 도크)

```kotlin
val status = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
when (status?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
    BatteryManager.BATTERY_PLUGGED_AC       -> ChargeType.WIRED_AC     // 충전기
    BatteryManager.BATTERY_PLUGGED_USB      -> ChargeType.WIRED_USB    // PC USB
    BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargeType.WIRELESS     // 무선
    BatteryManager.BATTERY_PLUGGED_DOCK     -> ChargeType.DOCK         // API 33+
    else                                    -> ChargeType.NONE
}
```

**실제 기기에서 터질 이슈들**

- 삼성 포함 일부 기기는 무선 충전 시작 직후 `AC`로 잠깐 보고한 뒤 `WIRELESS`로 바뀜
  → **1.5초 디바운스 + 상태머신**으로 확정 후 판정
- 역무선충전(배터리 공유, S25 Ultra 지원)중에 `WIRELESS`로 오탐되면 안 됨
  → 배터리 잔량 증감 추세로 교차검증
- `BATTERY_PLUGGED_DOCK`은 API 33부터 → 하위는 `UiModeManager.currentModeType`(CAR/DESK) 보조 판별

**이벤트 수신 경로**

- `ACTION_POWER_CONNECTED` / `ACTION_POWER_DISCONNECTED`는 Android 8+ 암시적 브로드캐스트 제한의 **예외 목록**에 포함 → 매니페스트 등록 가능
- 수신 즉시 `ChargingMonitorService`(포그라운드 서비스, `specialUse` 타입) 기동
- 보조 안전망: `BOOT_COMPLETED` 시 현재 충전 상태 재확인

### 2-2. 백그라운드에서 화면 띄우기 — 최대 난관 ①

Android 10(API 29)부터 **백그라운드 앱은 액티비티를 마음대로 띄울 수 없습니다.**

| 경로 | 장점 | 단점 | 채택 |
|---|---|---|---|
| **A. DreamService**(화면 보호기) | 권한 0개, Play 정책 100% 안전, OS가 "충전 중 화면보호기 시작" 옵션 기본 제공 | 화면이 꺼질 타이밍에만 발동, "2초 뒤 즉시" 불가, 설정에서 수동 지정 필요 | 보조 |
| **B. SYSTEM_ALERT_WINDOW**(다른 앱 위에 표시) | 이 권한이 있으면 **백그라운드 액티비티 실행이 허용됨** → "2초 뒤 페이드인" 완벽 구현 | 사용자가 설정에서 직접 허용 | **주력** |
| C. Full-screen Intent | 즉시 실행 | Android 14+ 부터 통화/알람 앱만 자동 승인 → **부적합** | 미채택 |

> **결론: B 주력 + A 보조 하이브리드.**
> 온보딩에서 B를 안내하되, 거부해도 A(화면보호기)로 동작 → 어떤 상황에서도 "먹통"이 되지 않음.

**잠금화면 위 표시**

```kotlin
setShowWhenLocked(true)   // 잠금화면 위에 표시
setTurnScreenOn(true)     // 꺼진 화면 켜기
// requestDismissKeyguard 는 호출하지 않음 — 보안상 잠금은 유지
```

### 2-3. 위젯 — 기기에 설치된 진짜 위젯 호스팅 · 최대 난관 ②

> **요구사항**: "휴대폰에 기본 장착된 위젯을 그대로 넣을 수 있어야 한다.
> 갤럭시면 갤럭시 위젯이 들어가야 한다."
> → 자체 제작 위젯이 아니라 **`AppWidgetHost` 기반 실제 위젯 호스팅**이 핵심 기능.
> (서드파티 런처 — Nova, Niagara 등 — 가 쓰는 것과 같은 방식)

**전체 흐름**

```kotlin
// 1) 설치된 위젯 목록 조회 → 애플 스타일 위젯 피커 구성
val providers = appWidgetManager.installedProviders   // 삼성 시계/날씨/캘린더 전부 여기 포함

// 2) ID 할당
val widgetId = appWidgetHost.allocateAppWidgetId()

// 3) 바인딩 — 서드파티 앱은 바로 안 되므로 사용자 승인 인텐트를 띄움
if (!appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, provider.provider)) {
    Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
    }  // → 시스템이 "위젯 추가를 허용할까요?" 다이얼로그 표시 (최초 1회)
}

// 4) 설정 액티비티가 있는 위젯이면 실행
if (provider.configure != null)
    appWidgetHost.startAppWidgetConfigureActivityForResult(...)

// 5) 뷰 생성 → Compose 에 AndroidView 로 삽입
val hostView = appWidgetHost.createView(context, widgetId, provider)
hostView.updateAppWidgetSize(Bundle(), listOf(SizeF(tileW, tileH)))   // API 31+
```

- `AppWidgetHost.startListening()` / `stopListening()` 을 StandBy 화면 수명주기에 연결
- `widgetId` + `ComponentName` + 위치/크기를 DataStore 에 영속화 (재부팅 후 복원)
- 제거 시 반드시 `deleteAppWidgetId()` — 안 하면 위젯 ID 누수

**예상되는 문제와 대응**

| 문제 | 대응 |
|---|---|
| 일부 삼성 위젯이 "런처 호스트"에서만 정상 동작 | 화이트리스트/블랙리스트 + 실패 시 대체 카드로 폴백 |
| 위젯 배경이 밝아 검정 StandBy와 충돌 | 어둡게 틴트 옵션 · 애플풍 프레임(스퀘어클 클립) 제공 |
| 위젯 내부 터치와 StandBy 스와이프 제스처 충돌 | 가장자리 영역만 스와이프 인식 · 편집 모드에서는 터치 차단 |
| 위젯 갱신 주기(`updatePeriodMillis` 최소 30분) | StandBy 활성 중에는 강제 갱신 요청 + 자체 시계는 1초 단위 자체 렌더 |
| 고해상도(QHD+)에서 위젯 크기 계산 | `OPTION_APPWIDGET_MIN/MAX_WIDTH` dp 정확히 전달, 스케일 보정 |
| 잠금화면 위에서 위젯의 `PendingIntent` 실행 | 잠금 상태에서는 탭 차단 또는 잠금 해제 요구 |

**병행 제공: 내장 위젯 (애플 감성 담당)**
시스템 위젯만으로는 StandBy 특유의 룩을 못 내므로, 직접 만든 위젯도 함께 제공:
시계 · 캘린더 · 날씨 · 배터리 · 음악 컨트롤 · 타이머 · 세계시계 · 사진 · 메모

**자동 넘김 (Smart Rotate)**

- Apple StandBy처럼 **세로 스택** 스와이프 → `VerticalPager`
- 좌우 2분할 패널(왼쪽 스택 / 오른쪽 스택) — StandBy 원본 구조 그대로
- 자동 전환 주기: 5초 / 10초 / 30초 / 1분 / 끔
- 사용자가 손으로 넘기면 자동 전환 10초 일시정지

### 2-4. 화면 유지 · 밝기 · 번인 방지

| 기능 | 구현 |
|---|---|
| 화면 켜둠 | `FLAG_KEEP_SCREEN_ON` (WakeLock 대신 — 배터리 안전) |
| 밝기 제어 | `window.attributes.screenBrightness` (시스템 설정 미변경) |
| 야간 모드 | `Sensor.TYPE_LIGHT` 조도 5 lux 미만 → 붉은 색조 + 최저 밝기 |
| 번인 방지 | 60초마다 ±4dp 픽셀 시프트, 3초 이징 애니메이션 |
| 화면 깨우기 | 가속도 센서(흔들기) / 근접 센서(손 흔들기) |
| 주사율 절감 | **S25 Ultra는 LTPO 1~120Hz** → `preferredRefreshRate = 1f`로 저전력 유지 |
| 화면 방향 | 가로 고정(`SENSOR_LANDSCAPE`), 세로용 별도 레이아웃 제공 |

### 2-5. 디자인 시스템 — "애플다움"의 정체

| 요소 | 원본(Apple) | 우리 구현 |
|---|---|---|
| 서체 | SF Pro Display | **SF Pro는 Apple 플랫폼 전용 라이선스라 번들 불가**<br>→ 영문 `Inter`, 한글 `Pretendard`(SF 메트릭 호환, OFL) |
| 모서리 | Continuous Corner (스퀘어클) | Compose 커스텀 `Shape` 직접 구현 (`RoundedCornerShape`로는 재현 불가) |
| 색 | 순수 블랙 `#000000` | 동일 (OLED 절전). 텍스트 화이트 90% opacity |
| 모션 | spring, cubic-bezier(0.25, 0.1, 0.25, 1) | `spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)` |
| 블러 | UIBlurEffect | API 31+ `RenderEffect.createBlurEffect` |
| 햅틱 | Taptic Engine | `HapticFeedbackConstants.CONFIRM` / 커스텀 `VibrationEffect` |

> **법적 메모**: Apple UI를 *참고한 디자인*은 문제없지만, Apple의 **폰트 파일 · 아이콘 에셋 · 로고를 그대로 복사해 배포하면 안 됩니다.** 아이콘은 직접 제작하거나 오픈소스(Lucide, Phosphor)를 사용하고, 스토어 등록명에 "Apple / iPhone / iOS"를 제품명처럼 쓰지 않습니다. → 배포용 이름은 §10 Q1 참조.

### 2-6. 시계 페이스 (6종)

| # | 이름 | 설명 |
|---|---|---|
| 1 | **Digital** | 초대형 숫자, 컬러 커스텀 |
| 2 | **Analog** | Apple Watch 스타일 원형, 부드러운 초침(sweep) |
| 3 | **Flip** | 플립 카드 애니메이션 |
| 4 | **World** | 다중 시간대 + 지구본 |
| 5 | **Solar** | 태양 그라데이션 (시간대별 색 변화) |
| 6 | **Float** | 떠다니는 아웃라인 숫자 |

---

## 3. 테스트 기기: Galaxy S25 Ultra 전용 고려사항

| 항목 | 내용 |
|---|---|
| OS | Android 15 (One UI 7) → Android 16 (One UI 8) 업데이트 대응 |
| 화면 | 6.9" QHD+ (3120×1440), LTPO 1~120Hz → 저주사율 고정으로 절전 |
| 충전 | Qi2 / 무선 15W, 역무선충전 지원 → **역충전 오탐 방지 로직 필수** |
| **Daily Board 충돌** | One UI에 이미 "충전 중 화면 표시(Daily Board)" 기능이 있음 → 동시에 뜨면 충돌. **설정에서 끄도록 온보딩에서 안내** |
| 배터리 최적화 | 삼성은 "사용 안 함 앱 절전", "자동 실행 방지"가 공격적 → **절전 예외 등록 가이드 필수** |
| Good Lock | 일부 모듈이 화면보호기 동작에 간섭 가능 → 개발자 모드에서 진단 항목 제공 |
| 위젯 | 삼성 기본 위젯(시계·날씨·캘린더·리마인더·모드 및 루틴 등)이 1차 검증 대상 |
| 디버깅 | 무선 디버깅(`adb pair`)으로 거치 상태 그대로 테스트 가능 |

---

## 4. 프로젝트 구조 (예정)

```
Apple_style_Clock/
├─ app/                        # 앱 진입점, 조립
├─ core/
│  ├─ design/                  # 디자인 시스템 (색·타이포·모션·스퀘어클)
│  ├─ common/                  # 공통 유틸
│  └─ data/                    # DataStore, 설정 영속화
├─ feature/
│  ├─ main/                    # 메인 화면 + 하단 5섹션 탭바
│  ├─ standby/                 # StandBy 본 화면 (시계·위젯 렌더링)
│  ├─ widgets/                 # AppWidgetHost, 위젯 피커, 자동넘김
│  ├─ charging/                # 충전 감지 엔진, 서비스, 리시버
│  ├─ developer/               # 개발자 모드
│  └─ donate/                  # 후원 (Play Billing)
├─ .github/workflows/          # CI
├─ docs/                       # 스크린샷, 설계 문서
├─ CLAUDE.md                   # AI 세션용 프로젝트 컨텍스트
├─ PROGRESS.md                 # 진행 상황 · 세션 인수인계
├─ plan.md
└─ README.md
```

---

## 5. 단계별 구현 계획 (Phase)

각 Phase가 끝날 때마다 **동작하는 APK**가 나오도록 분할했습니다.

### Phase 0 — 개발 환경 & 뼈대 · 1~2일

> **현재 이 PC에는 JDK도 Android SDK도 설치되어 있지 않습니다.** 가장 먼저 해결해야 합니다.

- [ ] JDK 17 설치 (Temurin 권장)
- [ ] Android Studio 설치 + SDK 36 / Build Tools
- [ ] Gradle 프로젝트 생성 (Kotlin DSL, Version Catalog)
- [ ] 멀티 모듈 뼈대 + Hilt + Compose 세팅
- [ ] GitHub 원격 연결 (`lsk0522/Apple_style_Clock`)
- [ ] **GitHub Actions: 푸시마다 빌드 + 디버그 APK 아티팩트 업로드**
      → 로컬 환경이 없어도 폰에 설치해볼 수 있는 안전망
- [ ] `.gitignore`, 라이선스, `CLAUDE.md`, `PROGRESS.md`

### Phase 1 — 디자인 시스템 & 메인 화면 · 3~4일

- [ ] 스퀘어클 Shape, 컬러 토큰, 타이포(Pretendard/Inter), 모션 스펙
- [ ] iOS풍 컴포넌트: 리스트 셀, 토글, 세그먼트 컨트롤, 슬라이더, 모달 시트
- [ ] **하단 5섹션 탭바** (이미지 아이콘, 블러 배경, 선택 애니메이션)
- [ ] 5개 탭 껍데기 화면 + 네비게이션
- [ ] 다크 / 라이트 테마
- **결과물**: 만져볼 수 있는 메인 화면

### Phase 2 — 충전 감지 엔진 · 2~3일

- [ ] `ChargeType` 판별 로직 + 디바운스 상태머신 + 역충전 교차검증
- [ ] `PowerConnectionReceiver` + `ChargingMonitorService`(포그라운드)
- [ ] **2번 탭: 유선/무선 판별 화면** — 무선만 / 유선만 / 둘 다 / 도크
- [ ] 현재 충전 상태 실시간 카드 (방식 · 전압 · 온도 · 잔량)
- [ ] 삼성 절전 예외 · Daily Board 끄기 온보딩
- **결과물**: S25 Ultra 무선패드에 올리면 "무선 충전 감지됨" 표시

### Phase 3 — StandBy 화면 실행 · 4~5일 · 핵심 ①

- [ ] 오버레이 권한 온보딩 (거부해도 진행 가능)
- [ ] `StandByActivity` — 잠금화면 위 표시, 화면 켜기, 가로 고정
- [ ] **2초 지연 → 800ms 페이드인** (Apple 이징 곡선)
- [ ] 충전 해제 → 페이드아웃 → 종료
- [ ] `DreamService` 구현(보조 경로) + 시스템 설정 안내
- [ ] 화면 유지, 밝기 제어, 저주사율 고정
- **결과물**: **무선 충전 올리면 진짜로 시계가 뜸** — 프로젝트의 심장

### Phase 4 — 시계 페이스 6종 · 5~7일

- [ ] Digital / Analog / Flip / World / Solar / Float
- [ ] 컬러 커스터마이즈, 12·24시간, 초 표시, 날짜 표시
- [ ] 메인 메뉴에서 미리보기 + 선택
- **결과물**: 시계만으로도 쓸 만한 앱

### Phase 5 — 위젯 호스팅 · 6~8일 · 핵심 ②

- [ ] `StandByWidgetHost` (AppWidgetHost 확장) 구현
- [ ] `ACTION_APPWIDGET_BIND` 사용자 승인 흐름
- [ ] 설정 액티비티가 있는 위젯 처리
- [ ] **애플 스타일 위젯 피커** — 설치된 위젯을 앱별로 그룹핑, 미리보기
- [ ] Compose `AndroidView` 통합 + 스퀘어클 클립 + 틴트 옵션
- [ ] **1번 탭: 위젯 관리** — 추가 / 삭제 / 드래그 정렬
- [ ] 위젯 ID 영속화 + 재부팅 복원
- [ ] **삼성 기본 위젯 실기기 호환성 검증** (시계 · 날씨 · 캘린더 · 리마인더)
- **결과물**: 갤럭시 위젯이 StandBy 화면에 올라감

> ### 🎯 여기까지가 **v0.1 MVP** — 폰에 설치해 실제로 쓸 수 있는 첫 버전
> 이 지점에서 태그 `v0.1.0` 을 찍고 S25 Ultra 실사용 피드백을 받은 뒤 Phase 6으로 진행합니다.

### Phase 6 — 스택 & 자동 넘김 · 3~4일

- [ ] 좌우 2분할 패널 + `VerticalPager` 세로 스택
- [ ] **자동 넘김**(5s / 10s / 30s / 1m / 끔) + 조작 시 일시정지
- [ ] 위젯 터치 vs 스와이프 제스처 충돌 해결
- [ ] 내장 위젯 세트(시계·캘린더·배터리·음악·타이머·사진)

### Phase 7 — 야간 모드 · 센서 · 번인 방지 · 2~3일

- [ ] 조도 센서 → 야간 레드 모드 자동 전환
- [ ] 번인 방지 픽셀 시프트
- [ ] 흔들어서 깨우기 / 근접 센서
- [ ] 취침 시간대 자동 어둡게 (스케줄)

### Phase 8 — 개발자 모드 · 2일

- [ ] **4번 탭**: 실시간 로그 뷰어, 센서 값 모니터
- [ ] 충전 이벤트 **시뮬레이션** (실제로 꽂지 않고 테스트)
- [ ] StandBy 강제 실행, FPS / 메모리 오버레이
- [ ] 위젯 호스트 진단 (바인딩 실패 목록)
- [ ] 설정 JSON 내보내기 · 가져오기

### Phase 9 — 후원 (Play Billing) · 2~3일

- [ ] **5번 탭**: Google Play Billing v7 연동
- [ ] 소모성 상품 3종 (커피 한 잔 / 밥 한 끼 / 든든한 후원)
- [ ] 결제 완료 애니메이션 + 후원자 배지
- [ ] Play 정책상 앱 내 디지털 후원은 **반드시 Play 결제** 사용

### Phase 10 — 완성도 · 4~5일

- [ ] 다국어 (한국어 / 영어 / 일본어)
- [ ] 접근성 (TalkBack, 큰 글씨, 고대비)
- [ ] 배터리 프로파일링 (목표: StandBy 1시간당 5% 이하)
- [ ] 기기 호환성 (픽셀 / 샤오미 / 태블릿 / 폴더블)
- [ ] 크래시 리포팅 (Firebase Crashlytics 또는 ACRA)

### Phase 11 — 배포 · 3~4일

- [ ] 앱 이름 / 아이콘 / 브랜딩 최종 결정 (상표 리스크 검토)
- [ ] 서명 키 생성 + GitHub Secrets 등록
- [ ] Release AAB 빌드, R8 난독화 규칙
- [ ] 개인정보처리방침 페이지 (GitHub Pages)
- [ ] Play Console: 데이터 안전 섹션, 스크린샷, 소개 영상
- [ ] 내부 테스트 → 비공개 테스트(신규 개발자 요건: 테스터 12명 · 14일) → 프로덕션
- [ ] GitHub Actions: 태그 푸시 시 자동 릴리스

---

## 6. 일정 요약

| Phase | 내용 | 예상 |
|---|---|---|
| 0 | 환경 · 뼈대 | 1~2일 |
| 1 | 디자인 시스템 · 메인 | 3~4일 |
| 2 | 충전 감지 | 2~3일 |
| 3 | **StandBy 실행** | 4~5일 |
| 4 | 시계 6종 | 5~7일 |
| 5 | **위젯 호스팅** | 6~8일 |
| 6 | 스택 · 자동넘김 | 3~4일 |
| 7 | 센서 · 야간모드 | 2~3일 |
| 8 | 개발자 모드 | 2일 |
| 9 | 후원 | 2~3일 |
| 10 | 완성도 | 4~5일 |
| 11 | 배포 | 3~4일 |
| | **합계** | **약 37~50일** |

---

## 7. 세션 연속성 — 토큰 소진 대비 (중요)

> 대화 토큰이 떨어져도 **다음 세션이 바로 이어서 작업**할 수 있도록 하는 규칙.

### 7-1. 원칙

1. **작은 커밋** — 기능 하나 = 커밋 하나. 절대 커밋을 몰아두지 않음
2. **커밋할 때마다 `PROGRESS.md` 동시 갱신** — 같은 커밋에 포함
3. **세션당 Phase 1개**를 넘기지 않음 (크면 Phase를 쪼갬)
4. 토큰이 **20% 남으면 작업 중단** → 커밋 + `PROGRESS.md` 마무리 + 인수인계 메모 작성
5. 애매한 결정은 코드에 `// TODO(decision):` 주석 + `PROGRESS.md` "미결정" 항목에 기록

### 7-2. `PROGRESS.md` 구조

```markdown
## 현재 상태
- 진행 중 Phase: 3 / 11
- 마지막 커밋: abc1234
- 빌드 상태: 성공 (CI #42)

## 완료
- [x] Phase 0 ...
## 진행 중
- [ ] StandByActivity 페이드인 (80%) — 파일: feature/standby/.../StandByActivity.kt

## 다음 할 일 (우선순위 순)
1. ...

## 미결정 / 막힌 것
- ...

## 다음 세션 시작 방법
"plan.md 와 PROGRESS.md 읽고 Phase 3 이어서 진행해줘"
```

### 7-3. `CLAUDE.md`

새 세션이 코드를 다시 훑지 않아도 되도록 아키텍처 · 컨벤션 · 주의사항을 담음.
Claude Code가 세션 시작 시 자동으로 읽는 파일.

### 7-4. Git 컨벤션

```
feat(widgets): AppWidgetHost 바인딩 승인 흐름 구현
fix(standby): 페이드인 중 화면 회전 시 크래시 수정
design(ui): 스퀘어클 Shape 구현
docs(progress): Phase 3 진행 상황 갱신
chore(ci): GitHub Actions 빌드 워크플로 추가
```

브랜치: `main` ← `main_code` ← `feat/phaseN-xxx`
Phase 완료 시 태그: `v0.1.0-phase1`

---

## 8. 위험 요소 & 대응

| 위험 | 영향 | 대응 |
|---|---|---|
| 백그라운드 액티비티 실행 차단 | 치명적 | 오버레이 권한 + DreamService 이중화 |
| **삼성 위젯이 서드파티 호스트에서 오동작** | 높음 | 실기기 조기 검증(Phase 5 착수 즉시), 실패 위젯 블랙리스트 + 폴백 |
| 삼성 절전 정책으로 서비스 강제 종료 | 높음 | 절전 예외 온보딩 + 제조사별 가이드 |
| One UI Daily Board 와 충돌 | 중간 | 온보딩에서 끄기 안내 + 개발자 모드 진단 |
| SF Pro 폰트 라이선스 | 중간 | Pretendard + Inter 대체 (무료 · 상업 이용 가능) |
| Apple 디자인 상표 이슈 | 중간 | 에셋 직접 제작, 앱 이름에 Apple/iOS 미사용 |
| OLED 번인 | 중간 | 픽셀 시프트 + 야간 자동 디밍 |
| Play 신규 개발자 테스트 요건(12명/14일) | 일정 | Phase 10부터 테스터 미리 모집 |
| 로컬에 JDK / Android SDK 없음 | 즉시 | Phase 0에서 설치 + CI 빌드 병행 |
| **대화 토큰 소진으로 작업 단절** | 높음 | §7 세션 연속성 규칙 |

---

## 9. 향후 계획 (v1.0 이후 로드맵)

### v1.1 — 확장
- Wear OS 동반 앱 (워치에서 같은 시계 페이스)
- 태블릿 / 폴더블 전용 레이아웃 (커버 화면 StandBy 포함)
- 시계 페이스 공유 (사용자 제작 테마)

### v1.2 — 스마트
- 자동화 연동 (Tasker, 삼성 모드 및 루틴)
- NFC 태그 → 위치별(침실 / 책상 / 차량) StandBy 프로필 자동 전환
- Bluetooth 기기 연결 감지 (차량 도크 모드)

### v1.3 — 개인화
- 사진 앨범 슬라이드쇼 (Ken Burns 효과)
- 사용자 정의 위젯
- 알람 / 수면 추적 연동

### v2.0 — 플랫폼
- Google Home / Matter 연동 (스마트홈 컨트롤 위젯)
- 음성 명령
- 클라우드 설정 동기화

---

## 10. 확정된 결정 사항

| # | 항목 | 결정 | 근거 |
|---|---|---|---|
| **D1** | 앱 이름 / 패키지 | **Nightstand** / `com.lsk0522.nightstand` | 상표 안전(Apple·iOS 미사용), 기능 직관적. 저장소 이름은 `Apple_style_Clock` 유지 |
| **D2** | 최소 지원 버전 | **API 29 (Android 10)** | 기기 약 95% 커버 + 백그라운드 실행 제한 기준선과 일치해 레거시 분기 최소화 |
| **D3** | v0.1 MVP 범위 | **Phase 0 ~ 5** (약 3주) | 충전 감지 · StandBy 실행 · 시계 · 위젯 호스팅까지 = 실사용 가능한 최소 단위 |
| **D4** | 자체 날씨 위젯 | **v1.1로 연기** | 삼성 날씨 위젯을 호스팅하면 중복. API 키 관리 비용도 절감 |
| **D5** | 후원 방식 | **Google Play Billing v7** | 앱 내 디지털 후원에 외부 결제 링크를 쓰면 Play 정책 위반 소지 |
| **D6** | 개발자 모드 | **하단 탭에 상시 노출** | 요청하신 5탭 구성 그대로 유지 |
| **D7** | 문서 푸시 시점 | 계획 확정 후 일괄 푸시 | 저장소 히스토리를 깔끔하게 유지 |

### 아직 열려 있는 항목 (해당 Phase 도달 시 결정)

| # | 항목 | 결정 시점 |
|---|---|---|
| O1 | 앱 아이콘 / 브랜딩 비주얼 | Phase 11 |
| O2 | 다국어 범위 (한 / 영 / 일) | Phase 10 |
| O3 | 크래시 리포팅 — Crashlytics vs ACRA | Phase 10 |
| O4 | 후원 상품 금액대 | Phase 9 |
