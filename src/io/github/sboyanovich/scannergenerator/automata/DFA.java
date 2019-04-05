package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.lex.StateTag;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.utility.Utility.copyTable;

public class DFA {
    private int numberOfStates;
    private int alphabetSize;
    private int initialState;
    private Set<Integer> acceptingStates;
    private List<StateTag> labels;

    private int[][] transitionTable;

    public DFA(int numberOfStates, int alphabetSize, int initialState, Set<Integer> acceptingStates,
               Map<Integer, StateTag> labelsMap, int[][] transitionTable) {
        // no NULLs allowed
        Objects.requireNonNull(acceptingStates);
        Objects.requireNonNull(labelsMap);
        Objects.requireNonNull(transitionTable);

        // defensive copies (against TOCTOU)
        acceptingStates = new HashSet<>(acceptingStates);
        labelsMap = new HashMap<>(labelsMap);
        transitionTable = copyTable(transitionTable);

        // Validate these:
        //  numberOfStates > 0
        //  alphabetSize > 0
        //  initialState in [0, numberOfStates - 1]
        //  acceptingStates subsetOf [0, numberOfStates - 1]
        //  labelsMap [0, numberOfStates - 1] -> StateTag
        //  transitionTable [numberOfStates][alphabetSize]

        // assume that all is validated
        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.initialState = initialState;
        this.acceptingStates = acceptingStates;
        this.labels = new ArrayList<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            this.labels.add(labelsMap.get(i));
        }
        this.transitionTable = transitionTable;
    }

    public int getNumberOfStates() {
        return numberOfStates;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public int getInitialState() {
        return initialState;
    }

    public Set<Integer> getAcceptingStates() {
        return Collections.unmodifiableSet(acceptingStates);
    }

    public List<StateTag> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public int[][] getTransitionTable() {
        return copyTable(transitionTable);
    }
}
