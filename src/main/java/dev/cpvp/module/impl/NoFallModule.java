package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.util.PacketUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class NoFallModule extends Module {
    public NoFallModule() { super("NoFall", "Cancel fall damage", Category.UTILITY); }

    @Override public void onTick() {
        if (mc.player.fallDistance > 2.5f && !mc.player.isOnGround())
            PacketUtil.send(new PlayerMoveC2SPacket.OnGroundOnly(true));
    }
}
