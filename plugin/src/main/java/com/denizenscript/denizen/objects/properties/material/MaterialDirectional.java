package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

public class MaterialDirectional implements Property {

    public static boolean describes(dObject material) {
        return material instanceof dMaterial
                && ((dMaterial) material).hasModernData()
                && ((dMaterial) material).getModernData().data instanceof Directional;
    }

    public static MaterialDirectional getFrom(dObject _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDirectional((dMaterial) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "direction", "valid_directions"
    };

    public static final String[] handledMechs = new String[] {
            "direction"
    };


    private MaterialDirectional(dMaterial _material) {
        material = _material;
    }

    dMaterial material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <m@material.valid_directions>
        // @returns Element
        // @mechanism dMaterial.direction
        // @group properties
        // @description
        // Returns a list of directions that are valid for a directional material.
        // See also <@link tag m@matereial.direction>
        // -->
        if (attribute.startsWith("valid_directions")) {
            dList toReturn = new dList();
            for (BlockFace face : getDirectional().getFaces()) {
                toReturn.add(face.name());
            }
            return toReturn.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <m@material.direction>
        // @returns Element
        // @mechanism dMaterial.direction
        // @group properties
        // @description
        // Returns the current facing direction for a directional material (like a door or a bed).
        // Output is a direction name like "NORTH".
        // -->
        if (attribute.startsWith("direction")) {
            return new Element(getDirectional().getFacing().name()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Directional getDirectional() {
        return (Directional) material.getModernData().data;
    }

    public BlockFace getDirection() {
        return getDirectional().getFacing();
    }

    @Override
    public String getPropertyString() {
        return getDirectional().getFacing().name();
    }

    @Override
    public String getPropertyId() {
        return "direction";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dMaterial
        // @name direction
        // @input Element
        // @description
        // Sets the current facing direction for a directional material (like a door or a bed).
        // @tags
        // <m@material.direction>
        // <m@material.valid_directions>
        // -->
        if (mechanism.matches("direction") && mechanism.requireEnum(false, BlockFace.values())) {
            getDirectional().setFacing(BlockFace.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}