# CLAUDE.md — AI 세션용 프로젝트 컨텍스트

> 새 세션이 코드베이스를 다시 훑지 않고 바로 작업할 수 있도록 하는 요약본.
> 상세 계획은 `plan.md`, 현재 진행 상황은 `PROGRESS.md`.

## 프로젝트 한 줄 요약

**무선/유선 충전을 감지해 2초 뒤 애플 StandBy 스타일 시계·위젯 화면을 띄우는 안드로이드 앱.**

| | |
|---|---|
| 앱 이름 | **Nightstand** |
| 패키지명 | `com.lsk0522.nightstand` (변경 금지) |
| 저장소 | https://github.com/lsk0522/Apple_style_Clock (저장소명은 앱 이름과 다름 — 정상) |
| 테스트 기기 | **Galaxy S25 Ultra** (One UI 7 / Android 15) |
| v0.1 MVP | **Phase 0 ~ 5** |

## 문서 지도 — 어디를 고칠지

| 문서 | 역할 | 대표 내용 |
|---|---|---|
| **Design.md** | **어떻게 보이는가** | 색·타이포·곡률·여백·모션 **수치 원장** |
| **iOS_Design.md** | **무엇을 만드는가** | StandBy 3단 화면 구조, 시계 6종, iOS→Compose 매핑 |
| **CLAUDE.md** (이 문서) | **어떻게 구현하는가** | 컴포넌트 규칙, 빌드 함정, 작업 규칙 |
| **plan.md** | **언제 만드는가** | Phase 0~11 일정 |
| **PROGRESS.md** | **지금 어디인가** | 진행 상황·인수인계 |

규칙: **수치는 `Design.md` 에만** 둔다. 다른 문서는 참조만 한다.
두 문서가 어긋나면 `Design.md` 가 맞다.

## 핵심 제약 (매번 기억할 것)

1. **백그라운드 액티비티 실행 제한** — `SYSTEM_ALERT_WINDOW` 권한이 주력 경로, `DreamService`가 보조. 둘 다 구현한다.
2. **위젯은 자체 제작이 아니라 `AppWidgetHost` 기반 실제 시스템 위젯 호스팅**이 핵심 요구사항. 갤럭시 기본 위젯이 들어가야 한다.
3. **SF Pro 폰트는 번들 불가** (Apple 플랫폼 전용 라이선스). 실제 번들은 `Pretendard` 가변 폰트 하나(OFL).
4. **Apple 상표/에셋 사용 금지** — 아이콘은 직접 제작 또는 오픈소스.
5. **삼성 절전 정책**이 서비스를 죽인다 — 배터리 최적화 예외 온보딩이 필수.
6. ~~One UI Daily Board 와 충돌~~ — **사실이 아니었다.** Daily Board 는 갤럭시
   **태블릿** 기능이고 폰에는 없다. One UI 7 폰의 충전 표시는 잠금화면 하단의
   작은 효과라 전체화면을 덮지 않으므로 충돌하지 않는다. 검증 없이 초기 계획서에
   써두고 계속 복사해 나른 오류였다.

## 기술 스택

```
Kotlin · Jetpack Compose · Hilt · DataStore(Preferences/Proto) · Coroutines/Flow
AppWidgetHost · DreamService · Foreground Service(specialUse) · SensorManager
Play Billing v7 · GitHub Actions
minSdk 29 / targetSdk 36 / JDK 17 / Gradle Kotlin DSL + Version Catalog
```

## 모듈 구조

```
app/                  진입점, 조립
core/design/          디자인 시스템 (스퀘어클 Shape, 컬러 토큰, 모션 스펙, 타이포)
core/common/          공통 유틸
core/data/            DataStore, 설정 영속화
feature/main/         메인 화면 + 하단 5섹션 탭바
feature/standby/      StandBy 본 화면 (시계·위젯 렌더링)
feature/widgets/      AppWidgetHost, 위젯 피커, 스택/자동넘김
feature/charging/     충전 감지 엔진, 리시버, 포그라운드 서비스
feature/developer/    개발자 모드
feature/donate/       후원 (Play Billing)
```

## UI 구조 (사용자 요청 고정 사항)

**첫 실행은 메뉴가 아니라 설정 안내부터.**
오버레이 권한·배터리 최적화 제외 없이는 앱이 동작하지 않으므로, `SetupScreen` 이
먼저 뜨고 각 항목이 해당 시스템 화면으로 바로 이동시킨다. 막지는 않는다 —
"이대로 계속하기" 가 있다. 이 설정들은 변경 알림이 없으므로
**포그라운드 복귀 때마다 재확인**한다 (`LifecycleEventEffect(ON_RESUME)`).

