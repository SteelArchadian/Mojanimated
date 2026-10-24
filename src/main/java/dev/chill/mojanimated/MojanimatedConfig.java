package dev.chill.mojanimated;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client settings in config/mojanimated-client.toml, editable in-game through NeoForge's
 * config screen (and Configured). Values are read when used, so edits apply on the next
 * loading screen without restarting.
 */
public final class MojanimatedConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue STARTUP_ANIMATION = BUILDER
            .comment("Play the animated logo intro when the game starts.")
            .translation(key("startupAnimation"))
            .define("startupAnimation", true);

    public static final ModConfigSpec.BooleanValue STARTUP_SOUND = BUILDER
            .comment("Play the Mojang Studios jingle with the startup intro.")
            .translation(key("startupSound"))
            .define("startupSound", true);

    public static final ModConfigSpec.BooleanValue RELOAD_ANIMATION = BUILDER
            .comment("Silently loop the logo animation while resource packs reload (F3+T, pack screen).")
            .translation(key("reloadAnimation"))
            .define("reloadAnimation", true);

    public static final ModConfigSpec.DoubleValue HOLD_SECONDS = BUILDER
            .comment("Seconds to hold the finished logo and \"Studios\" before the title screen.")
            .translation(key("holdSeconds"))
            .defineInRange("holdSeconds", 1.5, 0.0, 10.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MojanimatedConfig() {}

    private static String key(String name) {
        return Mojanimated.MOD_ID + ".configuration." + name;
    }

    public static boolean startupAnimation() {
        return read(STARTUP_ANIMATION);
    }

    public static boolean startupSound() {
        return read(STARTUP_SOUND);
    }

    public static boolean reloadAnimation() {
        return read(RELOAD_ANIMATION);
    }

    public static float holdSeconds() {
        return read(HOLD_SECONDS).floatValue();
    }

    /** Falls back to the default if the file isn't loaded yet, so the loading screen never crashes. */
    private static <T> T read(ModConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return value.getDefault();
        }
    }
}
