package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;

/** Cancels your swings at players until the attack cooldown is high enough (MinecraftClientMixin#doAttack). */
public final class HitSelectModule extends Module {
    public static boolean ACTIVE;
    private static double min = 0.9;
    private final NumberSetting minCooldown = add(new NumberSetting("Min Cooldown", 0.1, 1.0, 0.05, 0.9));

    public HitSelectModule() { super("HitSelect", "Only hit when your cooldown is ready", Category.COMBAT); }

    public static boolean shouldCancel() {
        MinecraftClient c = MinecraftClient.getInstance();
        return ACTIVE && c.player != null && c.crosshairTarget instanceof EntityHitResult
                && c.player.getAttackCooldownProgress(0f) < min;
    }

    @Override public void onEnable()  { ACTIVE = true; }
    @Override public void onDisable() { ACTIVE = false; }
    @Override public void onTick()    { min = minCooldown.get(); }
}
