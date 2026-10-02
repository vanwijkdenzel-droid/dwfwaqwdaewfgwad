package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.PacketUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public final class KillAuraModule extends Module {
    private final NumberSetting aps    = add(new NumberSetting("APS", 1, 20, 1, 12));
    private final NumberSetting range  = add(new NumberSetting("Range", 2, 6, 0.1, 3.8));
    private final BooleanSetting team  = add(new BooleanSetting("Target Team", false));
    private final BooleanSetting rotate = add(new BooleanSetting("Silent Rotate", true));
    private long last;

    public KillAuraModule() { super("KillAura", "Attacks the nearest player in range", Category.COMBAT); }

    @Override public void onTick() {
        if (mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - last < 1000L / aps.getInt()) return;

        PlayerEntity best = null; double bd = range.get() * range.get();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (!team.get() && p.isTeammate(mc.player)) continue;
            double d = mc.player.squaredDistanceTo(p);
            if (d < bd) { bd = d; best = p; }
        }
        if (best == null) return;
        last = now;

        if (rotate.get()) {
            Vec3d to = best.getEyePos().subtract(mc.player.getEyePos());
            float yaw = (float) (Math.toDegrees(Math.atan2(to.z, to.x)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)));
            PacketUtil.send(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, mc.player.isOnGround()));
        }
        PacketUtil.attack(best);
        PacketUtil.swing();
    }
}
