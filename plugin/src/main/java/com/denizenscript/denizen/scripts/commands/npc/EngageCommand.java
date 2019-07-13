package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.Settings;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;

import java.util.HashMap;
import java.util.Map;

public class EngageCommand extends AbstractCommand {

    // <--[command]
    // @Name Engage
    // @Syntax engage (<duration>)
    // @Required 0
    // @Plugin Citizens
    // @Short Temporarily disables an NPCs toggled interact script-container triggers.
    // @Group npc
    //
    // @Description
    // Engaging an NPC will temporarily disable any interact script-container triggers. To reverse
    // this behavior, use either the disengage command, or specify a duration in which the engage
    // should timeout. Specifying an engage without a duration will render the NPC engaged until
    // a disengage is used on the NPC. Engaging an NPC affects all players attempting to interact
    // with the NPC.
    //
    // While engaged, all triggers and actions associated with triggers will not 'fire', except
    // the 'on unavailable' assignment script-container action, which will fire for triggers that
    // were enabled previous to the engage command.
    //
    // Engage can be useful when NPCs are carrying out a task that shouldn't be interrupted, or
    // to provide a good way to avoid accidental 'retrigger'.
    //
    // See <@link command Disengage>
    //
    // @Tags
    // <n@npc.is_engaged>
    //
    // @Usage
    // Use to make an NPC appear 'busy'.
    // - engage
    // - chat 'Give me a few minutes while I mix you a potion!'
    // - walk <npc.anchor[mixing_station]>
    // - wait 10s
    // - walk <npc.anchor[service_station]>
    // - chat 'Here you go!'
    // - give potion <player>
    // - disengage
    //
    // @Usage
    // Use to avoid 'retrigger'.
    // - engage 5s
    // - take quest_item
    // - flag player finished_quests:->:super_quest
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Check for NPC
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires a linked NPC!");
        }

        // Parse arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else {
                arg.reportUnhandled();
            }

        }

        scriptEntry.defaultObject("duration", new Duration(0));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Duration duration = scriptEntry.getdObject("duration");
        dNPC npc = Utilities.getEntryNPC(scriptEntry);

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), npc.debug() + duration.debug());
        }

        if (duration.getSecondsAsInt() > 0) {
            setEngaged(npc.getCitizen(), duration.getSecondsAsInt());
        }
        else {
            setEngaged(npc.getCitizen(), true);
        }

    }

    /*
     * Engaged NPCs cannot interact with Players
     */
    private static Map<NPC, Long> currentlyEngaged = new HashMap<>();

    /**
     * Checks if the dNPC is ENGAGED. Engaged NPCs do not respond to
     * Player interaction.
     *
     * @param npc the Denizen NPC being checked
     * @return if the dNPC is currently engaged
     */
    public static boolean getEngaged(NPC npc) {
        if (currentlyEngaged.containsKey(npc)) {
            if (currentlyEngaged.get(npc) > System.currentTimeMillis()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a dNPC's ENGAGED status. Engaged NPCs do not respond to Player
     * interaction. Note: Denizen NPC will automatically disengage after the
     * engage_timeout_in_seconds which is set in the Denizen config.yml.
     *
     * @param npc     the dNPC affected
     * @param engaged true sets the dNPC engaged, false sets the dNPC as disengaged
     */
    public static void setEngaged(NPC npc, boolean engaged) {
        if (engaged) {
            currentlyEngaged.put(npc, System.currentTimeMillis()
                    + (long) (Duration.valueOf(Settings.engageTimeoutInSeconds()).getSeconds()) * 1000);
        }
        if (!engaged) {
            currentlyEngaged.remove(npc);
        }
    }

    /**
     * Sets a dNPC as ENGAGED for a specific amount of seconds. Engaged NPCs do not
     * respond to Player interaction. If the NPC is previously engaged, using this will
     * over-ride the previously set duration.
     *
     * @param npc      the dNPC to set as engaged
     * @param duration the number of seconds to engage the dNPC
     */
    public static void setEngaged(NPC npc, int duration) {
        currentlyEngaged.put(npc, System.currentTimeMillis() + duration * 1000);
    }
}