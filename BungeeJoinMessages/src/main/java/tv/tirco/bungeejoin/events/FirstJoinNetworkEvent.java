package tv.tirco.bungeejoin.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class FirstJoinNetworkEvent extends Event {
    private final ProxiedPlayer player;

    public FirstJoinNetworkEvent(ProxiedPlayer player) {
        this.player = player;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
