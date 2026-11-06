# FitTrack

An original fitness-tracking app (learning project), built using architecture and
UX patterns studied from Strava. Own name, own branding, own code — no Strava
assets, trademarks, or code are used. See `../Strava/BLUEPRINT.md` for the analysis.

## Sprint 1 (done) — scaffold
- Kotlin + Jetpack Compose + Material3
- Single-activity architecture (one `MainActivity`, Compose Navigation)
- Hilt dependency injection
- Supabase client (Auth + Postgrest + Storage + Realtime)
- Email/password auth (login + signup screens)
- Bottom-nav home: Feed / Map / Record / Profile (placeholders)

## Project layout
```
app/src/main/java/com/fittrack/app/
├─ FitTrackApp.kt            # @HiltAndroidApp
├─ MainActivity.kt           # single activity, chooses Auth vs Home
├─ di/SupabaseModule.kt      # provides Supabase client
├─ data/repository/AuthRepository.kt
├─ ui/
│  ├─ theme/Theme.kt         # FitTrack brand colors (teal, not orange)
│  ├─ navigation/            # routes + app nav host
│  └─ screens/               # auth, home scaffold, placeholders
```

## Setup (before first build)

### 1. Supabase
1. Create a free project at https://supabase.com
2. SQL Editor → paste & run `supabase_schema.sql`
3. Project Settings → API → copy the **Project URL** and **anon public key**

### 2. Mapbox (needed in Sprint 3)
1. Sign up at https://account.mapbox.com
2. Create a **public token** (`pk.*`) → put in `res/values/mapbox_token.xml`
3. Create a **secret download token** (`sk.*`, scope `DOWNLOADS:READ`)

### 3. Fill credentials in `gradle.properties` (do NOT commit real values)
```
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJ...
MAPBOX_DOWNLOADS_TOKEN=sk.xxxx
```

### 4. Build
Open in Android Studio (Koala+), let it sync, run on a device/emulator.
(This folder has no `gradlew` binary yet — Android Studio generates it, or run
`gradle wrapper` once if you have Gradle installed.)

## Next sprints
- **Sprint 2** — GPS recording: foreground service + live stats + Room buffer (the core)
- **Sprint 3** — Save activity → upload to Supabase + map detail
- **Sprint 4** — Feed, profile, totals
- **Sprint 5** — polish, charts, photos
- **Advanced** — 3D flyover (Mapbox terrain + camera flyTo) — see BLUEPRINT §8b
