package com.thecompanyinc.cobblemondeathmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DeathModConfig {

  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-death-mod.json"
  );

  private boolean scaleDamageByPartySize = true;
  private boolean useMaxHealth = true;
  private float minimumDamagePercent = 0.0f;
  private boolean applyInWildBattles = true;
  private boolean applyInTrainerBattles = true;
  private String damageMessage = "§c%pokemon% fainted! You take damage!";

  private boolean removeFaintedPokemon = true;
  private boolean sacrificeOnFlee = true;

  public DeathModConfig() {}

  public static DeathModConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          DeathModConfig config = GSON.fromJson(reader, DeathModConfig.class);
          return config != null ? config : new DeathModConfig();
        }
      }
    } catch (IOException e) {
      System.out.println(
        "[Cobblemon Death Mod] Error loading config, using defaults: " +
          e.getMessage()
      );
    }

    DeathModConfig config = new DeathModConfig();
    config.save();
    return config;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      System.out.println(
        "[Cobblemon Death Mod] Error saving config: " + e.getMessage()
      );
    }
  }

  public boolean isScaleDamageByPartySize() {
    return scaleDamageByPartySize;
  }

  public boolean isUseMaxHealth() {
    return useMaxHealth;
  }

  public float getMinimumDamagePercent() {
    return minimumDamagePercent;
  }

  public boolean isApplyInWildBattles() {
    return applyInWildBattles;
  }

  public boolean isApplyInTrainerBattles() {
    return applyInTrainerBattles;
  }

  public String getDamageMessage() {
    return damageMessage;
  }

  public boolean isRemoveFaintedPokemon() {
    return removeFaintedPokemon;
  }

  public boolean isSacrificeOnFlee() {
    return sacrificeOnFlee;
  }

  public void setScaleDamageByPartySize(boolean scaleDamageByPartySize) {
    this.scaleDamageByPartySize = scaleDamageByPartySize;
  }

  public void setUseMaxHealth(boolean useMaxHealth) {
    this.useMaxHealth = useMaxHealth;
  }

  public void setMinimumDamagePercent(float minimumDamagePercent) {
    this.minimumDamagePercent = minimumDamagePercent;
  }

  public void setApplyInWildBattles(boolean applyInWildBattles) {
    this.applyInWildBattles = applyInWildBattles;
  }

  public void setApplyInTrainerBattles(boolean applyInTrainerBattles) {
    this.applyInTrainerBattles = applyInTrainerBattles;
  }

  public void setDamageMessage(String damageMessage) {
    this.damageMessage = damageMessage;
  }

  public void setRemoveFaintedPokemon(boolean removeFaintedPokemon) {
    this.removeFaintedPokemon = removeFaintedPokemon;
  }

  public void setSacrificeOnFlee(boolean sacrificeOnFlee) {
    this.sacrificeOnFlee = sacrificeOnFlee;
  }
}
