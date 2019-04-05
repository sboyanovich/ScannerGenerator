package io.github.sboyanovich.scannergenerator.tests.data;

import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class TransitionTableExample {
    public static List<String> symbols =
            List.of("A", "Z", "a", "z", "e", "l", "i", "f",
                    "0", "9", "\r", "\n", "\t", " ", "*", "/");
    public static EquivalenceMap map1 = Utility.getCoarseSymbolClassMap(
            symbols.stream().map(Utility::asCodePoint).collect(Collectors.toList())
    );

    public static int[][] get() {

        int[][] transitionTable = new int[15][map1.getEqClassDomain()];
        for (int i = 0; i < transitionTable.length; i++) {
            for (int j = 0; j < transitionTable[i].length; j++) {
                transitionTable[i][j] = -1;
            }
        }

        Set<String> digits = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            digits.add(String.valueOf(i));
        }

        Set<String> capitalLatins = new HashSet<>();
        int capA = asCodePoint("A");
        int capZ = asCodePoint("Z");
        for (int i = capA; i <= capZ; i++) {
            capitalLatins.add(asString(i));
        }

        Set<String> lowercaseLatins = new HashSet<>();
        int lcA = asCodePoint("a");
        int lcZ = asCodePoint("z");
        for (int i = lcA; i <= lcZ; i++) {
            lowercaseLatins.add(asString(i));
        }

        Set<String> letters = union(capitalLatins, lowercaseLatins);
        Set<String> alphanumerics = union(letters, digits);

        // 0
        addEdge(transitionTable, 0, 1, map1, Set.of(" ", "\t", "\n"));
        addEdge(transitionTable, 0, 2, map1, Set.of("\r"));
        addEdge(transitionTable, 0, 3, map1, difference(letters, Set.of("e", "i")));
        addEdge(transitionTable, 0, 4, map1, Set.of("e"));
        addEdge(transitionTable, 0, 6, map1, Set.of("i"));
        addEdge(transitionTable, 0, 8, map1, digits);
        addEdge(transitionTable, 0, 9, map1, Set.of("*"));
        addEdge(transitionTable, 0, 10, map1, Set.of("/"));

        addEdge(transitionTable, 1, 1, map1, Set.of(" ", "\t", "\n"));
        addEdge(transitionTable, 1, 2, map1, Set.of("\r"));

        addEdge(transitionTable, 2, 1, map1, Set.of("\n"));

        addEdge(transitionTable, 3, 3, map1, alphanumerics);

        addEdge(transitionTable, 4, 5, map1, Set.of("l"));
        addEdge(transitionTable, 4, 3, map1, difference(alphanumerics, Set.of("l")));

        addEdge(transitionTable, 5, 6, map1, Set.of("i"));
        addEdge(transitionTable, 5, 3, map1, difference(alphanumerics, Set.of("i")));

        addEdge(transitionTable, 6, 7, map1, Set.of("f"));
        addEdge(transitionTable, 6, 3, map1, difference(alphanumerics, Set.of("f")));

        addEdge(transitionTable, 7, 3, map1, alphanumerics);

        addEdge(transitionTable, 8, 8, map1, digits);

        addEdge(transitionTable, 10, 11, map1, Set.of("*"));

        addEdge(transitionTable, 11, 13, map1, Set.of("*"));
        addEdgeSubtractive(transitionTable, 11, 12, map1, Set.of("*"));

        addEdge(transitionTable, 12, 13, map1, Set.of("*"));
        addEdgeSubtractive(transitionTable, 12, 12, map1, Set.of("*"));

        addEdge(transitionTable, 13, 13, map1, Set.of("*"));
        addEdge(transitionTable, 13, 14, map1, Set.of("/"));
        addEdgeSubtractive(transitionTable, 13, 12, map1, Set.of("/", "*"));

        return transitionTable;
    }
}
