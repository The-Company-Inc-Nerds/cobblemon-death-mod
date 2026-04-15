package com.thecompanyinc.cobblemondeathmod.mixin;

import com.thecompanyinc.cobblemondeathmod.CobblemonDeathModClient;
import com.thecompanyinc.cobblemondeathmod.screen.PokeballDeathScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class DeathScreenMixin {

  @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
  private void onSetScreen(Screen screen, CallbackInfo ci) {
    if (
      screen instanceof DeathScreen && CobblemonDeathModClient.isPokemonWhiteout
    ) {
      CobblemonDeathModClient.resetWhiteoutFlag();

      Minecraft client = (Minecraft) (Object) this;
      client.setScreen(new PokeballDeathScreen());
      ci.cancel();
    }
  }
}
