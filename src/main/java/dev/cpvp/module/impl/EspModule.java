package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.Render3D;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

/** Through-wall boxes for players, dropped items and storage blocks. */
public final class EspModule extends Module {
    private final BooleanSetting players = add(new BooleanSetting("Players", true));
    private final BooleanSetting items   = add(new BooleanSetting("Items", true));
    private final BooleanSetting chests  = add(new BooleanSetting("Chests", true));
    private final NumberSetting range    = add(new NumberSetting("Range", 16, 128, 8, 64));

    private record Target(Box box, int color) {}
    private final List<Target> storage = new ArrayList<>();
    private int scanTimer;

    public EspModule(String name, boolean p, boolean i, boolean c) {
        super(name, "Through-wall boxes", Category.RENDER);
        players.set(p); items.set(i); chests.set(c);
    }

    @Override public void onTick() {
        if (!chests.get()) { storage.clear(); return; }
        if (scanTimer-- > 0) return;
        scanTimer = 20;
        storage.clear();
        ChunkPos cp = mc.player.getChunkPos();
        int r = Math.min(8, mc.options.getViewDistance().getValue());
        double rsq = range.get() * range.get();
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            WorldChunk ch = mc.world.getChunkManager().getWorldChunk(cp.x + dx, cp.z + dz);
            if (ch == null) continue;
            for (BlockEntity be : ch.getBlockEntities().values()) {
                int color;
                if (be instanceof ChestBlockEntity) color = 0xFFFFA500;
                else if (be instanceof EnderChestBlockEntity) color = 0xFFB400FF;
                else if (be instanceof BarrelBlockEntity) color = 0xFFC8A064;
                else if (be instanceof ShulkerBoxBlockEntity) color = 0xFFFF55FF;
                else continue;
                if (mc.player.squaredDistanceTo(Vec3d.ofCenter(be.getPos())) > rsq) continue;
                storage.add(new Target(new Box(be.getPos()).contract(0.06), color));
            }
        }
    }

    @Override public void onWorldRender(WorldRenderContext ctx) {
        float td = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d cam = ctx.camera().getPos();
        double rsq = range.get() * range.get();
        Render3D.begin();
        for (Entity e : mc.world.getEntities()) {
            boolean isPlayer = e instanceof PlayerEntity && e.isAlive()
                    && (e != mc.player || mc.getCameraEntity() != mc.player);
            boolean isItem = e instanceof ItemEntity;
            if (!((isPlayer && players.get()) || (isItem && items.get()))) continue;
            if (e.squaredDistanceTo(mc.player) > rsq) continue;
            Box b = e.getBoundingBox().offset(e.getLerpedPos(td).subtract(e.getPos()));
            Render3D.box(ctx.matrixStack(), cam, b, isPlayer ? 0xFFFF3B3B : 0xFF3BE8FF);
        }
        for (Target t : storage) Render3D.box(ctx.matrixStack(), cam, t.box(), t.color());
        Render3D.end();
    }
}
