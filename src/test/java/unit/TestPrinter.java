package unit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.hardware.Printer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestPrinter {
    private static final String BARCODE_PATH = "unittest/jabcode.png";

    private Printer mPrinter;
    private PrinterJob mPrintJobMock;
    private BufferedImage mBarcode;

    @BeforeEach
    public void setup(){
        mPrintJobMock = mock(PrinterJob.class);
        mPrinter = new Printer(mPrintJobMock);

        try {
            File expectedFile = new File(getClass()
                    .getClassLoader().getResource(BARCODE_PATH).getPath());
            mBarcode = ImageIO.read(expectedFile);
        }
        catch(Exception e){
            throw new RuntimeException("Error setting up TestPrinter.java.\n" + e);
        }
    }

    @AfterAll
    public static void cleanup(){
        File f = new File("./failed_print.pdf");
        if(!f.delete()){
            fail("Error cleaning up failed_print.pdf, should be created by" +
                    "testPrinterThrows()");
        }
    }

    @Test
    public void testPageDimensions() throws Exception{
        PDDocument resultDoc = mPrinter.print(mBarcode);
        float mmWidth = 54.f, mmHeight = 84.f;

        assertEquals(mmWidth * Printer.POINTS_PER_MM,
                resultDoc.getPage(0).getMediaBox().getWidth());
        assertEquals(mmHeight * Printer.POINTS_PER_MM,
                resultDoc.getPage(0).getMediaBox().getHeight());
    }

    @Test
    public void testPrints() throws Exception {
        mPrinter.print(mBarcode);
        verify(mPrintJobMock).print();
    }

    @Test
    public void testPrinterThrows() throws Exception {
        doThrow(PrinterException.class).when(mPrintJobMock).print();
        assertThrows(PrinterException.class, () -> {
            mPrinter.print(mBarcode);
        });
    }
}
