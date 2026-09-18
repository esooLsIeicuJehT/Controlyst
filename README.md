# Controlyst

<p align="center">
  <strong>Universal Root & Non-Root Game Controller Keymapper, Precision HUD Overlays, and Kernel Module Controller for Android</strong>
</p>

---

## Overview

**Controlyst** is a high-performance Android gaming companion engineered for low-latency controller keymapping, HUD overlays, and kernel-level performance management.

Controlyst strictly divides responsibilities into two dedicated layers:
1. **The Native Android App**: Delivers high-performance user interfaces for:
   - **Phone Dashboard**: Active profile switcher, real-time FPS/latency telemetry, CPU/GPU thermal monitors, and performance mode selectors.
   - **In-Game / Screenshot Mapper**: Visual canvas editor with AI-assisted HUD detection, sweeping scanlines, 5 modular control types (*Tap, Joystick, Camera, Swipe, Macro*), and floating purple glowing touch nodes.
   - **Tactical Reticle Studio & FPS Overlays**: Customizable high-visibility crosshair reticles, dynamic spread, and interactive floating in-game HUDs.
2. **The Root WebUI Module (KernelSU / APatch / Magisk)**: Controls low-level hardware and module functions:
   - Direct kernel `/dev/uinput` touch/gamepad event injection (1000 Hz polling rate, < 1 ms latency).
   - CPU and GPU governor tuning (`performance`, `schedutil`, `powersave`).
   - Device frequency locks, memory compaction (ZRAM), and sysctl kernel optimizations.
   - Anti-cheat safety sandboxing and bypass verification.

---

## Key Features

### 1. In-Game & Screenshot Mapper
- **AI-Assisted HUD Detection**: Analyzes game screenshots or live HUD layouts with automated scanlines to pinpoint fire buttons, ADS, sprint, jump, and crouch targets.
- **5 Core Control Types**:
  - **Tap**: Single-tap or rapid-fire turbo button node.
  - **Joystick**: Analog thumbstick zones with deadzone and sensitivity calibration.
  - **Camera**: Swipe look controls with independent horizontal/vertical sensitivity curves.
  - **Swipe**: Directional gesture triggers for slides, dodges, and skill throws.
  - **Macro**: Multi-action timeline chains with millisecond delay precision.
- **Floating Mapping Nodes**: Distinctive purple-violet glowing circular mapping elements with high-contrast controller labels (`A`, `B`, `LT`, `RT`, `Y`, `L3`, `RS`).

### 2. Multi-Tier Privilege Architecture
Controlyst dynamically probes and adapts to available device privileges:
| Method | Latency | Permissions / Setup | Root Needed |
| :--- | :--- | :--- | :--- |
| **KernelSU / APatch** | **< 1.0 ms** | Direct kernel `/dev/uinput` injection | Yes (KernelSU/APatch) |
| **Magisk Root** | **~ 1.8 ms** | `su` root daemon with `input` / `sendevent` | Yes (Magisk) |
| **Shizuku (Wireless ADB)** | **~ 4.5 ms** | Wireless ADB pairing (`adb pair 5555`) | No |
| **Accessibility API** | **~ 12 ms** | Standard Android Accessibility Service | No (Zero Root) |

### 3. Tactical Reticle & FPS Floating Overlays
- **Precision Crosshair Studio**: Choose from classic cross, circle dot, tactical chevron, and sniper reticles with live color, gap, thickness, and dynamic spread adjustments.
- **Floating In-Game HUD**: Movable floating pill displaying real-time FPS, frame time, battery temperature, and quick-toggle mapping controls.

### 4. KernelSU / APatch Module & WebUI
- Bundled as an installable Magisk / KernelSU / APatch module (`controlyst_uinput.zip`).
- Runs an embedded high-speed WebUI inside KernelSU Manager for fine-grained kernel parameter controls.

---

## Installation & Setup

### 1. Standard APK Installation
1. Download the latest `Controlyst.apk` from [Releases](https://github.com/).
2. Install the APK on your Android device running Android 10 (API 29) or higher.
3. Launch the app and complete the interactive onboarding setup.

### 2. KernelSU / APatch Module Installation (Optional for < 1 ms Latency)
1. Open Controlyst and navigate to the **Root WebUI** tab.
2. Export the pre-built `controlyst_uinput.zip` module.
3. Open **KernelSU Manager** or **APatch Manager**, select **Modules** > **Install from storage**, and flash the zip.
4. Reboot your device to enable 1000 Hz direct `/dev/uinput` injection.

---

## Building from Source

Controlyst is built using standard Gradle with Kotlin DSL and Jetpack Compose.

### Prerequisites
- JDK 17 or higher
- Android SDK with Platform 34 and Build Tools 34.0.0+

### Build Steps
```bash
# Clone the repository
git clone https://github.com/controlyst/controlyst.git
cd controlyst

# Build the Debug APK
gradle assembleDebug

# Run unit and JVM tests
gradle testDebugUnitTest
```

---

## Project Architecture

```
controlyst/
├── app/src/main/java/com/example/
│   ├── MainActivity.kt               # Streamlined navigation & app shell
│   ├── ui/
│   │   ├── dashboard/                # Controlyst Phone Dashboard
│   │   ├── mapper/                   # In-Game & Screenshot Mapper
│   │   ├── crosshair/                # Tactical Reticle Studio & FPS Overlays
│   │   ├── webui/                    # KernelSU & APatch Root WebUI Controller
│   │   ├── onboarding/               # Welcome & Permission Onboarding
│   │   └── theme/                    # Controlyst Design System (Emblem, Colors, Typography)
│   ├── injector/                     # Multi-tier input injectors (KernelSU, Magisk, Shizuku, A11y)
│   ├── module/                       # Kernel module assets & WebUI daemon
│   └── data/                         # Room database & config repositories
├── downloadable_module/              # Flashable KernelSU/APatch/Magisk zip bundle
├── LICENSE                           # MIT Open Source License
└── README.md                         # Documentation
```

---

## License

Controlyst is open-source software licensed under the [MIT License](LICENSE).
