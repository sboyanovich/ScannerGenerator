package tests;

import tests.data.TransitionTableExample;
import utility.EquivalenceMap;

import static utility.Utility.composeEquivalenceMaps;
import static utility.Utility.refineEquivalenceMap;

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
