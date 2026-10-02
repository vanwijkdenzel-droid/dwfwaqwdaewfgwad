package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class AutoClickerModule extends Module {
    private final NumberSetting cps = add(new NumberSetting("CPS", 1, 20, 1, 10));
    private long last;

    public AutoClickerModule() { super("AutoClicker", "Attacks for you while LMB is held", Category.COMBAT); }

    @Override public void onFrame() {
        if (mc.currentScreen != null || !mc.options.attackKey.isPressed()) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult h)) return;
        long now = System.currentTimeMillis();
        if (now - last < 1000L / cps.getInt()) return;
        last = now;
        mc.interactionManager.attackEntity(mc.player, h.getEntity());
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
