# PVZ-2 — Advanced Programming Project

A hybrid **command-line + graphical** implementation of a Plants vs. Zombies inspired game for the Advanced Programming course.  
The project combines menu-driven workflows, persistent user data, and a real-time LibGDX game screen in a single codebase.

## Overview

This codebase is built around two complementary layers:

- **CLI menus** for account management, profile editing, collection browsing, quests, shop/greenhouse flows, leaderboard access, and other game-related management tasks.
- **Graphical gameplay** powered by **LibGDX**, with a dedicated game screen, HUD rendering, music management, brightness control, and in-game state updates.

The architecture is designed to keep game logic, menu logic, persistence, and rendering separated so the project remains maintainable as features grow.

## Key Features

- User registration, login, logout, and stay-logged-in support
- Persistent user storage with JSON-backed data files
- Secure password handling with hashing/encryption utilities
- Main menu, settings, news, profile, collection, shop, greenhouse, quest, and leaderboard flows
- Chapter and level selection
- Multiple minigame controllers and specialized menu handlers
- Plant and zombie libraries with factory-based creation
- Tick-based game progression and wave handling
- Sun, loot, and plant-food management
- Graphical game rendering with HUD, music, fonts, and screen transitions
- Clean separation between input parsing, output rendering, and game state

## Project Structure

A few of the main packages are:

- `com.PVZ.controller` — application and menu controllers
- `com.PVZ.database` — user persistence and cryptographic utilities
- `com.PVZ.model` — game state, entities, users, menus, chapters, and mechanics
- `com.PVZ.screen` — LibGDX screens and rendering layers
- `com.PVZ.view` — input parsing and output formatting
- `com.PVZ.util` — initialization and bootstrap helpers

## Architecture

The project uses a layered design:

- **Input layer**: command parsing and DTO-based command routing
- **Controller layer**: menu handlers and application dispatch
- **Model layer**: users, chapters, plants, zombies, waves, map state, and game engines
- **View layer**: textual output plus graphical rendering
- **Persistence layer**: file-based user data storage

This separation makes it easier to extend the project with new menus, gameplay systems, or UI flows without rewriting the whole codebase.

## Tech Stack

- **Java**
- **LibGDX**
- **Jackson** for JSON serialization/deserialization
- Standard Java concurrency and file I/O

## Run / Launch

The main application entry point is:

- `com.PVZ.PVZ`

Typical usage:

1. Open the project in your Java IDE.
2. Make sure the required resources and runtime dependencies are available.
3. Run the main class `com.PVZ.PVZ`.

## Persistence

User data is stored locally and loaded on startup, so progress can survive between runs.  
The project also supports a stay-logged-in flow for returning users.

## Team

This project was developed by:

- **Fatemeh Mostafavi**
- **Ali Ariakia**
- **Mahdi HajEbrahimi**

## Notes

- This repository is intended as a coursework project and ongoing development base.
- The current implementation focuses on structured gameplay systems, menu workflows, and a maintainable architecture.
- Future releases can expand balancing, content, polish, and presentation layers.

## License

No explicit license has been provided in the current project state.
