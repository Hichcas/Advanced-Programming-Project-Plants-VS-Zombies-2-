# Plants vs. Zombies-2 — Advanced Programming Project

A hybrid **CLI + graphical** implementation of a Plants vs. Zombies inspired game, developed for the Advanced Programming course.  
The project combines text-based menu workflows with a LibGDX-powered game screen, creating a single codebase that covers account management, progression systems, gameplay mechanics, and interactive rendering.

## Overview

This repository implements a course project inspired by *Plants vs. Zombies* and follows a layered architecture to keep the code organized and maintainable.

The application includes:

- **Command-line menus** for registration, login, profile management, collection browsing, settings, quests, leaderboard access, and more
- **Graphical gameplay** using **LibGDX**
- **Persistent user data** with local storage
- **Chapter-based progression**
- **Special minigames**
- **Plant and zombie systems** with multiple categories, abilities, and interactions
- **Tick-based game simulation** for time, waves, sun generation, and combat flow

## Main Features

### CLI / Menu System
The project includes a full menu framework with dedicated controllers and command parsing for:

- Register
- Login
- Main menu
- Chapter and level selection
- Plant selection
- Settings
- Network
- News
- Profile
- Collection
- Greenhouse
- Shop
- Travel log / quests
- Leaderboard
- In-game menu
- End-of-game menu

This part of the project provides the management layer for the entire game experience, including account flow, progression, unlocks, and player-related data.

### Graphical Game Layer
The graphical side of the project is handled through LibGDX and includes:

- A dedicated `GameScreen`
- Screen management and transitions
- Brightness control
- Font management
- Music management
- HUD and health bar rendering
- Real-time game state display

### Chapters
The adventure mode is organized into four chapters:

- **Ancient Egypt**
- **Frostbite Caves**
- **Big Wave Beach**
- **Dark Ages**

These chapters define different environments, stage behavior, and gameplay rules.

### Minigames
The project also supports several special minigame modes:

- **Vasebreaker**
- **Wallnut Bowling**
- **I, Zombie**
- **Beghouled**
- **Zombotany** *(bonus / extra content)*

### Plants
The plant system is built around a broad set of plant families and behavior types.  
Plants in the project are organized into categories such as:

- Sun producers
- Shooters
- Lobbers
- Explosives
- Melee attackers
- Defensive plants
- Support / modifier plants
- Through-strike plants
- Homing plants
- Mints

This structure makes it possible to support varied plant behavior, special effects, plant-food interactions, and upgrades.

### Zombies
The zombie system includes a large variety of zombie archetypes across the different chapters, including:

- Basic zombies
- Armored zombies
- Special-movement zombies
- Ranged / caster-style zombies
- Heavy zombies
- Special boss-related or bonus-type enemies

The codebase is designed around factory-based creation, scalable properties, status effects, and texture-path management.

### Core Gameplay Systems
The game logic includes:

- Tick-based time progression
- Wave spawning and wave difficulty scaling
- Sun production and sun collection
- Lawn mowers / lane-ending defense
- Plant food mechanics
- Loot and reward drops
- Unlock and progression systems
- Combat handling and battle flow
- Chapter and stage configuration
- Quest tracking
- Score / leaderboard integration

### Persistence
The project stores user data locally so progress is preserved between runs.  
It also includes encryption utilities and a file-based database layer for saved user information.

## Project Structure

The codebase is organized into clear packages:

- `com.PVZ.controller` — app control and menu controllers
- `com.PVZ.database` — local persistence and encryption utilities
- `com.PVZ.model` — users, game state, chapters, plants, zombies, quests, and mechanics
- `com.PVZ.screen` — LibGDX screens and rendering managers
- `com.PVZ.view` — command parsing, DTOs, and terminal/UI rendering helpers
- `com.PVZ.util` — bootstrap and helper utilities
- `com.PVZ.config` — configuration values

## Tech Stack

- **Java**
- **LibGDX**
- **Gradle**
- **Jackson** for JSON serialization/deserialization

## How to Run

The project is designed to be run with Gradle.

### Windows
```bash
gradlew.bat lwjgl3:run
```

### Linux / macOS
```bash
./gradlew lwjgl3:run
```

If your environment already provides a Gradle wrapper path or a project launcher, the same task name `lwjgl3:run` is the entry point for the graphical application.

## Team

This project was developed by:

- **Fatemeh Mostafavi**
- **Ali Ariakia**
- **Mahdi HajEbrahimi**

## Notes

- The project is a coursework implementation inspired by *Plants vs. Zombies*.
- It combines a command-line layer with a graphical runtime to cover both management and gameplay flows.
- The current codebase is structured for future expansion, balancing, and content growth.

## License

No explicit license has been provided in the current project state.
