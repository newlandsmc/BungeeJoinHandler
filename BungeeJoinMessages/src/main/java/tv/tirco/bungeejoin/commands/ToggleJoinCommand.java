package tv.tirco.bungeejoin.commands;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import tv.tirco.bungeejoin.ConfigSettings;
import tv.tirco.bungeejoin.Main;
import tv.tirco.bungeejoin.util.HexChat;

import java.util.List;

public class ToggleJoinCommand extends Command implements TabExecutor {

    List<String> commandArguments = ImmutableList.of("join", "leave", "quit", "switch", "all");

    public ToggleJoinCommand() {
        super("togglejoinmessage", "bungeejoinmessages.togglemessage", "bjointoggle");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            if (!player.hasPermission("bungeejoinmessages.togglemessage")) {
                String msg = Main.getInstance().getConfig().getString("Messages.Commands.NoPermission",
                        "&cYou do not have the permission to use this command.");
                player.sendMessage(new TextComponent(HexChat.translateHexCodes(msg)));
                return;
            }
            if (args.length < 1) {
                String msg = Main.getInstance().getConfig().getString("Messages.Commands.ToggleJoin.MissingFirstArgument",
                        "&6Please specify which messages you would like to disable/enable.\n"
                                + "&6Valid arguments are:&f join, leave, switch, all");
                player.sendMessage(new TextComponent(HexChat.translateHexCodes(msg)));
                return;
            }

            if (args.length < 2) {
                String msg = Main.getInstance().getConfig().getString("Messages.Commands.ToggleJoin.MissingState",
                        "&6Please specify which state you would like to set the message to.\n"
                                + "&6Valid arguments are: &aon &7/ &coff &6or &atrue &7/ &cfalse&f.");
                player.sendMessage(new TextComponent(HexChat.translateHexCodes(msg)));
            } else {
                Boolean state = false;
                if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true")) {
                    state = true;
                }
                if (commandArguments.contains(args[0].toLowerCase())) {
                    ConfigSettings.getInstance().setSendMessageState(args[0], player.getUniqueId(), state);
                    String msg = Main.getInstance().getConfig().getString("Messages.Commands.ToggleJoin.Confirmation",
                            "&6Receive messages for &f<mode>&6 has been set to &f<state>\n&6This will last until the network reboots.");
                    msg = msg.replace("<mode>", args[0]);
                    msg = msg.replace("<state>", state + "");
                    player.sendMessage(new TextComponent(HexChat.translateHexCodes(msg)));
				} else {
                    //Triggered if commandArguments does not contain the argument used.
                    String msg = Main.getInstance().getConfig().getString("Messages.Commands.ToggleJoin.MissingFirstArgument",
                            "&6Please specify which messages you would like to disable/enable.\n"
                                    + "&6Valid arguments are:&f join, leave, switch, all");
                    player.sendMessage(new TextComponent(HexChat.translateHexCodes(msg)));
				}
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return commandArguments;
            case 2:
                return ImmutableList.of("on", "off");
            default:
                return ImmutableList.of("No more arguments needed.");
        }
    }
}
