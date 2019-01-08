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
import java.util.BitSet;

public class HandsOnAssignment_Steganography {
    public static void main(String[] args) throws IOException {
        final byte[] payload = "My message to you.".getBytes(StandardCharsets.UTF_8);

        HandsOnAssignment_Steganography.encode(payload, "images/white_image.png", "images/steganogram_from_white.png");

        byte[] decoded = HandsOnAssignment_Steganography.decode("images/steganogram_from_white.png");

        System.out.printf("Decoded from white: %s%n", new String(decoded, StandardCharsets.UTF_8));

        HandsOnAssignment_Steganography.encode(payload, "images/black_image.png", "images/steganogram_from_black.png");

        decoded = HandsOnAssignment_Steganography.decode("images/steganogram_from_black.png");

        System.out.printf("Decoded from black: %s%n", new String(decoded, StandardCharsets.UTF_8));

        HandsOnAssignment_Steganography.encode(payload, "images/ljubljana.png", "images/steganogram_from_ljubljana.png");

        decoded = HandsOnAssignment_Steganography.decode("images/steganogram_from_ljubljana.png");

        System.out.printf("Decoded from Ljubljana: %s%n", new String(decoded, StandardCharsets.UTF_8));

        /*
        TODO: Assignment 2
        final SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        ImageSteganography.encryptAndEncode(payload, "images/2_Morondava.png", "images/steganogram-encrypted.png", key);
        final byte[] decodedEncrypted = ImageSteganography.decryptAndDecode("images/steganogram-encrypted.png", key);

        System.out.printf("Decoded: %s%n", new String(decodedEncrypted, "UTF-8")); */
    }

    public static void encode(final byte[] pt, final String inFile, final String outFile) throws IOException {
        // load the image
        final BufferedImage image = loadImage(inFile);

        // pt = len(pt) + pt
        final byte[] newPT = ByteBuffer.allocate(4 + pt.length)
                .putInt(pt.length)
                .put(pt)
                .array();

        // Convert byte array to bit sequence
        final BitSet bits = BitSet.valueOf(newPT);

        // encode the bits into image
        encode(bits, image);

        // save the modified image into outFile
        saveImage(outFile, image);
    }

    public static byte[] decode(final String fileName) throws IOException {
        // load the image
        final BufferedImage image = loadImage(fileName);

        // read all LSBs
        final BitSet bits = decode(image);

        // convert them to bytes
        return bits.toByteArray();
    }

    public static void encryptAndEncode(final byte[] pt, final String inFile, final String outFile, final Key key) throws Exception {
    }

    public static byte[] decryptAndDecode(final String fileName, final Key key) throws Exception {
        return null;
    }

    protected static BufferedImage loadImage(final String inFile) throws IOException {
        return ImageIO.read(new File(inFile));
    }

    protected static void saveImage(String outFile, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", new File(outFile));
    }

    protected static void encode(final BitSet payload, final BufferedImage image) {

    }

    protected static BitSet decode(final BufferedImage image) {
        final BitSet bits = new BitSet();

        return bits;
    }
}
