# YouTube TV Instant Voice Search & Screen Scroll Slider

Transform voice search into instant YouTube TV execution without extra screen taps, replace the CH rocker with a smooth vertical Screen Scroll slider, and provide an exact step-by-step phone update guide.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Voice Search Execution**: Configured for instant YouTube TV search execution. As soon as speech ends, it immediately sends a direct search payload to the PC (`action: "search_youtube"` and browser URL/keystroke execution) with zero extra taps.
> - **Scroll Slider**: The CH rocker pill on the bottom right is replaced with an interactive vertical Screen Scroll Slider that sends continuous smooth mouse wheel / page scroll commands.
> - **Phone Update Flow**: Clear step-by-step instructions for updating the installed Android APK on physical devices as well as using the instant Web Remote PWA.

---

## 1. Overview & Core Concept

- **Instant Voice Search for YouTube**: When the user taps the mic button and speaks, the recognized text is instantly sent to YouTube on the PC and executed immediately, opening search results directly without getting stuck on suggestion menus.
- **Vertical Screen Scroll Slider**: Replaces the legacy CH (Channel) rocker with a tactile vertical scroll slider on the right side of the bottom controls, allowing fluid up/down navigation across YouTube feeds, video lists, and web pages.
- **Clear Phone Update Pathway**: Comprehensive in-app and chat guidance explaining exactly how to update the app on an Android phone.

---

## 2. User Experience & Visual Design

- **Voice Search Flow**:
  1. User taps the center Mic button between Mute and Home.
  2. Inline glowing indicator shows *`🎙️ Listening...`*.
  3. When the user stops speaking, the query is immediately dispatched to PC and YouTube displays search results instantly.
  4. Indicator shows *`✓ Searched: [Query]`* for 1.5 seconds and auto-dismisses.
- **Screen Scroll Slider UI**:
  - Located on the bottom right (replacing CH rocker).
  - Width 56dp, height 144dp, matching the volume bar dimensions for perfect visual symmetry.
  - Features dynamic drag thumb, "SCROLL" typography, and up/down directional glide feedback with haptic ticks.
  - Dragging up scrolls up; dragging down scrolls down smoothly.

---

## 3. Technical Architecture & Component Mapping

```
┌───────────────────────────────────────────────────────────┐
│                     RemoteScreen.kt                       │
├─────────────────────────────┬─────────────────────────────┤
│   TouchpadView (Top)        │   SettingsScreen.kt         │
├─────────────────────────────┼─────────────────────────────┤
│   Joystick / D-Pad (Center) │   App Updates Guide Dialog  │
├─────────────────────────────┴─────────────────────────────┤
│                 RemoteBottomControls.kt                   │
│ ┌───────────────┐  ┌───────────────────────┐  ┌─────────┐ │
│ │ Volume Slider │  │ Mute | Mic | Home     │  │ Scroll  │ │
│ │ (Slideable)   │  │ (Built-in Voice)      │  │ Slider  │ │
│ └───────────────┘  └───────────────────────┘  └─────────┘ │
└───────────────────────────────────────────────────────────┘
```

- **`RemoteApiClient.kt` & `RemoteCommand`**:
  - Add `action: "search_youtube"` with `text` query.
  - Add `action: "scroll"` with delta `dy` for wheel scrolling on PC.
- **`RemoteViewModel.kt`**:
  - `sendInstantYouTubeSearch(query: String)`: dispatches instant search command and updates UI state.
  - `onScrollDrag(deltaY: Float)`: queues smooth mouse scroll events to PC.
- **`RemoteBottomControls.kt`**:
  - Replace `ChannelRockerPill` with `SlideableScrollBar`.

---

## 4. Verification Plan

1. **Compilation**: Run `compile_applet` to verify zero build errors.
2. **Unit Tests**: Run `gradle :app:testDebugUnitTest` to verify JSON command serialization and ViewModel state.
3. **User Acceptance**: Test instant voice search dispatch and scroll slider responsiveness.
