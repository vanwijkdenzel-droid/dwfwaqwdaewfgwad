package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Hold RMB with XP bottles in hand: throws several per tick instead of one every 4 ticks. */
public final class FastXpModule extends Module {
    private final NumberSetting throwsPerTick = add(new NumberSetting("Throws / Tick", 1, 8, 1, 3));

    public FastXpModule() { super("FastXP", "Throw XP bottles much faster while holding RMB", Category.UTILITY); }

    @Override public void onTick() {
        if (mc.currentScreen != null || !mc.options.useKey.isPressed()) return;
        if (!mc.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)) return;
        for (int i = 0; i < throwsPerTick.getInt(); i++) {
            if (!mc.player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)) break;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
