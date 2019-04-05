package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.lex.StateTag;

import java.security.InvalidParameterException;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.utility.Utility.copyTable;
import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

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
        for (int[] aTransitionTable : transitionTable) {
            Objects.requireNonNull(aTransitionTable);
        }

        // defensive copies (against TOCTOU)
        acceptingStates = new HashSet<>(acceptingStates);
        labelsMap = new HashMap<>(labelsMap);
        transitionTable = copyTable(transitionTable);

        // Validate these:
        //  numberOfStates > 0
        //  alphabetSize > 0
        //  initialState in [0, numberOfStates - 1]
        //  acceptingStates subsetOf [0, numberOfStates - 1]
        //  labelsMap [0, numberOfStates - 1] -> StateTag (must be defined at these)
        //  transitionTable [numberOfStates][alphabetSize]
        //  transitionTable elements in [0, numberOfStates - 1]

        if (!(numberOfStates > 0)) {
            throw new InvalidParameterException("Number of states must be non-negative!");
        }
        if (!(alphabetSize > 0)) {
            throw new InvalidParameterException("Alphabet size must be non-negative!");
        }
        if (!isInRange(initialState, 0, numberOfStates - 1)) {
            throw new InvalidParameterException("Initial state must be in range [0, numberOfStates-1]!");
        }
        for (int state : acceptingStates) {
            if (!isInRange(state, 0, numberOfStates - 1)) {
                throw new InvalidParameterException("Accepting states must be in range [0, numberOfStates-1]!");
            }
        }
        for (int i = 0; i < numberOfStates; i++) {
            if (!labelsMap.containsKey(i)) {
                throw new InvalidParameterException("All states should be labeled!");
            }
        }
        if (transitionTable.length != numberOfStates) {
            throw new InvalidParameterException("Transition table should have 'numberOfStates' rows!");
        }
        for (int i = 0; i < numberOfStates; i++) {
            if (transitionTable[i].length != alphabetSize) {
                throw new InvalidParameterException("Transition table should have 'alphabetSize' columns!");
            }
        }
        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < alphabetSize; j++) {
                int state = transitionTable[i][j];
                if (!isInRange(state, 0, numberOfStates - 1)) {
                    throw new InvalidParameterException(
                            "Transition table should point to states in range [0, numberOfStates-1]!");
                }
            }
        }

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