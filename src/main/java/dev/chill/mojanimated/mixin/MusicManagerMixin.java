package dev.chill.mojanimated.mixin;

import dev.chill.mojanimated.OverlayAnimation;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps menu music from starting on top of the startup jingle. */
@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void mojanimated$holdDuringIntro(CallbackInfo ci) {
        if (OverlayAnimation.isStartupPlaying()) ci.cancel();
    }
}
