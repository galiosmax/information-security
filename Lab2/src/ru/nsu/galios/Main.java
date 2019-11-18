package ru.nsu.galios;

import ru.nsu.galios.vernam.Vernam;

import java.io.*;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) {

        if (args.length == 3 && args[0].equals("-e")) {
            try {
                if (args[1].equals("-vernam")) {
                    encodeVernam(new File(args[2]));
                } else if (args[1].equals("-gost")) {
                    encodeGOST(new File(args[2]));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Can't write to file");
            }
        } else if (args.length == 4 && args[0].equals("-d")) {
            try {
                if (args[1].equals("-vernam")) {
                    decodeVernam(new File(args[2]), new File(args[3]));
                } else if (args[1].equals("-gost")) {
                    decodeGOST(new File(args[2]), new File(args[3]));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Can't read files");
            }
        } else {
            System.out.println("Wrong arguments");
            printHelp();
        }

    }

    private static void encodeVernam(File file) throws IOException {

        String parent = file.getParent();

        File encodedFile, keyFile;
        if (parent != null) {
            encodedFile = new File(file.getParent() + "\\encoded_" + file.getName());
            keyFile = new File(file.getParent() + "\\key_" + file.getName());
        } else {
            encodedFile = new File("encoded_" + file.getName());
            keyFile = new File("\\key_" + file.getName());
        }

        Vernam vernam = new Vernam(Files.readAllBytes(file.toPath()));

        FileOutputStream keyWriter = new FileOutputStream(keyFile);
        keyWriter.write(vernam.getKey());
        System.out.println("Printed key in " + keyFile.getName());

        FileOutputStream encodedWriter = new FileOutputStream(encodedFile);
        encodedWriter.write(vernam.encode());
        System.out.println("Printed encoded message in " + encodedFile.getName());
    }

    private static void decodeVernam(File input, File key) throws IOException {
        File decodedFile = new File(input.getParent() + "/decoded_" + input.getName());

        Vernam vernam = new Vernam(Files.readAllBytes(input.toPath()), Files.readAllBytes(key.toPath()));

        FileOutputStream decodedWriter = new FileOutputStream(decodedFile);
        decodedWriter.write(vernam.encode());
        System.out.println("Printed encoded message in " + decodedFile.getName());
    }

    private static void encodeGOST(File input) throws  IOException {

        String parent = file.getParent();

        File encodedFile, keyFile;
        if (parent != null) {
            encodedFile = new File(file.getParent() + "\\encoded_" + file.getName());
            keyFile = new File(file.getParent() + "\\key_" + file.getName());
        } else {
            encodedFile = new File("encoded_" + file.getName());
            keyFile = new File("\\key_" + file.getName());
        }


    }
    private static void printHelp() {
        System.out.println("Arguments: option file");
        System.out.println("option : ");
        System.out.println("-d decodeVernam");
        System.out.println("-e encodeVernam");
        System.out.println("file : input file with message / encoded message");
    }
}
