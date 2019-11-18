package ru.nsu.galios.vernam;

import java.util.Random;

public class Vernam {

    private final byte[] input;
    private final byte[] key;

    public Vernam(byte[] input) {
        this.input = input;
        this.key = formKey(input);
    }

    public Vernam(byte[] input, byte[] key) {
        this.input = input;
        this.key = key;
    }

    private byte[] formKey(byte[] input) {
        byte[] key = new byte[input.length];

        Random rand = new Random();
        rand.nextBytes(key);
        return key;
    }

    public byte[] getKey() {
        return this.key;
    }

    public byte[] encode() {
        byte[] output = new byte[input.length];
        for (int i = 0; i < output.length; ++i) {
            output[i] = (byte) (input[i] ^ key[i]);
        }
        return output;
    }
}
