package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class TriggerBotModule extends Module {
    private final NumberSetting cooldown = add(new NumberSetting("Min Cooldown", 0.1, 1.0, 0.05, 0.95));
    private final BooleanSetting crits   = add(new BooleanSetting("Crits Only", false));

    public TriggerBotModule() { super("TriggerBot", "Attacks when your crosshair is on a player", Category.COMBAT); }

    @Override public void onFrame() {
        if (mc.currentScreen != null || mc.player.isUsingItem() || mc.interactionManager == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit) || !(hit.getEntity() instanceof PlayerEntity p)) return;
        if (p == mc.player || !p.isAlive() || p.isSpectator()) return;
        if (mc.player.getAttackCooldownProgress(0f) < cooldown.get()) return;
        if (crits.get() && (mc.player.isOnGround() || mc.player.fallDistance <= 0f)) return;
        mc.interactionManager.attackEntity(mc.player, p);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
