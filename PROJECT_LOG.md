# FitTrack — Project Log & Conversation Summary

> Session log of building FitTrack, a Strava-style fitness app (learning project),
> by reverse-engineering Strava for architecture patterns and building an original app.
> Last updated: 2026-11-21

---

## What this project is
An **original** fitness-tracking Android app (own name, branding, code) built using
the architecture and UX patterns studied from Strava's APK. No Strava code, logos,
or trademarks are used. Analysis lives in `../Strava/BLUEPRINT.md`.

- **GitHub:** https://github.com/Mohit2-22/Reverse-engineering (SSH, account Mohit2-22)
- **Git identity:** Mohit2-22 <mohitprajapati4412@gmail.com>

---

## Environment setup (done on this machine — Pop!_OS / Linux)
Needed because there is NO Android Studio; everything is command-line.

| Tool | Where / version |
|------|-----------------|
| Node | v24.22.2 via nvm (nvm default = 24) — REA MCP needs it, v20 crashed |
| adb | /usr/bin/adb (apt: android-tools-adb) |
| Java JDK | openjdk-21-jdk (JRE alone was not enough — needed javac) |
| Android SDK | ~/Android/Sdk (cmdline-tools + platform-tools + platforms;android-35 + build-tools;35.0.0) |
| Gradle | wrapper (gradlew) — downloads Gradle 8.11.1 |
| jadx | ~/jadx/bin/jadx (1.5.0) — used to decompile Strava manifest/resources |

**Build command:**
```bash
cd FitTrack
export ANDROID_HOME="$HOME/Android/Sdk"
export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"
./gradlew :app:assembleDebug --no-daemon
# APK → app/build/outputs/apk/debug/app-debug.apk
```

**REA MCP note:** REA is registered to launch with Node 24 explicitly:
`~/.nvm/versions/node/v24.22.2/bin/node /usr/local/lib/node_modules/rea-agents/scripts/rea.mjs mcp`

---

## Test devices (adb)
- **Redmi Note 8** — serial `11849a4d`, Android 11 (SDK 30) — CURRENT test device (USB)
- Motorola Edge 70 Fusion — serial `ZA223NLM9K` — earlier device; USB kept dropping,
  used WiFi adb (`adb pair` / `adb connect <ip>:<port>`) which was stable.

**Install:** `adb -s 11849a4d install -r app/build/outputs/apk/debug/app-debug.apk`

---

## What Strava uses (from APK analysis — see ../Strava/BLUEPRINT.md)
- Kotlin + Jetpack Compose + Material3, single-activity (only 7 activities in manifest)
- Hilt (DI), Room + DataStore, OkHttp/Retrofit, REST `/api/v3/`
- **Mapbox** (maps, not Google Maps), FATMAP SDK (3D terrain), Rive (animations),
  Stream (chat), Sentry/Embrace (observability)
- **3D flyover** = FATMAP terrain engine + Mapbox camera `flyTo` (see BLUEPRINT §8b)
- 78 feature modules mapped to tiers in BLUEPRINT

---

## Progress — what's built

### Sprint 1 ✅ (scaffold + auth) — DONE, on phone, pushed
- Single-activity + Compose Navigation (bottom nav: Feed/Map/Record/Profile)
- Hilt DI, Supabase client (Auth/Postgrest/Storage/Realtime)
- Email/password auth (login + signup) — **worked end-to-end**
- FitTrack teal branding, adaptive launcher icon
- Mapbox deferred (commented out) until Sprint 3 — needs MAPBOX_DOWNLOADS_TOKEN

### Sprint 2 ✅ (GPS recording) — DONE, builds clean, installed
- `RecordingManager` — FusedLocationProvider → live RecordingStats StateFlow
  (distance/time/pace/elevation), haversine math, GPS noise filtering
- `RecordingService` — foreground service (background GPS, screen-off safe) + notification
- `GeoMath` — distance / plausibility filter / elevation gain
- `RecordScreen` — live UI, permission flow, start/pause/stop, type selector
- Files under `data/recording/` and `ui/screens/record/`

---

## Backend — Supabase
- Project ref was `ixtjeaqvjimuaioseaat` (URL `https://ixtjeaqvjimuaioseaat.supabase.co`)
- Uses NEW key format: publishable key `sb_publishable_...` (not legacy `eyJ` JWT)
- Schema in `supabase_schema.sql` (Postgres + PostGIS, RLS). Signup trigger fix in
  `supabase_fix_signup.sql` (fixed "Database error saving new user").
- **Secrets live in `local.properties`** (gitignored), read by `app/build.gradle.kts`.
  Never committed. `gradle.properties` has placeholders only.

### ⚠️ OPEN ISSUE (where we stopped)
Login started failing with: `Unable to resolve host "ixtjeaqvjimuaioseaat.supabase.co"`
— the hostname no longer resolves (confirmed from the dev machine too, not a phone issue).
**Cause:** the Supabase free project was **paused/restricted** (org had a quota-exceeded
warning: "restricted from 08 Nov 2026"). 

**To resume FitTrack work:**
1. Go to https://supabase.com/dashboard → open the `fittrack` project.
2. If "Paused" → click Restore/Resume (hostname comes back in 1-2 min). Same URL/key work again.
3. If quota-restricted and can't restore → create a NEW Supabase project, then update
   `local.properties` (SUPABASE_URL + SUPABASE_ANON_KEY = the new publishable key) and rebuild.

---

## Next steps (when resuming FitTrack)
1. Fix Supabase (resume or new project) → verify login again.
2. Sprint 3 — Save activity → upload to Supabase (activities + streams tables) +
   activity detail screen with map. Re-enable Mapbox (needs tokens; see settings.gradle.kts
   and app/build.gradle.kts commented blocks + res/values/mapbox_token.xml to recreate).
3. Sprint 4 — Feed, profile, totals.
4. Advanced — 3D flyover (Mapbox terrain + camera flyTo).

---

## Key files
- `BLUEPRINT` → `../Strava/BLUEPRINT.md` (the full RE analysis + plan)
- `README.md` — setup steps
- `supabase_schema.sql`, `supabase_fix_signup.sql` — DB
- `local.properties` — secrets (gitignored; must be recreated on a fresh clone)
