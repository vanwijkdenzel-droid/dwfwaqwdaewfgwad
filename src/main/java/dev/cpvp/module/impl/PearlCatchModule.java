package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.SlotUtil;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * Throw a pearl as normal. A few ticks later this finds YOUR pearl in flight and throws a wind charge
 * straight at it, so the pearl is boosted and you teleport higher in the air.
 */
public final class PearlCatchModule extends Module {
    private final NumberSetting delay   = add(new NumberSetting("Delay (ticks)", 1, 15, 1, 2));
    private final NumberSetting lead    = add(new NumberSetting("Aim Lead (ticks)", 0, 6, 0.5, 1.5));
    private final BooleanSetting swapBack = add(new BooleanSetting("Swap Back", true));

    private int countdown = -1;

    public PearlCatchModule() { super("Pearlcatch", "Wind charge hits your pearl mid-air", Category.COMBAT); }

    @Override public boolean ticksWhenDisabled() { return true; }

    /** Keybind: swap to a pearl, throw it, swap back, then the wind charge follows automatically. */
    @Override public void onKeyPress() {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
        int pearl = SlotUtil.find(s -> s.isOf(Items.ENDER_PEARL));
        if (pearl < 0) { status("No ender pearl in hotbar"); return; }
        if (SlotUtil.find(s -> s.isOf(Items.WIND_CHARGE)) < 0) { status("No wind charge in hotbar"); return; }
        if (mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)) { status("Pearl on cooldown"); return; }
        int prev = SlotUtil.selected();
        SlotUtil.swap(pearl);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        SlotUtil.swap(prev);
        countdown = delay.getInt();
    }

    @Override public void onRightClick() {
        if (mc.player == null || !mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) return;
        if (mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)) return;
        countdown = delay.getInt();
    }

    @Override public void onTick() {
        if (countdown < 0) return;
        if (countdown > 0) { countdown--; return; }
        countdown = -1;
        fire();
    }

    private void fire() {
        EnderPearlEntity pearl = null;
        for (EnderPearlEntity p : mc.world.getEntitiesByClass(EnderPearlEntity.class,
                mc.player.getBoundingBox().expand(32), e -> e.getOwner() == mc.player)) {
            if (pearl == null || p.age < pearl.age) pearl = p;   // newest pearl
        }
        if (pearl == null) { status("Pearl not found"); return; }
        int slot = SlotUtil.find(s -> s.isOf(Items.WIND_CHARGE));
        if (slot < 0) { status("No wind charge in hotbar"); return; }

        Vec3d aim = pearl.getPos().add(pearl.getVelocity().multiply(lead.get())).add(0, pearl.getHeight() / 2, 0);
        Vec3d to = aim.subtract(mc.player.getEyePos());
        float yaw = (float) (Math.toDegrees(Math.atan2(to.z, to.x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)));

        int prev = SlotUtil.selected();
        float oy = mc.player.getYaw(), op = mc.player.getPitch();
        SlotUtil.swap(slot);
        mc.player.setYaw(yaw); mc.player.setPitch(pitch);          // server receives this aim inside the use packet
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.setYaw(oy); mc.player.setPitch(op);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (swapBack.get()) SlotUtil.swap(prev);
    }
}
