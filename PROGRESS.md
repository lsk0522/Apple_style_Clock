# 진행 상황 (PROGRESS)

> **이 파일의 목적**: 대화 토큰이 소진되거나 세션이 끊겨도, 다음 세션이 이 파일만 읽고
> 곧바로 이어서 작업할 수 있게 한다. **커밋할 때마다 함께 갱신한다.**

---

## 현재 상태

| | |
|---|---|
| **진행 중 Phase** | **Phase 2 완료 ✅** — 다음은 Phase 3 (StandBy 화면 실행) |
| **마지막 갱신** | 2026-09-20 |
| **마지막 커밋** | feat(charging): Phase 2 — 충전 감지 엔진과 설정 영속화 |
| **빌드 상태** | 🟢 **CI 그린** — 빌드·단위테스트·린트 통과, 디버그 APK 14MB 생성 |
| **다음 마일스톤** | **v0.1 MVP = Phase 0~5** (약 3주) |

### 확정된 설정

| | |
|---|---|
| 앱 이름 | **Nightstand** |
| 패키지명 | `com.lsk0522.nightstand` |
| minSdk / targetSdk | **29** / 36 |
| 테스트 기기 | Galaxy S25 Ultra (One UI 7) |

### 빌드 툴체인 (2026-09-20 기준 실제 확인한 최신 안정판)

| | |
|---|---|
| Gradle | 9.7.1 |
| AGP | 9.4.1 |
| Kotlin | 2.4.20 (AGP 내장 — 별도 플러그인 적용 금지) |
| KSP | 2.3.12 |
| Hilt | 2.60.1 |
| Compose BOM | 2026.09.00 |
| SDK | compileSdk **37** / targetSdk 36 / minSdk 29 |
| JDK | 17 (Temurin) |

> Phase 0에서 실제 빌드를 돌려 확정한 조합. 제약과 함정은 `CLAUDE.md` 의
> "AGP 9 에서 반드시 지킬 것" 참조.

---

## 🏗 프로젝트 구조 (생성 완료)

```
app/                    앱 진입점 · Hilt Application · MainActivity
build-logic/convention/ 컨벤션 플러그인 5종 (설정 중복 제거)
core/design/            Design.md 토큰 구현 (Color/Type/Dimen/Squircle/Motion/Theme)
core/common/            (비어 있음 — Phase 2에서 채움)
core/data/              (비어 있음 — Phase 2에서 채움)
feature/main/           MainScreen 플레이스홀더 → Phase 1에서 5섹션 탭바로
feature/standby/        (비어 있음 — Phase 3)
feature/widgets/        (비어 있음 — Phase 5)
feature/charging/       (비어 있음 — Phase 2)
feature/developer/      (비어 있음 — Phase 8)
feature/donate/         (비어 있음 — Phase 9)
.github/workflows/      CI — 푸시마다 디버그 APK 아티팩트 생성
```

**컨벤션 플러그인 사용법** (새 모듈 추가 시):
```kotlin
plugins { alias(libs.plugins.nightstand.android.feature) }   // feature 모듈
plugins { alias(libs.plugins.nightstand.android.library) }   // core 모듈
```

---

## ✅ 완료

