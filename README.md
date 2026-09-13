# Recharge Android App

Recharge is a mindfulness and app-queueing tool designed to help you transition intentionally between apps rather than doom-scrolling. 

## Features
* **Intentional Queue**: Organize your most important apps into a sequence.
* **Mindful Restore**: A 5-minute cooldown (with video and breathing exercises) before you switch to your next app.
* **Sync**: Background syncing of app usage and events with Supabase.
* **Custom Quotes**: Edit the quotes that are displayed during transitions.

## Project Structure
* `MainActivity` sets up the Navigation-Compose graph and initializes background workers.
* **UI**: Uses Jetpack Compose. Organized into `/auth`, `/home`, `/settings`, `/video`, `/handoff`, and `/queue` packages.
* **Data**: Uses Room for local database (queue items, events, sessions) and DataStore for user preferences (queue state, timings).
* **Sync/Tracking**: Uses WorkManager for background tasks (`SupabaseSyncWorker` and `UsageTrackingWorker`).

## Setup
You need a Supabase project to handle auth and data syncing.
1. Copy `local.properties.example` to `local.properties` (or create it if it doesn't exist).
2. Add your Supabase credentials to `local.properties`:
```
SUPABASE_URL="https://your-project.supabase.co"
SUPABASE_ANON_KEY="your-anon-key"
```

## Building
Use Android Studio or Gradle to build the project:
```bash
./gradlew build
```

*Note: You need a Java runtime configured for Gradle.*
