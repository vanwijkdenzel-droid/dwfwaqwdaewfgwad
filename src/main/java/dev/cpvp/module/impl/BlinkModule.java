package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;

/** Holds ALL movement packets until you turn it off. */
public final class BlinkModule extends Module {
    public BlinkModule() { super("Blink", "Freeze your position for the server", Category.NETWORK); }
    @Override public void onEnable()  { FakeLagModule.setBlink(true); }
    @Override public void onDisable() { FakeLagModule.setBlink(false); }
}
