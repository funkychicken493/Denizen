package net.aufdemrand.denizen.objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.utilities.Utilities;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import net.aufdemrand.denizen.tags.Attribute;

public class dMaterial implements dObject {

    final static Pattern materialPattern = Pattern.compile("(\\w+):?(\\d+)?");
	
    //////////////////
    //    OBJECT FETCHER
    ////////////////
    
    /**
     * Gets a Material Object from a string form.
     *
     * @param string  the string
     * @return  a Material, or null if incorrectly formatted
     *
     */
    public static dMaterial valueOf(String string) {
    	
    	if (string.toUpperCase().matches("RANDOM")) {
    		
    		// Get a random material
    		return new dMaterial(Material.values()[Utilities.getRandom().nextInt(Material.values().length)]);
    	}
    	
    	Matcher m = materialPattern.matcher(string);
    	
    	if (m.matches()) {
    		
            int data = 0;
    		
    		if (m.group(2) != null) {
    			
    			data = aH.getIntegerFrom(m.group(2));
    		}
    		
    		if (aH.matchesInteger(m.group(1))) {
    			
    			return new dMaterial(aH.getIntegerFrom(m.group(1)), data);		
    		}
    		else {
    			
    			for (Material material : Material.values()) {
    				
    				if (material.name().equalsIgnoreCase(m.group(1))) {
    					
    					return new dMaterial(material, data);
    				}
    			}
    		}
    	}
    			
        // No match
        return null;
    }
    
    /**
     * Determine whether a string is a valid material.
     *
     * @param string  the string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {

    	if (arg.toUpperCase().matches("RANDOM"))
    		return true;
    	
    	Matcher m = materialPattern.matcher(arg);
    	
    	if (m.matches())
    		return true;
    	
        return false;

    }
    
    
    ///////////////
    //   Constructors
    /////////////

    public dMaterial(Material material) {
        this.material = material;
    }
    
    public dMaterial(Material material, int data) {
        this.material = material;
        this.data = (byte) data;
    }
    
    public dMaterial(int id) {
        this.material = Material.getMaterial(id);
    }
    
    public dMaterial(int id, int data) {
        this.material = Material.getMaterial(id);
        this.data = (byte) data;
    }
    
    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Associated with Bukkit Material

    private Material material;
    private byte data = 0;

    public Material getMaterial() {
        return material;
    }
    
    public MaterialData getMaterialData() {
        return new MaterialData(material, data);
    }
    

	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public String debug() {
		return null;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public String getType() {
		return "dMaterial";
	}

	@Override
	public String identify() {
		return null;
	}

	@Override
	public dObject setPrefix(String prefix) {
		return null;
	}

	@Override
	public String getAttribute(Attribute attribute) {
		return null;
	}

}
