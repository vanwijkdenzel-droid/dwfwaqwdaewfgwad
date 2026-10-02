package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.util.PacketUtil;
import net.minecraft.entity.projectile.FireballEntity;

public final class AntiFireballModule extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 2, 5, 0.1, 4.0));

    public AntiFireballModule() { super("AntiFireball", "Punches incoming fireballs", Category.COMBAT); }

    @Override public void onTick() {
        double r = range.get();
        for (FireballEntity f : mc.world.getEntitiesByClass(FireballEntity.class, mc.player.getBoundingBox().expand(r + 1), e -> e.isAlive())) {
            if (mc.player.distanceTo(f) > r) continue;
            PacketUtil.attack(f);
            PacketUtil.swing();
            return;
        }
    }
}
