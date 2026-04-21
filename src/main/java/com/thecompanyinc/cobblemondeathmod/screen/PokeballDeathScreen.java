package com.thecompanyinc.cobblemondeathmod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PokeballDeathScreen extends Screen {

  private int ticksOnScreen = 0;
  private final List<Shard> shards = new ArrayList<>();
  private final Random random = new Random();

  // Animation timing
  private static final int FADE_IN_TICKS = 20;
  private static final int POKEBALL_DISPLAY_TICKS = 40;
  private static final int SHATTER_TICKS = 60;
  private static final int SHOW_DEATH_SCREEN_TICKS = 100;
  private boolean shattered = false;
  private float pokeballScale = 0.0f;

  public PokeballDeathScreen() {
    super(Component.literal("You Blacked Out!"));
  }

  @Override
  public void tick() {
    ticksOnScreen++;

    if (ticksOnScreen < POKEBALL_DISPLAY_TICKS) {
      pokeballScale = Math.min(1.0f, ticksOnScreen / (float) FADE_IN_TICKS);
    }

    if (ticksOnScreen == SHATTER_TICKS && !shattered) {
      shattered = true;
      createShards();
    }

    for (Shard shard : shards) {
      shard.update();
    }

    if (ticksOnScreen >= SHOW_DEATH_SCREEN_TICKS && this.minecraft != null) {
      boolean isHardcore =
        this.minecraft.level != null &&
        this.minecraft.level.getLevelData().isHardcore();

      this.minecraft.setScreen(
        new DeathScreen(Component.literal("You Blacked Out!"), isHardcore)
      );
    }
  }

  private void createShards() {
    int centerX = this.width / 2;
    int centerY = this.height / 2;

    for (int i = 0; i < 12; i++) {
      double angle = (Math.PI * 2 * i) / 12;
      float velocityX = (float) (Math.cos(angle) *
        (2 + random.nextFloat() * 3));
      float velocityY = (float) (Math.sin(angle) *
        (2 + random.nextFloat() * 3));

      int color = random.nextBoolean() ? 0xFFFF0000 : 0xFFFFFFFF;
      shards.add(new Shard(centerX, centerY, velocityX, velocityY, color));
    }
  }

  @Override
  public void render(
    GuiGraphics graphics,
    int mouseX,
    int mouseY,
    float delta
  ) {
    int centerX = this.width / 2;
    int centerY = this.height / 2;

    if (!shattered) {
      drawPokeball(graphics, centerX, centerY, (int) (50 * pokeballScale));
    }

    for (Shard shard : shards) {
      shard.render(graphics);
    }

    super.render(graphics, mouseX, mouseY, delta);
  }

  private void drawPokeball(GuiGraphics graphics, int x, int y, int radius) {
    if (radius <= 0) return;

    fillCircle(graphics, x, y, radius, 0xFF000000);
    fillSemiCircle(graphics, x, y, radius - 2, 0xFFFF0000, true);
    fillSemiCircle(graphics, x, y, radius - 2, 0xFFFFFFFF, false);
    graphics.fill(x - radius, y - 3, x + radius, y + 3, 0xFF000000);

    fillCircle(graphics, x, y, radius / 4, 0xFF000000);
    fillCircle(graphics, x, y, radius / 4 - 2, 0xFFFFFFFF);
  }

  private void fillCircle(
    GuiGraphics graphics,
    int centerX,
    int centerY,
    int radius,
    int color
  ) {
    for (int dy = -radius; dy <= radius; dy++) {
      int dx = (int) Math.sqrt(radius * radius - dy * dy);
      graphics.fill(
        centerX - dx,
        centerY + dy,
        centerX + dx,
        centerY + dy + 1,
        color
      );
    }
  }

  private void fillSemiCircle(
    GuiGraphics graphics,
    int centerX,
    int centerY,
    int radius,
    int color,
    boolean top
  ) {
    int startY = top ? -radius : 0;
    int endY = top ? 0 : radius;

    for (int dy = startY; dy <= endY; dy++) {
      int dx = (int) Math.sqrt(radius * radius - dy * dy);
      graphics.fill(
        centerX - dx,
        centerY + dy,
        centerX + dx,
        centerY + dy + 1,
        color
      );
    }
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  private static class Shard {

    float x, y;
    float velocityX, velocityY;
    int color;
    int size;
    float gravity = 0.1f;
    float alpha = 1.0f;

    Shard(float x, float y, float vx, float vy, int color) {
      this.x = x;
      this.y = y;
      this.velocityX = vx;
      this.velocityY = vy;
      this.color = color;
      this.size = 5 + new Random().nextInt(10);
    }

    void update() {
      x += velocityX;
      y += velocityY;
      velocityY += gravity;
      alpha = Math.max(0, alpha - 0.015f);
    }

    void render(GuiGraphics graphics) {
      if (alpha <= 0) return;

      int a = (int) (alpha * 255);
      int renderColor = (a << 24) | (color & 0x00FFFFFF);

      graphics.fill(
        (int) x - size / 2,
        (int) y - size / 2,
        (int) x + size / 2,
        (int) y + size / 2,
        renderColor
      );
    }
  }
}
