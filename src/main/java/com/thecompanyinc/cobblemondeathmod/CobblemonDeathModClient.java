package com.thecompanyinc.cobblemondeathmod;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonDeathModClient implements ClientModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    CobblemonDeathMod.MOD_ID
  );

  public static boolean isPokemonWhiteout = false;

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
}
