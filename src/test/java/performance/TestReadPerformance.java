package performance;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;
import uk.ac.nottingham.hybridarcade.compression.PassThroughCompressor;
import uk.ac.nottingham.hybridarcade.encoding.IEncoder;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static com.ibm.icu.impl.Assert.fail;

public class TestReadPerformance {
    private static final String RSC_PATH = "performancetest";

    private static final String BRIGHTNESS_LOG_PATH = "performance/read/brightness.log";
    private static final String CONTRAST_LOG_PATH = "performance/read/contrast.log";
    private static final String BLUR_LOG_PATH = "performance/read/blur.log";
    private static final String ROTATE_LOG_PATH = "performance/read/rotate.log";

    private IEncoder mEncoder;
    private ICompressor mCompressor;

    private int mTestThreadCount = 4;
    ReentrantLock mFinishMutex = new ReentrantLock();
    ReentrantLock mDecodeMutex = new ReentrantLock();

    @BeforeEach
    public void setupClass(){
        mEncoder = new JabEncoder();
        mCompressor = new PassThroughCompressor();
    }

    private void tryRead(String method, Logger log, int start, int step, int finish){
        for(int i = start; i != finish+step; i += step){
            BufferedImage barcodePNG = null;
            try {
                File barcodeFile = new File(TestWritePerformance.class
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

    @Test
    public void testReadPerformance() {
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();
        testutil.Utility.addFileLogger(builder, "brightness", BRIGHTNESS_LOG_PATH);
        testutil.Utility.addFileLogger(builder, "contrast", CONTRAST_LOG_PATH);
        testutil.Utility.addFileLogger(builder, "blur", BLUR_LOG_PATH);
        testutil.Utility.addFileLogger(builder, "rotate", ROTATE_LOG_PATH);
        LoggerContext ctx = Configurator.initialize(builder.build());

        new Thread(() -> {
            tryRead("brightness", ctx.getLogger("brightness"), 0, -5, -100);
            mFinishMutex.lock();
            mTestThreadCount--;
            mFinishMutex.unlock();
        }).start();

        new Thread(() -> {
            tryRead("contrast", ctx.getLogger("contrast"), 0, -5, -100);;
            mFinishMutex.lock();
            mTestThreadCount--;
            mFinishMutex.unlock();
        }).start();

        new Thread(() -> {
            tryRead("blur", ctx.getLogger("blur"),0, 1, 10);
            mFinishMutex.lock();
            mTestThreadCount--;
            mFinishMutex.unlock();
        }).start();

        new Thread(() -> {
            tryRead("rotate", ctx.getLogger("rotate"), 0, 5, 90);
            mFinishMutex.lock();
            mTestThreadCount--;
            mFinishMutex.unlock();
        }).start();

        while(mTestThreadCount > 0){
            try { Thread.sleep(100); }
            catch(InterruptedException ignored){}
        }
    }
}
