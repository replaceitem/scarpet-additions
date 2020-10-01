package ScarpetAdditions.mixins;

import ScarpetAdditions.ScarpetAdditions;

import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CrashReport.class)
public class LoadScarpetAdditions_noopMixin {
    @Inject(method = "fillSystemDetails", at = @At("HEAD"))
    private void loadScarpetAdditions(CallbackInfo ci) {
        System.out.println("SCARPET-ADDITIONS LOADED");
        ScarpetAdditions.LOGGER.info("scarpet-additions loaded in main!");
        ScarpetAdditions.noop();
    }
}