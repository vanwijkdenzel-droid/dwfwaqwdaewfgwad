package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.util.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class TracersModule extends Module {
    public TracersModule() { super("Tracers", "Lines to every player", Category.RENDER); }

    @Override public void onWorldRender(WorldRenderContext ctx) {
        float td = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d cam = ctx.camera().getPos();
        Vec3d start = cam.add(Vec3d.fromPolar(ctx.camera().getPitch(), ctx.camera().getYaw()).multiply(0.5));
        Render3D.begin();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            Vec3d end = p.getLerpedPos(td).add(0, p.getHeight() / 2, 0);
            Render3D.line(ctx.matrixStack(), cam, start, end, 0xFF00D2FF);
        }
        Render3D.end();
    }
}
