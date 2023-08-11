# HikaBrain
[![License: CC BY-NC-SA 4.0](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-sa/4.0/)

A recreation of the classic french Minecraft minigame Hikabrain! Made for Paper 1.20+ with Kotlin

- 🎥🔴 **[Watch the demo on YouTube!](https://www.youtube.com/watch?v=VIDEO_ID)**

# Usage
WorldEdit is required to use this plugin

## In-Game commands
- `/arena list` to display the list of arenas
- `/arena join <ID>` to join an arena
- `/arena leave <ID>` to leave an arena

## Config
To create new arenas, simply add their position in the config.yml following this format:
```yml
arenas:
  0: # This is one arena
    type: 'HIKABRAIN-SOLO'
    paste-position:
      world: 'void'
      x: 100
      y: 60
      z: 100
      yaw: 0
      pitch: 0
  1: # this is another arena
    type: 'HIKABRAIN-SOLO'
    paste-position:
      world: 'void'
      x: 300
      y: 50
      z: 1000
      yaw: 0
      pitch: 0
  2: #...
```

# License
Distributed under the CC [BY-NC-SA 4.0](LICENSE.md) license


