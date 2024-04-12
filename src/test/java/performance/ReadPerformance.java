package performance;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;
import uk.ac.nottingham.hybridarcade.compression.PassThroughCompressor;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static com.ibm.icu.impl.Assert.fail;

// TODO: TRY WITH DIFFERENT ECC AND RAW SIZES

public class ReadPerformance {
    private static final String RSC_PATH = "performancetest";

    private JabEncoder mEncoder;
    private ICompressor mCompressor;

    ReentrantLock mDecodeMutex = new ReentrantLock();

    public ReadPerformance(){
        mEncoder = new JabEncoder();
        mCompressor = new PassThroughCompressor();
    }

    private void tryRead(String method, Logger log, int start, int step, int finish){
        for(int i = start; i != finish+step; i += step){
            BufferedImage barcodePNG = null;
            try {
                File barcodeFile = new File(WritePerformance.class
                        .getClassLoader().getResource(
                                String.format("%s/%s%d.png", RSC_PATH, method, i)
                        ).getPath());
                barcodePNG = ImageIO.read(barcodeFile);
            }
            catch(Exception e){
                fail(e);
            }

            byte[] decodedBytes;
            try{
                mDecodeMutex.lock();
                decodedBytes = mEncoder.decode(barcodePNG);
                mDecodeMutex.unlock();
            }
            catch(IOException e){
                mDecodeMutex.unlock();
                log.info(String.format("Decode FAIL: %s=%d", method, i));
                continue;
            }

            byte[] rawBytes;
            try{
                rawBytes = mCompressor.decompress(decodedBytes);
            }
            catch(IllegalArgumentException e){
                log.info(String.format("Compression FAIL: %s=%d", method, i));
                continue;
            }

            if(rawBytes.length != 724){
                log.info(String.format("Read FAIL: %s=%d", method, i));
            }
            else{
                log.info(String.format("%s=%s, raw=%d", method, i, rawBytes.length));
            }

        }
    }

    public void testReadPerformance(LoggerContext ctx) {
        new Thread(() -> {
            tryRead("brightness", ctx.getLogger("brightness"), 0, -5, -100);
        }).start();

        new Thread(() -> {
            tryRead("contrast", ctx.getLogger("contrast"), 0, -5, -100);
        }).start();

        new Thread(() -> {
            tryRead("blur", ctx.getLogger("blur"),0, 1, 10);
        }).start();

        new Thread(() -> {
            tryRead("rotate", ctx.getLogger("rotate"), 0, 5, 90);
        }).start();

        new Thread(() -> {
            tryRead("perspective", ctx.getLogger("perspective"), 0, 10, 300);
        }).start();
    }
}
