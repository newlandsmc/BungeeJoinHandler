package tv.tirco.bungeejoin.listeners;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tv.tirco.bungeejoin.Main;
import tv.tirco.bungeejoin.events.FirstJoinNetworkEvent;
import tv.tirco.bungeejoin.util.HexChat;
import tv.tirco.bungeejoin.util.MessageHandler;

public class FirstJoinListener implements Listener {
    @EventHandler
    public void onFirstJoin(FirstJoinNetworkEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String message = MessageHandler.getInstance().formatFirstJoinMessage(event.getPlayer());
        Main.getInstance().getLogger().info(" [JOIN-DBG] Sending first join message for \"" + event.getPlayer().getName() + "\"");
        if (!PlayerListener.processSilent(player, message)) {
            MessageHandler.getInstance().broadcastMessage(HexChat.translateHexCodes(message), "join", player);
        }
    }
}
