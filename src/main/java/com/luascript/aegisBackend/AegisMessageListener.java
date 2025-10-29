package com.luascript.aegisBackend;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for plugin messages from the Aegis Velocity proxy plugin.
 * Handles incoming messages on the "aegis:main" channel and executes
 * server-side actions such as playing sounds to players.
 */
public class AegisMessageListener implements PluginMessageListener {

    private static final String MESSAGE_TYPE_PLAY_SOUND = "PLAY_SOUND";
    private final Logger logger;

    public AegisMessageListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        // Verify channel (should always be "aegis:main" but good practice to check)
        if (!channel.equals("aegis:main")) {
            return;
        }

        try {
            // Parse the incoming message
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            // Read message type
            String messageType = in.readUTF();

            // Handle different message types
            switch (messageType) {
                case MESSAGE_TYPE_PLAY_SOUND:
                    handlePlaySound(in);
                    break;
                default:
                    logger.warning("Received unknown message type: " + messageType);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing plugin message from Aegis Velocity", e);
        }
    }

    /**
     * Handles a PLAY_SOUND message from the proxy.
     * Message format: UUID (String), Sound Name (String), Volume (float), Pitch (float)
     */
    private void handlePlaySound(ByteArrayDataInput in) {
        try {
            // Read message data
            String uuidString = in.readUTF();
            String soundName = in.readUTF();
            float volume = in.readFloat();
            float pitch = in.readFloat();

            // Parse UUID
            UUID playerUuid;
            try {
                playerUuid = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                logger.warning("Received invalid UUID in PLAY_SOUND message: " + uuidString);
                return;
            }

            // Get player from server
            Player targetPlayer = Bukkit.getPlayer(playerUuid);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                logger.fine("Player " + uuidString + " is not online on this server, cannot play sound");
                return;
            }

            // Parse sound name to Bukkit Sound using Registry
            Sound sound;
            try {
                // Convert enum-style name (ENTITY_VILLAGER_NO) to namespaced key format (entity.villager.no)
                String namespacedKeyString = soundName.toLowerCase().replace('_', '.');
                NamespacedKey soundKey = NamespacedKey.minecraft(namespacedKeyString);
                sound = Registry.SOUNDS.get(soundKey);

                if (sound == null) {
                    throw new IllegalArgumentException("Sound not found in registry: " + soundName);
                }
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid sound name received: " + soundName + ". Using default sound.");
                // Fallback to a default sound if the sound name is invalid
                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
            }

            // Play the sound to the player at their location
            targetPlayer.playSound(targetPlayer.getLocation(), sound, volume, pitch);

            logger.fine("Played sound " + soundName + " to player " + targetPlayer.getName() +
                       " (volume: " + volume + ", pitch: " + pitch + ")");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling PLAY_SOUND message", e);
        }
    }
}
