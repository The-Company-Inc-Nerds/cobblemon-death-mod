package com.thecompanyinc.cobblemondeathmod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PokeballDeathScreen extends Screen {

  private int ticksOnScreen = 0;
  private final List<Shard> shards = new ArrayList<>();
  private final Random random = new Random();

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
  public void renderBackground(
    GuiGraphics graphics,
    int mouseX,
    int mouseY,
    float delta
  ) {}

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
    graphics.fill(0, 0, this.width, this.height, 0xFF000000);

    int centerX = this.width / 2;
    int centerY = this.height / 2;

    if (!shattered && pokeballScale > 0) {
      int radius = (int) (50 * pokeballScale);
      drawPokeball(graphics, centerX, centerY, radius);
    }

    for (Shard shard : shards) {
      shard.render(graphics);
    }

    super.render(graphics, mouseX, mouseY, delta);
  }

  private void drawPokeball(GuiGraphics graphics, int x, int y, int radius) {
    if (radius <= 0) return;

    for (int dy = -radius; dy <= radius; dy++) {
      int dx = (int) Math.sqrt(radius * radius - dy * dy);
      graphics.fill(x - dx, y + dy, x + dx, y + dy + 1, 0xFF222222);
    }

    for (int dy = -radius + 2; dy < 0; dy++) {
      int dx = (int) Math.sqrt((radius - 2) * (radius - 2) - dy * dy);
      graphics.fill(x - dx, y + dy, x + dx, y + dy + 1, 0xFFFF0000);
    }

    for (int dy = 0; dy <= radius - 2; dy++) {
      int dx = (int) Math.sqrt((radius - 2) * (radius - 2) - dy * dy);
      graphics.fill(x - dx, y + dy, x + dx, y + dy + 1, 0xFFFFFFFF);
    }

    graphics.fill(x - radius, y - 2, x + radius, y + 3, 0xFF222222);

    int buttonRadius = radius / 4;
    for (int dy = -buttonRadius; dy <= buttonRadius; dy++) {
      int dx = (int) Math.sqrt(buttonRadius * buttonRadius - dy * dy);
      graphics.fill(x - dx, y + dy, x + dx, y + dy + 1, 0xFF222222);
    }
    int innerRadius = buttonRadius - 2;
    if (innerRadius > 0) {
      for (int dy = -innerRadius; dy <= innerRadius; dy++) {
        int dx = (int) Math.sqrt(innerRadius * innerRadius - dy * dy);
        graphics.fill(x - dx, y + dy, x + dx, y + dy + 1, 0xFFFFFFFF);
      }
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
