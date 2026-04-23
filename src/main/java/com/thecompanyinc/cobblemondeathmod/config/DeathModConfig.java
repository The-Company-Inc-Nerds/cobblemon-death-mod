package com.thecompanyinc.cobblemondeathmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
  private boolean mysterySacrifice = false;
  private boolean sendCaughtToPC = true;
  private boolean setCaughtToZeroHP = true;
  private DuplicateHandling duplicateHandling = DuplicateHandling.OFF;
  private Set<String> caughtSpecies = new HashSet<>();
  private boolean enableSafeZones = true;
  private List<SafeZone> safeZones = new ArrayList<>();

  public enum DuplicateHandling {
    OFF,
    RELEASE_IF_OWNED,
    RELEASE_IF_EVER_CAUGHT,
  }

  public static class SafeZone {

    public String name;
    public String dimension;
    public int centerX;
    public int centerY;
    public int centerZ;
    public int radius;
    public boolean preventHostileOnly;
    public boolean cylindrical;

    public SafeZone() {}

    public SafeZone(
      String name,
      String dimension,
      int x,
      int y,
      int z,
      int radius,
      boolean hostileOnly,
      boolean cylindrical
    ) {
      this.name = name;
      this.dimension = dimension;
      this.centerX = x;
      this.centerY = y;
      this.centerZ = z;
      this.radius = radius;
      this.preventHostileOnly = hostileOnly;
      this.cylindrical = cylindrical;
    }

    public boolean contains(String dim, int x, int y, int z) {
      if (!dimension.equals(dim)) return false;

      int dx = x - centerX;
      int dz = z - centerZ;
      int distSq = dx * dx + dz * dz;

      if (!cylindrical) {
        int dy = y - centerY;
        distSq += dy * dy;
      }

      return distSq <= (radius * radius);
    }
  }

  public DeathModConfig() {}

  public static DeathModConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          DeathModConfig config = GSON.fromJson(reader, DeathModConfig.class);
          if (config != null) {
            if (config.caughtSpecies == null) config.caughtSpecies =
              new HashSet<>();
            if (config.safeZones == null) config.safeZones = new ArrayList<>();
            return config;
          }
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

  public boolean isMysterySacrifice() {
    return mysterySacrifice;
  }

  public boolean isSendCaughtToPC() {
    return sendCaughtToPC;
  }

  public boolean isSetCaughtToZeroHP() {
    return setCaughtToZeroHP;
  }

  public DuplicateHandling getDuplicateHandling() {
    return duplicateHandling;
  }

  public Set<String> getCaughtSpecies() {
    return caughtSpecies;
  }

  public boolean isEnableSafeZones() {
    return enableSafeZones;
  }

  public List<SafeZone> getSafeZones() {
    return safeZones;
  }

  public void setScaleDamageByPartySize(boolean v) {
    this.scaleDamageByPartySize = v;
  }

  public void setUseMaxHealth(boolean v) {
    this.useMaxHealth = v;
  }

  public void setMinimumDamagePercent(float v) {
    this.minimumDamagePercent = v;
  }

  public void setApplyInWildBattles(boolean v) {
    this.applyInWildBattles = v;
  }

  public void setApplyInTrainerBattles(boolean v) {
    this.applyInTrainerBattles = v;
  }

  public void setDamageMessage(String v) {
    this.damageMessage = v;
  }

  public void setRemoveFaintedPokemon(boolean v) {
    this.removeFaintedPokemon = v;
  }

  public void setSacrificeOnFlee(boolean v) {
    this.sacrificeOnFlee = v;
  }

  public void setMysterySacrifice(boolean v) {
    this.mysterySacrifice = v;
  }

  public void setSendCaughtToPC(boolean v) {
    this.sendCaughtToPC = v;
  }

  public void setSetCaughtToZeroHP(boolean v) {
    this.setCaughtToZeroHP = v;
  }

  public void setDuplicateHandling(DuplicateHandling v) {
    this.duplicateHandling = v;
  }

  public void setEnableSafeZones(boolean v) {
    this.enableSafeZones = v;
  }

  public void setSafeZones(List<SafeZone> v) {
    this.safeZones = v;
  }

  public void addCaughtSpecies(String species) {
    caughtSpecies.add(species.toLowerCase());
    save();
  }

  public boolean hasEverCaught(String species) {
    return caughtSpecies.contains(species.toLowerCase());
  }

  public void addSafeZone(SafeZone zone) {
    safeZones.add(zone);
    save();
  }

  public boolean removeSafeZone(String name) {
    boolean removed = safeZones.removeIf(z -> z.name.equalsIgnoreCase(name));
    if (removed) save();
    return removed;
  }

  public boolean isInSafeZone(String dimension, int x, int y, int z) {
    if (!enableSafeZones) return false;
    for (SafeZone zone : safeZones) {
      if (zone.contains(dimension, x, y, z)) {
        return true;
      }
    }
    return false;
  }

  public SafeZone getSafeZoneAt(String dimension, int x, int y, int z) {
    for (SafeZone zone : safeZones) {
      if (zone.contains(dimension, x, y, z)) {
        return zone;
      }
    }
    return null;
  }
}
