package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;

/** Extends your entity interaction range on the client (PlayerEntityMixin). The server still enforces its own limit. */
public final class ReachModule extends Module {
    public static boolean ACTIVE;
    private static double range = 3.0;
    private final NumberSetting dist = add(new NumberSetting("Range", 3.0, 6.0, 0.1, 3.5));

    public ReachModule() { super("Reach", "Longer attack reach", Category.COMBAT); }

    public static double range() { return range; }

    @Override public void onEnable()  { ACTIVE = true; range = dist.get(); }
    @Override public void onDisable() { ACTIVE = false; }
    @Override public void onTick()    { range = dist.get(); }
}
