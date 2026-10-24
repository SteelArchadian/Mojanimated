package dev.chill.mojanimated.harness.mixin;

import dev.chill.mojanimated.harness.Capture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Test runs only: save what the loading screen looked like on the previous frame. */
@Mixin(LoadingOverlay.class)
public class LoadingOverlayCaptureMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void harness$capture(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Capture.maybeCapture("overlay");
    }
}
