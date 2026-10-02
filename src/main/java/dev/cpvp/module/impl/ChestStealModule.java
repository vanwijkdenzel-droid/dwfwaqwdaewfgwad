package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public final class ChestStealModule extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 10, 1, 1));
    private int timer;

    public ChestStealModule() { super("ChestSteal", "Takes everything from an open chest", Category.INVENTORY); }

    @Override public void onTick() {
        if (!(mc.currentScreen instanceof GenericContainerScreen gs)) return;
        if (timer-- > 0) return;
        GenericContainerScreenHandler h = gs.getScreenHandler();
        for (int i = 0; i < h.getRows() * 9; i++) {
            if (!h.getSlot(i).hasStack()) continue;
            mc.interactionManager.clickSlot(h.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer = delay.getInt();
            return;
        }
    }
}
