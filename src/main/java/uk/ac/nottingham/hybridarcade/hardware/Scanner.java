package uk.ac.nottingham.hybridarcade.hardware;

import uk.ac.nottingham.hybridarcade.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Scanner {
    private HttpClient mClient;

    public Scanner(HttpClient client){
        mClient = client;
    }

    private byte[] networkScan(URI uri) throws IOException {
        // Construct request
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri).timeout(Duration.ofSeconds(5))
                .GET().build();

        // Send request and receive bytestream of images
        HttpResponse<byte[]> response;
        try {
            response = mClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
        }
        catch(InterruptedException e){
            Constants.logger.error("HTTP request was interrupted.\n" + e);
            throw new IOException(e);
        }

        // Verify response and return
        if(response.statusCode() == 200){
            return response.body();
        }
        else{
            throw new IOException("Failed to scan from network, status code: " +
                    response.statusCode());
        }
    }

    public BufferedImage scan() throws IOException {
        byte[] scanResponse = networkScan(URI.create(Constants.SCANNER_URI));
        return ImageIO.read(new ByteArrayInputStream(scanResponse));
    }
}
