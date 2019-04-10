package io.github.sboyanovich.scannergenerator.automata;

class Utility {
    public static NFAStateGraph computeEdgeLabels(int[][] transitionTable) {
        int numberOfStates = transitionTable.length;
        int alphabetSize = transitionTable[0].length;

        NFAStateGraphBuilder result = new NFAStateGraphBuilder(numberOfStates, alphabetSize);

        for (int state = 0; state < numberOfStates; state++) {
            for (int symbol = 0; symbol < alphabetSize; symbol++) {
                int to = transitionTable[state][symbol];
                result.addSymbolToEdge(state, to, symbol);
            }
        }

        return result.build();
    }
}
