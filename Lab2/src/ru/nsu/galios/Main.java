package ru.nsu.galios;

import ru.nsu.galios.gost.GOST;
import ru.nsu.galios.vernam.Vernam;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        if (args.length == 2 && args[0].equals("-hash")) {
            try {
                hash(new File(args[1]));
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Can't write to file");
            }
        } else if (args.length == 3 && args[0].equals("-e")) {
            try {
                if (args[1].equals("-vernam")) {
                    encode(new File(args[2]), Algorithm.VERNAM);
                } else if (args[1].equals("-gost")) {
                    encode(new File(args[2]), Algorithm.GOST);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Can't write to file");
            }
        } else if (args.length == 3 && args[0].equals("-d")) {
            try {
                if (args[1].equals("-gost")) {
                    decode(new File(args[2]), null, Algorithm.GOST);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Can't read files");
            }
        } else if (args.length == 4 && args[0].equals("-d")) {
            try {
                if (args[1].equals("-vernam")) {
                    decode(new File(args[2]), new File(args[3]), Algorithm.VERNAM);
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

    private static void hash(File file) throws IOException {
        GOST gost = new GOST();
        DataInputStream stream = new DataInputStream(new FileInputStream(file));
        byte[] array = gost.calcHash(stream);
        System.out.println("Hash: \n" + new String(array));
        System.out.println("Hash: \n" + Arrays.toString(array));
    }

    private static void encode(File file, Algorithm algorithm) throws IOException {
        String parent = file.getParent();

        File encodedFile, keyFile;
        if (parent != null) {
            encodedFile = new File(parent + "\\encoded_" + file.getName());
            keyFile = new File(parent + "\\key_" + file.getName());
        } else {
            encodedFile = new File("encoded_" + file.getName());
            keyFile = new File("key_" + file.getName());
        }

        byte[] key = null;
        byte[] encoded = null;
        if (algorithm == Algorithm.VERNAM) {
            Vernam vernam = new Vernam(Files.readAllBytes(file.toPath()));
            key = vernam.getKey();
            encoded = vernam.encode();
        } else if (algorithm == Algorithm.GOST) {
            GOST gost = new GOST();
            DataInputStream stream = new DataInputStream(new FileInputStream(file));
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(encodedFile));
            try {
                gost.rpz(GOST.Mode.ENCRYPT, outputStream, stream);
                System.out.println("Printed encoded message in " + encodedFile.getName());
            } catch (Exception e) {
                System.out.println("Failed to encrypt " + file.getName());
            }
        }

        if (key != null) {
            FileOutputStream keyWriter = new FileOutputStream(keyFile);
            keyWriter.write(key);
            System.out.println("Printed key in " + keyFile.getName());
        }

        if (encoded != null) {
            FileOutputStream encodedWriter = new FileOutputStream(encodedFile);
            encodedWriter.write(encoded);
            System.out.println("Printed encoded message in " + encodedFile.getName());
        }
    }

    private static void decode(File input, File key, Algorithm algorithm) throws IOException {
        String parent = input.getParent();

        File decodedFile;
        if (parent != null) {
            decodedFile = new File(parent + "\\decoded_" + input.getName());
        } else {
            decodedFile = new File("decoded_" + input.getName());
        }

        byte[] encoded = null;
        if (algorithm == Algorithm.VERNAM) {
            Vernam vernam = new Vernam(Files.readAllBytes(input.toPath()), Files.readAllBytes(key.toPath()));
            encoded = vernam.encode();
        } else if (algorithm == Algorithm.GOST) {
            GOST gost = new GOST();
            DataInputStream stream = new DataInputStream(new FileInputStream(input));
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(decodedFile));
            try {
                gost.rpz(GOST.Mode.DECRYPT, outputStream, stream);
                System.out.println("Printed decoded message in " + decodedFile.getName());
            } catch (Exception e) {
                System.out.println("Failed to decrypt " + input.getName());
            }
        }

        if (encoded != null) {
            FileOutputStream decodedWriter = new FileOutputStream(decodedFile);
            decodedWriter.write(encoded);
            System.out.println("Printed decoded message in " + decodedFile.getName());
        }
    }

    private static void printHelp() {
        System.out.println("Arguments: option algorithm file(s)");
        System.out.println("option :");
        System.out.println("-d decode");
        System.out.println("-e encode");
        System.out.println("algorithm :");
        System.out.println("-vernam use vernam cypher");
        System.out.println("-gost use gost cypher");
        System.out.println("file(s) : input/output file with message / encoded message");
    }
}
