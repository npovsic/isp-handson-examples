package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.BitSet;

public class HandsOnAssignment_Steganography {
    public static void main(String[] args) throws Exception, IOException, NoSuchAlgorithmException {
        final byte[] payload = "My message to you, Alice.".getBytes(StandardCharsets.UTF_8);

        System.out.printf("Encode: %s%n", new String(payload, StandardCharsets.UTF_8));

        System.out.println("Payload size: " + payload.length);

        HandsOnAssignment_Steganography.encodeImage(payload, "images/white_image.png", "images/steganogram_from_white.png");

        byte[] decoded = HandsOnAssignment_Steganography.decodeImage("images/steganogram_from_white.png");

        System.out.printf("Decoded from white: %s%n", new String(decoded, StandardCharsets.UTF_8));

        HandsOnAssignment_Steganography.encodeImage(payload, "images/black_image.png", "images/steganogram_from_black.png");

        decoded = HandsOnAssignment_Steganography.decodeImage("images/steganogram_from_black.png");

        System.out.printf("Decoded from black: %s%n", new String(decoded, StandardCharsets.UTF_8));

        HandsOnAssignment_Steganography.encodeImage(payload, "images/ljubljana.png", "images/steganogram_from_ljubljana.png");

        decoded = HandsOnAssignment_Steganography.decodeImage("images/steganogram_from_ljubljana.png");

        System.out.printf("Decoded from Ljubljana: %s%n", new String(decoded, StandardCharsets.UTF_8));

        /*
        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        HandsOnAssignment_Steganography.encryptAndEncode(payload, "images/2_Morondava.png", "images/steganogram-encrypted.png", key);
        final byte[] decodedEncrypted = HandsOnAssignment_Steganography.decryptAndDecode("images/steganogram-encrypted.png", key);

        System.out.printf("Decoded: %s%n", new String(decodedEncrypted, "UTF-8"));
        */
    }

    public static void encodeImage(final byte[] plaintext, final String inFile, final String outFile) throws IOException {
        // Load the source image
        final BufferedImage image = loadImage(inFile);

        /*
            We will add the plaintext length to the first 4 bytes (32 bits)
         */
        final byte[] newPT = ByteBuffer.allocate(4 + plaintext.length)
                .putInt(plaintext.length)
                .put(plaintext)
                .array();

        // Convert byte array to bit sequence
        final BitSet bits = BitSet.valueOf(newPT);

        // Encode the bits into image
        encode(bits, image);

        // Save the modified image with encoded data into outFile
        saveImage(outFile, image);
    }

    public static byte[] decodeImage(final String fileName) throws IOException {
        // Load the image that contains the encoded data
        final BufferedImage image = loadImage(fileName);

        // Read all the least significant bits
        final BitSet bits = decode(image);

        byte[] payload = bits.toByteArray();

        // Convert the bits to a byte array -> this is our decoded plaintext
        return Arrays.copyOfRange(payload, 4, payload.length);
    }

    public static void encryptAndEncode(final byte[] pt, final String inFile, final String outFile, final Key key) throws Exception {
    }

    public static byte[] decryptAndDecode(final String fileName, final Key key) throws Exception {
        return null;
    }

    protected static BufferedImage loadImage(final String filePath) throws IOException {
        return ImageIO.read(new File(filePath));
    }

    protected static void saveImage(String filePath, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(filePath));
    }

    protected static void encode(final BitSet payload, final BufferedImage image) {
        int minX = image.getMinX();
        int minY = image.getMinY();

        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = minX, indexOfCurrentBit = 0; x < width && indexOfCurrentBit < payload.size(); x++) {
            for (int y = minY; y < height && indexOfCurrentBit < payload.size(); y++) {
                int pixelValue = image.getRGB(x, y);

                Color color = new Color(pixelValue);

                int[] colors = new int[] {
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()
                };

                for (int i = 0; i < colors.length; i++) {
                    boolean currentBit = payload.get(indexOfCurrentBit);

                    colors[i] = setLeastSignificantBitForColor(colors[i], currentBit);

                    // We only need to process the image until all the payload values are encoded
                    if (indexOfCurrentBit < payload.size()) indexOfCurrentBit++;
                }

                // Create a new color object with the modified values
                final Color modifiedColor = new Color(colors[0], colors[1], colors[2]);

                // Replace the current pixel with the new color
                image.setRGB(x, y, modifiedColor.getRGB());
            }
        }
    }

    protected static BitSet decode(final BufferedImage image) {
        final BitSet bits = new BitSet();

        int minX = image.getMinX();
        int minY = image.getMinY();

        int width = image.getWidth();
        int height = image.getHeight();

        // To get the payload size we need to process the first 4 bytes (32 bits) which gives us the length int
        int payloadSize = 4 * 8;

        boolean foundLength = false;

        for (int x = minX, indexOfCurrentBit = 0; x < width && indexOfCurrentBit < payloadSize; x++) {
            for (int y = minY; y < height && indexOfCurrentBit < payloadSize; y++) {
                int pixelValue = image.getRGB(x, y);

                Color color = new Color(pixelValue);

                int[] colors = new int[] {
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()
                };

                for (int singleColor : colors) {
                    bits.set(indexOfCurrentBit, getLeastSignificantBitFromColor(singleColor));

                    // We only need to process the image until all the payload values are encoded
                    if (indexOfCurrentBit < payloadSize) indexOfCurrentBit++;
                    else if (!foundLength) {
                        // We processed the first 4 bytes, now we get the size
                        payloadSize += (bitSetToInt(bits) * 8);

                        foundLength = true;

                        indexOfCurrentBit++;
                    }
                }
            }
        }

        return bits;
    }

    protected static int setLeastSignificantBitForColor(int colorValue, boolean payloadBit) {
        if (payloadBit) {
            colorValue = colorValue | 0x01; // sets LSB to 1
        } else {
            colorValue = colorValue & 0xfe; // sets LSB to 0
        }

        return colorValue;
    }

    protected static boolean getLeastSignificantBitFromColor(int colorValue) {
        return (colorValue & 0x01) != 0;
    }

    public static int bitSetToInt(BitSet bitSet) {
        int bitInteger = 0;

        for (int i = 0; i < 32; i++) {
            if (bitSet.get(i)) {
                bitInteger |= (1 << i);
            }
        }

        // I don't get why I need to divide by 2^24, but this works
        return (int) (bitInteger / (Math.pow(2, 24)));
    }
}
