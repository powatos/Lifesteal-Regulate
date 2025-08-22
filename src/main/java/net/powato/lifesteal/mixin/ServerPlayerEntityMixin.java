package net.powato.lifesteal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.powato.lifesteal.LifestealRegulate;
import net.powato.lifesteal.SafeChunks.ChunkCountInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ChunkCountInterface {

    private int chunkCount = 0;

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    private void writeCustomData(WriteView nbt, CallbackInfo ci){
        nbt.putInt("chunkCount", chunkCount);
    }

    @Inject(method = "readCustomData", at = @At("HEAD"))
    private void readCustomData(ReadView nbt, CallbackInfo ci){
        if (nbt.contains("chunkCount")){
            chunkCount = nbt.getInt("chunkCount", 0);
        }
    }

    @Override
    public int getChunkCount() {
        return chunkCount;
    }

    @Override
    public void incChunkCount() {
        chunkCount++;
    }

    @Override
    public void decChunkCount() {
        chunkCount--;
    }

    @Override
    public void zeroChunkCount() {
        chunkCount = 0;
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(DamageSource source, CallbackInfo ci){
        ServerPlayerEntity deadPlayer = (ServerPlayerEntity) (Object) this;
        Entity killer = source.getAttacker();

        MinecraftServer server = deadPlayer.getServer();

        if (killer == null) {return;}

        if (killer.getUuid().equals(deadPlayer.getUuid())){return;} // suicide

        if (killer instanceof ServerPlayerEntity killerPlayer){
            float deadPlayerMaxHealth = deadPlayer.getMaxHealth();

            if (deadPlayerMaxHealth <= 2.f){ return; } // if dead player was already at 1 heart

            float newDeadMax = deadPlayerMaxHealth - 2.f;
            float newKillerMax = killerPlayer.getMaxHealth() + 2.f;
            deadPlayer.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(newDeadMax);
            killerPlayer.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(newKillerMax);

            for (ServerPlayerEntity Player : server.getPlayerManager().getPlayerList()) {
                LifestealRegulate.updatePlayerName(server, Player);
            }
        }


    }
}
