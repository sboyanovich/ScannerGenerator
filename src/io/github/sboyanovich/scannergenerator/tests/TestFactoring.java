package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.tests.data.TransitionTableExample;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;

import static io.github.sboyanovich.scannergenerator.utility.Utility.composeEquivalenceMaps;
import static io.github.sboyanovich.scannergenerator.utility.Utility.refineEquivalenceMap;

public class TestFactoring {
    public static void main(String[] args) {

        EquivalenceMap map1 = TransitionTableExample.map1;
        int[][] transitionTable = TransitionTableExample.get();

        EquivalenceMap map2 = refineEquivalenceMap(map1, transitionTable);

        EquivalenceMap map = composeEquivalenceMaps(map1, map2);

        System.out.println(map.getDomain());
        System.out.println(map.getEqClassDomain());
        for (int i = 0; i < map.getDomain(); i++) {
            int ec = map.getEqClass(i);
            if (ec != 0) {
                System.out.println(i + ": " + " -> " + ec);
            }
        }
    }

}
