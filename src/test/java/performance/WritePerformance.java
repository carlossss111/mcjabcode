package performance;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import uk.ac.nottingham.hybridarcade.compression.*;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;
import uk.ac.nottingham.hybridarcade.hardware.Printer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class WritePerformance {
    // test
    private static final String BLOCKS_FILE_PATH = "performancetest/world.bytes";
    private static final int TARGET = 10*10*10;

    // logging & params
    private static final int PASS_THROUGH_STEP = 200;
    private static final int RUN_LENGTH_STEP = 200;
    private static final int RUN_LENGTH_MK2_STEP = 200;
    private static final int HUFFMAN_STEP = 200;

    // concurrency
    ReentrantLock mInputMutex = new ReentrantLock();

    // tested components
    private final byte[] mBlocksAsBytes;
    private final JabEncoder mEncoder;
    private final Printer mPrinter;
    private final PrinterJob mMockPrintJob;

    /* Setup */

    public WritePerformance(){
        try {
            File blockFile = new File(WritePerformance.class
                    .getClassLoader().getResource(BLOCKS_FILE_PATH).getPath());
            mBlocksAsBytes = Files.readAllBytes(blockFile.toPath());
        }
        catch(IOException e){
            fail("Error reading from file while setting up performance test." + e);
            throw new RuntimeException();
        }

        mEncoder = new JabEncoder();
        mMockPrintJob = mock(PrinterJob.class);
        mPrinter = new Printer(mMockPrintJob);
    }

    public float findBarcodeMinWidth(BufferedImage barcode) throws IOException{
        ByteArrayOutputStream asBytes = new ByteArrayOutputStream();
        ImageIO.write(barcode, "png", asBytes);
        PDDocument doc = new PDDocument();
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, asBytes.toByteArray(), null);
        int numOfModulesOnSide = pdImage.getWidth() / mPrinter.findModuleWidthPx(pdImage.getImage());
        return numOfModulesOnSide * 0.33f; // 0.33mm per module
    }

    /* Testing */

    // Tries to compress, encode and mock print
    private int findBestPerformance(byte[] rawBytes, ICompressor compressor, Logger log, int ecc){
        byte[] compressedBytes = compressor.compress(rawBytes);
        BufferedImage barcodePNG;
        try{
            barcodePNG = mEncoder.encode(compressedBytes, ecc);
        }
        catch(IOException e){
            log.info(String.format("FAIL: at ENCODE with raw=%d\n%s", rawBytes.length, e));
            return 0;
        }

        float physWidth;
        try{
            physWidth = findBarcodeMinWidth(barcodePNG);
            mPrinter.print(barcodePNG);
        }
        catch(IOException | IllegalArgumentException | PrinterException e){
           log.info(String.format("FAIL: at PRINT with raw=%d\n%s", rawBytes.length, e));
            return 0;
        }
        log.info(String.format("raw=%d, compressed=%d, width=%.2f", rawBytes.length,
                compressedBytes.length, physWidth));
        return rawBytes.length;
    }

    // Loops through a bytestream representing blocks converted to bytes and tries to encode them
    private void tryCompression(ICompressor compressor, int steps, Logger logger, int ecc){
        ByteArrayOutputStream bytesToTry = new ByteArrayOutputStream();
        int offset = 0;
        do{
            mInputMutex.lock();
            bytesToTry.write(mBlocksAsBytes, offset, steps);
            mInputMutex.unlock();
            offset += steps;
            if(offset + steps > mBlocksAsBytes.length){
                offset = 0;
            }
        }while(findBestPerformance(bytesToTry.toByteArray(), compressor, logger, ecc) > 0);
        if(bytesToTry.size() < TARGET){
            fail("Performance below target.");
        }
    }

    // Tries the compression algorithms in seperate threads
    public void testPerformance(LoggerContext ctx, int ecc) {
        new Thread(() -> {
            tryCompression(new PassThroughCompressor(),
                    PASS_THROUGH_STEP, ctx.getLogger("pass_through"), ecc);
        }).start();

        new Thread(() -> {
            tryCompression(new RunLengthCompressor(),
                    RUN_LENGTH_STEP, ctx.getLogger("run_length"), ecc);
        }).start();

        new Thread(() -> {
            tryCompression(new RunLengthCompressorMk2(),
                    RUN_LENGTH_MK2_STEP, ctx.getLogger("run_length_mk2"), ecc);
        }).start();

        new Thread(() -> {
            tryCompression(new HuffmanCompressor(),
                    HUFFMAN_STEP, ctx.getLogger("huffman"), ecc);
        }).start();
    }
}
