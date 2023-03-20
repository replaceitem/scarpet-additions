package net.replaceitem.scarpet.additions.mixins;

import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerMetadata.class)
public interface ServerMetadataAccessor {
    @Mutable
    @Accessor
    void setDescription(Text description);
}
