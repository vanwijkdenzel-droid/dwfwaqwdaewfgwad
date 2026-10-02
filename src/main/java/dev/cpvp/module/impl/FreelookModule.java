package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.CameraControl;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Orbit camera around you while your real aim stays put. */
public final class FreelookModule extends Module {
    private final NumberSetting distance = add(new NumberSetting("Distance", 1, 8, 0.5, 3.5));
    private float savedYaw, savedPitch;

    public FreelookModule() { super("Freelook", "Look around without turning", Category.RENDER); }

    @Override public void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        savedYaw = CameraControl.yaw = mc.player.getYaw();
        savedPitch = CameraControl.pitch = mc.player.getPitch();
        CameraControl.active = true;
    }

    @Override public void onDisable() {
        CameraControl.active = false;
        if (mc.player != null) { mc.player.setYaw(savedYaw); mc.player.setPitch(savedPitch); }
    }

    @Override public void onFrame() {
        if (!CameraControl.active || mc.player == null) return;
        CameraControl.yaw += mc.player.getYaw() - savedYaw;
        CameraControl.pitch = MathHelper.clamp(CameraControl.pitch + mc.player.getPitch() - savedPitch, -90f, 90f);
        mc.player.setYaw(savedYaw); mc.player.setPitch(savedPitch);
        mc.player.prevYaw = savedYaw; mc.player.prevPitch = savedPitch;

        float td = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d eye = mc.player.getLerpedPos(td).add(0, mc.player.getStandingEyeHeight(), 0);
        double y = Math.toRadians(CameraControl.yaw), p = Math.toRadians(CameraControl.pitch);
        Vec3d fwd = new Vec3d(-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p));
        Vec3d pos = eye.subtract(fwd.multiply(distance.get()));
        CameraControl.x = pos.x; CameraControl.y = pos.y; CameraControl.z = pos.z;
    }
}
