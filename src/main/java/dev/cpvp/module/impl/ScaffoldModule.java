package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.util.PlaceUtil;
import net.minecraft.util.math.BlockPos;

public final class ScaffoldModule extends Module {
    public ScaffoldModule() { super("Scaffold", "Places blocks under your feet", Category.WORLD); }

    @Override public void onTick() {
        if (mc.currentScreen != null) return;
        PlaceUtil.place(mc.player.getBlockPos().down());
    }
}
