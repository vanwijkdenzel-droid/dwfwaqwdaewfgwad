package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.PlaceUtil;
import dev.cpvp.util.SlotUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Saves you from a fall: water bucket MLG right before landing (then picks it back up), or a block under you. */
public final class ClutchModule extends Module {
    private final NumberSetting minFall = add(new NumberSetting("Min Fall", 3, 20, 1, 5));
    private final BooleanSetting water  = add(new BooleanSetting("Water Bucket", true));
    private final BooleanSetting blocks = add(new BooleanSetting("Block Clutch", true));
    private int pickup;

    public ClutchModule() { super("Clutch", "Water MLG / block clutch when falling", Category.UTILITY); }

    @Override public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (pickup > 0) {                                   // pick the water back up
            if (--pickup == 0 || mc.player.isTouchingWater() || mc.player.isOnGround()) {
                int b = SlotUtil.find(s -> s.isOf(Items.BUCKET));
                if (b >= 0) useDown(b);
                pickup = 0;
            }
            return;
        }
        if (mc.player.isOnGround() || mc.player.isFallFlying() || mc.player.fallDistance < minFall.get()) return;

        Vec3d from = mc.player.getPos();
        var hit = mc.world.raycast(new RaycastContext(from, from.add(0, -4.5, 0),
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        if (hit.getType() != HitResult.Type.BLOCK) return;   // ground not close yet
        double gap = from.y - hit.getPos().y;

        int bucket = SlotUtil.find(s -> s.isOf(Items.WATER_BUCKET));
        if (water.get() && bucket >= 0 && gap <= 3.5) { useDown(bucket); pickup = 20; return; }
        if (blocks.get() && gap <= 2.5) PlaceUtil.place(mc.player.getBlockPos().down());
    }

    private void useDown(int slot) {
        int prev = SlotUtil.selected();
        float op = mc.player.getPitch();
        SlotUtil.swap(slot);
        mc.player.setPitch(90f);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.setPitch(op);
        SlotUtil.swap(prev);
    }
}
