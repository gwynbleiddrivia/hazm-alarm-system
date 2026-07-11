# CLAUDE.md — Hazm Alarm

**Hazm**: native Android alarm (Kotlin + Jetpack Compose) for deep sleepers who pray.
Core promise: a Doze-proof, OEM-kill-proof alarm that won't stop until the user proves
they're awake. Package `com.dreyfus.hazm`, Gradle root `hazmAlarm`.

## How we work (conventions)
- Learning/guided: **the user writes the app code**; Claude explains and says what to type
  where. Claude may write docs (like this file), not app code, unless asked.
- **CI + real phone = source of truth.** edit → `git push` → GitHub Actions builds a debug
  APK artifact → install on phone. No emulators (they miss Doze / OEM kills).
- **One change per CI push** (e.g. Compose in one push, Room in the next) so a red build
  blames exactly one thing.
- No local Gradle builds, no `.devcontainer` (it broke the container; was never committed,
  won't return). A fresh Codespace = clean env if ever needed.
- **One chat session per build step** to save tokens; update `CURRENT STEP` at session end.
- Sessions don't share memory; this file is the carry-forward, so keep it lean.

## Toolchain (actual values — verify before bumping)
- AGP **9.0.1** → built-in Kotlin (KGP **2.2.10**); do NOT apply `kotlin.android`.
- minSdk 26, compile/target SDK **35**, Gradle **9.4.1**. CI: temurin **21** + setup-android.
- Compose: apply `org.jetbrains.kotlin.plugin.compose` @ **2.2.10** + `buildFeatures{compose=true}`
  + Compose BOM. (Compose 1.12+ needs compileSdk 37 — bump only if a BOM demands it.)
- Room **2.8.4** (Google Maven, not Central): **KSP** `com.google.devtools.ksp` @ **2.2.10-2.0.2**;
  `ksp("androidx.room:room-compiler")`. Plugin **version** goes in ROOT `build.gradle.kts`
  (`apply false`); the app module applies the bare `id(...)` (no version, no `apply false`).
- **AGP 9 + KSP gotcha:** needs `android.disallowKotlinSourceSets=false` in `gradle.properties`
  (KSP registers generated sources via the old `kotlin.sourceSets` DSL that built-in Kotlin blocks).
- Node.js-20-deprecation lines in CI = harmless GitHub Actions runner warning, not our code.
- Diagnose CI failures without the user: pull the run log via the stored PAT →
  `curl -H "Authorization: token <PAT>" .../actions/jobs/<id>/logs`.

## The reliable-alarm core — 6-link chain (north star)
1. SCHEDULE `AlarmManager.setAlarmClock()` + `USE_EXACT_ALARM`
2. SURVIVE `BOOT_COMPLETED` receiver reschedules alarms from Room
3. FIRE lightweight `AlarmReceiver` trampoline (no work in `onReceive`)
4. WAKE full-screen-intent Activity, `setShowWhenLocked` + `setTurnScreenOn`
5. RING foreground Service (mediaPlayback) + WakeLock; sound + vibrate
6. PERSIST Room holds alarms; rings until the dismiss-gate passes

Boss fight: OEM battery-killers (Xiaomi/Realme/Oppo/Vivo/Samsung) → battery-opt exemption
+ autostart onboarding (source: dontkillmyapp.com).

## Build order
- [x] 0 Skeleton + CI (done: APK installs on phone)
- [x] 1 Room alarm entity + DAO — save & read one alarm (CI GREEN, APK builds;
      final phone check = alarm count climbs each launch = persistence proven)
- [x] 1.5 Wire in Jetpack Compose (CI GREEN, APK installs; Compose `Text` renders the
      alarm summary, count still climbs each launch = Room intact through the swap)
- [x] 2 AlarmScheduler wrapping setAlarmClock + lightweight AlarmReceiver trampoline
      (CI GREEN, APK installs; firing is log-only so NOT visually confirmed yet — no
      logcat available. Real proof comes at Step 3/4 when a screen/service shows on fire.)
- [ ] 3 Full-screen Activity over the locked screen  ← NEXT
- [ ] 4 Foreground service: sound + vibrate + wakelock
- [ ] 5 Dismiss path (stop service, cancel notification)
- [ ] 6 BootReceiver reschedule (survive reboot)
- [ ] 7 Permission onboarding (exact-alarm, FSI, POST_NOTIFICATIONS, battery/OEM)
- [ ] 8 Overnight real-device test
- Feature layer after: proof-of-wake gates (shake→math→QR), prayer times (manual/OCR),
  sleep-cycle planner, "X min to prayer" escalation, calm UI.

## Key decisions
- `setAlarmClock` = the only Doze-exempt tier never delayed/batched (can't miss Fajr).
- `USE_EXACT_ALARM` = install-granted, non-revocable; app qualifies (its core is alarms).

## CURRENT STEP
Step 2 DONE — chain links 1 & 3 (SCHEDULE + FIRE) in place. CI GREEN, APK installs.
- `AlarmScheduler.kt`: wraps `AlarmManager.setAlarmClock(AlarmClockInfo, firePending)`.
  `firePending` = `getBroadcast` → `AlarmReceiver` (request code = `alarm.id`, so no
  collisions); `showPending` = `getActivity` → `MainActivity`. Both PendingIntents use
  `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` (IMMUTABLE mandatory on targetSdk 35).
- `AlarmReceiver.kt`: BroadcastReceiver trampoline, body is just `Log.d` (no heavy work in
  `onReceive`). Registered in manifest `<receiver ... exported="false">`.
- Manifest: added `<uses-permission USE_EXACT_ALARM>` (setAlarmClock is exempt anyway).
- `MainActivity`: after reading alarms, schedules `alarms.last()` for `now + 10_000ms` as a
  test-fire on every launch. TEMP scaffolding — keep it; Step 3 makes it launch the FSI.
CAVEAT: firing is LOG-ONLY, so not visually confirmed. User has no logcat (Codespace + phone
only, no Android Studio/adb), declined a Toast. Treat firing as "coded, unproven" until a
visible surface exists (Step 3 full-screen Activity, or Step 4 service). Verify then.
The white-screen-with-tiny-text UI and count-climbing-per-launch are both EXPECTED, not bugs.
Env: Codespace has no local JDK/gh/adb; push via PAT (creds stored). Git-LFS hooks removed.
`MainActivity` still uses `.allowMainThreadQueries()` — TEMP, move DB off UI thread later.
Files: `app/src/main/java/com/dreyfus/hazm/{Alarm,AlarmDao,AppDatabase,MainActivity,
AlarmScheduler,AlarmReceiver}.kt` + `AndroidManifest.xml`.
NEXT: Step 3 — full-screen-intent Activity over the locked screen (`setShowWhenLocked` +
`setTurnScreenOn`), launched when the alarm fires. This is where link 3's trampoline finally
does something visible: `AlarmReceiver` fires the FSI instead of just logging. Chain link 4.
