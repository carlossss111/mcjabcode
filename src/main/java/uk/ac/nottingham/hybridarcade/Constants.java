package uk.ac.nottingham.hybridarcade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Global Constants
 * @author Daniel Robinson 2024
 */
public class Constants {
    public static final String MOD_ID = "hybridarcade";
    public static final Logger logger = LogManager.getLogger("arcadelogger");

    public static final String MARKER_BLOCK_ID = "marker_block";
    public static final String MARKER_BLOCK_ITEM_ID = "marker_block_item";
    public static final String COPY_WAND_ID = "copy_wand";
    public static final String PASTE_WAND_ID = "paste_wand";

    public static final byte RESERVED_FOR_COMPRESSION_TK = (byte) 255;
    public static final String RESERVED_KEYWORD = "RESERVED";

    public static final String SCANNER_URI = "http://danrasp.local:5000";
}
