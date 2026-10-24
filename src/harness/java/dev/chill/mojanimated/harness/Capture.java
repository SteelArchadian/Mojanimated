package dev.chill.mojanimated.harness;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test runs only. Drives the scenario: startup → title screen → resource reload → title
 * screen → quit, saving frames along the way to run/captures.
 */
@Mod(value = "mojanimated_harness", dist = Dist.CLIENT)
@EventBusSubscriber(modid = "mojanimated_harness", value = Dist.CLIENT)
public class Capture {
    private static final Logger LOGGER = LoggerFactory.getLogger("mojanimated_harness");
    private static final long INTERVAL_MS = 200L;
    private static final Path DIR = Path.of("captures");

    private static int phase; // 0 startup, 1 title#1, 2 reload, 3 title#2, 4 done
    private static long lastCaptureMs;
    private static long phaseStartMs = -1L;
    private static int count;
    private static final long STARTED_MS = Util.getMillis();
    private static final long FAILSAFE_MS = 12 * 60_000L;
    private static boolean openedConfig;

    public Capture() {
        try {
            Files.createDirectories(DIR);
        } catch (Exception e) {
            LOGGER.error("Could not create capture dir", e);
        }
    }

    public static void maybeCapture(String tag) {
        long now = Util.getMillis();
        if (now - lastCaptureMs < INTERVAL_MS) return;
        lastCaptureMs = now;
        save(tag);
    }

    private static void save(String tag) {
        Minecraft mc = Minecraft.getInstance();
        try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(DIR.resolve(String.format("p%d_%04d_%s.png", phase, count++, tag)));
        } catch (Exception e) {
            LOGGER.error("Capture failed", e);
        }
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        long now = Util.getMillis();
        if (phase < 4 && now - STARTED_MS > FAILSAFE_MS) {
            LOGGER.warn("[harness] failsafe: still in phase {} after {} min, quitting", phase, FAILSAFE_MS / 60000);
            phase = 4;
            mc.stop();
            return;
        }
        boolean loading = mc.getOverlay() instanceof LoadingOverlay;

        switch (phase) {
            case 0, 2 -> {
                if (!loading && mc.screen != null) {
                    LOGGER.info("[harness] phase {} finished, screen up: {}", phase, mc.screen.getClass().getName());
                    phase++;
                    phaseStartMs = now;
                }
            }
            case 1, 3 -> {
                long elapsed = now - phaseStartMs;
                if (phase == 3 && elapsed >= 1500 && !openedConfig) {
                    openedConfig = true;
                    ModList.get().getModContainerById("mojanimated").ifPresent(c -> c.getCustomExtension(IConfigScreenFactory.class)
                            .ifPresent(f -> mc.setScreen(f.createScreen(c, mc.screen))));
                    LOGGER.info("[harness] opened config screen: {}", mc.screen == null ? null : mc.screen.getClass().getName());
                }
                if (elapsed > 600 && elapsed < 4000) maybeCapture(openedConfig ? "config" : "title");
                if (elapsed >= 4000) {
                    if (phase == 1) {
                        LOGGER.info("[harness] triggering resource reload");
                        phase = 2;
                        mc.reloadResourcePacks();
                    } else {
                        LOGGER.info("[harness] done, quitting");
                        phase = 4;
                        mc.stop();
                    }
                }
            }
            default -> {}
        }
    }
}
