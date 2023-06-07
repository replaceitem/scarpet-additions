package net.replaceitem.scarpet.additions.mixins;

import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.replaceitem.scarpet.additions.ScarpetAdditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerMetadata.class)
public class ServerMetadataMixin {
    @Inject(method = "description", at = @At("HEAD"), cancellable = true)
    private void getScarpetMotd(CallbackInfoReturnable<Text> cir) {
        if(ScarpetAdditions.MOTD != null) cir.setReturnValue(ScarpetAdditions.MOTD);
    }
}
