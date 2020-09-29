package discarpet.mixins;

import discarpet.ScarpetAdditions;
import net.minecraft.server.Main;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class LoadScarpetAdditions_noopMixin {
    @Inject(method = "main", at = @At("HEAD"))
    private static void loadScarpetAdditions(String[] args,CallbackInfo ci) {
        ScarpetAdditions.LOGGER.info("scarpet-additions loaded in main!");
        ScarpetAdditions.noop();
    }
}
