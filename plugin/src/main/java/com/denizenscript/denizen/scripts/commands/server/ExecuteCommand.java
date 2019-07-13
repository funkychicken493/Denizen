package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.DenizenCommandSender;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.PlayerHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecuteCommand extends AbstractCommand {

    // <--[command]
    // @Name Execute
    // @Syntax execute [as_player/as_op/as_npc/as_server] [<Bukkit command>] (silent)
    // @Required 2
    // @Short Executes an arbitrary server command as if the player, NPC, or server typed it in.
    // @Group server
    //
    // @Description
    // Allows the execution of server commands through a Denizen Script. Commands can be executed as the server,
    // as an npc, an op or as a player, as though it was typed by the respective source.
    //
    // @Tags
    // <entry[saveName].output> returns the output to an as_server sender.
    //
    // @Usage
    // Use to execute the save-all command as the server.
    // - execute as_server "save-all"
    //
    // @Usage
    // Use to make the linked (non-op) player execute a command that normally only ops can use.
    // - execute as_op "staffsay hi"
    // -->

    enum Type {AS_SERVER, AS_NPC, AS_PLAYER, AS_OP}

    public DenizenCommandSender dcs = new DenizenCommandSender();
    public static final List<UUID> silencedPlayers = new ArrayList<>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matches("ASPLAYER", "AS_PLAYER", "PLAYER")
                    && !scriptEntry.hasObject("type")) {
                if (!Utilities.entryHasPlayer(scriptEntry)) {
                    throw new InvalidArgumentsException("Must have a Player link when using AS_PLAYER.");
                }
                scriptEntry.addObject("type", new Element("AS_PLAYER"));
            }
            else if (arg.matches("ASOPPLAYER", "ASOP", "AS_OP", "AS_OP_PLAYER", "OP")
                    && !scriptEntry.hasObject("type")) {
                if (!Utilities.entryHasPlayer(scriptEntry)) {
                    throw new InvalidArgumentsException("Must have a Player link when using AS_OP.");
                }
                scriptEntry.addObject("type", new Element("AS_OP"));
            }
            else if (arg.matches("ASNPC", "AS_NPC", "NPC")
                    && !scriptEntry.hasObject("type")) {
                if (!Utilities.entryHasNPC(scriptEntry)) {
                    throw new InvalidArgumentsException("Must have a NPC link when using AS_NPC.");
                }
                scriptEntry.addObject("type", new Element("AS_NPC"));
            }
            else if (arg.matches("ASSERVER", "AS_SERVER", "SERVER")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", new Element("AS_SERVER"));
            }
            else if (!scriptEntry.hasObject("silent")
                    && arg.matches("silent")) {
                scriptEntry.addObject("silent", new Element("true"));
            }
            else if (!scriptEntry.hasObject("command")) {
                scriptEntry.addObject("command", new Element(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("type")) {
            throw new InvalidArgumentsException("Missing execution type!");
        }

        if (!scriptEntry.hasObject("command")) {
            throw new InvalidArgumentsException("Missing command text!");
        }

        scriptEntry.defaultObject("silent", new Element("false"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element cmd = scriptEntry.getElement("command");
        Element type = scriptEntry.getElement("type");
        Element silent = scriptEntry.getElement("silent");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    type.debug()
                            + cmd.debug()
                            + silent.debug());
        }

        String command = cmd.asString();

        switch (Type.valueOf(type.asString())) {

            case AS_PLAYER:
                try {
                    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity(), "/" + command);
                    Bukkit.getPluginManager().callEvent(pcpe);
                    if (!pcpe.isCancelled()) {
                        boolean silentBool = silent.asBoolean();
                        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                        if (silentBool) {
                            silencedPlayers.add(player.getUniqueId());
                        }
                        player.performCommand(pcpe.getMessage().startsWith("/") ?
                                pcpe.getMessage().substring(1) : pcpe.getMessage());
                        if (silentBool) {
                            silencedPlayers.remove(player.getUniqueId());
                        }
                    }
                }
                catch (Throwable e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as player.");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }
                break;

            case AS_OP:
                if (CoreUtilities.toLowerCase(command).equals("stop")) {
                    dB.echoError("Please use as_server to execute 'stop'.");
                    return;
                }
                Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                PlayerHelper playerHelper = NMSHandler.getInstance().getPlayerHelper();
                boolean isOp = player.isOp();
                if (!isOp) {
                    playerHelper.setTemporaryOp(player, true);
                }
                try {
                    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(player, "/" + command);
                    Bukkit.getPluginManager().callEvent(pcpe);
                    if (!pcpe.isCancelled()) {
                        boolean silentBool = silent.asBoolean();
                        if (silentBool) {
                            silencedPlayers.add(player.getUniqueId());
                        }
                        player.performCommand(pcpe.getMessage().startsWith("/") ?
                                pcpe.getMessage().substring(1) : pcpe.getMessage());
                        if (silentBool) {
                            silencedPlayers.remove(player.getUniqueId());
                        }
                    }
                }
                catch (Throwable e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as OP.");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }
                if (!isOp) {
                    playerHelper.setTemporaryOp(player, false);
                }
                break;

            case AS_NPC:
                if (!Utilities.getEntryNPC(scriptEntry).isSpawned()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cannot EXECUTE AS_NPC unless the NPC is Spawned.");
                    return;
                }
                if (Utilities.getEntryNPC(scriptEntry).getEntity().getType() != EntityType.PLAYER) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cannot EXECUTE AS_NPC unless the NPC is Player-Type.");
                    return;
                }
                ((Player) Utilities.getEntryNPC(scriptEntry).getEntity()).setOp(true);
                try {
                    ((Player) Utilities.getEntryNPC(scriptEntry).getEntity()).performCommand(command);
                }
                catch (Throwable e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as NPC-OP.");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }
                ((Player) Utilities.getEntryNPC(scriptEntry).getEntity()).setOp(false);
                break;

            case AS_SERVER:
                dcs.clearOutput();
                dcs.silent = silent.asBoolean();
                ServerCommandEvent sce = new ServerCommandEvent(dcs, command);
                Bukkit.getPluginManager().callEvent(sce);
                DenizenAPI.getCurrentInstance().getServer().dispatchCommand(dcs, sce.getCommand());
                scriptEntry.addObject("output", new dList(dcs.getOutput()));
                break;
        }
    }
}