# CLAUDE.md — AI 세션용 프로젝트 컨텍스트

> 새 세션이 코드베이스를 다시 훑지 않고 바로 작업할 수 있도록 하는 요약본.
> 상세 계획은 `plan.md`, 현재 진행 상황은 `PROGRESS.md`.

## 프로젝트 한 줄 요약

**무선/유선 충전을 감지해 2초 뒤 애플 StandBy 스타일 시계·위젯 화면을 띄우는 안드로이드 앱.**
저장소: https://github.com/lsk0522/Apple_style_Clock · 테스트 기기: **Galaxy S25 Ultra (One UI 7)**

## 핵심 제약 (매번 기억할 것)

1. **백그라운드 액티비티 실행 제한** — `SYSTEM_ALERT_WINDOW` 권한이 주력 경로, `DreamService`가 보조. 둘 다 구현한다.
2. **위젯은 자체 제작이 아니라 `AppWidgetHost` 기반 실제 시스템 위젯 호스팅**이 핵심 요구사항. 갤럭시 기본 위젯이 들어가야 한다.
3. **SF Pro 폰트는 번들 불가** (Apple 플랫폼 전용 라이선스). `Pretendard`(한글) + `Inter`(영문) 사용.
4. **Apple 상표/에셋 사용 금지** — 아이콘은 직접 제작 또는 오픈소스.
5. **삼성 절전 정책**이 서비스를 죽인다 — 배터리 최적화 예외 온보딩이 필수.
6. **One UI Daily Board**와 충돌 — 끄도록 안내해야 한다.

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

- 배경 순수 블랙 `#000000`, 텍스트 화이트 90% opacity
- 모서리는 스퀘어클(continuous corner) — `RoundedCornerShape` 금지, 커스텀 `Shape` 사용
- 모션: `spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)`
- StandBy 페이드인: **2초 지연 → 800ms 페이드인**
- 블러: API 31+ `RenderEffect.createBlurEffect`

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
