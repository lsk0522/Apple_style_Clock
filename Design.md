---
version: 2.0.0
name: Nightstand-Design-Tokens
project: Nightstand (com.lsk0522.nightstand)
role: DESIGN TOKENS — 무엇이 어떤 색·크기·곡률인가
description: Nightstand 의 단일 토큰 원장. 앱 UI(iOS 설정 앱 언어)와 StandBy 시계 화면, 두 체계의 색·타이포·곡률·여백을 정의한다. 화면 구조와 동작은 iOS_Design.md, 구현 규칙은 CLAUDE.md 를 본다.

# ============================================================
# 1. Color — iOS Semantic System (Light / Dark / Night Vision)
# ============================================================
colors:
  # Shared system tints (Apple HIG)
  system-blue: "#007AFF"             # 기본 틴트 (라이트)
  system-blue-dark: "#0A84FF"        # 기본 틴트 (다크)
  system-green: "#34C759"            # 스위치 on (라이트)
  system-green-dark: "#30D158"       # 스위치 on (다크)
  system-orange: "#FF9500"
  system-orange-dark: "#FF9F0A"
  system-red: "#FF3B30"
  system-red-dark: "#FF453A"         # 야간 모드 액센트 겸용

  # Light
  light:
    grouped-background: "#F2F2F7"         # 그룹 목록이 놓이는 바닥
    grouped-card: "#FFFFFF"               # 목록 섹션 카드
    row-pressed: "#D1D1D6"                # 행 눌림
    label: "#000000"
    secondary-label: "rgba(60, 60, 67, 0.60)"
    tertiary-label: "rgba(60, 60, 67, 0.30)"
    quaternary-label: "rgba(60, 60, 67, 0.18)"
    separator: "rgba(60, 60, 67, 0.29)"
    switch-track-off: "#E9E9EA"
    fill: "rgba(120, 120, 128, 0.12)"

  # Dark
  dark:
    grouped-background: "#000000"         # OLED 소자 소등
    grouped-card: "#1C1C1E"
    row-pressed: "#2C2C2E"
    label: "#FFFFFF"
    secondary-label: "rgba(235, 235, 245, 0.60)"
    tertiary-label: "rgba(235, 235, 245, 0.30)"
    quaternary-label: "rgba(235, 235, 245, 0.18)"
    separator: "rgba(84, 84, 88, 0.65)"
    switch-track-off: "#39393D"
    fill: "rgba(120, 120, 128, 0.18)"

  # Liquid Glass (iOS 26 도입 / iOS 27 조정치)
  # iOS 26 은 투명도가 높아 가독성 지적을 받았고 27에서 낮췄다. 아래는 27 기준.
  glass:
    fill-light: "rgba(255, 255, 255, 0.72)"
    fill-dark: "rgba(30, 30, 32, 0.72)"
    specular-light: "rgba(255, 255, 255, 0.65)"  # 상단 하이라이트
    specular-dark: "rgba(255, 255, 255, 0.28)"
    edge-light: "rgba(0, 0, 0, 0.18)"            # iOS 27이 추가한 어두운 가장자리
    edge-dark: "rgba(0, 0, 0, 0.45)"
    blur-radius: 24px

  # Night Vision (StandBy 전용, 저조도 취침 모드)
  night-vision:
    canvas: "#000000"
    label: "#FF453A"
    secondary-label: "#801B17"
    tertiary-label: "rgba(128, 27, 23, 0.50)"

  # StandBy 시계 화면 (라이트 모드 없음 — 항상 어둡다)
  standby:
    canvas: "#000000"
    tile: "#1C1C1E"
    tile-raised: "#2C2C2E"
    text-primary: "#FFFFFF"
    text-secondary: "rgba(255, 255, 255, 0.60)"
    text-tertiary: "rgba(255, 255, 255, 0.30)"
    hairline: "rgba(255, 255, 255, 0.08)"
    glass-rim: "rgba(255, 255, 255, 0.12)"
    accent: "#FF9F0A"

# ============================================================
# 2. Typography — Apple 실제 SF Pro 트래킹 테이블
# ============================================================
# 주의: 트래킹은 크기에 따라 부호가 바뀐다. 디스플레이 크기는 벌어지고(+),
#       본문 크기는 좁아진다(-). 전 사이즈에 음수를 깔면 애플처럼 보이지 않는다.
#       줄 높이는 비율이 아니라 고정 pt 다.
typography:
  font-family-korean: "Pretendard"   # 실제 번들 (OFL). SF Pro 는 번들 불가
  digit-feature: "tnum"              # iOS .monospacedDigit() 대응

  # --- 앱 UI (iOS text styles) ---
  large-title:  { size: 34, weight: 700, lineHeight: 41, tracking: "+0.0109em" } # +0.37pt
  title1:       { size: 28, weight: 700, lineHeight: 34, tracking: "+0.0129em" } # +0.36pt
  title2:       { size: 22, weight: 600, lineHeight: 28, tracking: "+0.0159em" } # +0.35pt
  title3:       { size: 20, weight: 600, lineHeight: 25, tracking: "+0.019em" }  # +0.38pt
  headline:     { size: 17, weight: 600, lineHeight: 22, tracking: "-0.024em" }  # -0.41pt
  body:         { size: 17, weight: 400, lineHeight: 22, tracking: "-0.024em" }  # 16 아님
  callout:      { size: 16, weight: 400, lineHeight: 21, tracking: "-0.019em" }
  subheadline:  { size: 15, weight: 400, lineHeight: 20, tracking: "-0.016em" }
  footnote:     { size: 13, weight: 400, lineHeight: 18, tracking: "-0.006em" }  # 섹션 헤더/푸터
  caption1:     { size: 12, weight: 400, lineHeight: 16, tracking: "0" }
  caption2:     { size: 11, weight: 500, lineHeight: 13, tracking: "+0.006em" }  # 탭바 라벨

  # --- StandBy 시계 (tnum 필수) ---
  hero-clock-display: { size: 140, weight: 700, lineHeight: 133, tracking: "-0.04em", tnum: true }
  hero-clock-medium:  { size: 88,  weight: 600, lineHeight: 88,  tracking: "-0.03em", tnum: true }
  widget-clock:       { size: 52,  weight: 600, lineHeight: 55,  tracking: "-0.02em", tnum: true }

