package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.SlotUtil;
import dev.cpvp.util.TargetUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Place -> charge -> explode. With "Double Anchor" it places and charges TWO anchors,
 * then detonates them back to back (anchors survive explosions, so both go off).
 *
 * Needs: another player within Target Range, anchor + glowstone in the HOTBAR, overworld/end.
 * The action bar tells you why it isn't firing.
 */
public final class AutoAnchorModule extends Module {
    private final NumberSetting range     = add(new NumberSetting("Target Range", 2, 10, 0.5, 6));
    private final NumberSetting stepDelay = add(new NumberSetting("Step Delay (ticks)", 0, 5, 1, 0));
    private final NumberSetting cooldown  = add(new NumberSetting("Cooldown (ticks)", 0, 20, 1, 4));
    private final NumberSetting minHealth = add(new NumberSetting("Min Health", 1, 20, 1, 8));
    private final BooleanSetting doubleAnchor = add(new BooleanSetting("Double Anchor", false));
    private final BooleanSetting swapBack = add(new BooleanSetting("Swap Back", true));
    private final BooleanSetting auto     = add(new BooleanSetting("Auto Trigger", true));

    private enum Kind { PLACE, CHARGE, EXPLODE }
    private record Step(Kind kind, int idx) {}
    private record Placement(BlockPos pos, BlockHitResult hit) {}

    private final List<Placement> spots = new ArrayList<>();
    private final ArrayDeque<Step> steps = new ArrayDeque<>();
    private boolean active;
    private BlockPos pendingPos;
    private int pendingTicks;
    private int timer, cooldownLeft, prevSlot = -1;

    public AutoAnchorModule() {
        super("AutoAnchor", "Place, charge and explode anchors on a target", Category.COMBAT);
    }

    @Override public void onDisable() { if (active) finish(); }

    /** Used by Shieldbreaker's anchor follow-up. */
    public void queue(PlayerEntity target) {
        if (!active && cooldownLeft == 0 && canRun()) begin(target);
    }

    @Override public boolean ticksWhenDisabled() { return true; }

    /** Keybind: run place/charge/explode on the nearest target right now. */
    @Override public void onKeyPress() {
        if (active || mc.player == null || mc.world == null) return;
        PlayerEntity t = TargetUtil.nearest(range.get());
        if (t == null) { status("No target in range"); return; }
        cooldownLeft = 0;
        if (canRun()) begin(t);
    }

    @Override public void onTick() {
        if (!isEnabled() && !active) return;   // disabled: only finish a sequence the key started
        if (!canRun()) { if (active) finish(); return; }
        if (cooldownLeft > 0) cooldownLeft--;

        // Manual assist (works with Auto Trigger OFF): you place the anchor, we charge + explode it.
        if (pendingPos != null && pendingTicks-- <= 0) {
            if (!active && mc.world.getBlockState(pendingPos).isOf(Blocks.RESPAWN_ANCHOR)) beginManual(pendingPos);
            pendingPos = null;
        }

        if (!active && auto.get() && cooldownLeft == 0) {
            PlayerEntity t = TargetUtil.nearest(range.get());
            if (t != null) begin(t);
        }
        if (active) run();
    }