- [x] 프로젝트 기획 및 기술 조사
- [x] `plan.md` (v0.3) · `README.md` · `PROGRESS.md` · `CLAUDE.md` 작성
- [x] `Design.md` 작성 (Apple HIG + getdesign.md 기반 토큰 명세)
- [x] `iOS_Design.md` 작성 (iOS 17/18 StandBy 모드 네이티브 아키텍처 및 SwiftUI 엔지니어링 전용 명세)
- [x] 주요 결정 확정 (앱 이름 · minSdk · MVP 범위)
- [x] GitHub 원격 연결 및 푸시 (`lsk0522/Apple_style_Clock`)
- [x] **Phase 0** — Gradle Wrapper 9.7.1 (공식 배포본)
- [x] **Phase 0** — Version Catalog + build-logic 컨벤션 플러그인 5종
- [x] **Phase 0** — 10개 모듈 골격 생성
- [x] **Phase 0** — app 매니페스트 · 권한 선언 · 어댑티브 런처 아이콘
- [x] **Phase 0** — core:design 토큰 구현 (Squircle 연속 곡률 포함)
- [x] **Phase 0** — Pretendard 가변 폰트 번들 (OFL, 6.7MB)
- [x] **Phase 0** — GitHub Actions CI (빌드·테스트·린트 + APK 아티팩트)
- [x] **Phase 0** — **AGP 9 마이그레이션 및 CI 그린 달성** 🟢
- [x] **Phase 0** — 고정 디버그 서명키 + versionCode 자동 증가 → **업데이트 설치 동작**
- [x] **Phase 1** — 탭 아이콘 5종 직접 제작 (24dp 모노라인 벡터)
- [x] **Phase 1** — NightstandTabBar + 5개 탭 골격 (feature:main)
- [x] **Phase 1** — 앱 UI 를 iOS 디자인 언어로 재구축 (그룹 목록·라지 타이틀·스위치)- [x] **Phase 1** — iOS 27 Liquid Glass + Haze 실제 배경 블러- [x] **Phase 1** — 문서 역할 분리 (Design / iOS_Design / CLAUDE / plan)- [x] **Phase 2** — 충전 감지 엔진 (1.5초 디바운스 + 역무선충전 교차검증)- [x] **Phase 2** — DataStore 설정 영속화- [x] **Phase 2** — PowerConnectionReceiver + 백그라운드 감지 기록- [x] **Phase 2** — 충전 탭 실동작 (조건 선택 · 실시간 상태)

---

## 🚧 진행 중

없음 — Phase 0 완료. Phase 1 착수 대기.

---

## 다음 할 일 (우선순위 순)

1. **[사용자]** 실기기 검증 — 아래 "Phase 2 검증 항목"
2. **Phase 3** — `ChargingMonitorService` (포그라운드, specialUse)
3. **Phase 3** — 오버레이 권한 + 삼성 절전 예외 + Daily Board 끄기 온보딩
4. **Phase 3** — `StandByActivity` — 잠금화면 위 표시, 2초 지연 → 800ms 페이드인
5. **Phase 3** — `DreamService` 보조 경로
6. **Phase 4** — 시계 페이스 6종

---

## Phase 2 검증 항목 (S25 Ultra)

| # | 확인할 것 | 기대 결과 |
|---|---|---|
| 1 | 무선 패드에 올리기 | 충전 탭 "충전 방식" = **무선** |
| 2 | 케이블 연결 | **유선 (충전기)** |
| 3 | PC USB 연결 | **유선 (USB)** |
| 4 | **역무선충전 켜고 다른 기기 올리기** | **충전 안 함** — 무선으로 나오면 버그 |
| 5 | 무선 패드에 올리는 순간 관찰 | 유선으로 깜빡이면 안 됨 (1.5초 디바운스) |
| 6 | 조건 "무선만" + 케이블 연결 | "지금 조건이면 **안 켜짐**" |
| 7 | **앱 완전 종료** → 충전기 꽂았다 빼기 → 앱 열기 | "마지막 감지"에 기록됨 |
| 8 | 설정 스위치 변경 → 앱 강제 종료 → 재실행 | 값 유지 |

> **4번과 5번이 이 엔진의 핵심**입니다. 나머지는 단순 조회라 거의 확실합니다.

---

## 🧱 Phase 0 에서 배운 것 (다음 세션이 같은 함정에 빠지지 않도록)

