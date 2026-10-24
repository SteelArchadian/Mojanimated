# Credits

**Mojanimated** is a NeoForge 1.21.1 mod that plays the animated Mojang Studios logo on the vanilla loading screen.

## Animated Mojang Logo — BouncingElf10

The logo animation frames (`assets/mojanimated/textures/gui/frames/`), the "Studios" image
(`assets/mojanimated/textures/gui/studios.png`), the startup jingle (`assets/mojanimated/sounds/startup.ogg`),
the icon, and the animation timing and layout are taken from
**Animated Mojang Logo** by **BouncingElf10**, published under GPL-3.0-or-later:

- Modrinth: https://modrinth.com/mod/animated-mojang-logo
- Source: https://github.com/BouncingElf10/Animated-Mojang-Logo

Mojanimated is a rewrite of how that animation hooks into the game: it uses the vanilla loading screen
(so it works with NeoForge's `earlyWindowControl` off), never cancels the loading screen's own rendering
(so FancyMenu and other menu mods keep working), and adds a silent looping animation for resource pack reloads.

## Mojang

The Mojang Studios logo, its animation, and the Mojang Studios jingle belong to **Mojang Studios / Microsoft**.
They are not covered by this project's license. This is an unofficial fan mod, not affiliated with or endorsed
by Mojang or Microsoft.

## License

Mojanimated's code is licensed under the GNU General Public License v3.0 or later. See `LICENSE`.
