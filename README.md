<div align="center">

# 🖼️ WallReddit — Reddit Wallpaper App for Android

**A modern, native Android wallpaper app that turns Reddit into an endless source of high-quality wallpapers — with auto-rotation, offline favorites, and a clean Material 3 interface.**

![Status](https://img.shields.io/badge/status-production-success)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-blue)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/architecture-MVVM%20%2B%20Clean-orange)](https://developer.android.com/topic/architecture)

</div>

---

## Overview

**WallReddit** is a native Android app built entirely in Kotlin with Jetpack Compose. It fetches images, GIFs, and videos from any combination of subreddits, lets users browse in a fluid staggered grid, set wallpapers directly to home/lock screens, save favorites offline, and even auto-rotate their wallpaper on a schedule.

It's built on a clean, layered architecture (data / domain / UI) with proper separation of concerns — the kind of structure that scales and stays maintainable, not a single-Activity prototype.

> Built by **[DG Technology](https://dgtechnology.com)** — engineered by Faiz Ullah.

---

## Key Features

- 🎴 **Multi-subreddit feed** — combine any subreddits into one infinite, paginated feed
- 🖌️ **One-tap wallpaper setting** — apply to home screen, lock screen, or both, with FIT / FILL / CROP scaling
- ⭐ **Offline favorites** — save wallpapers locally with full metadata, available without internet
- 🔄 **Auto-wallpaper scheduler** — rotate wallpapers automatically every 1h / 12h / 24h / weekly from favorites, downloads, or subreddits, powered by `WorkManager`
- 📥 **Download manager** — queue and track downloads via Android's native `DownloadManager`
- 🎬 **Image, GIF & video support** — full media handling including video wallpapers via Media3/ExoPlayer
- 📊 **Usage analytics screen** — track viewing and download stats with custom Compose-drawn charts
- 🔞 **NSFW controls** — hide, blur, or show, with separate blur-viewed option
- 🎨 **Material You theming** — AMOLED mode, dark mode, dynamic colors, adjustable grid density
- 🕐 **Viewed history** — automatically tracks what you've seen to avoid repeats

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin (100%) |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Clean Architecture (data / domain / UI layers) |
| **Async** | Kotlin Coroutines + Flow |
| **Networking** | Retrofit + OkHttp + Moshi |
| **Pagination** | Paging 3 (`androidx.paging`) |
| **Local storage** | Room (favorites, downloads, viewed history, subreddits) |
| **Preferences** | DataStore |
| **Images** | Coil |
| **Video** | Media3 / ExoPlayer |
| **Background work** | WorkManager (auto-wallpaper) |
| **DI** | Manual dependency injection via `AppContainer` |
| **Navigation** | Navigation Compose |

---

## Architecture

WallReddit follows **Clean Architecture** with three clearly separated layers:

```
app/src/main/java/com/example/
├── data/                      # Data layer
│   ├── remote/                # Retrofit Reddit API service + DTOs
│   ├── local/                 # Room database, DAOs, entities
│   ├── preferences/           # DataStore settings
│   └── repository/            # Repository implementation + Paging source
│
├── domain/                    # Domain layer (pure business logic)
│   ├── model/                 # WallpaperItem, MediaType
│   ├── repository/            # Repository interfaces
│   └── usecase/               # GetFeed, Favorites, Subreddit, Viewed use cases
│
├── ui/                        # Presentation layer
│   ├── screens/               # feed, favorites, downloads, subreddits,
│   │                          #   settings, analytics (Screen + ViewModel each)
│   ├── components/            # Reusable Compose components
│   └── theme/                 # Material 3 theming
│
├── service/                   # AutoWallpaperWorker (WorkManager)
├── wallpaper/                 # WallpaperSetter (bitmap scaling + apply)
├── download/                  # Download orchestration
└── di/                        # AppContainer (dependency graph)
```

**Why this matters:** the domain layer has zero Android dependencies — it's pure Kotlin. UI talks to use cases, use cases talk to repository interfaces, and only the data layer knows about Retrofit and Room. This makes every piece independently testable and swappable.

---

## Engineering Highlights

**Reactive settings with DataStore + Flow.** Every preference (NSFW mode, AMOLED, grid size, auto-wallpaper interval) is exposed as a `Flow`, so UI updates reactively the moment a setting changes — no manual refresh.

**Robust Reddit fetching.** The Retrofit client injects a real browser `User-Agent` to bypass Reddit's CDN anti-scraping and 403 blockers, with proper timeouts and logging interceptors.

**Smart bitmap scaling.** The `WallpaperSetter` computes screen aspect ratios and applies FIT (letterbox), FILL (center-crop), or CROP transformations with a `Matrix` before applying — so wallpapers always look right on any device.

**Battery-friendly automation.** Auto-wallpaper rotation runs through `WorkManager`, respecting Android's background execution limits and Doze mode.

---

## Build

```bash
# Open in Android Studio, then:
# 1. Set GEMINI_API_KEY in .env (see .env.example) if using AI features
# 2. Run on an emulator or device

./gradlew assembleRelease
# → app/build/outputs/apk/release/app-release.apk
```

**Permissions used:** `INTERNET`, `SET_WALLPAPER`, `SET_WALLPAPER_HINTS`, `RECEIVE_BOOT_COMPLETED`, `POST_NOTIFICATIONS` — all justified by the auto-wallpaper and download features.

---

<div align="center">

**Designed & engineered by Faiz Ullah**
Android Developer · Full-Stack Engineer · Founder of [DG Technology](https://dgtechnology.com)

📧 contact@faizullah.pk · 🌐 [faizullah.pk](https://faizullah.pk)

*Built with precision by DG Technology* 💙

</div>
