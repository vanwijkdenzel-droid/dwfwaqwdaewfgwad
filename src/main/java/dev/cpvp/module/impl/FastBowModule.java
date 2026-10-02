package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.PacketUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class FastBowModule extends Module {
    private final NumberSetting charge = add(new NumberSetting("Charge Ticks", 3, 20, 1, 3));

    public FastBowModule() { super("FastBow", "Release arrows early and re-draw", Category.COMBAT); }

    @Override public void onTick() {
        if (!mc.player.getMainHandStack().isOf(Items.BOW) || !mc.options.useKey.isPressed()) return;
        if (mc.player.isUsingItem() && mc.player.getItemUseTime() >= charge.getInt()) {
            PacketUtil.send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
            mc.player.stopUsingItem();
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
