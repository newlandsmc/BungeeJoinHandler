package tv.tirco.bungeejoin.util;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import tv.tirco.bungeejoin.ConfigSettings;
import tv.tirco.bungeejoin.Main;

import java.util.*;

public class MessageHandler {

    private static MessageHandler instance;
    String swapServerMessage = "";
    String joinNetworkMessage = "";
    String leaveNetworkMessage = "";
    String firstJoinMessage = "";
    HashMap<String, String> serverNames;

    public static MessageHandler getInstance() {
        if (instance == null) {
            instance = new MessageHandler();
        }
        return instance;
    }
    //String FirstTimeJoinMessage = "";

    public void setupConfigMessages() {
        Configuration config = Main.getInstance().getConfig();
        swapServerMessage = config.getString("Messages.SwapServerMessage", "&6&l%player%&r  &7[%from%&7] -> [%to%&7]");
        joinNetworkMessage = config.getString("Messages.JoinNetworkMessage", "&6%player% &6has connected to the network!");
        leaveNetworkMessage = config.getString("Messages.LeaveNetworkMessage", "&6%player% &6has disconnected from the network!");
        firstJoinMessage = config.getString("Messages.FirstJoinMessage", "&6%player% &6has joined the network for the first time!");
        HashMap<String, String> serverNames = new HashMap<>();

        for (String server : config.getSection("Servers").getKeys()) {
            //Main.getInstance().getLogger().info("Looping: " + server);
            serverNames.put(server.toLowerCase(), config.getString("Servers." + server, server));
            //Main.getInstance().getLogger().info("Put: " + server.toLowerCase() + " as " + config.getString("Servers." + server, server));
        }

        this.serverNames = serverNames;
    }

    public String getServerName(String key) {
        String name = key;
        if (serverNames != null) {
            //Main.getInstance().getLogger().info("ServerNames is not null");
            if (serverNames.containsKey(key.toLowerCase())) {
                //Main.getInstance().getLogger().info("serverNames contains the key");
                name = serverNames.get(key.toLowerCase());
            }
        }
        //Main.getInstance().getLogger().info("Fetched " + key + " got " + name);
        return name;
    }

    /**
     * Send a message globally, based on the players current server.
     *
     * @param text - The text to be displayed
     * @param type - What type of message should be sendt (switch/join/leave)
     * @param p    - The player to fetch the server from.
     */
    public void broadcastMessage(String text, String type, ProxiedPlayer p) {
        if (p.getServer() == null) {
            //Fixes NPE when connecting to offline server.
            MessageHandler.getInstance().log("Broadcast Message of " + p.getName() + " halted as Server returned Null. #01");
            return;
        }
        broadcastMessage(text, type, p.getServer().getInfo().getName(), "???");
    }

    public void broadcastMessage(String text, String type, String from, String to) {
        TextComponent msg = new TextComponent();
        msg.setText(text);
        //You could also use a StringBuilder here to get the arguments.

        List<ProxiedPlayer> receivers = new ArrayList<ProxiedPlayer>();
        if (type.equalsIgnoreCase("switch")) {
            receivers.addAll(ConfigSettings.getInstance().getSwitchMessageReceivers(to, from));
        } else if (type.equalsIgnoreCase("join")) {
            receivers.addAll(ConfigSettings.getInstance().getJoinMessageReceivers(from));
        } else if (type.equalsIgnoreCase("leave")) {
            receivers.addAll(ConfigSettings.getInstance().getLeaveMessageReceivers(from));
        } else {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
        }

        //Remove the players that have messages disabled
        List<UUID> ignorePlayers = ConfigSettings.getInstance().getIgnorePlayers(type);
        Main.getInstance().getLogger().info(text);

        //Add the players that are on ignored servers to the ignored list.
        ignorePlayers.addAll(ConfigSettings.getInstance().getIgnoredServerPlayers(type));

        //Parse through all receivers and ignore the ones that are on the ignore list.
        for (ProxiedPlayer player : receivers) {
            if (ignorePlayers.contains(player.getUniqueId())) {
                continue;
            } else {
                player.sendMessage(msg);
            }
        }
        //ProxyServer.getInstance().broadcast(msg);
    }

    public String getJoinNetworkMessage() {
        return joinNetworkMessage;
    }

    public String getLeaveNetworkMessage() {
        return leaveNetworkMessage;
    }

    public String getFirstJoinMessage() {
        return firstJoinMessage;
    }

    public String getSwapServerMessage() {
        return swapServerMessage;
    }

    public Iterable<String> getServerNames() {
        if (serverNames != null) {
            return serverNames.keySet();
        }
        return null;
    }

    public String getServerPlayerCount(ProxiedPlayer player, boolean leaving) {
        return getServerPlayerCount(player.getServer().getInfo(), leaving, player);
    }

    public String getServerPlayerCount(String serverName, boolean leaving, ProxiedPlayer player) {

        return getServerPlayerCount(Main.getInstance().getProxy().getServers().get(serverName), leaving, player);
    }

