package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;

/** Scales the knockback you take (hooked from ClientPlayNetworkHandlerMixin). */
public final class VelocityModule extends Module {
    private static VelocityModule instance;
    private final NumberSetting horizontal = add(new NumberSetting("Horizontal %", 0, 100, 1, 80));
    private final NumberSetting vertical   = add(new NumberSetting("Vertical %", 0, 100, 1, 100));

    public VelocityModule() { super("Velocity", "Reduces knockback", Category.COMBAT); instance = this; }

    public static void apply(EntityVelocityUpdateS2CPacket p) {
        if (instance == null || !instance.isEnabled() || mc.player == null || p.getEntityId() != mc.player.getId()) return;
        Vec3d v = mc.player.getVelocity();
        double h = instance.horizontal.get() / 100.0, y = instance.vertical.get() / 100.0;
        mc.player.setVelocity(v.x * h, v.y * y, v.z * h);
    }
}
