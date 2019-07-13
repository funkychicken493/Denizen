package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.objects.notable.NotableManager;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.notable.Notable;
import com.denizenscript.denizencore.objects.notable.Note;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class dEllipsoid implements dObject, Notable {

    // <--[language]
    // @name dEllipsoid
    // @group Object System
    // @description
    // A dEllipsoid represents an ellipsoidal region in the world.
    //
    // The word 'ellipsoid' means a less strict sphere.
    // Basically: an "ellipsoid" is to a 3D "sphere" what an "ellipse" (or "oval") is to a 2D "circle".
    //
    // For format info, see <@link language ellipsoid@>
    //
    // -->

    // <--[language]
    // @name ellipsoid@
    // @group Object Fetcher System
    // @description
    // ellipsoid@ refers to the 'object identifier' of a dEllipsoid. The 'ellipsoid@' is notation for Denizen's Object
    // Fetcher. The constructor for a dEllipsoid is <x>,<y>,<z>,<world>,<x-radius>,<y-radius>,<z-radius>
    // For example, 'ellipsoid@1,2,3,space,7,7,7'.
    //
    // For general info, see <@link language dEllipsoid>
    //
    // -->

    public static List<dEllipsoid> getNotableEllipsoidsContaining(Location location) {
        List<dEllipsoid> cuboids = new ArrayList<>();
        for (dEllipsoid ellipsoid : NotableManager.getAllType(dEllipsoid.class)) {
            if (ellipsoid.contains(location)) {
                cuboids.add(ellipsoid);
            }
        }

        return cuboids;
    }

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dEllipsoid valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets an Ellipsoid Object from a string form.
     *
     * @param string the string
     */
    @Fetchable("ellipsoid")
    public static dEllipsoid valueOf(String string, TagContext context) {

        if (string.startsWith("ellipsoid@")) {
            string = string.substring(10);
        }

        if (NotableManager.isType(string, dEllipsoid.class)) {
            return (dEllipsoid) NotableManager.getSavedObject(string);
        }

        List<String> split = CoreUtilities.split(string, ',');

        if (split.size() != 7) {
            return null;
        }

        dWorld world = dWorld.valueOf(split.get(3), false);
        if (world == null) {
            return null;
        }

        dLocation location = new dLocation(world.getWorld(),
                aH.getDoubleFrom(split.get(0)), aH.getDoubleFrom(split.get(1)), aH.getDoubleFrom(split.get(2)));
        dLocation size = new dLocation(null, aH.getDoubleFrom(split.get(4)),
                aH.getDoubleFrom(split.get(5)), aH.getDoubleFrom(split.get(6)));
        return new dEllipsoid(location, size);
    }

    /**
     * Determines whether a string is a valid ellipsoid.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {

        try {
            return dEllipsoid.valueOf(arg) != null;
        }
        catch (Exception e) {
            return false;
        }
    }


    ///////////////
    //   Constructors
    /////////////

    public dEllipsoid(dLocation loc, dLocation size) {
        this.loc = loc;
        this.size = size;
    }

    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private dLocation loc;

    private dLocation size;

    public dList getBlocks() {
        return getBlocks(null);
    }

    public dList getBlocks(List<dMaterial> materials) {
        List<dLocation> initial = new dCuboid(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlocks_internal(materials);
        dList list = new dList();
        for (dLocation loc : initial) {
            if (contains(loc)) {
                list.add(loc.identify());
            }
        }
        return list;
    }

    public List<dLocation> getBlockLocations() {
        List<dLocation> initial = new dCuboid(new Location(loc.getWorld(),
                loc.getX() - size.getX(), loc.getY() - size.getY(), loc.getZ() - size.getZ()),
                new Location(loc.getWorld(),
                        loc.getX() + size.getX(), loc.getY() + size.getY(), loc.getZ() + size.getZ()))
                .getBlocks_internal(null);
        List<dLocation> locations = new ArrayList<>();
        for (dLocation loc : initial) {
            if (contains(loc)) {
                locations.add(loc);
            }
        }
        return locations;
    }

    public boolean contains(Location test) {
        double xbase = test.getX() - loc.getX();
        double ybase = test.getY() - loc.getY();
        double zbase = test.getZ() - loc.getZ();
        return ((xbase * xbase) / (size.getX() * size.getX())
                + (ybase * ybase) / (size.getY() * size.getY())
                + (zbase * zbase) / (size.getZ() * size.getZ()) < 1);
    }

    String prefix = "ellipsoid";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String debuggable() {
        if (isUnique()) {
            return "cu@" + NotableManager.getSavedId(this) + " (" + identifyFull() + ")";
        }
        else {
            return identifyFull();
        }
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    @Note("Ellipsoids")
    public Object getSaveObject() {
        return identifyFull().substring(10);
    }

    @Override
    public void makeUnique(String id) {
        NotableManager.saveAs(this, id);
    }

    @Override
    public void forget() {
        NotableManager.remove(this);
    }

    @Override
    public String getObjectType() {
        return "Ellipsoid";
    }

    @Override
    public String identify() {
        if (isUnique()) {
            return "ellipsoid@" + NotableManager.getSavedId(this);
        }
        else {
            return identifyFull();
        }
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    public String identifyFull() {
        return "ellipsoid@" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getWorld().getName()
                + "," + size.getX() + "," + size.getY() + "," + size.getZ();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.blocks[<material>|...]>
        // @returns dList(dLocation)
        // @description
        // Returns each block location within the dEllipsoid.
        // Optionally, specify a list of materials to only return locations
        // with that block type.
        // -->
        registerTag("blocks", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                if (attribute.hasContext(1)) {
                    return new dList(((dEllipsoid) object).getBlocks(dList.valueOf(attribute.getContext(1)).filter(dMaterial.class, attribute.context)))
                            .getAttribute(attribute.fulfill(1));
                }
                else {
                    return new dList(((dEllipsoid) object).getBlocks())
                            .getAttribute(attribute.fulfill(1));
                }
            }
        });
        registerTag("get_blocks", registeredTags.get("blocks"));

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.location>
        // @returns dLocation
        // @description
        // Returns the location of the ellipsoid.
        // -->
        registerTag("location", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return ((dEllipsoid) object).loc.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.size>
        // @returns dLocation
        // @description
        // Returns the size of the ellipsoid.
        // -->
        registerTag("size", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return ((dEllipsoid) object).size.getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <ellipsoid@ellipsoid.type>
        // @returns Element
        // @description
        // Always returns 'Ellipsoid' for dEllipsoid objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Ellipsoid").getAttribute(attribute.fulfill(1));
            }
        });
    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                dB.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        String returned = CoreUtilities.autoPropertyTag(this, attribute);
        if (returned != null) {
            return returned;
        }

        return new Element(identify()).getAttribute(attribute);
    }

}