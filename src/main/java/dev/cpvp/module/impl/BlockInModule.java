package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.PlaceUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/** Surrounds you with blocks (feet, head, and above). */
public final class BlockInModule extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 5, 1, 1));
    private int timer;

    public BlockInModule() { super("BlockIn", "Cover yourself in blocks", Category.WORLD); }

    @Override public void onTick() {
        if (timer-- > 0 || mc.currentScreen != null) return;
        BlockPos f = mc.player.getBlockPos();
        for (Direction d : Direction.Type.HORIZONTAL) {
            if (PlaceUtil.place(f.offset(d)) || PlaceUtil.place(f.up().offset(d))) { timer = delay.getInt(); return; }
        }
        if (PlaceUtil.place(f.up(2))) timer = delay.getInt();
    }
}
