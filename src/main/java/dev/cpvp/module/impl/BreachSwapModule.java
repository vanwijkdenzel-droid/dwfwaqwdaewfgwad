package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.util.PacketUtil;
import dev.cpvp.util.SlotUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Attribute swap: on your click, the server receives  [swap to Breach mace] -> [attack] -> [swap back to sword]
 * inside one tick. The vanilla attack that would follow is cancelled (MinecraftClientMixin), so you land
 * exactly one mace hit and keep the sword in hand.
 */
public final class BreachSwapModule extends Module {
    private static boolean cancelNext;
    private final BooleanSetting onlySword     = add(new BooleanSetting("Only When Holding Sword", true));
    private final BooleanSetting requireBreach = add(new BooleanSetting("Require Breach Enchant", true));

    public BreachSwapModule() { super("BreachSwap", "Hit with Breach mace, keep the sword in hand", Category.COMBAT); }

    public static boolean consumeCancel() { boolean r = cancelNext; cancelNext = false; return r; }

    @Override public void onTick() { cancelNext = false; }   // safety: never carry the flag over a tick
    @Override public void onDisable() { cancelNext = false; }

    @Override public void onLeftClick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)) return;
        if (onlySword.get() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;

        int mace = SlotUtil.find(s -> s.isOf(Items.MACE) && (!requireBreach.get() || hasBreach(s)));
        if (mace < 0) { status(requireBreach.get() ? "No Breach mace in hotbar" : "No mace in hotbar"); return; }
        if (mace == SlotUtil.selected()) return;

        int prev = SlotUtil.selected();
        SlotUtil.swap(mace);
        PacketUtil.attack(target);
        PacketUtil.swing();
        SlotUtil.swap(prev);
        mc.player.resetLastAttackedTicks();
        cancelNext = true;
    }

    private static boolean hasBreach(ItemStack stack) {
        ItemEnchantmentsComponent e = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        for (RegistryEntry<Enchantment> en : e.getEnchantments()) if (en.matchesKey(Enchantments.BREACH)) return true;
        return false;
    }
}
