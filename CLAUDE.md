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
- [ ] 1.5 Wire in Jetpack Compose (deferred from Step 1: AGP9/Compose version risk)  ← NEXT
- [ ] 2 AlarmScheduler wrapping setAlarmClock (fires, logs on receive)
- [ ] 3 Full-screen Activity over the locked screen
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
Step 1 DONE — Room `Alarm` entity + `AlarmDao` + `AppDatabase`; MainActivity inserts one
alarm and shows all on a plain TextView (static text `Fajr @ 4:30` on phone = working;
count climbs each launch = persistence proven). CI GREEN, produces `app-debug-apk`.
Files: `app/src/main/java/com/dreyfus/hazm/{Alarm,AlarmDao,AppDatabase,MainActivity}.kt`.
Note: `MainActivity` uses `.allowMainThreadQueries()` — TEMP, move DB off UI thread later.
Env: Codespace has no local JDK/gh; push via PAT (creds stored). Git-LFS hooks were
removed from `.git/hooks` (LFS not installed / not used).
NEXT: Step 1.5 — wire in Jetpack Compose (minimal @Composable screen), one CI push.
