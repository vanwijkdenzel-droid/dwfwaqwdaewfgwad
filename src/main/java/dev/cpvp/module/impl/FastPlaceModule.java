package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;

/** Removes the 4-tick right-click delay (MinecraftClientMixin#doItemUse). */
public final class FastPlaceModule extends Module {
    public static boolean ACTIVE;
    public FastPlaceModule() { super("FastPlace", "No delay between placements", Category.WORLD); }
    @Override public void onEnable()  { ACTIVE = true; }
    @Override public void onDisable() { ACTIVE = false; }
}