| 겪은 문제 | 해결 |
|---|---|
| build-logic 이 KSP jar 의 Kotlin 2.3 메타데이터를 못 읽음 | build-logic 클래스패스엔 **AGP jar 만** 둔다 |
| Hilt 2.60.1 이 AGP 9.0+ 요구 | AGP 8 유지 대신 **AGP 9 로 상향** (Hilt 2.58 이하로 내리는 선택지도 있음) |
| AGP 9 가 `kotlin.android` 플러그인 적용을 거부 | AGP 9 는 **Kotlin 내장** — 플러그인 적용하지 않는다 |
| `projects.core.design` 해석 실패 | `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` 는 Gradle 9 에서도 필요 |
| AAR 메타데이터 검사 실패 (18건) | 최신 AndroidX 가 **compileSdk 37** 요구 |

---


## 🧊 iOS 27 Liquid Glass 적용 현황

| 항목 | 상태 |
|---|---|
| 떠 있는 캡슐 탭바 | ✅ |
| 콘텐츠가 탭바 아래로 스크롤 | ✅ |
| 유리 채움 (불투명도 72%, iOS 27 기준) | ✅ |
| 상단 스페큘러 하이라이트 | ✅ |
| 어두운 가장자리 림 (iOS 27 추가분) | ✅ |
| 목록 카드 곡률 18dp (동심) | ✅ |
| 접힌 내비게이션 바 유리화 | ✅ |
| **실제 배경 블러** | ✅ Haze 1.7.3 적용 |

**배경 블러가 빠진 이유**: Compose 는 자기 레이어만 블러할 수 있고 뒤에 그려진
것은 블러하지 못한다. 해결책은 `dev.chrisbanes.haze:haze` (1.7.3 안정판,
2.0 베타에는 굴절 기반 `haze-glass` 모듈까지 있음).

→ **사용자 확인 필요**: 의존성 하나 추가해서 진짜 블러까지 맞출지 여부.
   넣으면 Liquid Glass 재현도가 확실히 올라가고, 안 넣으면 지금의 근사 유지.
   (API 31 미만에서는 Haze 도 반투명으로 폴백하므로 minSdk 29 와 충돌 없음)

---

## ❓ 미결정 / 막힌 것

| # | 항목 | 상태 |
|---|---|---|
| B1 | 로컬에 JDK / Android SDK 없음 | 🟡 CI로 우회 중 — 실기기 설치는 CI 아티팩트 사용 |
| O1 | 탭바 5종 아이콘 이미지 에셋 | Phase 1 — 직접 제작 필요 (Apple 에셋 사용 불가) |
| O2 | 다국어 범위 (한 / 영 / 일) | Phase 10 |
| O3 | 크래시 리포팅 — Crashlytics vs ACRA | Phase 10 |
| O4 | 후원 상품 금액대 | Phase 9 |
| O5 | 폰트 서브셋팅 (6.7MB → 축소) | Phase 10 |

---

## 🧪 실기기 검증 로그 (Galaxy S25 Ultra)

| 날짜 | 항목 | 결과 |
|---|---|---|
| — | — | 아직 없음 |

> Phase 2부터 여기에 "무선 충전 감지 OK", "삼성 시계 위젯 바인딩 실패" 같은
> 실기기 결과를 계속 누적한다.

---

## 📌 세션 종료 체크리스트

작업을 마치거나 토큰이 20% 남았을 때 **반드시** 수행:

- [ ] 작업물 커밋 (Conventional Commits 형식)
- [ ] 이 파일의 **현재 상태 / 완료 / 진행 중 / 다음 할 일** 갱신
- [ ] 미완성 코드에 `// TODO(next):` 주석으로 이어갈 지점 표시
- [ ] 막힌 것이 있으면 **미결정** 표에 추가
- [ ] `git push`

---

## 🔄 다음 세션 시작 방법

새 대화를 열고 아래처럼 말하면 됩니다:

```
plan.md 와 PROGRESS.md 읽고, Phase N 이어서 진행해줘
```

Claude Code는 `CLAUDE.md` 를 자동으로 읽으므로 아키텍처·컨벤션은 다시 설명할 필요가 없습니다.
