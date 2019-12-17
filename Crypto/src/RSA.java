public class RSA {

    private long first;
    private long second;
    private long modulus;
    private long euler;
    private long publicExp;
    private long secretExp;

    public static void algorithm() {

        long first = MyMath.generatePrime();
        long second = MyMath.generatePrime();

        while (first == second || first * second < 0) {
            second = MyMath.generatePrime();
        }

        RSA alice = new RSA(first, second);
        RSA bob = new RSA(first, second);

        bob.receive(alice.send(bob));
        alice.receive(bob.send(alice));
    }

    private RSA(long first, long second) {

        modulus = first * second;
        euler = (first - 1) * (second - 1);
        publicExp = MyMath.generatePrime();
        while (publicExp >= euler) {
            publicExp = MyMath.generatePrime();
        }
        secretExp = MyMath.MODULO_INVERSUS(publicExp, euler);
    }

    public long send(RSA user) {
        long message = MyMath.generateBigNumber(modulus);

        System.out.println("Sending message: " + message);
        return MyMath.pow(message, user.getPublicExp(), user.getModulus());
    }

    public long receive(long encoded) {
        if (encoded > modulus) {
            encoded %= modulus;
        }
        long message = MyMath.pow(encoded, secretExp, modulus);
        System.out.println("Received message: " + message);
        return message;
    }

    public long getPublicExp() {
        return publicExp;
    }

    public long getModulus() {
        return modulus;
    }
}
