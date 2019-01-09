package isp.handson;

import fri.isp.Agent;
import fri.isp.Environment;
import sun.security.util.BitArray;

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

/**
 * Steganography is the process of encoding information into an image by manipulating the bits that represent the colors
 */
public class HandsOnAssignment_Steganography {
    public static void main(String[] args) throws Exception, IOException, NoSuchAlgorithmException {
        final byte[] payload = "My message to you, Alice. Hello from Bob.".getBytes(StandardCharsets.UTF_8);

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

        BitArray bits = new BitArray(newPT.length * 8, newPT);

        // Encode the bits into image
        encode(bits, image);

        // Save the modified image with encoded data into outFile
        saveImage(outFile, image);
    }

    public static byte[] decodeImage(final String fileName) throws IOException {
        // Load the image that contains the encoded data
        final BufferedImage image = loadImage(fileName);

        // Read all the least significant bits
        final BitArray bits = decode(image);

        byte[] payload = bits.toByteArray();

        // Convert the bits to a byte array -> this is our decoded plaintext
        return payload;
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

    /**
     * Encode the payload into the least significant bits of all colors of a single pixel
     *
     * @param payload
     * @param image
     */
    protected static void encode(final BitArray payload, final BufferedImage image) {
        int minX = image.getMinX();
        int minY = image.getMinY();

        int width = image.getWidth();
        int height = image.getHeight();

        String bitsString = "";

        for (int x = minX, indexOfCurrentBit = 0; x < width && indexOfCurrentBit < payload.length() - 1; x++) {
            for (int y = minY; y < height && indexOfCurrentBit < payload.length() - 1; y++) {
                int pixelValue = image.getRGB(x, y);

                Color color = new Color(pixelValue);

                int[] colors = new int[] {
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()
                };

                // This example uses all the color channels to encode the payload
                for (int i = 0; i < colors.length; i++) {
                    boolean currentBit = payload.get(indexOfCurrentBit);

                    if (currentBit) bitsString += "1";
                    else bitsString += "0";

                    colors[i] = setLeastSignificantBitForColor(colors[i], currentBit);

                    // We only need to process the image until all the payload values are encoded
                    if (indexOfCurrentBit < payload.length() - 1) indexOfCurrentBit++;
                }

                // Create a new color object with the modified values
                final Color modifiedColor = new Color(colors[0], colors[1], colors[2]);

                // Replace the current pixel with the new color
                image.setRGB(x, y, modifiedColor.getRGB());
            }
        }

        System.out.println(bitsString);
    }

    /**
     * Retrieve the information encoded in the image
     * First we need to get the length of the whole payload, which is encoded in the first 32 bits (int)
     * Then we
     *
     * @param image
     * @return
     */
    protected static BitArray decode(final BufferedImage image) {
        // To get the payload size we need to process the first 4 bytes (32 bits) which gives us the length (int)
        int payloadSize = 4 * 8;

        BitArray bits = new BitArray(payloadSize);

        String bitsString = "";

        int minX = image.getMinX();
        int minY = image.getMinY();

        int width = image.getWidth();
        int height = image.getHeight();

        boolean foundLength = false;

        for (int x = minX, indexOfCurrentBit = 0; x < width && indexOfCurrentBit < payloadSize - 1; x++) {
            for (int y = minY; y < height && indexOfCurrentBit < payloadSize - 1; y++) {
                int pixelValue = image.getRGB(x, y);

                Color color = new Color(pixelValue);

                int[] colors = new int[] {
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()
                };

                for (int singleColor : colors) {
                    if (getLeastSignificantBitFromColor(singleColor)) bitsString += "1";
                    else bitsString += "0";

                    bits.set(indexOfCurrentBit, getLeastSignificantBitFromColor(singleColor));

                    // We only need to process the image until all the payload values are encoded
                    if (indexOfCurrentBit < payloadSize - 1) indexOfCurrentBit++;
                    else if (!foundLength) {
                        // We processed the first 4 bytes, now we get the size
                        payloadSize = bitsToInt(bits) * 8;

                        foundLength = true;

                        indexOfCurrentBit = 0;

                        bits = new BitArray(payloadSize);
                    }
                }
            }
        }

        System.out.println(bitsString);

        return bits;
    }

    /**
     * Encode the information into the least significant bit
     *
     * @param colorValue
     * @param payloadBit
     * @return
     */
    protected static int setLeastSignificantBitForColor(int colorValue, boolean payloadBit) {
        if (payloadBit) {
            // 0x01 = 00000001 -> use bitwise OR with this value to only set the LSB to 1
            colorValue = colorValue | 0x01;
        } else {
            // 0xfe = 11111110 -> use bitwise AND with this value to only set the LSB to 0
            colorValue = colorValue & 0xfe;
        }

        return colorValue;
    }

    /**
     * Returns the information encoded into the least significant bit
     *
     * @param colorValue
     * @return
     */
    protected static boolean getLeastSignificantBitFromColor(int colorValue) {
        // 0x01 = 00000001 -> use bitwise AND with this value to check if the LSB is either 0 or 1
        return (colorValue & 0x01) != 0;
    }

    /**
     * This function converts a bit array with the length of 32 to an integer, which is comprised of 4 bytes or 32 bits
     *
     * We need to take care of which endian to choose -> here we need the Big Endian, where the most significant byte
     * is in the lowest address (https://chortle.ccsu.edu/AssemblyTutorial/Chapter-15/ass15_3.html)
     *
     * @param bitArray
     * @return
     */
    public static int bitsToInt(BitArray bitArray) {
        int integerFromBits = 0;

        for (int i = 0; i < 32; i++) {
            if (bitArray.get(i)) {
                // This particular bit is on

                integerFromBits = integerFromBits | (1 << (32 - i - 1));
            }
        }

        return integerFromBits;
    }
}
