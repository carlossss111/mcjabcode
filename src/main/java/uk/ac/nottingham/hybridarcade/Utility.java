package uk.ac.nottingham.hybridarcade;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class Utility {
    // For debug only, may not be threadsafe!
    public static BlockState[][][] debugBlocks;

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
