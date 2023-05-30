package tv.tirco.bungeejoin.listeners;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import tv.tirco.bungeejoin.Main;
import tv.tirco.bungeejoin.events.FirstJoinNetworkEvent;
import tv.tirco.bungeejoin.util.HexChat;
import tv.tirco.bungeejoin.util.MessageHandler;

import java.util.concurrent.TimeUnit;

public class FirstJoinListener implements Listener {
    @EventHandler
    public void onFirstJoin(FirstJoinNetworkEvent event) {
        ProxyServer.getInstance().getScheduler().schedule(Main.getInstance().getPlugin(), () -> {
            ProxiedPlayer player = event.getPlayer();
            String message = MessageHandler.getInstance().formatFirstJoinMessage(event.getPlayer());
            if (!PlayerListener.processSilent(player, message)) {
                Main.getInstance().getLogger().info(" [JOIN-DBG] Sending first join message for \"" + event.getPlayer().getName() + "\": \"" + message + "\"");
                MessageHandler.getInstance().broadcastMessage(HexChat.translateHexCodes(message), "join", player);
            } else {
                Main.getInstance().getLogger().info(" [JOIN-DBG] Not sending first join message for \"" + event.getPlayer().getName() + "\" because they are silent.");
            }
        }, 1, TimeUnit.SECONDS);
    }
}
