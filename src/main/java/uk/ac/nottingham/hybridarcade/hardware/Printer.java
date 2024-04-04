package uk.ac.nottingham.hybridarcade.hardware;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.printing.PDFPageable;
import uk.ac.nottingham.hybridarcade.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Prints out a card of a given width with the barcode centered.
 * @see PDDocument
 * @see PDPage
 * @see PrinterJob
 * @author Daniel Robinson 2024
 */
public class Printer {
    public static final float POINTS_PER_MM = 1 / (10 * 2.54f) * 72;

    private static final float PAGE_WIDTH_MM = 54.f;
    private static final float PAGE_HEIGHT_MM = 84.f;
    private static final float PAGE_WIDTH_PT = PAGE_WIDTH_MM * POINTS_PER_MM;
    private static final float PAGE_HEIGHT_PT = PAGE_HEIGHT_MM * POINTS_PER_MM;
    private static final float MIN_BARCODE_MODULE_SIZE_MM = 0.33f;

    private final static String FAILED_PRINT_PATH = "./failed_print.pdf";
    private final PrinterJob mPrintJob;

    /**
     * Constructor takes hardware arg.
     * @param printJob handle from {@link PrinterJob#getPrinterJob()}
     */
    public Printer(PrinterJob printJob){
        mPrintJob = printJob;
    }

    /**
     * Return the pixel size of a module in the barcode image.
     * @param barcode barcode to be examined
     * @return module size of barcode in pixels
     */
    // If we go diagonally up and right we are guaranteed to encounter
    // the finder pattern and can use this to determine the module width.
    public int findModuleWidthPx(BufferedImage barcode) throws IllegalArgumentException{
        final Color finderPatternPrimary = new Color(0x00FFFF);
        final Color finderPatternSecondary = new Color(0x000000);

        int lastCol = barcode.getRGB(0, 0), currentCol, moduleStart = 0;
        boolean finderPatternFound = false;
        for(int xy = 0; xy < barcode.getWidth(); xy++){
            currentCol = barcode.getRGB(xy,xy);
            if(currentCol != lastCol
                    && (currentCol == finderPatternPrimary.getRGB()
                    || currentCol == finderPatternSecondary.getRGB())){
                lastCol = currentCol;
                finderPatternFound = true;
                moduleStart = xy;
            }

            if(currentCol != lastCol && finderPatternFound){
                return xy - moduleStart;
            }
        }
        throw new IllegalArgumentException("Barcode has no finder pattern! (If we got this " +
                "far that's pretty bad.)");
    }

    /**
     * Returns a printable page in the form of a PDF. The barcode will be centered
     * on the page.
     * @param barcode Barcode to include on the page.
     * @return PDDocument file of PAGE_WIDTH_MM and PAGE_HEIGHT_MM containing the barcode.
     * @throws IOException if there is a problem creating the printable file.
     * @throws IllegalArgumentException if the barcode is too large to be printed with a minimum
     * module size of {@link #MIN_BARCODE_MODULE_SIZE_MM}.
     */
    private PDDocument createPrintable(byte[] barcode) throws IOException, IllegalArgumentException{
        // Initialize page
        PDDocument doc = new PDDocument();
        PDRectangle size = new PDRectangle(PAGE_WIDTH_PT, PAGE_HEIGHT_PT);
        PDPage page = new PDPage(size);

        // Position Image
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, barcode, null);
        PDPageContentStream content = new PDPageContentStream(doc, page);
        float targetImageWidth, targetImageHeight;
        targetImageWidth = targetImageHeight = PAGE_WIDTH_PT;

        // Validation that Module Width X >= MIN_BARCODE_MODULE_SIZE_MM
        int numOfModulesOnSide = pdImage.getWidth() / findModuleWidthPx(pdImage.getImage());
        if(numOfModulesOnSide * MIN_BARCODE_MODULE_SIZE_MM > PAGE_WIDTH_MM){
            throw new IllegalArgumentException("Barcode is too large to fit on the page, " +
                    "the minimum module width would be less than " + MIN_BARCODE_MODULE_SIZE_MM);
        }

        // Finish
        content.drawImage(pdImage,
                0,                                      // x
                PAGE_HEIGHT_PT/2 - targetImageHeight/2, // y
                targetImageWidth,                       // image width
                targetImageHeight                      // image height
        );
        content.close();
        doc.addPage(page);
        return doc;
    }

    /**
     * Prints the barcode centered on a blank page with {@link #PAGE_WIDTH_MM} and
     * {@link #PAGE_HEIGHT_MM}.
     * @param barcode barcode PNG to be printed
     * @throws IOException if either there is a problem creating the document.
     * @throws IllegalArgumentException if the barcode is too large to be printed with a minimum
     * module size of {@link #MIN_BARCODE_MODULE_SIZE_MM}.
     * @throws java.awt.print.PrinterException if there is a problem connecting to
     * the hardware printer.
     * @return created PDF document for further use (as an optional side-effect).
     */
    public PDDocument print(BufferedImage barcode) throws IOException, IllegalArgumentException, PrinterException {
        ByteArrayOutputStream asBytes = new ByteArrayOutputStream();
        ImageIO.write(barcode, "png", asBytes);
        PDDocument doc = createPrintable(asBytes.toByteArray());
        try{
            mPrintJob.setPageable(new PDFPageable(doc));
            mPrintJob.print();
            return doc;
        } catch (PrinterException e) {
            Constants.logger.info("Failed to print physically, saving to " + FAILED_PRINT_PATH);
            doc.save(FAILED_PRINT_PATH);
            throw e;
        }
    }
}
