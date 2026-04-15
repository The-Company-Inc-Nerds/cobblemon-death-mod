package com.thecompanyinc.cobblemondeathmod;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.thecompanyinc.cobblemondeathmod.config.DeathModConfigScreen;

public class ModMenuIntegration implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return DeathModConfigScreen::create;
  }
}
