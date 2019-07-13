package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUnbreakable implements Property {

    public static boolean describes(dObject object) {
        return object instanceof dItem;
    }

    public static ItemUnbreakable getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        return new ItemUnbreakable((dItem) object);
    }

    public static final String[] handledTags = new String[] {
            "unbreakable"
    };

    public static final String[] handledMechs = new String[] {
            "unbreakable"
    };


    private ItemUnbreakable(dItem item) {
        this.item = item;
    }

    dItem item;

    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.unbreakable>
        // @returns Element(Boolean)
        // @group properties
        // @mechanism dItem.unbreakable
        // @description
        // Returns whether an item has the unbreakable flag.
        // -->
        if (attribute.startsWith("unbreakable")) {
            return new Element(getPropertyString() != null).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public String getPropertyString() {
        ItemStack itemStack = item.getItemStack();
        return (itemStack.hasItemMeta() && itemStack.getItemMeta().isUnbreakable()) ? "true" : null;
    }

    public String getPropertyId() {
        return "unbreakable";
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name unbreakable
        // @input Element(Boolean)
        // @description
        // Changes whether an item has the unbreakable item flag.
        // @tags
        // <i@item.unbreakable>
        // -->
        if (mechanism.matches("unbreakable") && mechanism.requireBoolean()) {
            ItemStack itemStack = item.getItemStack().clone();
            ItemMeta meta = itemStack.getItemMeta();
            meta.setUnbreakable(mechanism.getValue().asBoolean());
            itemStack.setItemMeta(meta);
            item.setItemStack(itemStack);
        }
    }
}