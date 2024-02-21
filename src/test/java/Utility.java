import java.awt.image.BufferedImage;

class Utility {
    static boolean areImagesTheSame(BufferedImage imageA, BufferedImage imageB){
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
}
