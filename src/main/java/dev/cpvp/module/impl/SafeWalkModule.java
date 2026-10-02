package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;

/** Never walk off ledges (PlayerEntityMixin#clipAtLedge). */
public final class SafeWalkModule extends Module {
    public static boolean ACTIVE;
    public SafeWalkModule() { super("SafeWalk", "Don't fall off edges", Category.WORLD); }
    @Override public void onEnable()  { ACTIVE = true; }
    @Override public void onDisable() { ACTIVE = false; }
}
