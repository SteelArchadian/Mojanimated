package dev.chill.mojanimated.harness.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.windows.User32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Test runs only: create the window hidden, then show it minimized without taking focus. */
@Mixin(Window.class)
public class WindowMixin {
    @Shadow @org.spongepowered.asm.mixin.Final private long window;

    @Inject(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/fml/loading/ImmediateWindowHandler;setupMinecraftWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J"))
    private void harness$hiddenHints(CallbackInfo ci) {
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void harness$showMinimized(CallbackInfo ci) {
        long hwnd = GLFWNativeWin32.glfwGetWin32Window(this.window);
        User32.ShowWindow(hwnd, User32.SW_SHOWMINNOACTIVE);
    }
}
