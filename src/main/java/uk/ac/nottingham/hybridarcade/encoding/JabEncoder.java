package uk.ac.nottingham.hybridarcade.encoding;

public class JabEncoder implements IEncoder{

    @Override
    public boolean encode(byte[] data, String filePath) {
        return false;
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
