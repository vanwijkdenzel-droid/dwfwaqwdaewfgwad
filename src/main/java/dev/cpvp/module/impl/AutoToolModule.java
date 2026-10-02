package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.util.SlotUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class AutoToolModule extends Module {
    private int prev = -1;
    public AutoToolModule() { super("AutoTool", "Best tool when mining", Category.UTILITY); }

    @Override public void onTick() {
        if (mc.currentScreen == null && mc.options.attackKey.isPressed()
                && mc.crosshairTarget instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
            if (prev < 0) prev = SlotUtil.selected();
            SlotUtil.swap(SlotUtil.bestTool(mc.world.getBlockState(b.getBlockPos())));
        } else if (prev >= 0) { SlotUtil.swap(prev); prev = -1; }
    }

    @Override public void onDisable() { if (prev >= 0 && mc.player != null) SlotUtil.swap(prev); prev = -1; }
}