    public String getServerPlayerCount(ServerInfo serverInfo, boolean leaving, ProxiedPlayer player) {
        String serverPlayerCount = "?";
        if (serverInfo != null) {
            int count = 0;
            List<ProxiedPlayer> players = new ArrayList<ProxiedPlayer>(serverInfo.getPlayers());

            //VanishAPI Count
            if (Main.getInstance().useVanishAPI) {
                if (Main.getInstance().getConfig().getBoolean("OtherPlugins.PremiumVanish.RemoveVanishedPlayersFromPlayerCount", true)) {
                    List<UUID> vanished = BungeeVanishAPI.getInvisiblePlayers();
                    for (ProxiedPlayer p : serverInfo.getPlayers()) {
                        if (vanished.contains(p.getUniqueId())) {
                            players.remove(p);
                        }
                    }
                }
            }
            if (leaving && player != null) {
                if (players.contains(player)) {
                    count = players.size() - 1;
                }
            } else if (player != null) {
                if (!players.contains(player)) {
                    count = players.size() + 1;
                } else {
                    count = players.size();
                }
            }
            if (count < 0) {
                count = 0;
            }

            serverPlayerCount = String.valueOf(count);
        }
        return serverPlayerCount;
    }

    public String getNetworkPlayerCount(ProxiedPlayer player, Boolean leaving) {
        Collection<ProxiedPlayer> players = Main.getInstance().getProxy().getPlayers();
        if (leaving && player != null) {
            if (players.contains(player)) {
                return String.valueOf(players.size() - 1);
            } else {
                return String.valueOf(players.size());
            }
        } else if (player != null) {
            if (players.contains(player)) {
                return String.valueOf(players.size());
            } else {
                return String.valueOf(players.size() + 1);
            }
        }
        return String.valueOf(players.size());
    }

    public String formatSwitchMessage(ProxiedPlayer player, String fromName, String toName) {
        String from = getServerName(fromName);
        String to = getServerName(toName);

        String messageFormat = getSwapServerMessage();
        messageFormat = messageFormat.replace("%player%", player.getName());
        messageFormat = messageFormat.replace("%displayname%", player.getDisplayName());
        messageFormat = messageFormat.replace("%to%", to);
        messageFormat = messageFormat.replace("%to_clean%", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', to)));
        messageFormat = messageFormat.replace("%from%", from);
        messageFormat = messageFormat.replace("%from_clean%", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', from)));
        if (messageFormat.contains("%playercount_from%")) {
            messageFormat = messageFormat.replace("%playercount_from%", getServerPlayerCount(fromName, true, player));
        }
        if (messageFormat.contains("%playercount_to%")) {
            messageFormat = messageFormat.replace("%playercount_to%", getServerPlayerCount(toName, false, player));
        }
        if (messageFormat.contains("%playercount_network%")) {
            messageFormat = messageFormat.replace("%playercount_network%", getNetworkPlayerCount(player, false));
        }

        return messageFormat;
    }

    public String formatJoinMessage(ProxiedPlayer player) {
        String messageFormat = getJoinNetworkMessage();
        messageFormat = replacePlaceholders(player, messageFormat);
        if (messageFormat.contains("%playercount_server%")) {
            messageFormat = messageFormat.replace("%playercount_server%", getServerPlayerCount(player, false));
        }
        if (messageFormat.contains("%playercount_network%")) {
            messageFormat = messageFormat.replace("%playercount_network%", getNetworkPlayerCount(player, false));
        }

        return messageFormat;
    }


    public String formatQuitMessage(ProxiedPlayer player) {
        String messageFormat = getLeaveNetworkMessage();
        messageFormat = replacePlaceholders(player, messageFormat);
        if (messageFormat.contains("%playercount_server%")) {
            messageFormat = messageFormat.replace("%playercount_server%", getServerPlayerCount(player, true));
        }
        if (messageFormat.contains("%playercount_network%")) {
            messageFormat = messageFormat.replace("%playercount_network%", getNetworkPlayerCount(player, true));
        }

        return messageFormat;
    }
    public String formatFirstJoinMessage(ProxiedPlayer player) {
        String messageFormat = getFirstJoinMessage();
        messageFormat = replacePlaceholders(player, messageFormat);
        if (messageFormat.contains("%playercount_server%")) {
            messageFormat = messageFormat.replace("%playercount_server%", getServerPlayerCount(player, false));
        }
        if (messageFormat.contains("%playercount_network%")) {
            messageFormat = messageFormat.replace("%playercount_network%", getNetworkPlayerCount(player, false));
        }

        return messageFormat;
    }

    private String replacePlaceholders(ProxiedPlayer player, String messageFormat) {
        messageFormat = messageFormat.replace("%player%", player.getName());
        messageFormat = messageFormat.replace("%displayname%", player.getDisplayName());
        if (messageFormat.contains("%server_name%")) {
            ServerInfo server = player.getServer().getInfo();
            messageFormat = messageFormat.replace("%server_name%", getServerName(server.getName()));
        }
        if (messageFormat.contains("%server_name_clean%")) {
            ServerInfo server = player.getServer().getInfo();
            messageFormat = messageFormat.replace("%server_name_clean%", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', getServerName(server.getName()))));
        }
        return messageFormat;
    }

    public void log(String string) {
        Main.getInstance().getLogger().info(string);

    }


}
