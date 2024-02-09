import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import java.nio.charset.StandardCharsets;

public class TestJabEncoder {

    @Test
    public void testTemp(){
        byte[] bytesTest = new byte[4];
        bytesTest[0] = 65;
        bytesTest[1] = 66;
        bytesTest[2] = 67;
        bytesTest[3] = 0;

        new JabEncoder().encode(bytesTest);

    }
}
