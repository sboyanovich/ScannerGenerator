package io.github.sboyanovich.scannergenerator.tests.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class CommonCharClasses {
    public static Set<String> digits;
    public static Set<String> capitalLatins;
    public static Set<String> lowercaseLatins;
    public static Set<String> letters;
    public static Set<String> alphanumerics;

    static {
        Set<String> digits = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            digits.add(String.valueOf(i));
        }
        CommonCharClasses.digits = Collections.unmodifiableSet(digits);

        Set<String> capitalLatins = new HashSet<>();
        int capA = asCodePoint("A");
        int capZ = asCodePoint("Z");
        for (int i = capA; i <= capZ; i++) {
            capitalLatins.add(asString(i));
        }
        CommonCharClasses.capitalLatins = Collections.unmodifiableSet(capitalLatins);

        Set<String> lowercaseLatins = new HashSet<>();
        int lcA = asCodePoint("a");
        int lcZ = asCodePoint("z");
        for (int i = lcA; i <= lcZ; i++) {
            lowercaseLatins.add(asString(i));
        }
        CommonCharClasses.lowercaseLatins = Collections.unmodifiableSet(lowercaseLatins);

        Set<String> letters = union(capitalLatins, lowercaseLatins);
        CommonCharClasses.letters = Collections.unmodifiableSet(letters);

        Set<String> alphanumerics = union(letters, digits);
        CommonCharClasses.alphanumerics = Collections.unmodifiableSet(alphanumerics);
    }
}
