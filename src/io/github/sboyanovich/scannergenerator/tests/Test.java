package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.ArrayList;
import java.util.List;

import static io.github.sboyanovich.scannergenerator.utility.Utility.asCodePoint;

public class Test {
    public static void main(String[] args) {
        List<Integer> codePoints = new ArrayList<>();
        List<String> symbols = List.of("A", "Z", "a", "z", "e", "l", "i", "f",
                "0", "9", "\r", "\n", "\t", " ", "*", "/");
        for(String symbol : symbols) {
            codePoints.add(asCodePoint(symbol));
        }
        EquivalenceMap map = Utility.getCoarseSymbolClassMap(codePoints);
        System.out.println(map.getEqClass(Character.MAX_CODE_POINT));
        System.out.println(map.getDomain());
        System.out.println(map.getEqClassDomain());
    }
}
