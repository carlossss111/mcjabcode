package uk.ac.nottingham.hybridarcade.encoding;

public class JabEncoder implements IEncoder{

    static {
        System.loadLibrary("png16");
        System.loadLibrary("tiff");
        System.loadLibrary("z");
        System.loadLibrary("JabEncoder");
    }

    private native void printJab(byte[] a, byte[] b);

    @Override
    public boolean encode(byte[] data, byte[] filePath) {
        printJab(data, filePath);
        return false;
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
