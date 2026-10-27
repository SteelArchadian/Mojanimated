> **Found a bug or have an idea?** Please post it on [GitHub Issues](https://github.com/SteelArchadian/Lightning/issues) instead of the comments. That's where every report gets read and tracked.

![Lightning](https://raw.githubusercontent.com/SteelArchadian/Lightning/main/.github/assets/banner.png)

**Lightning** finds out what's making your game slow and tells you in plain English. A profiler for frames, ticks, graphics, memory and mods, for Minecraft 1.21.1 with NeoForge.

Type `/lightning report`, play for a minute, and a report opens in your browser. Its first page lists the biggest problems with the evidence underneath.

<p align="center"><img src="https://raw.githubusercontent.com/SteelArchadian/Lightning/main/.github/assets/overview.png" alt="Report overview: the biggest problems in plain English" width="760"></p>

### What it records

- **Frames and server ticks** on one timeline, with stalls, garbage-collection pauses and log lines marked where they happened.
- **Graphics card time** per render step, including shader packs (Iris) and distant terrain.
- **Which mod** is behind slow code, memory use and memory churn.
- **Chunks, mobs and machines** that cost the most server time, with a chunk map.
- **Phases:** mark moments like "boss fight" and compare them side by side.

<p align="center"><img src="https://raw.githubusercontent.com/SteelArchadian/Lightning/main/.github/assets/frames.png" alt="Frame timeline with stalls and GC pauses marked" width="760"></p>

<p align="center"><img src="https://raw.githubusercontent.com/SteelArchadian/Lightning/main/.github/assets/sections.png" alt="Time per render section" width="760"></p>

### Slow start-up or world joins

Every loading step is timed from the moment Java starts: the slowest resource loaders, which mods' mixins slow loading, and what the game sat waiting on. Add this to the game's Java arguments, launch, and open the report's **Loading** tab:

```
-Dlightning.startup=join -XX:StartFlightRecording=name=lightning,settings=profile,class-loading=true
```

Use `title` instead of `join` to stop at the title screen.

<p align="center"><img src="https://raw.githubusercontent.com/SteelArchadian/Lightning/main/.github/assets/loading.png" alt="The Loading tab" width="760"></p>

### Commands

| Command | What it does |
|---|---|
| `/lightning report [seconds]` | Record for a while (default 60 s), then open the report |
| `/lightning profiler start` / `stop` / `cancel` / `status` | Manual recording |
| `/lightning mark <name>` | Start a named phase |
| `/lightning heap` | Quick look at what's filling memory |
| `/lightning heap deep` | Full memory breakdown by mod (freezes the game for a few seconds) |

<p align="center"><img src="https://raw.githubusercontent.com/SteelArchadian/Lightning/main/.github/assets/commands.png" alt="Lightning commands in chat" width="640"></p>

Lightning reports its own overhead, and if the game crashes mid-recording the report is rebuilt the next time you start. Each recorder can be turned on or off in **Mods → Lightning → Config**.

#### Credits
Source code on [GitHub](https://github.com/SteelArchadian/Lightning), licensed LGPL-3.0-only.

---
Made for the Foundation modpack, with help from Claude (an AI).
