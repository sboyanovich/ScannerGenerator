package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.List;

public class Test {
    public static void main(String[] args) {
/*        List<Integer> codePoints = new ArrayList<>();
        List<String> symbols = List.of("A", "Z", "a", "z", "e", "l", "i", "f",
                "0", "9", "\r", "\n", "\t", " ", "*", "/");
        for (String symbol : symbols) {
            codePoints.add(asCodePoint(symbol));
        }
        EquivalenceMap map = Utility.getCoarseSymbolClassMap(codePoints);
        System.out.println(map.getEqClass(Character.MAX_CODE_POINT));
        System.out.println(map.getDomain());
        System.out.println(map.getEqClassDomain());*/
        String text = Utility.generateSimpleDomainsEnum(
                List.of("PLUS_OP", "MINUS_OP", "MUL_OP", "DIV_OP", "INC_OP"),
                "io.github.sboyanovich.scannergenerator.generated"
        );

        text = Utility.generateDomainWithAttributeEnum(
                "String",
                List.of("NAMED_EXPR", "DOMAINS_GROUP_MARKER", "STATE_NAME", "IDENTIFIER"),
                "io.github.sboyanovich.scannergenerator.generated"
        );

        String path = "generated/testFiles/Test.java";
        Utility.writeTextToFile(text, path);
    }
}

