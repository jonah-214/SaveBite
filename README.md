# SaveBite

SaveBite is an Android app that helps you track your food inventory, reduce food waste, and get recipe ideas based on what you already have at home.

## Features

- **Inventory Tracking** — Add and manage food items in your pantry, fridge, or freezer, with expiry date tracking and reminders before items expire.
- **Shopping List** — Keep a shopping list and move purchased items straight into your inventory.
- **Recipe Suggestions** — Get recipe recommendations based on your current inventory and personal recipe preferences.
- **Waste Reports** — View reports and breakdowns of wasted vs. used items to understand your food waste habits over time.
- **Reminders & Notifications** — Get notified before items are close to expiring.
- **User Profiles** — Sign up, log in, and manage your account and profile settings.
- **Cloud Sync** — Data is synced with Supabase so your inventory and reports stay up to date.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Backend:** Supabase
- **Architecture:** MVVM (ViewModel + Repository pattern)

## Project Structure

```
app/src/main/java/com/example/savebite/
├── data/
│   ├── ai/        # AI-related logic (e.g. recipe suggestions)
│   ├── local/      # Local data sources
│   ├── remote/     # Remote/Supabase data sources
│   └── repo/       # Repositories
├── model/          # Data models
├── notification/   # Reminder/notification handling
├── ui/
│   ├── navigation/ # App navigation
│   ├── screen/     # Compose screens
│   ├── theme/       # App theming
│   └── viewmodel/  # ViewModels
└── utils/          # Utility/helper classes
```
