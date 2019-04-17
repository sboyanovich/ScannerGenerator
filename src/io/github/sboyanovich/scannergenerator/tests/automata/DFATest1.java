package io.github.sboyanovich.scannergenerator.tests.automata;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.tests.data.states.StateTags;

import java.util.Map;

public class DFATest1 {
    public static void main(String[] args) {
        int numberOfStates = 1;
        int alphabetSize = 1;
        int initialState = 0;
        Map<Integer, StateTag> stateLabels = Map.of(
                0, StateTags.IDENTIFIER
        );
        int[][] transitionTable = new int[][]{
                {1}
        };
        transitionTable = new int[numberOfStates][];

        DFA dfa = new DFA(numberOfStates, alphabetSize, initialState, stateLabels, transitionTable);
    }
}
