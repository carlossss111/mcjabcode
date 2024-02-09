package uk.ac.nottingham.hybridarcade.encoding;

public class QrEncoder implements IEncoder{

    @Override
    public boolean encode(byte[] data) {
        return false;
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
