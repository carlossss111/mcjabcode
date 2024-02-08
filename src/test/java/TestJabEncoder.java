import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

public class TestJabEncoder {

    @Test
    public void testTemp(){
        new JabEncoder().encode(new byte[1], "test");

    }
}
