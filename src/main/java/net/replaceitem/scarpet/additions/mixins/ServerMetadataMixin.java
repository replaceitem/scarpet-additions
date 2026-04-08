package net.replaceitem.scarpet.additions.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.replaceitem.scarpet.additions.ScarpetAdditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerStatus.class)
public class ServerMetadataMixin {
    @Inject(method = "description", at = @At("HEAD"), cancellable = true)
    private void getScarpetMotd(CallbackInfoReturnable<Component> cir) {
        if(ScarpetAdditions.MOTD != null) cir.setReturnValue(ScarpetAdditions.MOTD);
    }
}
