package testutil;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class Utility {
    public static boolean areImagesTheSame(BufferedImage imageA, BufferedImage imageB){
        int width  = imageA.getWidth();
        int height = imageA.getHeight();

        if(width != imageB.getWidth() || height != imageB.getHeight()){
            return false;
        }

        // Compare every pixel in the image
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                if(imageA.getRGB(x,y) != imageB.getRGB(x,y)){
                    return false;
                }
            }
        }
        return true;
    }

    public static void assertByteArrayEquals(byte[] expected, byte[] actual){
        if(expected.length != actual.length){
            fail("Expected size != actual size");
            return;
        }

        for(int i = 0; i < actual.length; i++){
            assertEquals(expected[i], actual[i]);
        }
    }


    public static void addFileLogger(ConfigurationBuilder<BuiltConfiguration> builder,
                                     String loggerName, String fileName){

        builder .add(builder.newAppender(loggerName, "File").addAttribute("fileName", fileName))
                .add(builder.newLogger(loggerName, Level.INFO)
                        .add(builder.newAppenderRef(loggerName)));

        AppenderComponentBuilder file
                = builder.newAppender(loggerName, "File");
        file.addAttribute("fileName", fileName);
        builder.add(file);
    }
}
