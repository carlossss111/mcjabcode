package uk.ac.nottingham.hybridarcade.encoding;

public class JabEncoder implements IEncoder{

    static {
        System.loadLibrary("jabcode");
    }

    private native void printJab(int a);

    @Override
    public boolean encode(byte[] data, String filePath) {
        printJab(10);
        return false;
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
