package uk.ac.nottingham.hybridarcade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.net.URL;

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

    private static String fixWeirdArtefact(String path){
        if(path.split("%.*!").length > 1) {
            return path.split("%.*!")[0] + path.split("%.*!")[1];
        }
        return path;
    }

    public static String getResourcePath(Object context, String filename) throws IOException {
        URL rsc;

        //Try relative to project root (or mods folder on running)
        File rscFile = new File(String.format("%s/mods/%s", System.getProperty("user.dir"),
                filename));
        if(rscFile.exists()){
            Constants.logger.debug("Read: " + rscFile.getAbsolutePath());
            return rscFile.getAbsolutePath();
        }

        //Try raw filename as path
        rsc = context.getClass().getClassLoader().getResource(filename);
        if(rsc != null){
            Constants.logger.debug("Read: " + rsc.getPath());
            return fixWeirdArtefact(rsc.getPath());
        }

        //Try relative to data
        rsc = context.getClass().getClassLoader().getResource(
                String.format("data/hybridarcade/%s", filename));
        if(rsc != null){
            Constants.logger.debug("Read: " + rsc.getPath());
            return fixWeirdArtefact(rsc.getPath());
        }

        //Try relative to assets
        rsc = context.getClass().getClassLoader().getResource(
                String.format("assets/hybridarcade/%s", filename));
        if(rsc != null){
            Constants.logger.debug("Read: " + rsc.getPath());
            return fixWeirdArtefact(rsc.getPath());
        }

        throw new RuntimeException("Failed to read resource named: "+ filename);
    }
}
