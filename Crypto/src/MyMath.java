import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MyMath {

    private static final List<Long> primes = new LinkedList<>();

    static {
        for (int i = 0; i < 100000; ++i) {
            if (isPrime(i)) {
                primes.add((long) i);
            }
        }
    }

    public static long pow(long base, long exp, long mod) {
        long res = 1;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                res = base * res % mod;
            }
            base = base * base % mod;
            exp >>= 1;
        }
        return res;
    }

    public static long generateBigNumber() {
        Random rand;
        rand = new Random();

        //return rand.nextInt();
        return rand.nextLong();
    }

    public static long generateBigNumber(long modulus) {

        Random rand;
        rand = new Random();

        return rand.nextInt((int) Math.min(Integer.MAX_VALUE, modulus));
    }

    public static long generatePrime() {
        Random rand;
        rand = new Random();

        return primes.get(rand.nextInt(primes.size()));
    }

    private static boolean isPrime(int n) {
        // Corner cases
        if (n <= 1) return false;
        if (n <= 3) return true;

        // This is checked so that we can skip
        // middle five numbers in below loop
        if (n % 2 == 0 || n % 3 == 0) return false;

        for (int i = 5; i * i <= n; i = i + 6)
            if (n % i == 0 || n % (i + 2) == 0)
                return false;

        return true;
    }

    public static long MODULO_INVERSUS(long a, long m)
    {
        long m0 = m;
        long y = 0, x = 1;

        if (m == 1)
            return 0;

        while (a > 1)
        {
            // q is quotient
            long q = a / m;
            long t = m;

            // m is remainder now, process same as
            // Euclid's algo
            m = a % m;
            a = t;
            t = y;

            // Update y and x
            y = x - q * y;
            x = t;
        }

        // Make x positive
        if (x < 0)
            x += m0;

        return x;
    }
}
