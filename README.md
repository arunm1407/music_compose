# DVibes - Tamil Music Streaming App

A modern Android music streaming app built with Jetpack Compose and Material 3, focused on Tamil music. Streams audio via the Deezer API with background playback powered by Media3 ExoPlayer.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Audio:** Media3 ExoPlayer with MediaSessionService
- **Networking:** Retrofit + OkHttp (Deezer API)
- **Image Loading:** Coil
- **Architecture:** MVVM (ViewModel + StateFlow)

## Screens

### Home Screen

Curated Tamil music sections with a quick-access grid and horizontally scrollable song cards. Pull-to-refresh to reload content.

```
┌─────────────────────────────────────┐
│  (D)  Good vibes           [!] [⚙] │
├─────────────────────────────────────┤
│ ┌───────────────┐ ┌───────────────┐ │
│ │ [img] Song 1  │ │ [img] Song 2  │ │
│ └───────────────┘ └───────────────┘ │
│ ┌───────────────┐ ┌───────────────┐ │
│ │ [img] Song 3  │ │ [img] Song 4  │ │
│ └───────────────┘ └───────────────┘ │
│                                     │
│  Tamil Hits                         │
│  ┌───────┐ ┌───────┐ ┌───────┐     │
│  │       │ │       │ │       │ ──>  │
│  │  img  │ │  img  │ │  img  │     │
│  │       │ │       │ │       │     │
│  └───────┘ └───────┘ └───────┘     │
│  Song Title Song Title Song Title   │
│  Artist     Artist     Artist       │
│                                     │
│  Anirudh Ravichander                │
│  ┌───────┐ ┌───────┐ ┌───────┐     │
│  │       │ │       │ │       │ ──>  │
│  │  img  │ │  img  │ │  img  │     │
│  └───────┘ └───────┘ └───────┘     │
│                                     │
│  A.R. Rahman                        │
│  ┌───────┐ ┌───────┐ ┌───────┐     │
│  │  ...  │ │  ...  │ │  ...  │ ──>  │
│  └───────┘ └───────┘ └───────┘     │
└─────────────────────────────────────┘
```

**Sections:** Tamil Hits, Anirudh Ravichander, A.R. Rahman, Tamil Love Songs, Mass & Kuthu, New Releases

---

### Search Screen

Search songs with debounced input. Browse by Tamil genre when idle.

```
┌─────────────────────────────────────┐
│  Search                             │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ [Q] Songs, artists, albums...  │ │
│ └─────────────────────────────────┘ │
│                                     │
│  Browse Tamil                       │
│  ┌────────────┐  ┌────────────┐     │
│  │ Tamil Hits │  │  Anirudh   │     │
│  │   (red)    │  │  (navy)    │     │
│  └────────────┘  └────────────┘     │
│  ┌────────────┐  ┌────────────┐     │
│  │ AR Rahman  │  │   Yuvan    │     │
│  │  (purple)  │  │  (green)   │     │
│  └────────────┘  └────────────┘     │
│  ┌────────────┐  ┌────────────┐     │
│  │ Ilaiyaraaja│  │ Hip-Hop    │     │
│  │   (pink)   │  │  Tamil     │     │
│  └────────────┘  └────────────┘     │
│  ┌────────────┐  ┌────────────┐     │
│  │ Tamil      │  │   Kuthu    │     │
│  │  Melody    │  │   (blue)   │     │
│  └────────────┘  └────────────┘     │
└─────────────────────────────────────┘

         Search Results View:

┌─────────────────────────────────────┐
│ ┌─────────────────────────────────┐ │
│ │ [Q] anirudh                     │ │
│ └─────────────────────────────────┘ │
│                                     │
│  [img]  Song Title                  │
│         Artist . Album              │
│  [img]  Song Title                  │
│         Artist . Album              │
│  [img]  Song Title                  │
│         Artist . Album              │
│  [img]  Song Title                  │
│         Artist . Album              │
│         ...                         │
└─────────────────────────────────────┘
```

---

### Library Screen

Displays all available songs in a scrollable list.

```
┌─────────────────────────────────────┐
│  Your Library                  [+]  │
│                                     │
│  ┌────┐  Song Title                 │
│  │img │  Artist . Song              │
│  └────┘                             │
│  ┌────┐  Song Title                 │
│  │img │  Artist . Song              │
│  └────┘                             │
│  ┌────┐  Song Title                 │
│  │img │  Artist . Song              │
│  └────┘                             │
│  ┌────┐  Song Title                 │
│  │img │  Artist . Song              │
│  └────┘                             │
│  ┌────┐  Song Title                 │
│  │img │  Artist . Song              │
│  └────┘                             │
│         ...                         │
│                                     │
│                                     │
└─────────────────────────────────────┘

         Empty State:

┌─────────────────────────────────────┐
│  Your Library                  [+]  │
│                                     │
│                                     │
│                                     │
│     Songs you play will appear      │
│               here                  │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

---

### Player Screen

Full-screen player with album art, seek bar, and playback controls. Gradient background (dark green to black).

```
┌─────────────────────────────────────┐
│  [v]   PLAYING FROM        [...]   │
│          Album Name                 │
│                                     │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │                             │    │
│  │                             │    │
│  │        Album Artwork        │    │
│  │                             │    │
│  │                             │    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                     │
│  Song Title                    [<3] │
│  Artist Name                        │
│                                     │
│  ──●──────────────────────────      │
│  0:42                      3:28     │
│                                     │
│  [shuffle] [<<] [ ▶ ] [>>] [repeat]│
│                                     │
└─────────────────────────────────────┘
```

**Controls:** Shuffle, Previous, Play/Pause, Next, Repeat (Off/One/All)

---

### Mini Player

Compact player bar that appears above the bottom navigation when a song is playing. Tap to open the full player.

```
┌─────────────────────────────────────┐
│ ┌─────────────────────────────────┐ │
│ │ [img]  Song Title      [▶] [>>]│ │
│ │        Artist                   │ │
│ │ ████████░░░░░░░░░░░░░░░░░░░░░░ │ │
│ └─────────────────────────────────┘ │
│   [ Home ]    [ Search ]  [ Library]│
└─────────────────────────────────────┘
```

---

### Bottom Navigation

Three tabs: Home, Search, Library. Uses filled icons for selected tab and outlined icons for unselected.

```
┌─────────────────────────────────────┐
│   [Home]      [Search]   [Library]  │
│    Home        Search     Library   │
└─────────────────────────────────────┘
```

## Project Structure

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt              # Single Activity, navigation host
├── MusicViewModel.kt            # App state management
├── data/
│   ├── Models.kt                # Song, PlayerState, HomeState
│   └── MusicRepository.kt       # Deezer API calls
├── network/
│   └── DeezerApi.kt             # Retrofit API interface
├── service/
│   └── MusicService.kt          # Media3 background playback
├── ui/
│   ├── components/
│   │   ├── MiniPlayer.kt        # Compact player bar
│   │   └── BottomNavBar.kt      # Bottom navigation
│   ├── screens/
│   │   ├── HomeScreen.kt        # Curated music sections
│   │   ├── SearchScreen.kt      # Search + genre browse
│   │   ├── PlayerScreen.kt      # Full-screen player
│   │   └── LibraryScreen.kt     # Song library
│   └── theme/
│       ├── Color.kt             # Spotify-inspired palette
│       ├── Theme.kt             # Dark theme config
│       └── Type.kt              # Typography
```

## Building

```bash
./gradlew assembleDebug
```

## License

Private project.
