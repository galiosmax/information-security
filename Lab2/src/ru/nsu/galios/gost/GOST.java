package ru.nsu.galios.gost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class GOST {

    private byte[] H = new byte[32];
    private byte[] S = new byte[32];

    private final static byte[][] table = {
            {4, 10, 9, 2, 13, 8, 0, 14, 6, 11, 1, 12, 7, 15, 5, 3},
            {14, 11, 4, 12, 6, 13, 15, 10, 2, 3, 8, 1, 0, 7, 5, 9},
            {5, 8, 1, 13, 10, 3, 4, 2, 14, 15, 12, 7, 6, 0, 9, 11},
            {7, 13, 10, 1, 0, 8, 9, 15, 14, 4, 6, 12, 11, 2, 5, 3},
            {6, 12, 7, 1, 5, 15, 13, 8, 4, 10, 9, 14, 0, 3, 11, 2},
            {4, 11, 10, 0, 7, 2, 1, 13, 3, 6, 8, 5, 9, 12, 15, 14},
            {13, 11, 4, 1, 3, 15, 5, 9, 0, 10, 14, 7, 6, 8, 2, 12},
            {1, 15, 13, 0, 5, 7, 10, 4, 9, 2, 3, 14, 6, 11, 8, 12}
    };

    private byte[][] key = new byte[8][4];

    private final static int[] keyMap = {0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 7, 6, 5, 4, 3, 2, 1, 0};

    public void rpz(Mode mode, DataOutputStream dos, DataInputStream dis) throws IOException {
        byte[] data = new byte[8];
        int count = dis.read(data);
        while (count != -1) {
            if (count < 8) {
                for (int i = count; i < 8; i++) {
                    data[i] = 0;
                }
            }
            data = calc(data, mode);
            dos.write(data, 0, data.length);

            count = dis.read(data);
        }
        dis.close();
        dos.close();
    }

    public byte[] calcHash(DataInputStream dis) throws IOException {

        int size = 0;
        Arrays.fill(H, (byte) 0);
        Arrays.fill(S, (byte) 0);
        byte[] xorSum = new byte[32];

        int count = dis.read(S);

        while (count != -1) {
            size += count;
            if (count < 32) {
                for (int i = count; i < 32; i++) {
                    S[i] = 0;
                }
            }
            for (int i = 0; i < 32; i++) {
                xorSum[i] ^= S[i];
            }
            H = stepHash(H, S);
            count = dis.read(S);
        }

        return endSteps(H, size, xorSum);
    }

    private void setKey(byte[][] key) {
        this.key = key;
    }

    private byte[] calc(byte[] data, Mode mode) {
        byte[] B = Arrays.copyOfRange(data, 0, 4);
        byte[] A = Arrays.copyOfRange(data, 4, 8);

        for (int k = 0; k < 32; k++) {
            byte[] K = Mode.ENCRYPT.equals(mode) ? key[keyMap[k]] : key[keyMap[31 - k]];
            int buf = ByteBuffer.wrap(A).getInt() + ByteBuffer.wrap(K).getInt();
            buf &= 0xffffffff; // A + K (mod 2^32)
            int[] s = {
                    (buf & 0xF0000000) >>> 28,
                    (buf & 0x0F000000) >>> 24,
                    (buf & 0x00F00000) >>> 20,
                    (buf & 0x000F0000) >>> 16,
                    (buf & 0x0000F000) >>> 12,
                    (buf & 0x00000F00) >>> 8,
                    (buf & 0x000000F0) >>> 4,
                    (buf & 0x0000000F)
            };
            buf = 0x00000000;
            for (int b = 0; b < 8; b++) {
                buf <<= 4;
                buf += table[b][s[b] & 0x0000000f];
            }
            buf = ((buf << 11) | (buf >>> 21));
            byte[] resBytes = ByteBuffer.allocate(4).putInt(buf).array();
            byte[] newB = {0x00, 0x00, 0x00, 0x00};

            System.arraycopy(A, 0, newB, 0, 4);
            for (int b = 0; b < 4; b++) {
                A[b] = (byte) (resBytes[b] ^ B[b]);
            }
            System.arraycopy(newB, 0, B, 0, 4);
        }
        byte[] newData = new byte[8];
        System.arraycopy(A, 0, newData, 0, A.length);
        System.arraycopy(B, 0, newData, 4, B.length);
        return newData;
    }

    private byte[][] K = {new byte[32], new byte[32], new byte[32], new byte[32]};

    private byte[] stepHash(byte[] prev, byte[] mes) {

        generateKey(prev, mes);
        byte[][] h = divideArray(prev, 4);
        byte[][] s = {performEncode(h[0], K[0]), performEncode(h[1], K[1]), performEncode(h[2], K[2]), performEncode(h[3], K[3])};

        byte[] S = collapseArray(s);

        for (int i = 0; i < 12; i++) {
            S = psi(S);
        }

        byte[] res = psi(getXorArray(prev, psi(getXorArray(mes, S))));
        for (int i = 0; i < 60; i++) {
            res = psi(res);
        }

        return res;
    }

    private byte[] endSteps(byte[] prev, long size, byte[] xorSum) {

        long[] longsSize = {0, 0, 0, size};
        byte[] bytesSize = longToBytes(longsSize);

        return stepHash(stepHash(prev, bytesSize), xorSum);
    }

    private byte[][] C = {new byte[32], new byte[32], new byte[32]};
    private final long[] C1 = {0xff00ffff, 0x000000ff, 0xff0000ff, 0x00ffff00, 0x00ff00ff, 0x00ff00ff, 0xff00ff00, 0xff00ff00};

    private void generateKey(byte[] prev, byte[] mes) {
        Arrays.fill(C[0], (byte) 0);
        Arrays.fill(C[2], (byte) 0);
        C[1] = longToBytes(C1);
        byte[] U = Arrays.copyOf(prev, 32);
        byte[] V = Arrays.copyOf(mes, 32);
        byte[] W = getXorArray(U, V);
        K[0] = P(W);

        for (int i = 0; i < 3; i++) {
            U = getXorArray(A(U), C[i]);
            V = A(A(V));
            W = getXorArray(U, V);
            K[i + 1] = P(W);
        }
    }

    private byte[] performEncode(byte[] mes, byte[] key) {
        setKey(divideArray(key, 8));
        return calc(mes, Mode.ENCRYPT);
    }

    private byte[] psi(byte[] array) {
        byte[][] a = divideArray(array, 16);

        byte[] xorSum = Arrays.copyOf(a[0], 2);

        for (int i = 1; i < 16; i++) {
            xorSum[0] ^= a[i][0];
            xorSum[1] ^= a[i][1];
        }

        byte[] result = new byte[32];
        result[0] = xorSum[0];
        result[1] = xorSum[1];

        for (int i = 1; i < 16; i++) {
            result[2 * i] = a[16 - i][0];
            result[2 * i + 1] = a[16 - i][1];
        }
        return result;
    }

    private byte[][] divideArray(byte[] array, int parts) {

        int size = array.length / parts;

        byte[][] result = new byte[parts][size];
        for (int i = 0; i < parts; i++) {
            for (int j = 0; j < size; j++) {
                result[i][j] = array[i + j * parts];
            }
        }
        return result;
    }

    private byte[] collapseArray(byte[][] array) {

        int size = array[0].length;
        byte[] result = new byte[array.length * size];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < size; j++) {
                result[i] = array[i][j];
            }
        }
        return result;
    }

    private byte[] getXorArray(byte[] a, byte[] b) {
        byte[] result = Arrays.copyOf(a, a.length);

        for (int i = 0; i < a.length; i++) {
            result[i] ^= b[i];
        }
        return result;
    }

    private byte[] A(byte[] array) {

        long[] longArray = bytesToLong(array);
        long[] convertedArray = new long[4];

        convertedArray[0] = longArray[0] ^ longArray[1];
        convertedArray[1] = longArray[3];
        convertedArray[2] = longArray[2];
        convertedArray[3] = longArray[1];

        return longToBytes(convertedArray);
    }

    private final byte[] fi = {1, 9, 17, 25, 2, 10, 18, 26, 3, 11, 19, 27, 4, 12, 20, 28, 5, 13, 21, 29, 6, 14, 22, 30, 7, 15, 23, 31, 8, 16, 24, 32};

    private byte[] P(byte[] array) {

        byte[] convertedArray = new byte[32];
        for (int i = 0; i < 32; i++) {
            convertedArray[i] = array[fi[31 - i] - 1];
        }
        return convertedArray;
    }

    private byte[] longToBytes(long[] array) {

        int length = array.length;
        byte[] byteArray = new byte[length * Long.BYTES];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < Long.BYTES; j++) {
                byteArray[i * Long.BYTES + (Long.BYTES - 1 - j)] = (byte) (array[i] >>> j * Byte.SIZE);
            }
        }
        return byteArray;
    }

    private long[] bytesToLong(byte[] array) {
        int length = array.length / Long.BYTES;
        long[] longArray = new long[length];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < Long.BYTES; j++) {
                longArray[i] <<= Byte.SIZE;
                longArray[i] |= (array[j] & 0xFF);
            }
        }
        return longArray;
    }

    public enum Mode {
        ENCRYPT,
        DECRYPT
    }
}