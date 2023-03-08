package tv.tirco.bungeejoin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tv.tirco.bungeejoin.util.MessageHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConfigSettings {

    private static ConfigSettings instance;

    HashMap<ProxiedPlayer, String> previousServer;
    HashMap<UUID, Boolean> messageState;
    List<UUID> onlinePlayers;
    List<UUID> noJoinMessage;
    List<UUID> noLeaveMessage;
    List<UUID> noSwitchMessage;

    boolean swapServerMessageEnabled = true;
    boolean joinNetworkMessageEnabled = true;
    boolean firstJoinNetworkMessageEnabled = true;
    boolean leaveNetworkMessageEnabled = true;
    boolean notifyAdminsOnSilentMove = true;

    boolean swapViewableByJoined = true;
    boolean swapViewableByLeft = true;
    boolean swapViewableByOther = true;

    boolean joinViewableByJoined = true;
    boolean joinViewableByOther = true;

    boolean leftViewableByLeft = true;
    boolean leftViewableByOther = true;

    List<String> serverJoinMessageDisabled;
    List<String> serverLeaveMessageDisabled;

    //BlackList settings
    List<String> blacklistedServers;
    boolean useBlacklistAsWhitelist;
    String swapServerMessageRequires = "ANY";

    public ConfigSettings() {
        this.previousServer = new HashMap<ProxiedPlayer, String>();
        this.messageState = new HashMap<UUID, Boolean>();
        this.onlinePlayers = new ArrayList<UUID>();
        this.noJoinMessage = new ArrayList<UUID>();
        this.noLeaveMessage = new ArrayList<UUID>();
        this.noSwitchMessage = new ArrayList<UUID>();
    }

    /**
     * Get current instance. Make new if there is none.
     *
     * @return instance of the storage.
     */
    public static ConfigSettings getInstance() {
        if (instance == null) {
            instance = new ConfigSettings();
        }

        return instance;
    }

    /**
     * Grab values from config and save them here.
     */
    public void setUpDefaultValuesFromConfig() {
        this.swapServerMessageEnabled = Main.getInstance().getConfig().getBoolean("Settings.SwapServerMessageEnabled", true);
        this.joinNetworkMessageEnabled = Main.getInstance().getConfig().getBoolean("Settings.JoinNetworkMessageEnabled", true);
        this.firstJoinNetworkMessageEnabled = Main.getInstance().getConfig().getBoolean("Settings.FirstJoinNetworkMessageEnabled", true);
        this.leaveNetworkMessageEnabled = Main.getInstance().getConfig().getBoolean("Settings.LeaveNetworkMessageEnabled", true);
        this.notifyAdminsOnSilentMove = Main.getInstance().getConfig().getBoolean("Settings.NotifyAdminsOnSilentMove", true);

        this.swapViewableByJoined = Main.getInstance().getConfig().getBoolean("Settings.SwapServerMessageViewableBy.ServerJoined", true);
        this.swapViewableByLeft = Main.getInstance().getConfig().getBoolean("Settings.SwapServerMessageViewableBy.ServerLeft", true);
        this.swapViewableByOther = Main.getInstance().getConfig().getBoolean("Settings.SwapServerMessageViewableBy.OtherServer", true);

        this.joinViewableByJoined = Main.getInstance().getConfig().getBoolean("Settings.JoinNetworkMessageViewableBy.ServerJoined", true);
        this.joinViewableByOther = Main.getInstance().getConfig().getBoolean("Settings.JoinNetworkMessageViewableBy.OtherServer", true);

        this.leftViewableByLeft = Main.getInstance().getConfig().getBoolean("Settings.LeaveNetworkMessageViewableBy.ServerLeft", true);
        this.leftViewableByOther = Main.getInstance().getConfig().getBoolean("Settings.LeaveNetworkMessageViewableBy.OtherServer", true);

        //Blacklist
        this.blacklistedServers = Main.getInstance().getConfig().getStringList("Settings.ServerBlacklist");
        this.useBlacklistAsWhitelist = Main.getInstance().getConfig().getBoolean("Settings.UseBlacklistAsWhitelist", false);
        this.swapServerMessageRequires = Main.getInstance().getConfig().getString("Settings.SwapServerMessageRequires", "ANY").toUpperCase();

        this.serverJoinMessageDisabled = Main.getInstance().getConfig().getStringList("Settings.IgnoreJoinMessagesList");
        this.serverLeaveMessageDisabled = Main.getInstance().getConfig().getStringList("Settings.IgnoreLeaveMessagesList");

        //Verify Swap Server Message
        switch (swapServerMessageRequires) {
            case "JOINED":
            case "LEFT":
            case "BOTH":
            case "ANY":
                break;
            default:
                MessageHandler.getInstance().log("Setting error: Settings.SwapServerMessageRequires "
                        + "only allows JOINED LEFT BOTH or ANY. Got " + swapServerMessageRequires +
                        "Defaulting to ANY.");
                this.swapServerMessageRequires = "ANY";
        }

        MessageHandler.getInstance().log("BungeeJoinMessages Config has been reloaded.");

    }

    public boolean isSwapServerMessageEnabled() {
        return swapServerMessageEnabled;
    }

    public boolean isJoinNetworkMessageEnabled() {
        return joinNetworkMessageEnabled;
    }

    public boolean isLeaveNetworkMessageEnabled() {
        return leaveNetworkMessageEnabled;
    }

    public boolean notifyAdminsOnSilentMove() {
        return notifyAdminsOnSilentMove;
    }

    public boolean getAdminMessageState(ProxiedPlayer p) {
        if (p.hasPermission("bungeejoinmessages.silent")) {
            if (messageState.containsKey(p.getUniqueId())) {
                return messageState.get(p.getUniqueId());
            } else {
                boolean state = Main.getInstance().getConfig().getBoolean("Settings.SilentJoinDefaultState", true);
                messageState.put(p.getUniqueId(), state);
                return state;
            }
        } else {
            return false; //Is not silent by default as they don't have silent perm..
        }
    }

    public void setAdminMessageState(ProxiedPlayer player, Boolean state) {
        messageState.put(player.getUniqueId(), state);
    }

    public Boolean isConnected(ProxiedPlayer p) {
        return onlinePlayers.contains(p.getUniqueId());
    }

    public void setConnected(ProxiedPlayer p, Boolean state) {
        if (state) {
            if (!isConnected(p)) {
                onlinePlayers.add(p.getUniqueId());
            }
        } else {
            if (isConnected(p)) {
                onlinePlayers.remove(p.getUniqueId());
            }
        }
    }

    public String getFrom(ProxiedPlayer p) {
        if (previousServer.containsKey(p)) {
            return previousServer.get(p);
        } else {
            return p.getServer().getInfo().getName();
        }
    }

    public void setFrom(ProxiedPlayer p, String name) {
        previousServer.put(p, name);
    }


    public boolean isElsewhere(ProxiedPlayer player) {
        return previousServer.containsKey(player);
    }

    public void clearPlayer(ProxiedPlayer player) {
        previousServer.remove(player);

    }

    private void setJoinState(UUID id, boolean state) {
        if (state) {
            noJoinMessage.remove(id);
        } else {
            if (!noJoinMessage.contains(id)) { //Prevent duplicates.
                noJoinMessage.add(id);
            }
        }
    }

    private void setLeaveState(UUID id, boolean state) {
        if (state) {
            noLeaveMessage.remove(id);
        } else {
            if (!noLeaveMessage.contains(id)) { //Prevent duplicates.
                noLeaveMessage.add(id);
            }
        }
    }

    private void setSwitchState(UUID id, boolean state) {
        if (state) {
            noSwitchMessage.remove(id);
        } else {
            if (!noSwitchMessage.contains(id)) { //Prevent duplicates.
                noSwitchMessage.add(id);
            }
        }
    }

    public void setSendMessageState(String list, UUID id, boolean state) {
        switch (list) {
            case "all":
                setSwitchState(id, state);
                setJoinState(id, state);
                setLeaveState(id, state);
                return;
            case "join":
                setJoinState(id, state);
                return;
            case "leave":
            case "quit":
                setLeaveState(id, state);
                return;
            case "switch":
                setSwitchState(id, state);
                return;
            default:
        }
    }

    public List<UUID> getIgnorePlayers(String type) {
        switch (type) {
            case "join":
                return noJoinMessage;
            case "leave":
                return noLeaveMessage;
            case "switch":
                return noSwitchMessage;
            default:
                return new ArrayList<UUID>();
        }
    }

    public List<ProxiedPlayer> getSwitchMessageReceivers(String to, String from) {
        List<ProxiedPlayer> receivers = new ArrayList<ProxiedPlayer>();
        //If all are true, add all players:
        if (swapViewableByJoined && swapViewableByLeft && swapViewableByOther) {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
            return receivers;
        }

        //Other server is true, but atleast one of the to or from are set to false:
        else if (swapViewableByOther) {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
            //Players on the connected server is not allowed to see. Remove them all.
            if (!swapViewableByJoined) {
                receivers.removeAll(getServerPlayers(to));
            }

            if (!swapViewableByLeft) {
                receivers.removeAll(getServerPlayers(from));
            }
            return receivers;
        }

        //OtherServer is false.
        else {
            if (swapViewableByJoined) {
                receivers.addAll(getServerPlayers(to));
            }

            if (swapViewableByLeft) {
                receivers.addAll(getServerPlayers(from));
            }
            return receivers;
        }
    }

    public List<ProxiedPlayer> getJoinMessageReceivers(String server) {
        List<ProxiedPlayer> receivers = new ArrayList<ProxiedPlayer>();
        //If all are true, add all players:
        if (joinViewableByJoined && joinViewableByOther) {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
            return receivers;
        }

        //Other server is true, but atleast one of the to or from are set to false:
        else if (joinViewableByOther) {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
            receivers.removeAll(getServerPlayers(server));
            return receivers;
        } else {
            if (joinViewableByJoined) {
                receivers.addAll(getServerPlayers(server));
            }
            return receivers;
        }
    }

    public List<ProxiedPlayer> getLeaveMessageReceivers(String server) {
        List<ProxiedPlayer> receivers = new ArrayList<ProxiedPlayer>();
        //If all are true, add all players:
        if (leftViewableByLeft && leftViewableByOther) {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
            return receivers;
        }

        //Other server is true, but atleast one of the to or from are set to false:
        else if (leftViewableByOther) {
            receivers.addAll(ProxyServer.getInstance().getPlayers());
            receivers.removeAll(getServerPlayers(server));
            return receivers;
        } else {
            if (leftViewableByLeft) {
                receivers.addAll(getServerPlayers(server));
            }
            return receivers;
        }
    }


    public List<ProxiedPlayer> getServerPlayers(String serverName) {
        ServerInfo info = Main.getInstance().getProxy().getServers().get(serverName);
        if (info != null) {
            return new ArrayList<ProxiedPlayer>(info.getPlayers());
        } else {
            return new ArrayList<ProxiedPlayer>();
        }
    }


    public boolean blacklistCheck(ProxiedPlayer player) {
        if (player.getServer() == null) {
            MessageHandler.getInstance().log("Warning: Server of " + player.getName() + " came back as Null. Blackisted Server check failed. #01");
            return false;
        }
        String server = player.getServer().getInfo().getName();
        //Null check because of Geyser issues.
        if (server == null) {
            MessageHandler.getInstance().log("Warning: Server of " + player.getName() + " came back as Null. Blackisted Server check failed. #02");
            return false;
        }

        if (blacklistedServers == null) {
            MessageHandler.getInstance().log("Warning: Blacklisted servers returned null instead of an empty list...");
            return false;
        }
        boolean listed = blacklistedServers.contains(server);
        if (useBlacklistAsWhitelist) {
            //WHITELIST
            return !listed;
            //Returns TRUE if it's NOT in the list (Deny MessagE)
            //FALSE if it is in the list. (Allow Message)

        } else {
            //BLACKLIST
            return listed;
            //Returns TRUE if it is listed. (Allow Message)
            //FALSE if it's not listed. (Deny Message)

        }
        //Returning FALSE allows the message to go further, TRUE will stop it.
    }

    public boolean blacklistCheck(String from, String to) {
        boolean fromListed = false;
        if (from != null) {
            fromListed = blacklistedServers.contains(from);
        }

        boolean toListed = false;
        if (to != null) {
            toListed = blacklistedServers.contains(to);
        }

        boolean result = true;
        switch (swapServerMessageRequires.toUpperCase()) {
            case "JOINED":
                result = toListed;
                //MessageHandler.getInstance().log("Joined - toListed = " + toListed);
                break;
            case "LEFT":
                result = fromListed;
                //MessageHandler.getInstance().log("Left - fromListed = " + fromListed);
                break;
            case "ANY":
                result = fromListed || toListed;
                //MessageHandler.getInstance().log("ANY - fromListed = " + fromListed + "toListed = " + toListed + " result = " + result);
                break;
            case "BOTH":
                result = fromListed && toListed;
                //MessageHandler.getInstance().log("BOTH - fromListed = " + fromListed + "toListed = " + toListed + " result = " + result);
                break;
            default:
                //MessageHandler.getInstance().log("Warning: Default action triggered under blacklist check. Is it correctly configured?");
                break;
        }
        //Returning FALSE allows the message to go further, TRUE will stop it.
        if (useBlacklistAsWhitelist) {
            //WHITELIST
            return !result;
        } else {
            //BLACKLIST
            return result;
        }
    }


    public List<UUID> getIgnoredServerPlayers(String type) {
        List<UUID> ignored = new ArrayList<UUID>();
        if (type.equalsIgnoreCase("join")) {
            for (String s : serverJoinMessageDisabled) {
                ServerInfo info = Main.getInstance().getProxy().getServers().get(s);
                if (info != null) {
                    for (ProxiedPlayer p : info.getPlayers()) {
                        ignored.add(p.getUniqueId());
                    }
                }
            }
        } else if (type.equalsIgnoreCase("leave")) {
            for (String s : serverLeaveMessageDisabled) {
                ServerInfo info = Main.getInstance().getProxy().getServers().get(s);
                if (info != null) {
                    for (ProxiedPlayer p : info.getPlayers()) {
                        ignored.add(p.getUniqueId());
                    }
                }
            }
        }
        return ignored;
    }
}
