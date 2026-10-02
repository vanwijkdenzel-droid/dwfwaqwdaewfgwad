package dev.cpvp.module.impl;

import dev.cpvp.module.Category;
import dev.cpvp.module.Module;
import dev.cpvp.setting.NumberSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.List;

/** Holds your movement packets for N ticks, then releases them all at once (ClientPlayNetworkHandlerMixin). */
public final class FakeLagModule extends Module {
    private static boolean active, flushing, blink;
    private static final List<Packet<?>> QUEUE = new ArrayList<>();

    private final NumberSetting delay = add(new NumberSetting("Delay (ticks)", 1, 20, 1, 4));
    private int ticks;

    public FakeLagModule() { super("FakeLag", "Delays your movement packets in bursts", Category.NETWORK); }

    /** Returns true if the packet was captured (and must not be sent now). */
    public static boolean intercept(Packet<?> p) {
        if ((!active && !blink) || flushing || !(p instanceof PlayerMoveC2SPacket)) return false;
        QUEUE.add(p);
        return true;
    }

    public static void setBlink(boolean b) { blink = b; if (!b) flush(); }

    private static void flush() {
        var h = net.minecraft.client.MinecraftClient.getInstance().getNetworkHandler();
        flushing = true;
        try { if (h != null) for (Packet<?> p : QUEUE) h.sendPacket(p); }
        finally { flushing = false; QUEUE.clear(); }
    }

    @Override public void onEnable()  { active = true; ticks = 0; }
    @Override public void onDisable() { active = false; flush(); }

    @Override public void onTick() {
        if (++ticks >= delay.getInt()) { flush(); ticks = 0; }
    }
}
