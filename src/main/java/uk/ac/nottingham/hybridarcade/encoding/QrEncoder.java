package uk.ac.nottingham.hybridarcade.encoding;

import java.awt.image.BufferedImage;

public class QrEncoder implements IEncoder{

    @Override
    public BufferedImage encode(byte[] data) {
        return null;
    }

    @Override
    public byte[] decode(BufferedImage image) {
        return new byte[0];
    }
}
