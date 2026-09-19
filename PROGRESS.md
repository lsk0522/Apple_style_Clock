# 진행 상황 (PROGRESS)

> **이 파일의 목적**: 대화 토큰이 소진되거나 세션이 끊겨도, 다음 세션이 이 파일만 읽고
> 곧바로 이어서 작업할 수 있게 한다. **커밋할 때마다 함께 갱신한다.**

---

## 현재 상태

| | |
|---|---|
| **진행 중 Phase** | **Phase 0 (기초 공사) — 거의 완료, CI 검증 중** |
| **마지막 갱신** | 2026-09-20 |
| **마지막 커밋** | `3565c79` chore(phase0): Gradle 멀티 모듈 프로젝트 기초 공사 |
| **빌드 상태** | GitHub Actions 첫 빌드 검증 중 |
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
| Gradle | 8.14.3 |
| AGP | 8.13.2 |
| Kotlin | 2.4.20 |
| KSP | 2.3.12 |
| Hilt | 2.60.1 |
| Compose BOM | 2026.09.00 |
| JDK | 17 (Temurin) |

> ⚠️ AGP는 9.4.1까지 나와 있으나, 메이저 변경 리스크를 피해 8.x 최신을 채택.
> AGP 9 마이그레이션은 Phase 10에서 검토.

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
- [x] **Phase 0** — Gradle Wrapper 8.14.3 (공식 배포본)
- [x] **Phase 0** — Version Catalog + build-logic 컨벤션 플러그인
- [x] **Phase 0** — 10개 모듈 골격 생성
- [x] **Phase 0** — app 매니페스트 · 권한 선언 · 어댑티브 런처 아이콘
- [x] **Phase 0** — core:design 토큰 구현 (Squircle 연속 곡률 포함)
- [x] **Phase 0** — GitHub Actions CI (디버그 APK 아티팩트)
- [x] **Phase 0** — Pretendard 가변 폰트 번들 (OFL, 6.7MB)

---

## 🚧 진행 중

- [ ] **Phase 0** — CI 첫 빌드 그린 만들기
      (로컬에 Android SDK가 없어 CI가 유일한 검증 수단)

---

## 📋 다음 할 일 (우선순위 순)

1. **CI 빌드 그린 확인** — 실패 시 로그 보고 수정
2. **[사용자]** Android Studio 설치 (선택 — CI만으로도 진행 가능)
3. **[사용자]** CI 아티팩트에서 APK 받아 S25 Ultra 설치 확인
4. **Phase 1** — iOS풍 공통 컴포넌트 (리스트 셀, 토글, 세그먼트, 시트)
5. **Phase 1** — 하단 5섹션 탭바 (이미지 아이콘 · 글래스 블러 배경)
6. **Phase 1** — 5개 탭 화면 + 네비게이션 그래프
7. **Phase 2** — 충전 감지 엔진 (`core:common` / `feature:charging`)

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
