package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;

/** Expands other players' targeting hitbox on the client (EntityMixin#getTargetingMargin). */
public final class HitboxesModule extends Module {
    public static boolean ACTIVE;
    private static double expand;
    private final NumberSetting size = add(new NumberSetting("Expand", 0.0, 1.0, 0.05, 0.3));

    public HitboxesModule() { super("Hitboxes", "Bigger player hitboxes for aiming", Category.COMBAT); }

    public static double expand() { return expand; }

    @Override public void onEnable()  { ACTIVE = true; expand = size.get(); }
    @Override public void onDisable() { ACTIVE = false; }
    @Override public void onTick()    { expand = size.get(); }
}
