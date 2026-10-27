> **Found a bug or have an idea?** Please post it on [GitHub Issues](https://github.com/SteelArchadian/Mojanimated/issues) instead of the comments. That's where every report gets read and tracked.

![Mojanimated](https://raw.githubusercontent.com/SteelArchadian/Mojanimated/main/.github/assets/banner.png)

![The animated logo](https://raw.githubusercontent.com/SteelArchadian/Mojanimated/main/.github/assets/showcase.gif)

Mojanimated plays an animated Mojang Studios logo with its jingle on the startup loading screen, and on resource reloads too.

- **Plays on NeoForge every time:** works whether or not NeoForge's early loading window is on, and on resource pack reloads.
- **Friendly to other mods:** it doesn't take over the loading screen, so FancyMenu layouts and other title-screen mods keep working.
- **Configurable:** switch the startup animation, the jingle and the reload animation on or off separately, and set how long the logo stays on screen.

### Credits and what's different
Mojanimated is based on **[Animated Mojang Logo](https://modrinth.com/mod/animated-mojang-logo)** by **BouncingElf10** ([source code](https://github.com/BouncingElf10/Animated-Mojang-Logo)), which is open source under **GPL-3.0-or-later**. Mojanimated uses its logo animation frames, "Studios" image, startup jingle, icon and animation timing, and is released under the same GPL-3.0-or-later license. Thank you, BouncingElf10!

**How Mojanimated differs from the original:**
- **New loading-screen hook:** the code that shows the animation was rewritten to draw on the vanilla loading screen, so it works when NeoForge's early loading window is turned off (`earlyWindowControl = false`).
- **Works with other menu mods:** it never cancels the loading screen's own drawing, so FancyMenu layouts and other loading/title-screen mods keep working.
- **Resource reload animation:** a new silent, looping version of the logo plays during resource pack reloads.

The Mojang Studios logo and jingle belong to Mojang Studios / Microsoft. This is an unofficial fan mod, not made or endorsed by Mojang, Microsoft or BouncingElf10.

---
Made for the Foundation modpack, with help from Claude (an AI).