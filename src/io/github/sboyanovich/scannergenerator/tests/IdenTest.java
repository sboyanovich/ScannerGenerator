package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.HashSet;
import java.util.Set;

public class IdenTest {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;

        Set<Integer> javaIdenStart = new HashSet<>();
        Set<Integer> javaIdenPart = new HashSet<>();

        for (int i = 0; i < alphabetSize; i++) {
            if(Character.isJavaIdentifierStart(i)) {
                javaIdenStart.add(i);
            }
            if(Character.isJavaIdentifierPart(i)) {
                javaIdenPart.add(i);
            }
        }

        var jss = Utility.compressIntoSegments(javaIdenStart);
        var jsp = Utility.compressIntoSegments(javaIdenPart);

        System.out.println(jss.size() + " segments");
        System.out.println(jsp.size() + " segments");
        var s = new HashSet<>(javaIdenStart);
        s.removeAll(javaIdenPart);
        System.out.println(s.size());

        System.out.println(Utility.displayAsSegments(javaIdenStart, Utility::defaultUnicodeInterpretation));
        System.out.println(Utility.displayAsSegments(javaIdenPart, Utility::defaultUnicodeInterpretation));
    }
}
