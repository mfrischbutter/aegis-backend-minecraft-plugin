package com.luascript.aegisBackend;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Aegis Backend Plugin for Paper/Spigot servers.
 * Receives plugin messages from the Aegis Velocity proxy plugin
 * and executes server-side actions such as playing sounds to players.
 */
public final class AegisBackend extends JavaPlugin {

    private static final String PLUGIN_CHANNEL = "aegis:main";
    private AegisMessageListener messageListener;

    @Override
    public void onEnable() {
        // Initialize the message listener
        messageListener = new AegisMessageListener(getLogger());

        // Register the incoming plugin message channel
        getServer().getMessenger().registerIncomingPluginChannel(this, PLUGIN_CHANNEL, messageListener);

        getLogger().info("Aegis Backend has been enabled!");
        getLogger().info("Listening for plugin messages on channel: " + PLUGIN_CHANNEL);
    }

    @Override
    public void onDisable() {
        // Unregister the plugin message channel
        if (messageListener != null) {
            getServer().getMessenger().unregisterIncomingPluginChannel(this, PLUGIN_CHANNEL, messageListener);
        }

        getLogger().info("Aegis Backend has been disabled!");
    }
}
