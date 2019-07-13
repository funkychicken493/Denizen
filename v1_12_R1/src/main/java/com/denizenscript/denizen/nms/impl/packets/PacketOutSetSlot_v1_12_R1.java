package com.denizenscript.denizen.nms.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSetSlot;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetSlot;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class PacketOutSetSlot_v1_12_R1 implements PacketOutSetSlot {

    private PacketPlayOutSetSlot internal;
    private org.bukkit.inventory.ItemStack itemStack;

    public PacketOutSetSlot_v1_12_R1(PacketPlayOutSetSlot internal) {
        this.internal = internal;
        try {
            ItemStack nms = (ItemStack) ITEM_STACK.get(internal);
            itemStack = CraftItemStack.asBukkitCopy(nms);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void setItemStack(org.bukkit.inventory.ItemStack itemStack) {
        try {
            ITEM_STACK.set(internal, CraftItemStack.asNMSCopy(itemStack));
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    private static final Field ITEM_STACK;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutSetSlot.class);
        ITEM_STACK = fields.get("c");
    }
}