package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.utility.Utility.copyTable;

// IMMUTABLE
// EARLY VERSION
public class DFATransitionTable {
    private int numberOfStates;
    private int alphabetSize;

    private int[][] transitionTable;
    private EquivalenceMap equivalenceMap;

    public DFATransitionTable(
            int numberOfStates,
            int alphabetSize,
            int[][] transitionTable,
            EquivalenceMap equivalenceMap
    ) {
        // VALIDATION

        // TODO:
        // numberOfStates > 0
        // alphabetSize > 0
        // equivalenceMap domain = alphabetSize
        // transitionTable dim1 = numberOfStates
        // transitionTable dim2 = equivalenceMap eqc domain

        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.transitionTable = copyTable(transitionTable);
        this.equivalenceMap = equivalenceMap; // IMMUTABLE
    }

    public int transition(int state, int symbol) {
        return transitionTable[state][equivalenceMap.getEqClass(symbol)];
    }

    public int getNumberOfStates() {
        return numberOfStates;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public int[][] getTable() {
        return copyTable(transitionTable); // FOR NOW
    }

    public EquivalenceMap getEquivalenceMap() {
        return equivalenceMap;
    }

    private boolean areGeneralizedSymbolsEquivalent(int a, int b) {
        for (int[] aTransitionTable : this.transitionTable) {
            if (aTransitionTable[a] != aTransitionTable[b]) {
                return false;
            }
        }
        return true;
    }

    // numbers distinct elements from 0 and renames (returns new array)
    private static int[] normalizeMapping(int[] map) {
        int n = map.length;
        int[] result = new int[n];
        Map<Integer, Integer> known = new HashMap<>();

        int c = 0;
        for (int i = 0; i < map.length; i++) {
            int elem = map[i];
            if (!known.containsKey(elem)) {
                known.put(elem, c);
                c++;
            }
            result[i] = known.get(elem);
        }

        return result;
    }

    // THIS SHOULD GO TO DFATransitionTable
    // map eqDomain == transitionTable alphabet
    public DFATransitionTable compress() {
        EquivalenceMap map = this.getEquivalenceMap();
        int n = map.getEqClassDomain();

        int[] auxMap = new int[n];
        // everyone is equivalent to themselves
        for (int i = 0; i < auxMap.length; i++) {
            auxMap[i] = i;
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if ((auxMap[i] != auxMap[j]) && areGeneralizedSymbolsEquivalent(i, j)) {
                    auxMap[j] = auxMap[i];
                }
            }
        }

        auxMap = normalizeMapping(auxMap);

        List<Integer> aux = new ArrayList<>();
        for (int elem : auxMap) {
            aux.add(elem);
        }
        int c = Collections.max(aux) + 1;

        EquivalenceMap map2 = new EquivalenceMap(n, c, auxMap);

        EquivalenceMap newMap = map.compose(map2);
        int[][] newTable = compressTable(map2);

        return new DFATransitionTable(this.numberOfStates, this.alphabetSize, newTable, newMap);
    }

    // map maps alphabetSize -> eqDomain, where alphabetSize = transitionTable[0].length
    private int[][] compressTable(EquivalenceMap map) {
        int n = this.numberOfStates;
        int m = map.getEqClassDomain();

        int[][] result = new int[n][m];

        for (int i = 0; i < transitionTable.length; i++) {
            for (int j = 0; j < transitionTable[i].length; j++) {
                int state = i;
                int symbol = map.getEqClass(j);
                result[state][symbol] = transitionTable[i][j];
            }
        }
        return result;
    }

    int generalizedTransition(int fromState, int genSymbol) {
        return this.transitionTable[fromState][genSymbol];
    }
}
