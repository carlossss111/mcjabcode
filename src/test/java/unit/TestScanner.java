package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.hardware.Scanner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestScanner {
    private final String NETWORK_RETURNED_FILE = "unittest/jabcode.png";

    HttpClient mMockClient;
    HttpResponse mMockResponse;
    BufferedImage mMockReturnedImage;
    byte[] mMockReturnedImageBytes;

    Scanner mScanner;

    @BeforeEach
    public void setup() throws Exception {
        mMockClient = mock(HttpClient.class);
        mMockResponse = mock(HttpResponse.class);

        File inputFile = new File(getClass()
                .getClassLoader().getResource(NETWORK_RETURNED_FILE).getPath());
        mMockReturnedImage = ImageIO.read(inputFile);
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        ImageIO.write(mMockReturnedImage, "png", imageStream);
        mMockReturnedImageBytes = imageStream.toByteArray();

        mScanner = new Scanner(mMockClient);
    }

    @Test
    public void testReturnsImageOn200() throws Exception {
        when(mMockResponse.statusCode()).thenReturn(200);
        when(mMockResponse.body()).thenReturn(mMockReturnedImageBytes);
        when(mMockClient.send(any(), any())).thenReturn(mMockResponse);

        BufferedImage returnedImage =  mScanner.scan();

        assertEquals(200, mMockResponse.statusCode());
        assertTrue(Utility.areImagesTheSame(returnedImage, mMockReturnedImage));
    }

    @Test
    public void testThrowsOnBadStatusCode() throws Exception{
        when(mMockResponse.statusCode()).thenReturn(401);
        when(mMockClient.send(any(), any())).thenReturn(mMockResponse);

        assertThrows(IOException.class, () -> mScanner.scan());
    }

    @Test
    public void testThrowsOnInterruption() throws Exception{
        when(mMockClient.send(any(), any())).thenThrow(InterruptedException.class);

        assertThrows(IOException.class, () -> mScanner.scan());
    }

}
