package com.thecompanyinc.cobblemondeathmod.screen;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemondeathmod.CobblemonDeathMod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SacrificeSelectionScreen extends Screen {

  private final List<PokemonSlot> pokemonSlots = new ArrayList<>();
  private int selectedSlot = -1;
  private final boolean mysteryMode;

  public SacrificeSelectionScreen() {
    super(Component.literal("Choose a Pokémon to Sacrifice"));
    this.mysteryMode = CobblemonDeathMod.getConfig().isMysterySacrifice();
  }

  @Override
  protected void init() {
    pokemonSlots.clear();

    if (this.minecraft == null || this.minecraft.player == null) return;

    ClientParty party = CobblemonClient.INSTANCE.getStorage().getParty();

    List<Pokemon> partyPokemon = new ArrayList<>();
    for (Pokemon pokemon : party) {
      if (pokemon != null) {
        partyPokemon.add(pokemon);
      }
    }

    if (mysteryMode) {
      Collections.shuffle(partyPokemon);
    }

    int slotSize = 80;
    int spacing = 10;
    int totalWidth = 3 * slotSize + 2 * spacing;
    int startX = (this.width - totalWidth) / 2;
    int startY = this.height / 2 - slotSize - spacing / 2;

    for (int index = 0; index < partyPokemon.size(); index++) {
      Pokemon pokemon = partyPokemon.get(index);
      int row = index / 3;
      int col = index % 3;
      int x = startX + col * (slotSize + spacing);
      int y = startY + row * (slotSize + spacing);

      pokemonSlots.add(
        new PokemonSlot(index, x, y, slotSize, pokemon, mysteryMode)
      );
    }

    this.addRenderableWidget(
      Button.builder(Component.literal("Sacrifice Selected"), button ->
        confirmSacrifice()
      )
        .bounds(this.width / 2 - 75, this.height - 50, 150, 20)
        .build()
    );
  }

  @Override
  public void render(
    GuiGraphics graphics,
    int mouseX,
    int mouseY,
    float delta
  ) {
    String title = "You fled from battle!";
    int titleWidth = this.font.width(title);
    graphics.drawString(
      this.font,
      title,
      (this.width - titleWidth) / 2,
      30,
      0xFF5555,
      true
    );

    String subtitle = mysteryMode
      ? "Choose wisely... their identities are hidden:"
      : "Choose a Pokémon to sacrifice:";
    int subtitleWidth = this.font.width(subtitle);
    graphics.drawString(
      this.font,
      subtitle,
      (this.width - subtitleWidth) / 2,
      50,
      0xFFFFFF,
      true
    );

    for (PokemonSlot slot : pokemonSlots) {
      slot.render(
        graphics,
        this.font,
        mouseX,
        mouseY,
        selectedSlot == slot.index
      );
    }

    super.render(graphics, mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0) {
      for (PokemonSlot slot : pokemonSlots) {
        if (slot.isMouseOver((int) mouseX, (int) mouseY)) {
          selectedSlot = slot.index;
          return true;
        }
      }
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  private void confirmSacrifice() {
    if (
      selectedSlot < 0 ||
      this.minecraft == null ||
      this.minecraft.player == null
    ) {
      return;
    }

    PokemonSlot selectedPokemonSlot = pokemonSlots.get(selectedSlot);
    Pokemon pokemon = selectedPokemonSlot.pokemon;

    CobblemonDeathMod.setSacrificePending(
      this.minecraft.player.getUUID(),
      pokemon.getUuid()
    );

    ClientParty party = CobblemonClient.INSTANCE.getStorage().getParty();
    party.remove(pokemon.getUuid());

    this.minecraft.setScreen(null);
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  private static class PokemonSlot {

    final int index;
    final int x, y, size;
    final Pokemon pokemon;
    final String name;
    final String displayName;
    final int level;
    final boolean mystery;

    PokemonSlot(
      int index,
      int x,
      int y,
      int size,
      Pokemon pokemon,
      boolean mysteryMode
    ) {
      this.index = index;
      this.x = x;
      this.y = y;
      this.size = size;
      this.pokemon = pokemon;
      this.name = pokemon.getSpecies().getName();
      this.level = pokemon.getLevel();
      this.mystery = mysteryMode;

      if (mysteryMode) {
        this.displayName = "§k" + name + "§r";
      } else {
        this.displayName =
          name.length() > 10 ? name.substring(0, 9) + "…" : name;
      }
    }

    void render(
      GuiGraphics graphics,
      net.minecraft.client.gui.Font font,
      int mouseX,
      int mouseY,
      boolean selected
    ) {
      int bgColor = selected
        ? 0xFF4444AA
        : (isMouseOver(mouseX, mouseY) ? 0xFF666666 : 0xFF333333);
      graphics.fill(x, y, x + size, y + size, bgColor);

      int borderColor = selected ? 0xFFFFFF00 : 0xFFAAAAAA;
      graphics.renderOutline(x, y, size, size, borderColor);

      int nameWidth = font.width(displayName);
      graphics.drawString(
        font,
        displayName,
        x + (size - nameWidth) / 2,
        y + size / 2 - 10,
        0xFFFFFF,
        false
      );

      String levelStr = mystery ? "§kLv. ??§r" : "Lv. " + level;
      int levelWidth = font.width(levelStr);
      graphics.drawString(
        font,
        levelStr,
        x + (size - levelWidth) / 2,
        y + size / 2 + 5,
        0xAAAAAA,
        false
      );

      if (mystery) {
        String hpStr = "§k??/??§r";
        int hpWidth = font.width(hpStr);
        graphics.drawString(
          font,
          hpStr,
          x + (size - hpWidth) / 2,
          y + size / 2 + 20,
          0x888888,
          false
        );
      } else {
        int currentHp = pokemon.getCurrentHealth();
        int maxHp = pokemon.getHp();
        String hpStr = currentHp + "/" + maxHp;
        int hpWidth = font.width(hpStr);
        int hpColor = pokemon.isFainted() ? 0xFF5555 : 0x55FF55;
        graphics.drawString(
          font,
          hpStr,
          x + (size - hpWidth) / 2,
          y + size / 2 + 20,
          hpColor,
          false
        );
      }
    }

    boolean isMouseOver(int mouseX, int mouseY) {
      return (
        mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size
      );
    }
  }
}
