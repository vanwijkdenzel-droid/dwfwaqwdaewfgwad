package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoArmorModule extends Module {
    private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 0, 10, 1, 2));
    private int timer;

    public AutoArmorModule() { super("AutoArmor", "Equips the best armor", Category.INVENTORY); }

    private static int armorSlot(EquipmentSlot s) {
        return switch (s) { case HEAD -> 5; case CHEST -> 6; case LEGS -> 7; case FEET -> 8; default -> -1; };
    }

    private static int prot(ItemStack s) {
        return s.getItem() instanceof ArmorItem a ? a.getProtection() : -1;
    }

    @Override public void onTick() {
        if (timer-- > 0 || mc.currentScreen != null) return;
        var slots = mc.player.playerScreenHandler.slots;
        for (int id = 9; id <= 44; id++) {
            ItemStack cand = slots.get(id).getStack();
            if (!(cand.getItem() instanceof ArmorItem a)) continue;
            int target = armorSlot(a.getType().getEquipmentSlot());
            if (target < 0) continue;
            ItemStack worn = slots.get(target).getStack();
            if (prot(cand) <= prot(worn)) continue;
            int sync = mc.player.playerScreenHandler.syncId;
            if (worn.isEmpty()) {
                mc.interactionManager.clickSlot(sync, id, 0, SlotActionType.QUICK_MOVE, mc.player);
            } else {
                mc.interactionManager.clickSlot(sync, id, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, target, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, id, 0, SlotActionType.PICKUP, mc.player);
            }
            timer = delay.getInt();
            return;
        }
    }
}
