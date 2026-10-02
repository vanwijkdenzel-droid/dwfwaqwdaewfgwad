package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.util.PacketUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.hit.EntityHitResult;

/** Resets your sprint on each hit so every hit deals full knockback. */
public final class WTapModule extends Module {
    public WTapModule() { super("WTap", "Sprint reset on hit", Category.COMBAT); }

    @Override public void onLeftClick() {
        if (mc.player == null || !mc.player.isSprinting() || !(mc.crosshairTarget instanceof EntityHitResult)) return;
        PacketUtil.send(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        PacketUtil.send(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
    }
}
