package ru.nsu.fit.galios;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            printHelp();
        }

        try {
            File imageFile = new File(args[1]);
            BufferedImage image = ImageIO.read(imageFile);
            if (args[0].equals("-hide")) {
                if (args.length == 2) {
                    Steganography.hideText(image, getUserText());
                } else if (args.length == 3) {
                    File file = new File(args[2]);
                    Steganography.hideText(image, Files.readAllBytes(file.toPath()));
                } else {
                    return;
                }
                File outputFile = new File("hidden_" + imageFile.getName());
                ImageIO.write(image, "png", outputFile);

                System.out.println("Hidden in " + outputFile.getName());
            } else if(args[0].equals("-get")) {
                byte[] text = Steganography.getText(image);
                if (args.length == 2) {
                    System.out.println("Text is: " + new String(text));
                } else if (args.length == 3) {
                    File file = new File(args[2]);
                    Files.write(Paths.get(file.getPath()), text);
                    System.out.println("Text is written in " + file.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static byte[] getUserText() {

        Scanner in = new Scanner(System.in);
        System.out.println("Please write a line you want to hide:");
        String str = in.nextLine();

        return str.getBytes();
    }

    private static void printHelp() {
        System.err.println("Arguments: (-hide|-get) <file> [inputFile | outputFile]");
    }

}
