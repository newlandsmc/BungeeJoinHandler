package tv.tirco.bungeejoin.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.myzelyam.api.vanish.BungeeVanishAPI;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tv.tirco.bungeejoin.ConfigSettings;
import tv.tirco.bungeejoin.Main;
import tv.tirco.bungeejoin.events.FirstJoinNetworkEvent;
import tv.tirco.bungeejoin.util.HexChat;
import tv.tirco.bungeejoin.util.MessageHandler;
import tv.tirco.bungeejoin.util.TitleTracker;

import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    public static String silentPrefix = Main.getInstance().getConfig().getString("Messages.Misc.SilentPrefix", "&7[Silent] ");

    @EventHandler
    public void prePlayerSwitchServer(ServerConnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if (player == null) {
            return;
        }

        if (e.getReason() != null) {
            if (e.getReason().equals(Reason.COMMAND) || e.getReason().equals(Reason.JOIN_PROXY) || e.getReason().equals(Reason.PLUGIN) || e.getReason().equals(Reason.PLUGIN_MESSAGE)) {
                //Normal connection reason. All is okay,
            } else {
                //Remove player from OldServer list, so that their movement is not notified.
                ConfigSettings.getInstance().clearPlayer(player);
            }
        }

        Server server = player.getServer();
        if (server != null) {
            String serverName = server.getInfo().getName();
            if (serverName != null) {
                ConfigSettings.getInstance().setFrom(player, server.getInfo().getName());
            }
        }
    }

    @EventHandler
    public void onPlayerSwitchServer(ServerConnectedEvent e) {
        ProxiedPlayer player = e.getPlayer();
        Server server = e.getServer();

        boolean firstSession = false;
        if (TitleTracker.isJustConnected(e.getPlayer().getUniqueId())) {
            firstSession = true;
            TitleTracker.getJustConnected().remove(e.getPlayer().getUniqueId());
            String channel = "bungeejoin:title";
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Title");
            out.writeUTF(player.getUniqueId() + "");
            out.writeBoolean(false); // firstJoin
            server.sendData(channel, out.toByteArray());
        }
        if (TitleTracker.isJustConnectedNew(e.getPlayer().getUniqueId())) {
            firstSession = true;
            TitleTracker.getJustConnectedNew().remove(e.getPlayer().getUniqueId());
            String channel = "bungeejoin:title";
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Title");
            out.writeUTF(player.getUniqueId() + "");
            out.writeBoolean(true);
            server.sendData(channel, out.toByteArray());
        }

        if (!ConfigSettings.getInstance().isConnected(player)) {
            return;
        }

        String to = server.getInfo().getName();
        String from = "???";
        if (ConfigSettings.getInstance().isElsewhere(player)) {
            from = ConfigSettings.getInstance().getFrom(player);
        } else {
            return; //Event was not a To-From event, so we send no message.
        }

        if (ConfigSettings.getInstance().isSwapServerMessageEnabled()) {

            if (ConfigSettings.getInstance().blacklistCheck(from, to)) {
                return;
            }

            String message = MessageHandler.getInstance().formatSwitchMessage(player, from, to);

            //Silent
            if (ConfigSettings.getInstance().getAdminMessageState(player)) {
                Main.getInstance().silentEvent("MOVE", player.getName(), from, to);
                if (ConfigSettings.getInstance().notifyAdminsOnSilentMove()) {
                    TextComponent silentMessage = new TextComponent(HexChat.translateHexCodes(silentPrefix + message));
                    for (ProxiedPlayer p : Main.getInstance().getProxy().getPlayers()) {
                        if (p.hasPermission("bungeejoinmessages.silent")) {
                            p.sendMessage(silentMessage);
                        }
                    }
                }
                //Not silent
            } else {
                //This one is special as there are certain settings in place.
                MessageHandler.getInstance().broadcastMessage(HexChat.translateHexCodes(message), "switch", from, to);
            }
        }

    }


    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }
        ConfigSettings.getInstance().setConnected(player, true);
        boolean firstJoin = false;
        if (Main.getInstance().getStorageHandler() != null) {
            if (!Main.getInstance().getStorageHandler().doesPlayerExist(player.getUniqueId())) {
                Main.getInstance().getStorageHandler().createPlayer(player.getUniqueId(), player.getName());
                ProxyServer.getInstance().getPluginManager().callEvent(new FirstJoinNetworkEvent(player));
                firstJoin = true;
            } else {
                Main.getInstance().getStorageHandler().setLastJoin(player.getUniqueId(), System.currentTimeMillis());
            }
        }
        if (!firstJoin && ConfigSettings.getInstance().isShowTitleOnEveryJoin()) {
            TitleTracker.getJustConnected().add(player.getUniqueId());
        }
        // JoinTracker.getJustConnected().add(player.getUniqueId()); // not the best code but whatever
        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance().getPlugin(), () -> {
            if (player.isConnected()) {
                String message = MessageHandler.getInstance().formatJoinMessage(player);
                if (!processSilent(player, message)) {
                    MessageHandler.getInstance().broadcastMessage(HexChat.translateHexCodes(message), "join", player);
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static boolean processSilent(ProxiedPlayer player, String message) {
        //VanishAPI support
        if (Main.getInstance().useVanishAPI) {
            if (Main.getInstance().getConfig().getBoolean("OtherPlugins.PremiumVanish.ToggleFakemessageWhenVanishing", false))
                ConfigSettings.getInstance().setAdminMessageState(player, BungeeVanishAPI.isInvisible(player));
        }

        //Blacklist Check
        if (ConfigSettings.getInstance().blacklistCheck(player)) {
            return true;
        }

        //Silent
        if (ConfigSettings.getInstance().getAdminMessageState(player)) {
            //Notify player about the toggle command.
            if (player.hasPermission("bungeejoinmessages.fakemessage")) {
                String toggleNotif = Main.getInstance().getConfig().getString("Messages.Commands.Fakemessage.JoinNotification", "&7[BungeeJoin] You joined the server while silenced.\n" + "&7To have messages automatically enabled for you until\n" + "&7next reboot, use the command &f/fm toggle&7.");
                player.sendMessage(new TextComponent(HexChat.translateHexCodes(toggleNotif)));
            }


            //Send to console
            Main.getInstance().silentEvent("JOIN", player.getName());
            //Send to admin players.
            if (ConfigSettings.getInstance().notifyAdminsOnSilentMove()) {
                TextComponent silentMessage = new TextComponent(HexChat.translateHexCodes(silentPrefix + message));
                for (ProxiedPlayer p : Main.getInstance().getProxy().getPlayers()) {
                    if (p.hasPermission("bungeejoinmessages.silent")) {
                        p.sendMessage(silentMessage);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPostQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }


        if (!ConfigSettings.getInstance().isConnected(player)) {
            return;
        }

        if (!ConfigSettings.getInstance().isJoinNetworkMessageEnabled()) {
            ConfigSettings.getInstance().setConnected(player, false);
            return;
        }

        if (ConfigSettings.getInstance().blacklistCheck(player)) {
            return;
        }

        String message = MessageHandler.getInstance().formatQuitMessage(player);

        //Silent
        if (ConfigSettings.getInstance().getAdminMessageState(player)) {
            //Send to console
            Main.getInstance().silentEvent("QUIT", player.getName());
            //Send to admin players.
            if (ConfigSettings.getInstance().notifyAdminsOnSilentMove()) {
                TextComponent silentMessage = new TextComponent(HexChat.translateHexCodes(silentPrefix + message));
                for (ProxiedPlayer p : Main.getInstance().getProxy().getPlayers()) {
                    if (p.hasPermission("bungeejoinmessages.silent")) {
                        p.sendMessage(silentMessage);
                    }
                }
            }
            //Not silent
        } else {
            MessageHandler.getInstance().broadcastMessage(HexChat.translateHexCodes(message), "leave", player);

        }

        //Set them as not connected, as they have left the server.
        ConfigSettings.getInstance().setConnected(player, false);
    }
}
