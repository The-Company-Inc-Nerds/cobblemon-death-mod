package com.thecompanyinc.cobblemondeathmod;

import com.thecompanyinc.cobblemondeathmod.screen.SacrificeSelectionScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonDeathModClient implements ClientModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    CobblemonDeathMod.MOD_ID
  );

  public static boolean isPokemonWhiteout = false;

  public static boolean needsSacrifice = false;

  @Override
  public void onInitializeClient() {
    LOGGER.info("Cobblemon Death Mod client initialized!");
  }

  public static void triggerWhiteoutDeath() {
    isPokemonWhiteout = true;
  }

  public static void resetWhiteoutFlag() {
    isPokemonWhiteout = false;
  }

  public static void triggerSacrificeSelection() {
    needsSacrifice = true;
    Minecraft.getInstance().execute(() -> {
      Minecraft.getInstance().setScreen(new SacrificeSelectionScreen());
    });
  }
}
