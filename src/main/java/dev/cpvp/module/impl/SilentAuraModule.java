package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.PacketUtil;
import dev.cpvp.util.TargetUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * Cooldown-aware aura with SILENT rotation: the server gets a look packet at the target, your
 * screen never turns. Uses the normal attack path so cooldown/animation stay in sync.
 */
public final class SilentAuraModule extends Module {
    private final NumberSetting range    = add(new NumberSetting("Range", 2, 6, 0.1, 3.0));
    private final NumberSetting cooldown = add(new NumberSetting("Min Cooldown", 0.5, 1.0, 0.05, 0.95));
    private final BooleanSetting mobs    = add(new BooleanSetting("Target Mobs", true));
    private final BooleanSetting rotate  = add(new BooleanSetting("Silent Rotate", true));

    public SilentAuraModule() { super("SilentAura", "Cooldown-timed aura with silent rotations", Category.COMBAT); }

    @Override public void onTick() {
        if (mc.currentScreen != null || mc.interactionManager == null) return;
        if (mc.player.getAttackCooldownProgress(0f) < cooldown.get()) return;
        LivingEntity t = TargetUtil.nearestLiving(range.get(), mobs.get());
        if (t == null) return;

        if (rotate.get()) {
            Vec3d to = t.getBoundingBox().getCenter().subtract(mc.player.getEyePos());
            float yaw = (float) (Math.toDegrees(Math.atan2(to.z, to.x)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)));
            PacketUtil.send(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        }
        mc.interactionManager.attackEntity(mc.player, t);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
