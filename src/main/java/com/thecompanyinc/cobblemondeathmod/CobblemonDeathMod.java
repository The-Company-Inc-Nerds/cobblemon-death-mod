package com.thecompanyinc.cobblemondeathmod;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemondeathmod.config.DeathModConfig;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonDeathMod implements ModInitializer {

  public static final String MOD_ID = "cobblemon-death-mod";
  private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private static DeathModConfig config;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing Cobblemon Death Mod...");

    config = DeathModConfig.load();
    config.save();

    CobblemonEvents.BATTLE_FAINTED.subscribe(
      Priority.NORMAL,
      CobblemonDeathMod::handleBattleFainted
    );

    LOGGER.info("Cobblemon Death Mod initialized!");
  }

  private static Unit handleBattleFainted(BattleFaintedEvent event) {
    var battle = event.getBattle();
    var faintedPokemon = event.getKilled();

    BattleActor ownerActor = null;
    for (BattleActor actor : battle.getActors()) {
      for (var bp : actor.getPokemonList()) {
        if (bp.getUuid().equals(faintedPokemon.getUuid())) {
          ownerActor = actor;
          break;
        }
      }
      if (ownerActor != null) break;
    }

    if (ownerActor == null || ownerActor.getType() != ActorType.PLAYER) {
      return Unit.INSTANCE;
    }

    boolean hasWildOpponent = false;
    boolean hasNpcOpponent = false;
    for (BattleActor actor : battle.getActors()) {
      if (actor.getType() == ActorType.WILD) hasWildOpponent = true;
      if (actor.getType() == ActorType.NPC) hasNpcOpponent = true;
    }

    if (hasWildOpponent && !config.isApplyInWildBattles()) return Unit.INSTANCE;
    if (
      hasNpcOpponent && !config.isApplyInTrainerBattles()
    ) return Unit.INSTANCE;

    if (!(ownerActor instanceof PlayerBattleActor playerActor)) {
      return Unit.INSTANCE;
    }

    ServerPlayer player = playerActor.getEntity();
    if (player == null) {
      return Unit.INSTANCE;
    }

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

    int totalPartySize = countPartySize(party);

    int remainingAfterThis = countRemainingPokemon(
      party,
      faintedPokemon.getEffectedPokemon()
    );

    float damageAmount = calculateDamage(
      player,
      totalPartySize,
      remainingAfterThis
    );

    String pokemonName = faintedPokemon
      .getEffectedPokemon()
      .getSpecies()
      .getName();

    Pokemon faintedPokemonObj = faintedPokemon.getEffectedPokemon();
    boolean removed = party.remove(faintedPokemonObj);

    if (removed) {
      LOGGER.info(
        "Removed {} from {}'s party (Nuzlocke rule)",
        pokemonName,
        player.getName().getString()
      );
    }

    applyDamageToPlayer(
      player,
      damageAmount,
      pokemonName,
      remainingAfterThis == 0
    );

    return Unit.INSTANCE;
  }

  /**
   * Count total Pokemon in party (including fainted ones)
   */
  private static int countPartySize(PlayerPartyStore party) {
    int count = 0;
    for (Pokemon pokemon : party) {
      if (pokemon != null) {
        count++;
      }
    }
    return Math.max(count, 1); // At least 1 to avoid division by zero
  }

  /**
   * Count remaining non-fainted Pokemon, excluding the one that just fainted
   */
  private static int countRemainingPokemon(
    PlayerPartyStore party,
    Pokemon justFainted
  ) {
    int remaining = 0;
    for (Pokemon pokemon : party) {
      if (pokemon != null && pokemon != justFainted && !pokemon.isFainted()) {
        remaining++;
      }
    }
    return remaining;
  }

  private static float calculateDamage(
    ServerPlayer player,
    int totalPartySize,
    int remainingPokemon
  ) {
    if (remainingPokemon == 0) {
      return 20.0f;
    }

    float healthBase = config.isUseMaxHealth()
      ? player.getMaxHealth()
      : player.getHealth();

    float damage;
    if (config.isScaleDamageByPartySize() && totalPartySize > 0) {
      damage = healthBase / totalPartySize;
    } else {
      damage = player.getMaxHealth();
    }

    float minimumDamage =
      player.getMaxHealth() * config.getMinimumDamagePercent();
    return Math.max(damage, minimumDamage);
  }

  private static void applyDamageToPlayer(
    ServerPlayer player,
    float damage,
    String pokemonName,
    boolean isWhiteOut
  ) {
    String message;
    if (isWhiteOut) {
      message =
        "§4" +
        pokemonName +
        " fainted and was released! You have no Pokémon left!";
      CobblemonDeathModClient.triggerWhiteoutDeath();
    } else {
      message =
        "§c" + pokemonName + " fainted and was released! You take damage!";
    }
    player.sendSystemMessage(Component.literal(message));

    player.hurt(player.damageSources().generic(), damage);

    LOGGER.debug(
      "Applied {} damage to {} due to {} fainting (white out: {})",
      damage,
      player.getName().getString(),
      pokemonName,
      isWhiteOut
    );
  }

  public static void reloadConfig() {
    config = DeathModConfig.load();
    LOGGER.info("Configuration reloaded");
  }

  public static DeathModConfig getConfig() {
    return config;
  }
}
