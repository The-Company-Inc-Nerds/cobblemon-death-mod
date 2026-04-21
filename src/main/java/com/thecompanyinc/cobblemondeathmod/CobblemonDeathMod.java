package com.thecompanyinc.cobblemondeathmod;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemondeathmod.config.DeathModConfig;
import java.util.UUID;
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
  private static boolean pendingWhiteoutDeath = false;

  @Override
  public void onInitialize() {
    LOGGER.info("Initializing Cobblemon Death Mod...");

    config = DeathModConfig.load();
    config.save();

    CobblemonEvents.BATTLE_FAINTED.subscribe(
      Priority.NORMAL,
      CobblemonDeathMod::handleBattleFainted
    );
    CobblemonEvents.BATTLE_FLED.subscribe(
      Priority.NORMAL,
      CobblemonDeathMod::handleBattleFled
    );
    CobblemonEvents.POKEMON_CAPTURED.subscribe(
      Priority.NORMAL,
      CobblemonDeathMod::handlePokemonCaptured
    );

    LOGGER.info("Cobblemon Death Mod initialized!");
  }

  private static Unit handlePokemonCaptured(PokemonCapturedEvent event) {
    Pokemon pokemon = event.getPokemon();
    ServerPlayer player = event.getPlayer();
    String speciesName = pokemon.getSpecies().getName();

    DeathModConfig.DuplicateHandling handling = config.getDuplicateHandling();

    if (handling != DeathModConfig.DuplicateHandling.OFF) {
      boolean shouldRelease = false;

      if (handling == DeathModConfig.DuplicateHandling.RELEASE_IF_OWNED) {
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(
          player
        );
        for (Pokemon partyPokemon : party) {
          if (
            partyPokemon != null &&
            partyPokemon.getSpecies().getName().equalsIgnoreCase(speciesName) &&
            partyPokemon != pokemon
          ) {
            shouldRelease = true;
            break;
          }
        }

        if (!shouldRelease) {
          PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
          for (Pokemon pcPokemon : pc) {
            if (
              pcPokemon != null &&
              pcPokemon.getSpecies().getName().equalsIgnoreCase(speciesName)
            ) {
              shouldRelease = true;
              break;
            }
          }
        }
      } else if (
        handling == DeathModConfig.DuplicateHandling.RELEASE_IF_EVER_CAUGHT
      ) {
        if (config.hasEverCaught(speciesName)) {
          shouldRelease = true;
        }
      }

      if (shouldRelease) {
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(
          player
        );
        party.remove(pokemon);

        player.sendSystemMessage(
          Component.literal(
            "§e" +
              speciesName +
              " was automatically released (duplicate species)."
          )
        );

        LOGGER.info(
          "Auto-released duplicate {} for player {}",
          speciesName,
          player.getName().getString()
        );
        return Unit.INSTANCE;
      }
    }

    config.addCaughtSpecies(speciesName);

    if (config.isSetCaughtToZeroHP()) {
      pokemon.setCurrentHealth(0);
      player.sendSystemMessage(
        Component.literal("§7" + speciesName + " arrived fainted...")
      );
    }

    if (config.isSendCaughtToPC()) {
      PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
      PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);

      party.remove(pokemon);

      var pcPosition = pc.getFirstAvailablePosition();
      if (pcPosition != null) {
        pc.set(pcPosition, pokemon);
        player.sendSystemMessage(
          Component.literal("§a" + speciesName + " was sent to your PC.")
        );
      } else {
        party.add(pokemon);
        player.sendSystemMessage(
          Component.literal(
            "§cPC is full! " + speciesName + " was added to your party."
          )
        );
      }
    }

    LOGGER.info(
      "Player {} captured {}",
      player.getName().getString(),
      speciesName
    );
    return Unit.INSTANCE;
  }

  private static Unit handleBattleFled(BattleFledEvent event) {
    if (!config.isSacrificeOnFlee()) {
      return Unit.INSTANCE;
    }

    PlayerBattleActor playerActor = event.getPlayer();
    ServerPlayer player = playerActor.getEntity();

    if (player == null) {
      return Unit.INSTANCE;
    }

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    int partyCount = countPartySize(party);

    if (partyCount <= 1) {
      player.sendSystemMessage(
        Component.literal(
          "§4You fled with only one Pokémon! There is no escape..."
        )
      );

      pendingWhiteoutDeath = true;
      player.hurt(player.damageSources().generic(), 20.0f);

      LOGGER.info(
        "Player {} fled with only one Pokemon - killed",
        player.getName().getString()
      );
      return Unit.INSTANCE;
    }

    player.sendSystemMessage(
      Component.literal(
        "§cYou fled from battle! You must sacrifice a Pokémon..."
      )
    );

    CobblemonDeathModClient.triggerSacrificeSelection();

    LOGGER.info(
      "Player {} fled from battle, sacrifice required",
      player.getName().getString()
    );

    return Unit.INSTANCE;
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

    if (config.isRemoveFaintedPokemon()) {
      Pokemon faintedPokemonObj = faintedPokemon.getEffectedPokemon();
      party.remove(faintedPokemonObj);
      LOGGER.info(
        "Removed {} from {}'s party",
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

  public static void sacrificePokemon(UUID playerUuid, UUID pokemonUuid) {
    net.minecraft.client.Minecraft mc =
      net.minecraft.client.Minecraft.getInstance();
    if (mc.getSingleplayerServer() == null) return;

    ServerPlayer player = mc
      .getSingleplayerServer()
      .getPlayerList()
      .getPlayer(playerUuid);
    if (player == null) return;

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);

    for (Pokemon pokemon : party) {
      if (pokemon != null && pokemon.getUuid().equals(pokemonUuid)) {
        String name = pokemon.getSpecies().getName();
        party.remove(pokemon);
        player.sendSystemMessage(
          Component.literal("§c" + name + " was sacrificed for your escape!")
        );
        LOGGER.info(
          "Sacrificed {} for player {}",
          name,
          player.getName().getString()
        );
        break;
      }
    }
  }

  private static int countPartySize(PlayerPartyStore party) {
    int count = 0;
    for (Pokemon pokemon : party) {
      if (pokemon != null) {
        count++;
      }
    }
    return Math.max(count, 1);
  }

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
      String releaseText = config.isRemoveFaintedPokemon()
        ? " and was released"
        : "";
      message =
        "§4" +
        pokemonName +
        " fainted" +
        releaseText +
        "! You have no Pokémon left!";
      pendingWhiteoutDeath = true;
    } else {
      if (config.isRemoveFaintedPokemon()) {
        message =
          "§c" + pokemonName + " fainted and was released! You take damage!";
      } else {
        message = config.getDamageMessage().replace("%pokemon%", pokemonName);
      }
    }
    player.sendSystemMessage(Component.literal(message));

    player.hurt(player.damageSources().generic(), damage);
  }

  public static boolean consumePendingWhiteoutDeath() {
    if (pendingWhiteoutDeath) {
      pendingWhiteoutDeath = false;
      return true;
    }
    return false;
  }

  public static void reloadConfig() {
    config = DeathModConfig.load();
    LOGGER.info("Configuration reloaded");
  }

  public static DeathModConfig getConfig() {
    return config;
  }
}
