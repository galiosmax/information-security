package ru.nsu.fit.galios;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class Steganography {


    public static void hideText(BufferedImage image, byte[] userText) {

        int width = image.getWidth();
        int height = image.getHeight();

        int x = 0;
        int y = 0;

        byte[] length = ByteBuffer.allocate(4).putInt(userText.length).array();
        byte[] array = new byte[4 + userText.length];

        System.arraycopy(length, 0, array, 0, 4);
        System.arraycopy(userText, 0, array, 4, userText.length);

        for (byte b : array) {
            for (int j = 7; j >= 0; --j) {
                Color color = new Color(image.getRGB(x, y));
                int blue = color.getBlue();
                int bit = ((b >>> j) & 1);
                blue = ((blue & 0xFE) | bit);
                image.setRGB(x, y, new Color(color.getRed(), color.getGreen(), blue & 0xFF).getRGB());
                if (x < width - 1) {
                    ++x;
                } else if (y < height - 1) {
                    x = 0;
                    ++y;
                } else {
                    System.out.println("Wrote only " + (width * height) / 8 + " symbols. Sorry :(");
                    return;
                }
            }
        }

        System.out.println("X: " + x + " Y: " + y);
    }

    public static byte[] getText(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        int x = 0;
        int y = 0;

        int length = 0;

        for (int i = 31; i >= 0; --i) {
            Color color = new Color(image.getRGB(x, y));
            int blue = color.getBlue();
            int bit = blue & 1;
            length += bit << i;
            if (x < width - 1) {
                ++x;
            } else if (y < height - 1) {
                x = 0;
                ++y;
            } else {
                System.out.println("Can't read length");
            }
        }

        System.out.println("Len: " + length);
        byte[] message = new byte[length];

        for (int i = 0; i < length; ++i) {
            for (int j = 7; j >= 0; --j) {
                Color color = new Color(image.getRGB(x, y));
                int blue = color.getBlue();
                int bit = blue & 1;
                message[i] += bit << j;
                if (x < width - 1) {
                    ++x;
                } else if (y < height - 1) {
                    x = 0;
                    ++y;
                } else {
                    System.out.println("Can't read all message");
                    return message;
                }
            }
        }
        return message;
    }
}
