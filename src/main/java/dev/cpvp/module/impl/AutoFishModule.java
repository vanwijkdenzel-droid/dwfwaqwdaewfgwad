package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Reels in when the bobber dips, then recasts. Bite detection is a velocity heuristic - tune if needed. */
public final class AutoFishModule extends Module {
    private final NumberSetting recast = add(new NumberSetting("Recast Delay (ticks)", 5, 40, 1, 15));
    private int wait;

    public AutoFishModule() { super("AutoFish", "Automatic fishing", Category.UTILITY); }

    @Override public void onTick() {
        if (!mc.player.getMainHandStack().isOf(Items.FISHING_ROD)) return;
        if (wait > 0) { if (--wait == 0) cast(); return; }
        FishingBobberEntity hook = mc.player.fishHook;
        if (hook != null && hook.isTouchingWater() && hook.age > 40 && hook.getVelocity().y < -0.08) {
            cast();               // reel in
            wait = recast.getInt();
        }
    }

    private void cast() {
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
