package uk.ac.nottingham.hybridarcade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;

/**
 * Global Utilities
 * @author Daniel Robinson
 */
public class Utility {

    /**
     * Sends a chat message as the player
     * @param message The string to be displayed in-game.
     */
    public static void sendChat(String message) {
        Player player =  Minecraft.getInstance().player;
        if(player == null){
            return;
        }

        PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), message);
        player.createCommandSourceStack().sendChatMessage(
                new OutgoingChatMessage.Player(chatMessage),
                false,
                ChatType.bind(ChatType.CHAT, player));
    }
}
