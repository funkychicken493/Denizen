package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.npc.traits.PushableTrait;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class PushableCommand extends AbstractCommand {

    // <--[command]
    // @Name Pushable
    // @Syntax pushable (state:true/false/{toggle}) (delay:<duration>) (returnable:true/false)
    // @Required 0
    // @Short Edits the pushable trait for NPCs.
    // @Group npc
    //
    // @Description
    // Enables, disables, toggles, or edits the Pushable trait on the attached NPC.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to toggle the Pushable trait for a specified NPC.
    // - pushable npc:n@321
    //
    // @Usage
    // Use to enable the Pushable trait and return after 2 seconds.
    // - pushable state:true delay:2s returnable:true
    // -->

    private enum Toggle {TOGGLE, TRUE, FALSE, ON, OFF}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("state")
                    && arg.matchesPrefix("state", "s")
                    && arg.matchesEnum(Toggle.values())) {
                scriptEntry.addObject("state", arg.asElement());
            }
            else if (!scriptEntry.hasObject("delay")
                    && arg.matchesPrefix("delay", "d")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("delay", arg.asType(Duration.class));
            }
            else if (!scriptEntry.hasObject("return")
                    && arg.matchesPrefix("return", "r")
                    && arg.matchesPrimitive(aH.PrimitiveType.Boolean)) {
                scriptEntry.addObject("return", arg.asElement());
            }

        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        dNPC denizenNPC = Utilities.getEntryNPC(scriptEntry);
        if (denizenNPC == null) {
            dB.echoError("No valid NPC attached to this queue!");
            return;
        }
        PushableTrait trait = denizenNPC.getPushableTrait();

        Element state = scriptEntry.getElement("state");
        Duration delay = scriptEntry.getdObject("delay");
        Element returnable = scriptEntry.getElement("return");

        if (state == null && delay == null && returnable == null) {
            state = new Element("TOGGLE");
        }

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    (state != null ? state.debug() : "") +
                            (delay != null ? delay.debug() : "") +
                            (returnable != null ? returnable.debug() : ""));

        }

        if (delay != null) {
            trait.setDelay(delay.getSecondsAsInt());
        }

        if (returnable != null) {
            trait.setReturnable(returnable.asBoolean());
        }

        if (state != null) {
            switch (Toggle.valueOf(state.asString().toUpperCase())) {

                case TRUE:
                case ON:
                    trait.setPushable(true);
                    break;

                case FALSE:
                case OFF:
                    trait.setPushable(false);
                    break;

                case TOGGLE:
                    trait.setPushable(!trait.isPushable());
                    break;

            }
        }
    }
}