하단 탭바 5개 — **순서를 바꾸지 말 것**:

| # | 탭 | 역할 |
|---|---|---|
| 1 | 위젯 | 위젯 추가/정렬/자동넘김 설정 |
| 2 | 충전 판별 | 무선만 / 유선만 / 둘 다 / 도크 선택 |
| 3 | 메인 메뉴 | 시계 스타일, 테마, 미리보기, 전체 설정 |
| 4 | 개발자 모드 | 로그, 센서값, 충전 시뮬레이션 |
| 5 | 후원 | Play 인앱결제 |

모든 탭 아이콘은 **이미지 에셋** 사용.

## 디자인 규칙

**두 체계를 절대 섞지 말 것** — 이걸 섞어서 앱 UI를 한 번 다시 만들었다.

| | 앱 UI (설정·탭바·내비) | StandBy 시계 화면 |
|---|---|---|
| 토큰 | `NightstandColor.Ios` | `NightstandColor.Standby` |
| 테마 | `NightstandTheme` (라이트/다크 추종) | `StandbyTheme` (항상 블랙) |
| 바탕 | 라이트 `#F2F2F7` / 다크 `#000000` | 항상 `#000000` |
| 카드 | 라이트 `#FFFFFF` / 다크 `#1C1C1E` | `#1C1C1E` 타일 |

앱 UI 필수 규칙 — 직접 쌓지 말고 컴포넌트를 쓸 것:

- 화면은 `IosScreen` (라지 타이틀 → 스크롤 시 중앙 타이틀로 접힘)
- 목록은 `ListSection` + `ListRow` / `SwitchRow` / `SelectionRow`
- 구분선은 카드 끝이 아니라 **라벨 시작 위치**에서, 마지막 행은 없음
- 스위치는 `IosSwitch` (51×31) — Material `Switch` 금지
- 탭바는 바닥에 붙이지 않는다 — **떠 있는 캡슐형 유리 바**. Material `NavigationBar` 금지
  (콘텐츠가 그 아래로 스크롤되도록 화면은 `Column` 이 아니라 `Box` 로 쌓는다)
- 타이포는 `NightstandType` 만 사용. SF Pro 트래킹은 **사이즈별로 부호가 바뀐다**
  (34pt는 +0.37, 17pt는 -0.41). 전부 음수로 깔면 "비슷하지만 아닌" 느낌이 난다.

공통:

- 모서리는 연속 곡률 — `SquircleShape`(슈퍼타원 n=4). 반지름을 정확히 지킨다
- **예외**: 떠 있는 탭바·알약 버튼은 `CapsuleShape`(진짜 반원 끝).
  여기에 슈퍼타원을 쓰면 끝이 납작해져 애플이 그리지 않는 모양이 된다
- 중첩 곡률은 `Radius.concentric(outer, padding)` 로 계산
- 누름 피드백은 리플이 아니라 scale 0.96
- StandBy 페이드인: **2초 지연 → 800ms 페이드인**
- 유리 표면은 `Modifier.liquidGlass(shape, hazeState)` — 직접 반투명 배경 쌓지 말 것
- **배경 블러는 `RenderEffect` 로 안 된다.** Compose 는 자기 레이어만 블러한다.
  `dev.chrisbanes.haze` 를 쓰며, 블러 대상에 `hazeSource`, 유리 면에 `hazeEffect`.
  **효과가 자기 소스 안에 들어가면 자기 출력을 먹으므로** 내비게이션 바와 탭바는
  각각 별도 `HazeState` 를 쓴다

## 작업 규칙 (세션 연속성)

1. **기능 하나 = 커밋 하나.** 커밋을 몰아두지 않는다.
2. 커밋할 때 **`PROGRESS.md`를 같은 커밋에 갱신**한다.
3. **세션당 Phase 1개**를 넘기지 않는다.
4. **토큰이 20% 남으면 중단** → 커밋 + `PROGRESS.md` 마무리 + push.
5. 미완성 지점은 `// TODO(next):`, 미결정 사항은 `// TODO(decision):` 주석으로 남긴다.
6. 실기기(S25 Ultra) 검증 결과는 `PROGRESS.md`의 검증 로그에 누적한다.

## 커밋 컨벤션

```
feat(widgets): AppWidgetHost 바인딩 승인 흐름 구현
fix(standby): 페이드인 중 화면 회전 시 크래시 수정
design(ui): 스퀘어클 Shape 구현
docs(progress): Phase 3 진행 상황 갱신
chore(ci): GitHub Actions 빌드 워크플로 추가
```

브랜치: `main` ← `main_code` ← `feat/phaseN-xxx` · Phase 완료 시 태그 `v0.1.0-phaseN`

