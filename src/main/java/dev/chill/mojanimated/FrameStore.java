package dev.chill.mojanimated;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Loads the logo frames straight from this mod's jar (not through the resource manager, so
 * resource reloads and resource packs can't pull them out from under the loading screen).
 * PNGs are decoded and downscaled to on-screen size on a background thread, then uploaded a
 * few per frame on the render thread. Textures stay resident for the session.
 */
public final class FrameStore {
    public static final int FRAMES = 79;
    public static final int SOURCE_LOGO_WIDTH = 2400;
    public static final int SOURCE_LOGO_HEIGHT = 514;
    public static final int SOURCE_STUDIOS_WIDTH = 2240;
    public static final int SOURCE_STUDIOS_HEIGHT = 360;

    private static final int UPLOADS_PER_FRAME = 6;
    private static final ResourceLocation STUDIOS_ID = Mojanimated.id("dyn/studios");

    private record Decoded(ResourceLocation id, NativeImage image) {}

    private static final ConcurrentLinkedQueue<Decoded> pending = new ConcurrentLinkedQueue<>();
    private static volatile boolean started;
    private static volatile boolean decodeFinished;
    private static volatile boolean failed;
    private static int uploaded;

    private FrameStore() {}

    public static ResourceLocation frameId(int frame) {
        return Mojanimated.id("dyn/frame_" + frame);
    }

    public static ResourceLocation studiosId() {
        return STUDIOS_ID;
    }

    public static boolean isReady() {
        return !failed && decodeFinished && pending.isEmpty() && uploaded == FRAMES + 1;
    }

    public static boolean hasFailed() {
        return failed;
    }

    /** Starts the background decode once. Safe to call every frame. */
    public static void ensureLoading() {
        if (started) return;
        started = true;

        // Size the textures for the current window: the logo is drawn about
        // min(width * 0.75, height) * 0.24 pixels tall (see LogoRenderer).
        Window window = Minecraft.getInstance().getWindow();
        double logoPixelHeight = Math.min(window.getWidth() * 0.75, window.getHeight()) * 0.24;
        float scale = (float) Math.min(1.0, Math.max(0.35, logoPixelHeight * 1.1 / SOURCE_LOGO_HEIGHT));

        Thread thread = new Thread(() -> decodeAll(scale), "Mojanimated frame loader");
        thread.setDaemon(true);
        thread.start();
    }

    private static void decodeAll(float scale) {
        long start = System.currentTimeMillis();
        try {
            Path root = ModList.get().getModFileById(Mojanimated.MOD_ID).getFile().findResource("assets", Mojanimated.MOD_ID, "textures", "gui");
            for (int i = 0; i < FRAMES; i++) {
                Path file = root.resolve("frames").resolve(String.format("frame_%04d.png", i + 1));
                pending.add(new Decoded(frameId(i), decode(file, scale)));
            }
            pending.add(new Decoded(STUDIOS_ID, decode(root.resolve("studios.png"), scale)));
            Mojanimated.LOGGER.info("Decoded {} logo frames at {}% size in {} ms",
                    FRAMES, Math.round(scale * 100), System.currentTimeMillis() - start);
        } catch (Throwable t) {
            failed = true;
            Mojanimated.LOGGER.error("Could not load logo frames; falling back to the vanilla logo", t);
        } finally {
            decodeFinished = true;
        }
    }

    private static NativeImage decode(Path file, float scale) throws Exception {
        NativeImage source;
        try (InputStream in = Files.newInputStream(file)) {
            source = NativeImage.read(NativeImage.Format.RGBA, in);
        }
        if (scale >= 0.999f) return source;

        int w = Math.max(1, Math.round(source.getWidth() * scale));
        int h = Math.max(1, Math.round(source.getHeight() * scale));
        NativeImage scaled = new NativeImage(NativeImage.Format.RGBA, w, h, false);
        source.resizeSubRectTo(0, 0, source.getWidth(), source.getHeight(), scaled);
        source.close();
        return scaled;
    }

    /** Uploads a few decoded frames. Must be called on the render thread. */
    public static void tickUpload() {
        if (failed) return;
        RenderSystem.assertOnRenderThread();
        for (int i = 0; i < UPLOADS_PER_FRAME; i++) {
            Decoded next = pending.poll();
            if (next == null) return;
            try {
                Minecraft.getInstance().getTextureManager().register(next.id(), new FrameTexture(next.image()));
                uploaded++;
            } catch (Throwable t) {
                failed = true;
                next.image().close();
                Mojanimated.LOGGER.error("Could not upload logo frame {}", next.id(), t);
                return;
            }
        }
    }

    /** A GPU texture filled once from an image; the CPU copy is freed after upload. */
    private static final class FrameTexture extends AbstractTexture {
        FrameTexture(NativeImage image) {
            TextureUtil.prepareImage(this.getId(), image.getWidth(), image.getHeight());
            image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), true, true, false, true);
        }

        @Override
        public void load(ResourceManager resourceManager) {
            // Pixels were uploaded in the constructor; nothing to reload.
        }
    }
}