    private boolean canRun() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null) return false;
        if (mc.world.getDimension().respawnAnchorWorks()) { status("Anchors don't explode in this dimension"); return false; }
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() < minHealth.get()) { status("Health below Min Health"); return false; }
        return true;
    }

    private int count(Item item) {
        int n = 0;
        for (int i = 0; i < 9; i++) {
            var st = mc.player.getInventory().getStack(i);
            if (st.isOf(item)) n += st.getCount();
        }
        return n;
    }

    @Override public void onRightClick() {
        if (mc.player == null || !mc.player.getMainHandStack().isOf(Items.RESPAWN_ANCHOR)) return;
        if (mc.crosshairTarget instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
            pendingPos = b.getBlockPos().offset(b.getSide());
            pendingTicks = 2;
        }
    }

    private void beginManual(BlockPos pos) {
        spots.clear();
        spots.add(new Placement(pos, null));
        steps.clear();
        steps.add(new Step(Kind.CHARGE, 0));
        steps.add(new Step(Kind.EXPLODE, 0));
        prevSlot = SlotUtil.selected();
        timer = 0;
        active = true;
    }

    private void begin(PlayerEntity target) {
        int n = doubleAnchor.get() ? 2 : 1;
        n = Math.min(n, Math.min(count(Items.RESPAWN_ANCHOR), count(Items.GLOWSTONE)));
        if (n <= 0) { status("Put a Respawn Anchor AND Glowstone in your hotbar"); return; }

        spots.clear();
        spots.addAll(findPlacements(target, n));
        if (spots.isEmpty()) { status("No valid anchor spot next to the target (out of reach?)"); return; }

        steps.clear();
        for (int i = 0; i < spots.size(); i++) { steps.add(new Step(Kind.PLACE, i)); steps.add(new Step(Kind.CHARGE, i)); }
        for (int i = 0; i < spots.size(); i++) steps.add(new Step(Kind.EXPLODE, i));
        prevSlot = SlotUtil.selected();
        timer = 0;
        active = true;
    }

    private void run() {
        for (int guard = 0; guard < 12 && !steps.isEmpty(); guard++) {
            if (timer > 0) { timer--; return; }
            if (!exec(steps.peek())) { finish(); return; }
            steps.poll();
            timer = stepDelay.getInt();
            if (timer > 0) return;
        }
        if (steps.isEmpty()) finish();
    }

    private boolean exec(Step s) {
        Placement p = spots.get(s.idx());
        switch (s.kind()) {
            case PLACE -> {
                int slot = SlotUtil.find(st -> st.isOf(Items.RESPAWN_ANCHOR));
                if (slot < 0) return false;
                SlotUtil.swap(slot);
                ActionResult r = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, p.hit());
                if (r.isAccepted()) mc.player.swingHand(Hand.MAIN_HAND);
                return r.isAccepted();
            }
            case CHARGE -> {
                int slot = SlotUtil.find(st -> st.isOf(Items.GLOWSTONE));
                if (slot < 0) return false;
                SlotUtil.swap(slot);
                return mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, anchorHit(p)).isAccepted();
            }
            default -> {
                int slot = safeSlot();
                if (slot < 0) return false;
                SlotUtil.swap(slot); // non-glowstone item in hand -> charged anchor explodes
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, anchorHit(p));
                return true;
            }
        }
    }

    private BlockHitResult anchorHit(Placement p) {
        return new BlockHitResult(Vec3d.ofCenter(p.pos()), Direction.UP, p.pos(), false);
    }

    private int safeSlot() {
        if (prevSlot >= 0 && !isGlow(prevSlot)) return prevSlot;
        for (int i = 0; i < 9; i++) if (!isGlow(i)) return i;
        return -1;
    }

    private boolean isGlow(int slot) { return mc.player.getInventory().getStack(slot).isOf(Items.GLOWSTONE); }

    private void finish() {
        if (swapBack.get() && prevSlot >= 0 && mc.player != null) SlotUtil.swap(prevSlot);
        prevSlot = -1; active = false; steps.clear(); spots.clear();
        cooldownLeft = cooldown.getInt();
    }

    /** Free blocks around the target that we can click against. Works in open ground (uses the floor). */
    private List<Placement> findPlacements(PlayerEntity t, int n) {
        BlockPos feet = t.getBlockPos();
        List<BlockPos> cand = new ArrayList<>();
        for (Direction d : Direction.Type.HORIZONTAL) cand.add(feet.offset(d));           // beside feet (floor support)
        cand.add(feet.up(2));                                                             // above head
        for (Direction d : Direction.Type.HORIZONTAL) cand.add(feet.up(1).offset(d));     // beside head

        Vec3d eye = mc.player.getEyePos();
        List<Placement> out = new ArrayList<>();
        for (BlockPos c : cand) {
            if (out.size() >= n) break;
            if (!mc.world.getBlockState(c).isReplaceable()) continue;
            if (eye.squaredDistanceTo(Vec3d.ofCenter(c)) > 4.5 * 4.5) continue;
            if (!mc.world.getOtherEntities(null, new Box(c), e -> !e.isSpectator()).isEmpty()) continue;
            BlockHitResult hit = support(c);
            if (hit != null) out.add(new Placement(c, hit));
        }
        return out;
    }

    private BlockHitResult support(BlockPos c) {
        for (Direction d : Direction.values()) {
            BlockPos n = c.offset(d);
            var st = mc.world.getBlockState(n);
            if (st.isReplaceable() || !st.isSolidBlock(mc.world, n)) continue;
            Direction face = d.getOpposite();
            Vec3d hitPos = Vec3d.ofCenter(n).add(Vec3d.of(face.getVector()).multiply(0.5));
            return new BlockHitResult(hitPos, face, n, false);
        }
        return null;
    }
}
