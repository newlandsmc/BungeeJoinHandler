package tv.tirco.bungeejoin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import tv.tirco.bungeejoin.commands.FakeCommand;
import tv.tirco.bungeejoin.commands.ReloadCommand;
import tv.tirco.bungeejoin.commands.ToggleJoinCommand;
import tv.tirco.bungeejoin.listeners.PlayerListener;
import tv.tirco.bungeejoin.listeners.VanishListener;
import tv.tirco.bungeejoin.util.HexChat;
import tv.tirco.bungeejoin.util.MessageHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class Main extends Plugin {

    private static Main instance;
    public boolean useVanishAPI = false;
    //private File file;
    private Configuration configuration;
    private Plugin mainPlugin;

    public static Main getInstance() {
        return instance;
    }

    public static void setInstance(Main instance) {
        Main.instance = instance;
    }

    @Override
    public void onEnable() {
        getLogger().info("Bungee Version is loading...");
        setInstance(this);
        this.mainPlugin = this;
        // Don't log enabling, Spigot does that for you automatically!

        loadConfig();
        // Commands enabled with following method must have entries in plugin.yml
        //getCommand("example").setExecutor(new ExampleCommand(this));
        MessageHandler.getInstance().setupConfigMessages();
        ConfigSettings.getInstance().setUpDefaultValuesFromConfig();
        getProxy().getPluginManager().registerListener(this, new PlayerListener());

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new FakeCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ReloadCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ToggleJoinCommand());

        if (ProxyServer.getInstance().getPluginManager().getPlugin("SuperVanish") != null
                || ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null) {
            getLogger().info("Detected PremiumVanish! - Using API.");
            this.useVanishAPI = true;
            getProxy().getPluginManager().registerListener(this, new VanishListener());
        }
        getLogger().info("has loaded!");
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) getDataFolder().mkdir(); //Make folder
        File file = new File(getDataFolder(), "config.yml");
        try {
            if (!file.exists()) {
                try (InputStream in = getResourceAsStream("config.yml")) {
                    Files.copy(in, file.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Configuration getConfig() {
        return configuration;
    }

    @Override
    public void onDisable() {
        // Don't log disabling, Spigot does that for you automatically!
    }

    public Plugin getPlugin() {
        return mainPlugin;
    }

    /**
     * Attempt to load values from configfile.
     */
    public void reloadConfig() {
        loadConfig();
        MessageHandler.getInstance().setupConfigMessages();
        ConfigSettings.getInstance().setUpDefaultValuesFromConfig();
    }

    /**
     * Used when there's no specific from/to server.
     *
     * @param type - Type of event
     * @param name - Name of player.
     */
    public void SilentEvent(String type, String name) {
        SilentEvent(type, name, "", "");
    }

    /**
     * Used to send a move message.
     *
     * @param type - The type of event that is silenced.
     * @param name - Name of the player.
     * @param from - Name of the server that is being moved from.
     * @param to   - Name of the server that is being moved to.
     */
    public void SilentEvent(String type, String name, String from, String to) {
        String message = "";
        switch (type) {
            case "MOVE":
                message = getConfig().getString("Messages.Misc.ConsoleSilentMoveEvent", "&1Move Event was silenced. <player> <from> -> <to>");
                message = message.replace("<to>", to);
                message = message.replace("<from>", from);
                break;
            case "QUIT":
                message = getConfig().getString("Messages.Misc.ConsoleSilentQuitEvent", "&6Quit Event was silenced. <player> left the network.");
                break;
            case "JOIN":
                message = getConfig().getString("Messages.Misc.ConsoleSilentJoinEvent", "&6Join Event was silenced. <player> joined the network.");
                break;
            default:
                return;
        }
        message = message.replace("<player>", name);
        getLogger().info(HexChat.translateHexCodes(message));
    }
}
