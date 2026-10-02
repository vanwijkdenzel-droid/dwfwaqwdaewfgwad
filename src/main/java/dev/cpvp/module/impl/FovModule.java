package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;

public final class FovModule extends Module {
    private final NumberSetting fov = add(new NumberSetting("FOV", 30, 110, 1, 100));
    private int original = 70;

    public FovModule() { super("FOV", "Change your field of view", Category.RENDER); }

    @Override public void onEnable()  { original = mc.options.getFov().getValue(); }
    @Override public void onDisable() { if (mc.options != null) mc.options.getFov().setValue(original); }
    @Override public void onTick()    { mc.options.getFov().setValue(fov.getInt()); }
}
