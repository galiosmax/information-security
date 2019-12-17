public class DiffieHellman {

    private long remainder;
    private long primeFirst;
    private long primeSecond;
    private long randNum;

    private long foreignNumber;
    private long secretKey;

    public static void algorithm() {

        long first = MyMath.generatePrime();
        long second = MyMath.generatePrime();

        DiffieHellman alice = new DiffieHellman(first, second);
        DiffieHellman bob = new DiffieHellman(first, second);

        bob.receive(alice.send());
        alice.receive(bob.send());

        System.out.println("Alice secret number: " + alice.getSecretKey());
        System.out.println("Bob secret number: " + bob.getSecretKey());
    }

    private DiffieHellman(long primeFirst, long primeSecond) {
        this.primeFirst = primeFirst;
        this.primeSecond = primeSecond;
        this.randNum = MyMath.generateBigNumber();

        this.remainder = MyMath.pow(this.primeFirst, this.randNum, this.primeSecond);
    }

    private long send() {
        return this.remainder;
    }

    private void receive(long number) {
        this.foreignNumber = number;
        this.secretKey = MyMath.pow(this.foreignNumber, this.randNum, this.primeSecond);
    }

    private long getSecretKey() {
        return this.secretKey;
    }

}
