package ScarpetAdditions.mixins;

import ScarpetAdditions.ScarpetAdditions;

import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CrashReport.class)
public class LoadScarpetAdditions_noopMixin {
    @Inject(method = "initCrashReport", at = @At("HEAD"))
    private static void loadScarpetAdditions(CallbackInfo ci) {
        ScarpetAdditions.noop();
    }
}