package com.thecompanyinc.cobblemondeathmod.config;

import com.thecompanyinc.cobblemondeathmod.CobblemonDeathMod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DeathModConfigScreen {

  public static Screen create(Screen parent) {
    DeathModConfig config = DeathModConfig.load();
    DeathModConfig defaults = new DeathModConfig();

    ConfigBuilder builder = ConfigBuilder.create()
      .setParentScreen(parent)
      .setTitle(Component.literal("Cobblemon Death Mod Config"));

    ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    ConfigCategory general = builder.getOrCreateCategory(
      Component.literal("General Settings")
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Scale Damage by Party Size"),
          config.isScaleDamageByPartySize()
        )
        .setDefaultValue(defaults.isScaleDamageByPartySize())
        .setTooltip(
          Component.literal("If enabled, damage = maxHealth / partySize")
        )
        .setSaveConsumer(config::setScaleDamageByPartySize)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Use Max Health"),
          config.isUseMaxHealth()
        )
        .setDefaultValue(defaults.isUseMaxHealth())
        .setTooltip(
          Component.literal(
            "Use max health instead of current health for damage calculation"
          )
        )
        .setSaveConsumer(config::setUseMaxHealth)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startFloatField(
          Component.literal("Minimum Damage Percent"),
          config.getMinimumDamagePercent()
        )
        .setDefaultValue(defaults.getMinimumDamagePercent())
        .setMin(0.0f)
        .setMax(1.0f)
        .setTooltip(
          Component.literal(
            "Minimum damage as a percentage of max health (0.0 - 1.0)"
          )
        )
        .setSaveConsumer(config::setMinimumDamagePercent)
        .build()
    );

    ConfigCategory nuzlocke = builder.getOrCreateCategory(
      Component.literal("Nuzlocke Rules")
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Remove Fainted Pokémon"),
          config.isRemoveFaintedPokemon()
        )
        .setDefaultValue(defaults.isRemoveFaintedPokemon())
        .setTooltip(
          Component.literal(
            "Permanently remove Pokémon from party when they faint"
          )
        )
        .setSaveConsumer(config::setRemoveFaintedPokemon)
        .build()
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Sacrifice on Flee"),
          config.isSacrificeOnFlee()
        )
        .setDefaultValue(defaults.isSacrificeOnFlee())
        .setTooltip(
          Component.literal("Must sacrifice a Pokémon when fleeing from battle")
        )
        .setSaveConsumer(config::setSacrificeOnFlee)
        .build()
    );

    ConfigCategory battleTypes = builder.getOrCreateCategory(
      Component.literal("Battle Types")
    );

    battleTypes.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Apply in Wild Battles"),
          config.isApplyInWildBattles()
        )
        .setDefaultValue(defaults.isApplyInWildBattles())
        .setTooltip(
          Component.literal("Take damage when Pokémon faint in wild battles")
        )
        .setSaveConsumer(config::setApplyInWildBattles)
        .build()
    );

    battleTypes.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Apply in Trainer Battles"),
          config.isApplyInTrainerBattles()
        )
        .setDefaultValue(defaults.isApplyInTrainerBattles())
        .setTooltip(
          Component.literal(
            "Take damage when Pokémon faint in NPC trainer battles"
          )
        )
        .setSaveConsumer(config::setApplyInTrainerBattles)
        .build()
    );

    ConfigCategory messages = builder.getOrCreateCategory(
      Component.literal("Messages")
    );

    messages.addEntry(
      entryBuilder
        .startStrField(
          Component.literal("Damage Message"),
          config.getDamageMessage()
        )
        .setDefaultValue(defaults.getDamageMessage())
        .setTooltip(
          Component.literal(
            "Message shown when taking damage. Use %pokemon% for the Pokémon name."
          )
        )
        .setSaveConsumer(config::setDamageMessage)
        .build()
    );

    builder.setSavingRunnable(() -> {
      config.save();
      CobblemonDeathMod.reloadConfig();
    });

    return builder.build();
  }
}
