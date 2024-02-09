package uk.ac.nottingham.hybridarcade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;

public class Utility {
    // This is a *PLACEHOLDER* for passing blocks from copy to paste
    // until the game is ready to print and scan cards
    public static byte[] debugBlockBytes;

    // Sends chat globally as the player
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