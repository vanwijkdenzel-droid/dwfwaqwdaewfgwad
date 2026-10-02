package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimAssistModule extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 2, 8, 0.5, 5));
    private final NumberSetting fov   = add(new NumberSetting("FOV", 10, 180, 5, 90));
    private final NumberSetting speed = add(new NumberSetting("Smoothing Speed", 1, 100, 1, 12));
    private final BooleanSetting head = add(new BooleanSetting("Aim At Head", false));
    private final BooleanSetting click = add(new BooleanSetting("Only While Attacking", true));

    private long last = System.nanoTime();

    public AimAssistModule() { super("AimAssist", "Smoothly pulls your aim toward nearby players", Category.COMBAT); }

    @Override public void onFrame() {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1e9f);
        last = now;
        if (mc.currentScreen != null || (click.get() && !mc.options.attackKey.isPressed())) return;

        PlayerEntity best = null; double bestAng = fov.get() / 2.0;
        Vec3d eye = mc.player.getEyePos();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator() || mc.player.distanceTo(p) > range.get()) continue;
            float[] r = rot(eye, aimPoint(p));
            double ang = Math.abs(MathHelper.wrapDegrees(r[0] - mc.player.getYaw()));
            if (ang < bestAng) { bestAng = ang; best = p; }
        }
        if (best == null) return;

        float[] r = rot(eye, aimPoint(best));
        float k = 1f - (float) Math.pow(1.0 - speed.get() / 100.0, dt * 60.0); // frame-rate independent
        float dy = MathHelper.wrapDegrees(r[0] - mc.player.getYaw());
        float dp = r[1] - mc.player.getPitch();
        mc.player.setYaw(mc.player.getYaw() + dy * k);
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + dp * k, -90f, 90f));
    }

    private Vec3d aimPoint(PlayerEntity p) {
        return head.get() ? p.getEyePos() : p.getPos().add(0, p.getHeight() * 0.6, 0);
    }

    private static float[] rot(Vec3d from, Vec3d to) {
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        return new float[] { yaw, pitch };
    }
}
