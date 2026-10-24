package dev.chill.mojanimated;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;

/**
 * Animation state for one LoadingOverlay. The mixin asks it three things each frame:
 * what to draw in place of the vanilla logo, whether to draw the progress bar, and whether
 * the overlay may treat the reload as done (so vanilla's own finish code runs only after
 * the animation has landed — that is what keeps FancyMenu's screen setup working).
 */
public final class OverlayAnimation {
    private enum Kind { STARTUP, RELOAD }

    private static final int LAST_FRAME = FrameStore.FRAMES - 1;
    private static final float FPS = 25f;

    // Startup timeline, matching the original mod so the jingle stays in sync.
    private static final float LOGO_FADE_IN = 0.3f;
    private static final float LOGO_END = 3.0f;
    private static final float STUDIOS_FADE_START = LOGO_END + 0.2f;
    private static final float STUDIOS_FADE_END = LOGO_END + 0.6f;
    /** Give up waiting for frames this long after loading finished, so the game never hangs. */
    private static final long FRAME_WAIT_LIMIT_MS = 15_000L;

    // Reload loop: after loading finishes, finish the forward pass and hold briefly on "MOJANG".
    private static final long RELOAD_SETTLE_HOLD_MS = 250L;

    private static boolean firstOverlaySeen;
    private static boolean startupPlaying;

    private final Kind kind;
    private boolean abandoned;

    // STARTUP
    private long doneSeenMs = -1L;
    private long introStartMs = -1L;

    // RELOAD
    private long loopStartMs = -1L;
    private long settleStartMs = -1L;
    private int settleFromFrame;

    private OverlayAnimation(Kind kind) {
        this.kind = kind;
    }

    /** Returns null when this overlay should stay fully vanilla. */
    public static OverlayAnimation create(boolean fadeIn) {
        boolean first = !firstOverlaySeen;
        firstOverlaySeen = true;
        Kind kind = null;
        if (first && !fadeIn && MojanimatedConfig.startupAnimation()) kind = Kind.STARTUP;
        else if (fadeIn && MojanimatedConfig.reloadAnimation()) kind = Kind.RELOAD;
        if (kind == null) return null;

        FrameStore.ensureLoading();
        return new OverlayAnimation(kind);
    }

    /** True while the startup intro is on screen; used to hold back menu music. */
    public static boolean isStartupPlaying() {
        return startupPlaying;
    }

    private boolean active() {
        return !abandoned && !FrameStore.hasFailed();
    }

    /** Whether the vanilla Mojang Studios logo should be replaced this frame. */
    public boolean replacesLogo() {
        if (!active()) return false;
        // Startup: the original mod shows only the bar while loading, then the intro.
        if (kind == Kind.STARTUP) return true;
        // Reload: keep the vanilla logo until our frames are uploaded.
        return FrameStore.isReady();
    }

    public boolean hidesProgressBar() {
        return active() && kind == Kind.STARTUP && introStartMs >= 0;
    }

    /**
     * Wraps ReloadInstance.isDone() inside LoadingOverlay.render. Vanilla only calls it until
     * the overlay starts finishing, so returning false here just delays the normal finish.
     */
    public boolean gate(boolean reallyDone) {
        if (!active()) return reallyDone;
        long now = Util.getMillis();

        if (kind == Kind.STARTUP) {
            if (!reallyDone) return false;
            if (introStartMs < 0 && net.neoforged.fml.ModLoader.hasErrors()) {
                // Mod loading failed: our registrations may never have happened, and the player needs to see
                // NeoForge's error screen right away, not a logo.
                Mojanimated.LOGGER.warn("Mod loading reported errors; skipping the intro so the error screen shows");
                abandoned = true;
                return true;
            }
            if (introStartMs < 0) {
                if (doneSeenMs < 0) doneSeenMs = now;
                if (!FrameStore.isReady()) {
                    if (now - doneSeenMs > FRAME_WAIT_LIMIT_MS) {
                        Mojanimated.LOGGER.warn("Logo frames were not ready in time; skipping the intro");
                        abandoned = true;
                        return true;
                    }
                    return false;
                }
                startIntro(now);
                return false;
            }
            boolean finished = seconds(now - introStartMs) >= STUDIOS_FADE_END + MojanimatedConfig.holdSeconds();
            if (finished) startupPlaying = false;
            return finished;
        }

        // RELOAD
        if (!reallyDone) return false;
        if (!FrameStore.isReady() || loopStartMs < 0) return true;
        if (settleStartMs < 0) {
            settleStartMs = now;
            settleFromFrame = loopFrame(now);
        }
        long settleMs = (long) ((LAST_FRAME - settleFromFrame) * 1000f / FPS);
        return now - settleStartMs >= settleMs + RELOAD_SETTLE_HOLD_MS;
    }

    private void startIntro(long now) {
        introStartMs = now;
        startupPlaying = true;
        Mojanimated.LOGGER.info("Starting Mojang logo intro");
        if (MojanimatedConfig.startupSound() && Mojanimated.STARTUP_SOUND.isBound()) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Mojanimated.STARTUP_SOUND.get(), 1.0f));
        }
    }

    /**
     * Draws in place of the vanilla logo. The vanilla code has already set the shader colour
     * alpha to its own fade value, so we multiply by that to fade in and out with the overlay.
     */
    public void renderLogo(GuiGraphics graphics) {
        FrameStore.tickUpload();
        float overlayAlpha = RenderSystem.getShaderColor()[3];
        long now = Util.getMillis();

        if (kind == Kind.STARTUP) {
            if (introStartMs < 0) return;
            float t = seconds(now - introStartMs);
            float logoAlpha = Mth.clamp(t / LOGO_FADE_IN, 0f, 1f);
            float progress = Mth.clamp((t - LOGO_FADE_IN) / (LOGO_END - LOGO_FADE_IN), 0f, 1f);
            int frame = Math.round(progress * LAST_FRAME);
            float studiosAlpha = Mth.clamp((t - STUDIOS_FADE_START) / (STUDIOS_FADE_END - STUDIOS_FADE_START), 0f, 1f);
            LogoRenderer.render(graphics, frame, overlayAlpha * logoAlpha, overlayAlpha * studiosAlpha);
            return;
        }

        if (!FrameStore.isReady()) return;
        if (loopStartMs < 0) loopStartMs = now;
        int frame;
        if (settleStartMs >= 0) {
            frame = Math.min(LAST_FRAME, settleFromFrame + (int) ((now - settleStartMs) * FPS / 1000f));
        } else {
            frame = loopFrame(now);
        }
        LogoRenderer.render(graphics, frame, overlayAlpha, 0f);
    }

    /**
     * Ping-pong through the frames (tumble into MOJANG, then back out) so the loop never jumps.
     * When settling, a backwards pass simply turns around and runs forward from where it is.
     */
    private int loopFrame(long now) {
        int period = LAST_FRAME * 2;
        int step = (int) ((now - loopStartMs) * FPS / 1000f) % period;
        return step <= LAST_FRAME ? step : period - step;
    }

    private static float seconds(long millis) {
        return millis / 1000f;
    }
}
