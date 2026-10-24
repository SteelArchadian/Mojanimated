package dev.chill.mojanimated.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chill.mojanimated.OverlayAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Hooks the vanilla loading screen without cancelling its render method. Vanilla's own code
 * still runs onFinish and screen.init() at the end, which is where FancyMenu (and other mods)
 * apply their screen setup, so they keep working.
 */
@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
    @Unique
    private OverlayAnimation mojanimated$animation;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mojanimated$create(Minecraft minecraft, ReloadInstance reload, Consumer<Optional<Throwable>> onFinish, boolean fadeIn, CallbackInfo ci) {
        this.mojanimated$animation = OverlayAnimation.create(fadeIn);
    }

    /** First half of the vanilla logo: draw the animation instead. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"))
    private void mojanimated$drawAnimation(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                    float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight,
                                    Operation<Void> original) {
        if (this.mojanimated$animation != null && this.mojanimated$animation.replacesLogo()) {
            this.mojanimated$animation.renderLogo(graphics);
        } else {
            original.call(graphics, texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
        }
    }

    /** Second half of the vanilla logo: skip it while the animation is shown. */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"))
    private void mojanimated$skipSecondHalf(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                     float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight,
                                     Operation<Void> original) {
        if (this.mojanimated$animation == null || !this.mojanimated$animation.replacesLogo()) {
            original.call(graphics, texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;drawProgressBar(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"))
    private void mojanimated$progressBar(LoadingOverlay overlay, GuiGraphics graphics, int x1, int y1, int x2, int y2, float alpha,
                                  Operation<Void> original) {
        if (this.mojanimated$animation == null || !this.mojanimated$animation.hidesProgressBar()) {
            original.call(overlay, graphics, x1, y1, x2, y2, alpha);
        }
    }

    /** Delay vanilla's normal finish until the animation has landed. */
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ReloadInstance;isDone()Z"))
    private boolean mojanimated$holdUntilAnimationDone(boolean done) {
        return this.mojanimated$animation == null ? done : this.mojanimated$animation.gate(done);
    }
}
