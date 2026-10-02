package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public final class RightClickerModule extends Module {
    private final NumberSetting cps = add(new NumberSetting("CPS", 1, 20, 1, 12));
    private long last;

    public RightClickerModule() { super("RightClicker", "Uses / places for you while RMB is held", Category.COMBAT); }

    @Override public void onFrame() {
        if (mc.currentScreen != null || !mc.options.useKey.isPressed()) return;
        long now = System.currentTimeMillis();
        if (now - last < 1000L / cps.getInt()) return;
        last = now;
        if (mc.crosshairTarget instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK)
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, b);
        else mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
