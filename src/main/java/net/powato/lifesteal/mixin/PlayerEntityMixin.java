package net.powato.lifesteal.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.powato.lifesteal.SafeChunks.SafeChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void CancelAttack(Entity target, CallbackInfo ci){

        if (!(target instanceof PlayerEntity)){ return; } // returns if 'target' isn't a Player Entity


        PlayerEntity Player = (PlayerEntity) (Object) this;

        if (Player.getWorld().isClient) return; // only run on server

        SafeChunks state = SafeChunks.getServerState(Player.getServer());

        ChunkPos chunkPos = new ChunkPos(target.getBlockPos());
        RegistryKey<World> dimension = target.getWorld().getRegistryKey();

        if (state.isSafeChunk(chunkPos, dimension)){
            ci.cancel();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(ServerWorld World, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        PlayerEntity target = (PlayerEntity) (Object) this;

        if (source.getSource() instanceof ProjectileEntity && source.getAttacker() instanceof ServerPlayerEntity player){
            if (player.getWorld().isClient) return; // only run on server

            SafeChunks state = SafeChunks.getServerState(player.getServer());

            ChunkPos chunkPos = new ChunkPos(target.getBlockPos());
            RegistryKey<World> dimension = target.getWorld().getRegistryKey();

            if (state.isSafeChunk(chunkPos, dimension)){
                cir.setReturnValue(false);
            }
        }

    }
}
