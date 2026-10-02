package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.CameraControl;
import net.minecraft.client.input.Input;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Spectator-style free camera: you fly through blocks with smooth acceleration while your real
 * player stands still (input blanked, rotation pinned). Camera is overridden in CameraMixin so the
 * HUD, hotbar and hands stay yours.
 */
public final class FreecamModule extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.5, 10, 0.5, 3.0));

    private Input savedInput;
    private float savedYaw, savedPitch;
    private Vec3d vel = Vec3d.ZERO;
    private long last;

    public FreecamModule() { super("Freecam", "Spectator-style camera", Category.RENDER); }

    @Override public void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        savedYaw = CameraControl.yaw = mc.player.getYaw();
        savedPitch = CameraControl.pitch = mc.player.getPitch();
        Vec3d eye = mc.player.getEyePos();
        CameraControl.x = eye.x; CameraControl.y = eye.y; CameraControl.z = eye.z;
        CameraControl.active = true;
        savedInput = mc.player.input;
        mc.player.input = new Input();
        vel = Vec3d.ZERO;
        last = System.nanoTime();
    }

    @Override public void onDisable() {
        CameraControl.active = false;
        if (mc.player == null) return;
        if (savedInput != null) mc.player.input = savedInput;
        mc.player.setYaw(savedYaw);
        mc.player.setPitch(savedPitch);
    }

    @Override public void onFrame() {
        if (!CameraControl.active || mc.player == null) return;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - last) / 1e9f);
        last = now;

        // Mouse turned the real player: move that rotation onto the camera, pin the player.
        CameraControl.yaw += mc.player.getYaw() - savedYaw;
        CameraControl.pitch = MathHelper.clamp(CameraControl.pitch + mc.player.getPitch() - savedPitch, -90f, 90f);
        mc.player.setYaw(savedYaw); mc.player.setPitch(savedPitch);
        mc.player.prevYaw = savedYaw; mc.player.prevPitch = savedPitch;

        double y = Math.toRadians(CameraControl.yaw), p = Math.toRadians(CameraControl.pitch);
        Vec3d fwd = new Vec3d(-Math.sin(y) * Math.cos(p), -Math.sin(p), Math.cos(y) * Math.cos(p));
        Vec3d left = new Vec3d(Math.cos(y), 0, Math.sin(y));
        Vec3d wish = Vec3d.ZERO;
        if (mc.currentScreen == null) {
            if (mc.options.forwardKey.isPressed()) wish = wish.add(fwd);
            if (mc.options.backKey.isPressed())    wish = wish.subtract(fwd);
            if (mc.options.leftKey.isPressed())    wish = wish.add(left);
            if (mc.options.rightKey.isPressed())   wish = wish.subtract(left);
            if (mc.options.jumpKey.isPressed())    wish = wish.add(0, 1, 0);
            if (mc.options.sneakKey.isPressed())   wish = wish.add(0, -1, 0);
        }
        Vec3d target = wish.lengthSquared() > 0 ? wish.normalize().multiply(speed.get() * 6.0) : Vec3d.ZERO;
        vel = vel.lerp(target, 1.0 - Math.exp(-dt * 9.0));
        CameraControl.x += vel.x * dt; CameraControl.y += vel.y * dt; CameraControl.z += vel.z * dt;
    }
}
