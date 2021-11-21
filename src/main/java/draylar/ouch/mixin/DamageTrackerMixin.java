package draylar.ouch.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {

    @Shadow @Final private LivingEntity entity;

    @Inject(method = "onDamage", at = @At("HEAD"))
    private void sendOuchPacket(DamageSource damageSource, float originalHealth, float damage, CallbackInfo ci) {
        PacketByteBuf packet = PacketByteBufs.create();
        packet.writeInt(entity.getId());
        packet.writeDouble(damage);
        PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, new Identifier("popup_update"), packet));
    }
}
