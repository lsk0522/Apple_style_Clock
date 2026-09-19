# 진행 상황 (PROGRESS)

> **이 파일의 목적**: 대화 토큰이 소진되거나 세션이 끊겨도, 다음 세션이 이 파일만 읽고
> 곧바로 이어서 작업할 수 있게 한다. **커밋할 때마다 함께 갱신한다.**

---

## 현재 상태

| | |
|---|---|
| **진행 중 Phase** | Phase 0 착수 대기 (기획 확정 완료) |
| **마지막 갱신** | 2026-09-19 |
| **마지막 커밋** | `f1ed15b` docs: 프로젝트 기획 문서 작성 (로컬, 미푸시) |
| **빌드 상태** | 프로젝트 미생성 |
| **다음 마일스톤** | **v0.1 MVP = Phase 0~5** (약 3주) |

### 확정된 설정

| | |
|---|---|
| 앱 이름 | **Nightstand** |
| 패키지명 | `com.lsk0522.nightstand` |
| minSdk / targetSdk | **29** / 36 |
| 테스트 기기 | Galaxy S25 Ultra (One UI 7) |

---

## ✅ 완료

- [x] 프로젝트 기획 및 기술 조사
- [x] `plan.md` 작성 (v0.3 — 결정 사항 반영)
- [x] `README.md` 작성
- [x] `PROGRESS.md` / `CLAUDE.md` 작성
- [x] 주요 결정 확정 (앱 이름 · minSdk · MVP 범위)
- [x] GitHub 원격 연결 (`origin` → `lsk0522/Apple_style_Clock`, 원격은 아직 비어 있음)

---

## 🚧 진행 중

없음 — 최종 승인 및 문서 푸시 대기.

---

## 📋 다음 할 일 (우선순위 순)

1. **[사용자]** 계획 최종 승인 → 문서 4개 푸시
2. **[사용자]** JDK 17 + Android Studio 설치 🔴 **Phase 0 블로커**
3. **Phase 0** — Gradle 멀티 모듈 프로젝트 생성 (`com.lsk0522.nightstand`, minSdk 29)
4. **Phase 0** — `.gitignore`, Version Catalog, Hilt/Compose 세팅
5. **Phase 0** — **GitHub Actions 빌드 워크플로** (푸시마다 디버그 APK 아티팩트)
6. **Phase 1** — 디자인 시스템 (스퀘어클 Shape, 컬러 토큰, Pretendard/Inter)
7. **Phase 1** — 하단 5섹션 탭바 + 5개 탭 껍데기 + 네비게이션

---

## ❓ 미결정 / 막힌 것

| # | 항목 | 상태 |
|---|---|---|
| **B1** | **로컬에 JDK / Android SDK 없음** | 🔴 Phase 0 블로커 — 설치 필요.<br>단, GitHub Actions CI를 먼저 깔면 빌드 확인은 가능 |
| O1 | 앱 아이콘 / 브랜딩 비주얼 | Phase 11에서 결정 |
| O2 | 다국어 범위 (한 / 영 / 일) | Phase 10에서 결정 |
| O3 | 크래시 리포팅 — Crashlytics vs ACRA | Phase 10에서 결정 |
| O4 | 후원 상품 금액대 | Phase 9에서 결정 |

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
