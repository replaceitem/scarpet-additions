package ScarpetAdditions.mixins;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ScarpetAdditions.ScarpetAdditions;

@Mixin(PlayerListHeaderS2CPacket.class)
public class PlayerList_Mixin {

	@Shadow
	private Text footer;

	@Shadow
	private Text header;

	@Inject(at= @At("RETURN"), method = "<init>")
	public void PlayerListHeaderS2CPacket(CallbackInfo ci) {
		if(ScarpetAdditions.customHeader.getString() != "") {
			this.header = ScarpetAdditions.customHeader;
		}
		if(ScarpetAdditions.customFooter.getString() != "") {
			this.footer = ScarpetAdditions.customFooter;
		}
	}
}