# ============================================================
# 3. Corner Radii — 연속 곡률(Continuous / Squircle)
# ============================================================
# 원형 호가 아니라 슈퍼타원이다. RoundedCornerShape 로는 재현되지 않는다.
# 중첩 시 동심 공식: R_inner = R_outer - padding
rounded:
  none: 0px
  xs: 6px
  sm: 10px
  md: 16px
  list-card: 32px       # 앱 UI 그룹 목록 카드 (iOS 26 에서 10 -> 커짐)
  widget-tile: 22px     # StandBy / 홈 위젯 타일
  panel: 32px
  pill: 9999px          # 떠 있는 탭바, 캡슐 버튼
  app-icon-ratio: 0.225 # 아이콘 곡률 = 너비 x 22.5%

# ============================================================
# 4. Spacing — 8pt 그리드
# ============================================================
spacing:
  xxs: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  xxl: 48px

  # 앱 UI
  list-inset: 16px       # 카드가 화면 끝에서 들어온 거리
  row-padding: 16px      # 행 내부 좌우 여백 (구분선 시작점이기도 하다)
  row-min-height: 44px
  section-gap: 35px      # 그룹 섹션 사이
  tab-bar-height: 56px
  tab-bar-inset: 16px

  # StandBy
  screen-margin: 32px    # 가로 거치 시 베젤과의 안전 거리
  widget-gutter: 24px

# ============================================================
# 5. Motion
# ============================================================
motion:
  standard-spring: "dampingRatio LowBouncy / stiffness MediumLow"
  press-scale: 0.96            # 리플 대신 수축
  standby-enter-delay: 2000ms
  standby-fade-in: 800ms
  standby-fade-out: 400ms
  pixel-shift-interval: 10min  # 번인 방지
  pixel-shift-max: 2px
---

# Design Tokens

> **이 문서의 역할**: 색·타이포·곡률·여백·모션 **수치의 단일 원장**.
> 수치를 바꿀 일이 생기면 **여기만** 고친다.

## 문서 역할 분담

| 문서 | 역할 | 여기에 쓰지 않는 것 |
|---|---|---|
| **Design.md** (이 문서) | **어떻게 보이는가** — 토큰 수치 | 화면 구조, 구현 방법 |
| **iOS_Design.md** | **무엇을 만드는가** — StandBy 화면 구조·인터랙션 | 토큰 수치 (이 문서 참조) |
| **CLAUDE.md** | **어떻게 구현하는가** — 컴포넌트 규칙·함정 | 수치 원본 |
| **plan.md** | **언제 만드는가** — 단계·일정 | 디자인 상세 |

같은 수치가 두 문서에 있으면 **이 문서가 맞다.**

## 두 체계를 섞지 말 것

이 프로젝트에는 성격이 다른 두 표면이 있다. 한때 이를 섞어 앱 UI를 전부 다시 만든 적이 있다.

| | 앱 UI (5개 탭 설정 화면) | StandBy 시계 화면 |
|---|---|---|
| 테마 | 시스템 라이트/다크 추종 | **항상 어둡다** (+ 야간 적색 모드) |
| 바탕 | `#F2F2F7` / `#000000` | 항상 `#000000` |
| 카드 | `#FFFFFF` / `#1C1C1E` | `#1C1C1E` 타일 |
| 곡률 | `list-card` 32px | `widget-tile` 22px |
| 여백 | `list-inset` 16px | `screen-margin` 32px |
| 코드 | `NightstandTheme` / `NightstandColor.Ios` | `StandbyTheme` / `NightstandColor.Standby` |

## 폰트

`SF Pro` 는 Apple 플랫폼 전용 라이선스라 **번들할 수 없다.**
실제 번들되는 것은 `Pretendard`(OFL) 하나이며, 가변 폰트 한 파일이 전 굵기를 담당한다.
고지는 `THIRD_PARTY_NOTICES.md`.

## 코드에서의 위치

| 토큰 | 파일 |
|---|---|
| Color | `core/design/theme/Color.kt` |
| Typography | `core/design/theme/Type.kt`, `Font.kt` |
| Radii / Spacing | `core/design/theme/Dimen.kt` |
| 연속 곡률 Shape | `core/design/theme/Squircle.kt` |
| Liquid Glass 머티리얼 | `core/design/component/LiquidGlass.kt` |
| Motion | `core/design/theme/Motion.kt` |
| 팔레트 조립 | `core/design/theme/Theme.kt` |

> 이 문서에 있던 StandBy 화면 구조·시계 페이스·SwiftUI 매핑 설명은
> **`iOS_Design.md` 로 옮겼다.** 중복 유지는 두 문서가 어긋나는 원인이 된다.
