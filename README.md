<div align="center">

# Nightstand

### 무선 충전 중에 켜지는, 애플 스탠바이 스타일 안드로이드 시계

아이폰을 무선 충전기에 가로로 올리면 나타나는 **StandBy 화면**.
그 경험을 안드로이드에서 그대로 — 그리고 **안드로이드 위젯의 자유도까지 더해서** 구현합니다.

[![Build](https://github.com/lsk0522/Apple_style_Clock/actions/workflows/build.yml/badge.svg)](https://github.com/lsk0522/Apple_style_Clock/actions/workflows/build.yml)
[![Status](https://img.shields.io/badge/status-Phase%201%20in%20progress-orange)]()
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84)]()
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF)]()
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

**[기능](#-기능) · [설치](#-설치) · [사용법](#-사용법) · [FAQ](#-faq) · [개발](#-개발자용) · [로드맵](#-로드맵)**

</div>

---

> [!IMPORTANT]
> **현재 개발 초기 단계입니다.** 기능은 아직 들어가지 않았지만,
> 프로젝트 골격과 CI는 완성되어 **디버그 APK가 자동으로 빌드**됩니다.
>
> - 설계 문서: **[plan.md](plan.md)** · **[Design.md](Design.md)**
> - 진행 상황: **[PROGRESS.md](PROGRESS.md)**
> - 최신 APK: [Actions](https://github.com/lsk0522/Apple_style_Clock/actions/workflows/build.yml) 에서 최신 성공 빌드 → `nightstand-debug-apk`

---

## ✨ 기능

### 충전하면 알아서 켜집니다

충전기를 연결하면 **2초 뒤 화면이 서서히 밝아지며** 시계가 나타납니다.
충전을 빼면 부드럽게 사라집니다. 버튼을 누를 필요가 없습니다.

### 무선만? 유선만? 직접 고르세요

| 설정 | 동작 |
|---|---|
| 🔋 **무선 충전만** | 무선 충전 패드에 올렸을 때만 켜짐 |
| 🔌 **유선 충전만** | 케이블을 꽂았을 때만 켜짐 |
| ⚡ **둘 다** | 어떤 방식이든 충전 중이면 켜짐 |
| 🖥️ **도크** | 도크에 거치했을 때만 켜짐 |

> 침대 옆 무선 충전기에서만 켜지게 하고, 차 안 유선 충전에서는 안 켜지게 — 이런 게 됩니다.

### 내 폰에 있는 위젯을 그대로

아이폰 StandBy와 달리, **폰에 설치된 진짜 위젯**을 올릴 수 있습니다.
갤럭시라면 삼성 시계·날씨·캘린더·리마인더 위젯이 그대로 들어갑니다.

- 앱별로 정리된 **위젯 고르기 화면**
- 드래그로 **순서 바꾸기**
- **자동 넘기기** — 5초 / 10초 / 30초 / 1분 간격으로 위젯이 스스로 전환
- 좌우 2분할 · 세로 스택 스와이프 (StandBy 원본 그대로)

### 시계 6종

| | | |
|:--:|:--:|:--:|
| **Digital**<br>초대형 숫자 | **Analog**<br>애플워치 스타일 | **Flip**<br>플립 카드 |
| **World**<br>세계 시간 | **Solar**<br>시간대별 그라데이션 | **Float**<br>떠다니는 아웃라인 |

### 밤에도 눈이 편하게

- 🌙 **야간 모드** — 어두워지면 붉은 색조로 자동 전환
- 🔅 **자동 밝기** — 주변 조도에 맞춰 조절
- 🛡️ **번인 방지** — 화면이 천천히 미세하게 움직여 OLED 잔상 예방
- 🔋 **저전력** — 주사율을 낮춰 배터리 소모 최소화

---

## 📲 설치

### 방법 1 — Google Play (배포 예정)

아직 등록 전입니다.

### 방법 2 — 개발 빌드 APK (지금 가능)

푸시될 때마다 GitHub Actions가 디버그 APK를 만들어 둡니다.

1. [Actions 탭](https://github.com/lsk0522/Apple_style_Clock/actions/workflows/build.yml) 열기
2. 맨 위 초록색 성공 빌드 클릭
3. 아래 **Artifacts** 에서 `nightstand-debug-apk` 다운로드 (zip)
4. 압축을 풀어 `.apk` 를 폰으로 옮긴 뒤 열기
5. **"이 출처 허용"** 을 켜고 설치
6. 앱을 실행해 아래 [최초 설정](#-최초-설정-3분)을 진행

> 개발 빌드는 패키지명이 `...nightstand.debug` 라서 정식 버전과 동시에 설치됩니다.

---

## 🚀 사용법

### 최초 설정 (3분)

앱을 처음 열면 안내가 순서대로 나옵니다. 세 가지만 켜주면 끝입니다.

#### 1단계 · 다른 앱 위에 표시 허용 ⭐ 필수

> 안드로이드는 앱이 백그라운드에서 화면을 띄우는 걸 막아둡니다.
> 이 권한이 있어야 충전할 때 자동으로 시계가 켜집니다.

```
설정 → 애플리케이션 → 특별한 접근 권한 → 다른 앱 위에 표시 → Nightstand → 허용
```

#### 2단계 · 배터리 최적화 제외 ⭐ 필수

> 삼성·샤오미는 안 쓰는 앱을 강제로 재우기 때문에, 제외하지 않으면 충전해도 안 켜집니다.

```
설정 → 배터리 → 백그라운드 사용 제한 → 사용 안 함 앱 절전 → Nightstand 제외
```

<details>
<summary><b>제조사별 상세 경로 보기</b></summary>

| 제조사 | 경로 |
|---|---|
| **삼성 (One UI)** | 설정 → 배터리 → 백그라운드 사용 제한 → **절전 모드로 전환되지 않는 앱**에 Nightstand 추가 |
| **샤오미 (MIUI)** | 설정 → 앱 → 앱 관리 → 앱 선택 → **자동 시작 허용** + 배터리 절약 **제한 없음** |
| **구글 픽셀** | 설정 → 앱 → 앱 선택 → 배터리 → **제한 없음** |
| **오포 / 리얼미** | 설정 → 배터리 → 앱 배터리 관리 → **백그라운드 실행 허용** |

</details>

#### 3단계 · 삼성 Daily Board 끄기 (갤럭시만)

> 갤럭시에는 이미 비슷한 "충전 중 화면 표시" 기능이 있어서, 켜져 있으면 서로 충돌합니다.

```
설정 → 디스플레이 → 화면 보호기 / Daily Board → 끄기
```

### 매일 쓰는 법

```
1. 폰을 무선 충전기에 올린다  (또는 케이블을 꽂는다)
2. 2초 기다린다
3. 시계가 서서히 나타난다 ✨
```

| 제스처 | 동작 |
|---|---|
| 좌우 스와이프 | 모드 전환 (위젯 / 사진 / 시계) |
| 상하 스와이프 | 위젯 스택 넘기기 |
| 길게 누르기 | 편집 모드 |
| 한 번 탭 | 밝기 토글 |
| 두 번 탭 | StandBy 종료 |

---

## ❓ FAQ

<details>
<summary><b>충전해도 시계가 안 켜져요</b></summary>

순서대로 확인해 보세요.

1. **"다른 앱 위에 표시"** 권한이 켜져 있나요? ([1단계](#1단계--다른-앱-위에-표시-허용--필수))
2. **배터리 최적화**에서 제외했나요? ([2단계](#2단계--배터리-최적화-제외--필수))
3. 앱 안 **"충전 판별"** 탭에서 고른 방식(무선/유선)과 실제 충전 방식이 같은가요?
4. 갤럭시라면 **Daily Board**가 켜져 있지 않나요? ([3단계](#3단계--삼성-daily-board-끄기-갤럭시만))
5. 그래도 안 되면 **개발자 모드 탭 → 진단 리포트**를 확인하거나 [이슈](https://github.com/lsk0522/Apple_style_Clock/issues)로 알려주세요.

</details>

<details>
<summary><b>배터리가 많이 닳지 않나요?</b></summary>

StandBy는 **충전 중에만** 동작하므로 배터리가 줄어들지 않습니다.
그래도 발열을 줄이기 위해 주사율을 낮추고, 검정 배경(OLED 절전)을 사용하며,
목표 소모량은 시간당 5% 이하입니다.

</details>

<details>
<summary><b>화면 번인이 걱정돼요</b></summary>

10분마다 화면 전체를 아주 조금씩(1~2px) 움직여 같은 픽셀이 계속 켜지지 않게 합니다.
어두운 곳에서는 밝기를 자동으로 낮춥니다.

</details>

<details>
<summary><b>내 위젯이 목록에 안 보여요 / 넣었는데 깨져요</b></summary>

일부 위젯은 "런처 앱"에서만 정상 동작하도록 제작되어 있습니다.
호환되지 않는 위젯은 앱이 자동으로 걸러내고 대체 카드를 보여줍니다.
문제가 되는 위젯을 [이슈](https://github.com/lsk0522/Apple_style_Clock/issues)로 알려주시면 대응하겠습니다.

</details>

<details>
<summary><b>잠금화면에서도 뜨나요?</b></summary>

네. 잠금화면 위에 표시됩니다. 다만 **보안을 위해 잠금을 해제하지는 않습니다.**
위젯을 눌러 앱으로 들어가려면 잠금 해제가 필요합니다.

</details>

<details>
<summary><b>개인정보를 수집하나요?</b></summary>

아니요. 모든 설정은 **기기 안에만** 저장됩니다.
캘린더·사진 권한은 위젯을 쓸 때만 요청하며, 데이터를 외부로 보내지 않습니다.

</details>

---

## 🛠 개발자용

### 요구 사항

| | |
|---|---|
| JDK | 17 (Temurin 권장) |
| Gradle | 9.7.1 — wrapper 포함이라 따로 설치 불필요 |
| AGP | 9.4.1 |
| Android SDK | compileSdk 37 / targetSdk 36 |
| 최소 지원 | Android 10 (API 29) |
| 패키지명 | `com.lsk0522.nightstand` |

> AGP 9는 Kotlin을 내장하므로 `org.jetbrains.kotlin.android` 플러그인을 적용하면 안 됩니다.
> 모듈 설정은 `build-logic/` 의 컨벤션 플러그인으로 일원화되어 있습니다.

### 빌드

```bash
git clone https://github.com/lsk0522/Apple_style_Clock.git
cd Apple_style_Clock

./gradlew assembleDebug          # 디버그 APK 빌드
./gradlew installDebug           # 연결된 기기에 설치
./gradlew test                   # 단위 테스트
```

> 로컬에 안드로이드 개발 환경이 없어도, GitHub Actions가 푸시마다 디버그 APK를 만들어
> **Actions 탭의 아티팩트**로 올려둡니다. 거기서 받아 폰에 설치할 수 있습니다.

### 기술 스택

```
Kotlin · Jetpack Compose · Hilt · DataStore · Coroutines/Flow
AppWidgetHost · DreamService · Foreground Service · SensorManager
Play Billing v7 · GitHub Actions
```

### 기여

1. 이슈를 먼저 열어 논의합니다
2. `feat/기능이름` 브랜치를 만듭니다
3. [Conventional Commits](https://www.conventionalcommits.org/ko/) 형식으로 커밋합니다
4. PR을 보냅니다

```
feat(widgets): AppWidgetHost 바인딩 승인 흐름 구현
fix(standby): 페이드인 중 화면 회전 시 크래시 수정
docs(readme): 설치 가이드 보강
```

자세한 구조와 설계 의도는 **[plan.md](plan.md)** 와 **[CLAUDE.md](CLAUDE.md)** 를 참고하세요.

---

## 🗺 로드맵

| 버전 | 내용 | 상태 |
|---|---|---|
| **v0.1** | 충전 감지 + StandBy 화면 + 시계 6종 + 위젯 호스팅 | 🚧 계획 |
| **v1.0** | 자동 넘김 · 야간 모드 · 개발자 모드 · 후원 · Play 배포 | 📋 예정 |
| **v1.1** | Wear OS · 태블릿/폴더블 · 테마 공유 | 💭 구상 |
| **v1.2** | Tasker 연동 · NFC 프로필 · 차량 도크 모드 | 💭 구상 |
| **v2.0** | 스마트홈 연동 · 음성 명령 · 클라우드 동기화 | 💭 구상 |

---

## 📄 라이선스

[MIT License](LICENSE) — 자유롭게 쓰고, 고치고, 배포하세요.

번들된 폰트 등 제3자 저작물 고지는 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 를 참고하세요.
(Pretendard, SIL Open Font License 1.1)

## ⚖️ 고지

이 프로젝트는 Apple Inc.와 **아무 관련이 없으며, 승인받지도 않았습니다.**
"iPhone", "iOS", "StandBy"는 Apple Inc.의 상표입니다.
이 앱은 해당 디자인 언어에서 **영감을 받아 독자적으로 제작**되었으며,
Apple의 폰트·아이콘·이미지 등 어떠한 자산도 포함하지 않습니다.

---

<div align="center">

**마음에 드셨다면 ⭐ 를 눌러주세요**

Made with ☕ by [lsk0522](https://github.com/lsk0522)

</div>
