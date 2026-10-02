package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Keeps a totem in your offhand. "Delay" is how many ms after the offhand empties before it re-equips. */
public final class AutoTotemModule extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay (ms)", 0, 1000, 10, 120));
    private long missingSince;

    public AutoTotemModule() { super("AutoTotem", "Re-equips a totem after a set delay", Category.INVENTORY); }

    @Override public void onFrame() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) { missingSince = 0; return; }
        long now = System.currentTimeMillis();
        if (missingSince == 0) { missingSince = now; return; }
        if (now - missingSince < delay.getInt()) return;

        var slots = mc.player.playerScreenHandler.slots;
        for (int id = 9; id <= 44; id++) {
            if (!slots.get(id).getStack().isOf(Items.TOTEM_OF_UNDYING)) continue;
            mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, id, 40, SlotActionType.SWAP, mc.player);
            missingSince = 0;
            return;
        }
    }
}
