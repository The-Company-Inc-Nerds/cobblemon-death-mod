package com.thecompanyinc.cobblemondeathmod.mixin;

import com.thecompanyinc.cobblemondeathmod.CobblemonDeathMod;
import com.thecompanyinc.cobblemondeathmod.config.DeathModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class MobSpawnMixin {

  @Inject(
    method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
    at = @At("HEAD"),
    cancellable = true
  )
  private static void onSpawnCategoryForPosition(
    MobCategory category,
    ServerLevel level,
    ChunkAccess chunk,
    BlockPos pos,
    NaturalSpawner.SpawnPredicate predicate,
    NaturalSpawner.AfterSpawnCallback callback,
    CallbackInfo ci
  ) {
    DeathModConfig config = CobblemonDeathMod.getConfig();
    if (!config.isEnableSafeZones()) return;

    String dimension = level.dimension().location().toString();
    DeathModConfig.SafeZone zone = config.getSafeZoneAt(
      dimension,
      pos.getX(),
      pos.getY(),
      pos.getZ()
    );

    if (zone != null) {
      if (zone.preventHostileOnly) {
        if (category == MobCategory.MONSTER) {
          ci.cancel();
        }
      } else {
        if (
          category == MobCategory.MONSTER || category == MobCategory.CREATURE
        ) {
          ci.cancel();
        }
      }
    }
  }
}