## 빌드

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test
```

로컬에 Android SDK가 없을 수 있으므로, **GitHub Actions가 푸시마다 디버그 APK를 아티팩트로 생성**한다.
빌드 확인이 필요하면 CI 결과를 본다.

---

## 빌드 툴체인 (Phase 0에서 확정 — 추측하지 말 것)

`gradle/libs.versions.toml` 이 단일 진실 공급원이다. 버전을 바꿀 때는
**Maven 저장소에서 실제 존재 여부를 확인한 뒤** 바꾼다.

| | |
|---|---|
| Gradle | 9.7.1 |
| AGP | 9.4.1 |
| Kotlin | 2.4.20 (AGP 내장) · KSP 2.3.12 |
| Hilt | 2.60.1 |
| Compose BOM | 2026.09.00 |
| SDK | compileSdk **37** / targetSdk 36 / minSdk 29 |
| JDK | 17 (Temurin) |

### AGP 9 에서 반드시 지킬 것 — Phase 0 에서 실제로 깨졌던 것들

1. **`org.jetbrains.kotlin.android` 플러그인을 적용하지 않는다.**
   AGP 9 는 Kotlin 을 내장하고 있어, 함께 적용하면 빌드가 거부된다.
2. **`build-logic` 클래스패스에는 AGP jar 만 둔다.**
   KSP·Compose·Kotlin 플러그인 jar 를 올리면 Gradle 내장 Kotlin 이 그
   최신 메타데이터를 읽지 못해 컴파일이 깨진다. 이 플러그인들은 전부
   id 로만 적용하므로 타입이 필요 없다.
3. **Hilt 는 2.59 부터 AGP 9 가 필수.**
   AGP 를 내리려면 Hilt 도 2.58 이하로 함께 내려야 한다.
4. **`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` 는 Gradle 9 에서도 필요.**
   빼면 `projects.core.design` 이 해석되지 않는다.
5. **compileSdk 는 37.** 최신 AndroidX 가 요구한다. targetSdk 는 Play 기준 36 유지.

## 컨벤션 플러그인 (build-logic/)

모듈 설정은 절대 복붙하지 않는다. 새 모듈은 한 줄로 끝낸다.

```kotlin
plugins { alias(libs.plugins.nightstand.android.feature) }   // feature/*
plugins { alias(libs.plugins.nightstand.android.library) }   // core/*
```

| 플러그인 | 하는 일 |
|---|---|
| `nightstand.android.application` | app 모듈 · SDK 레벨 · JVM 17 |
| `nightstand.android.library` | 라이브러리 모듈 공통 설정 |
| `nightstand.android.compose` | Compose 활성화 + BOM + 공통 의존성 |
| `nightstand.android.hilt` | Hilt + KSP |
| `nightstand.android.feature` | library + compose + hilt + core 모듈 + 네비게이션 |

## 디자인 토큰 위치 (core:design)

| 파일 | 내용 |
|---|---|
| `theme/Color.kt` | `Ios`(앱 UI) / `Standby`(시계) 두 체계 + 유리 토큰 |
| `theme/Type.kt` | tnum 고정폭 필수, body는 17sp (16sp 아님) |
| `theme/Font.kt` | Pretendard 가변 폰트 (SF Pro 대체) |
| `theme/Dimen.kt` | 8pt 그리드, `Radius.concentric(outer, padding)` |
| `theme/Squircle.kt` | 애플 연속 곡률 — **`RoundedCornerShape` 쓰지 말 것** |
| `theme/Motion.kt` | 스프링 스펙, StandBy 2초 지연 + 800ms 페이드인 |
| `theme/Theme.kt` | `NightstandTheme`(앱 UI) · `StandbyTheme`(시계 화면) |
| `component/LiquidGlass.kt` | Liquid Glass 머티리얼 (Haze 배경 블러 포함) |
| `component/IosScreen.kt` | 라지 타이틀 화면 + `listSection` 헬퍼 |
| `component/IosList.kt` | 인셋 그룹 목록 · 행 · 선택행 |
| `component/IosSwitch.kt` | 51x31 iOS 스위치 |
| `component/NightstandTabBar.kt` | 떠 있는 캡슐 탭바 |

상세 근거는 `Design.md` 참조.

## 검증 방법 — 로컬 빌드 불가

이 개발 환경에는 **JDK / Android SDK가 없다.** 유일한 검증 수단은 CI다.

```bash
git push                    # 푸시하면 GitHub Actions가 빌드
gh run list --limit 3       # 상태 확인
gh run view <id> --log-failed   # 실패 로그
```

빌드 성공 시 Actions 아티팩트 `nightstand-debug-apk` 에서 APK를 받아
S25 Ultra에 설치해 확인한다.
