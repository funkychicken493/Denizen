package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Arrays;
import java.util.List;

public class TeleportCommand extends AbstractCommand {

    // <--[command]
    // @Name Teleport
    // @Syntax teleport (<entity>|...) [<location>]
    // @Required 1
    // @Short Teleports the entity(s) to a new location.
    // @Group entity
    //
    // @Description
    // Teleports the entity or entities to the new location. Entities can be teleported between worlds using this
    // command, assuming the location is valid.
    //
    // @Tags
    // <e@entity.location>
    //
    // @Usage
    // Use to teleport a player to the location its cursor is pointing on
    // - teleport <player> <player.location.cursor_on>
    //
    // @Usage
    // Use to teleport a player high above
    // - teleport <player> <player.location.add[0,200,0]>
    //
    // @Usage
    // Use to teleport to a random online player
    // - teleport <player> <server.list_online_players.random.location>
    //
    // @Usage
    // Use to teleport all players to your location
    // - teleport <server.list_online_players> <player.location>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }

            // NPC arg for compatibility with old scripts
            else if (arg.matches("npc") && Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("entities", Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        // Use player or NPC as default entity
        if (!scriptEntry.hasObject("entities")) {
            scriptEntry.defaultObject("entities", (Utilities.entryHasPlayer(scriptEntry) ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getDenizenEntity()) : null),
                    (Utilities.entryHasNPC(scriptEntry) ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getDenizenEntity()) : null));
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {
        // Get objects

        dLocation location = (dLocation) scriptEntry.getObject("location");
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), aH.debugObj("location", location) +
                    aH.debugObj("entities", entities.toString()));
        }

        for (dEntity entity : entities) {
            // Call a Bukkit event for compatibility with "on entity teleports"
            // world event and other plugins
            if (entity.isSpawned()) {
                if (entity.getBukkitEntityType() != EntityType.PLAYER) {
                    Bukkit.getPluginManager().callEvent(new EntityTeleportEvent(entity.getBukkitEntity(), entity.getLocation(), location));
                }
                else {
                    Bukkit.getPluginManager().callEvent(new PlayerTeleportEvent((Player) entity.getBukkitEntity(), entity.getLocation(), location));
                }
            }
            entity.spawnAt(location);
        }
    }
}