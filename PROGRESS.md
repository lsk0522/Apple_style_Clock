# 진행 상황 (PROGRESS)

> **이 파일의 목적**: 대화 토큰이 소진되거나 세션이 끊겨도, 다음 세션이 이 파일만 읽고
> 곧바로 이어서 작업할 수 있게 한다. **커밋할 때마다 함께 갱신한다.**

---

## 현재 상태

| | |
|---|---|
| **진행 중 Phase** | **Phase 1 (디자인 시스템 · 메인 화면)** — 탭바 완료 |
| **마지막 갱신** | 2026-09-20 |
| **마지막 커밋** | feat(ui): 하단 5섹션 탭바 + 고정 디버그 서명키 |
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

---

## 🚧 진행 중

없음 — Phase 0 완료. Phase 1 착수 대기.

---

## 📋 다음 할 일 (우선순위 순)

1. **[사용자]** 새 APK 설치 — **이번 한 번만 기존 앱 삭제 후 설치**.
   서명키가 바뀌었기 때문이며, 이후로는 덮어쓰기(업데이트)로 설치된다.
2. **[사용자]** 아이콘 디자인 확인 후 수정 의견
3. **Phase 1** — iOS풍 공통 컴포넌트 (리스트 셀, 토글, 세그먼트, 모달 시트)
4. **Phase 1** — 각 탭 실제 화면 골격 + 네비게이션 그래프
5. **Phase 1** — 탭바 배경 실제 블러 (API 31+ RenderEffect)
6. **Phase 2** — 충전 감지 엔진 (`core:common` / `feature:charging`)

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
