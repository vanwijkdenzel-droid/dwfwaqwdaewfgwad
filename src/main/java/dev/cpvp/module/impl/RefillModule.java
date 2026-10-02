package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/** Tops up low hotbar stacks from your inventory. */
public final class RefillModule extends Module {
    private final NumberSetting threshold = add(new NumberSetting("Refill At", 1, 32, 1, 8));
    private final NumberSetting delay     = add(new NumberSetting("Delay (ticks)", 1, 20, 1, 4));
    private int timer;

    public RefillModule() { super("Refill", "Refill hotbar stacks", Category.INVENTORY); }

    @Override public void onTick() {
        if (timer-- > 0 || mc.currentScreen != null) return;
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty() || !s.isStackable() || s.getCount() > threshold.getInt() || s.getCount() >= s.getMaxCount()) continue;
            for (int j = 9; j < 36; j++) {
                if (!ItemStack.areItemsAndComponentsEqual(s, inv.getStack(j))) continue;
                mc.interactionManager.clickSlot(0, j, i, SlotActionType.SWAP, mc.player);
                timer = delay.getInt();
                return;
            }
        }
    }
}
