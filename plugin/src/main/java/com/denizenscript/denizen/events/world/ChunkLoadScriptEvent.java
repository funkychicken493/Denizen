package com.denizenscript.denizen.events.world;

import com.denizenscript.denizen.objects.ChunkTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // chunk loads (for the first time)
    //
    // @Regex ^on chunk loads( for the first time)?$
    //
    // @Group World
    //
    // @Switch in:<area> to only process the event if it occurred within a specified area.
    //
    // @Warning This event will fire *extremely* rapidly and often when using 'for the first time'.
    // When not using that, it will fire so rapidly that lag is almost guaranteed. Use with maximum caution.
    //
    // @Triggers when a new chunk is loaded
    //
    // @Context
    // <context.chunk> returns the loading chunk.
    //
    // -->

    public ChunkLoadScriptEvent() {
        instance = this;
    }

    public static ChunkLoadScriptEvent instance;

    public ChunkTag chunk;
    public ChunkLoadEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("chunk loads");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("for") && !event.isNewChunk()) {
            return false;
        }
        if (!runInCheck(path, chunk.getCenter())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "ChunkLoads";
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("chunk")) {
            return chunk;
        }
        else if (name.equals("world")) {
            Deprecations.worldContext.warn();
            return new WorldTag(event.getWorld());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        chunk = new ChunkTag(event.getChunk());
        this.event = event;
        fire(event);
    }
}
