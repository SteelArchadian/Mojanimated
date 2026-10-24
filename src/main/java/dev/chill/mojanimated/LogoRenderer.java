package dev.chill.mojanimated;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/** Draws a logo frame and the "Studios" text, laid out like the original mod. */
final class LogoRenderer {
    private LogoRenderer() {}

    static void render(GuiGraphics graphics, int frame, float logoAlpha, float studiosAlpha) {
        if (logoAlpha <= 0f && studiosAlpha <= 0f) return;

        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();

        // Vanilla left additive blending on for its logo; our frames need normal alpha blending.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (logoAlpha > 0f) {
            double logoScale = Math.min(screenW * 0.75, screenH) * 0.24;
            int h = (int) logoScale;
            int w = (int) (h * ((float) FrameStore.SOURCE_LOGO_WIDTH / FrameStore.SOURCE_LOGO_HEIGHT));
            int x = screenW / 2 - w / 2;
            int y = (screenH - 28) / 2 - h / 2;
            graphics.setColor(1f, 1f, 1f, logoAlpha);
            graphics.blit(FrameStore.frameId(frame), x, y, 0, 0, w, h, w, h);
        }

        if (studiosAlpha > 0f) {
            int logoHeight = (int) (Math.min(screenW * 0.75, screenH) * 0.3);
            int logoY = screenH / 2 - logoHeight / 2;
            int h = (int) (logoHeight * 0.3);
            int w = (int) (h * ((float) FrameStore.SOURCE_STUDIOS_WIDTH / FrameStore.SOURCE_STUDIOS_HEIGHT));
            int x = screenW / 2 - w / 2;
            int y = logoY + logoHeight - 25;
            graphics.setColor(1f, 1f, 1f, studiosAlpha);
            graphics.blit(FrameStore.studiosId(), x, y, 0, 0, w, h, w, h);
        }

        // Restore what vanilla expects for the rest of its logo block.
        graphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.blendFunc(770, 1);
    }
}
