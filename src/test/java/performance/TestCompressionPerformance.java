package performance;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;
import uk.ac.nottingham.hybridarcade.compression.PassThroughCompressor;
import uk.ac.nottingham.hybridarcade.compression.RunLengthCompressor;
import uk.ac.nottingham.hybridarcade.encoding.IEncoder;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;
import uk.ac.nottingham.hybridarcade.hardware.Printer;

import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

import static com.ibm.icu.impl.Assert.fail;
import static org.mockito.Mockito.mock;

public class TestCompressionPerformance {
    // consts
    private static final String BLOCKS_FILE_PATH = "performancetest/world.bytes";
    private static final String PASS_THROUGH_LOG_PATH = "performance/pass_through.log";
    private static final String RUN_LENGTH_LOG_PATH = "performance/run_length.log";

    // target for number of minecraft blocks to store
    private static final int TARGET = 10*10*10;

    // concurrency
    private int mTestThreadCount = 2;
    ReentrantLock mInputMutex = new ReentrantLock();
    ReentrantLock mFinishMutex = new ReentrantLock();

    // tested components
    private static byte[] mBlocksAsBytes;
    private IEncoder mEncoder;
    private Printer mPrinter;
    private PrinterJob mMockPrintJob;

    /* Setup */

    @BeforeAll
    public static void setupAll(){
        try {
            File blockFile = new File(TestCompressionPerformance.class
                    .getClassLoader().getResource(BLOCKS_FILE_PATH).getPath());
            mBlocksAsBytes = Files.readAllBytes(blockFile.toPath());
        }
        catch(IOException e){
            fail("Error reading from file while setting up performance test." + e);
            throw new RuntimeException();
        }
    }

    @BeforeEach
    public void setup(){
        mEncoder = new JabEncoder();
        mMockPrintJob = mock(PrinterJob.class);
        mPrinter = new Printer(mMockPrintJob);
    }

    // Log to file
    private void addPerformanceLogger(ConfigurationBuilder<BuiltConfiguration> builder,
                                      String loggerName, String fileName){

        builder .add(builder.newAppender(loggerName, "File").addAttribute("fileName", fileName))
                .add(builder.newLogger(loggerName, Level.INFO)
                        .add(builder.newAppenderRef(loggerName)
                                .addAttribute("additivity", false)));

        AppenderComponentBuilder file
                = builder.newAppender(loggerName, "File");
        file.addAttribute("fileName", fileName);
        builder.add(file);
    }

    /* Testing */

    // Tries to compress, encode and mock print
    private int findBestPerformance(byte[] rawBytes, ICompressor compressor, Logger log){
        byte[] compressedBytes = compressor.compress(rawBytes);
        BufferedImage barcodePNG;
        try{
            barcodePNG = mEncoder.encode(compressedBytes);
        }
        catch(IOException e){
            log.info(String.format("FAIL: at ENCODE with raw=%d\n%s", rawBytes.length, e));
            return 0;
        }

        try{
            mPrinter.print(barcodePNG);
        }
        catch(IOException | IllegalArgumentException | PrinterException e){
           log.info(String.format("FAIL: at PRINT with raw=%d\n%s", rawBytes.length, e));
            return 0;
        }
        log.info(String.format("raw=%d, compressed=%d", rawBytes.length, compressedBytes.length));
        return rawBytes.length;
    }

    // Loops through a bytestream representing blocks converted to bytes and tries to encode them
    private void tryCompression(ICompressor compressor, int steps, Logger logger){
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
        }while(findBestPerformance(bytesToTry.toByteArray(), compressor, logger) > 0);
        if(bytesToTry.size() < TARGET){
            fail("Performance below target.");
        }
    }

    // Tries the compression algorithms in seperate threads
    @Test
    public void testCompressionPerformance() {
        ConfigurationBuilder<BuiltConfiguration> builder
                = ConfigurationBuilderFactory.newConfigurationBuilder();
        addPerformanceLogger(builder, "pass_through", PASS_THROUGH_LOG_PATH);
        addPerformanceLogger(builder, "run_length", RUN_LENGTH_LOG_PATH);
        LoggerContext ctx = Configurator.initialize(builder.build());

        new Thread(() -> {
            tryCompression(new PassThroughCompressor(), 200,
                    ctx.getLogger("pass_through"));
            mFinishMutex.lock();
            mTestThreadCount--;
            mFinishMutex.unlock();
        }).start();

        new Thread(() -> {
            tryCompression(new RunLengthCompressor(), 500,
                    ctx.getLogger("run_length"));
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
