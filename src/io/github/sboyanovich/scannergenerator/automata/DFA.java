package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.lex.StateTag;

import java.util.*;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.utility.Utility.copyTable;
import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

public class DFA {
    private int numberOfStates;
    private int alphabetSize;
    private int initialState;
    private List<StateTag> labels;
    private int[][] transitionTable;

    public DFA(int numberOfStates, int alphabetSize, int initialState,
               Map<Integer, StateTag> labelsMap, int[][] transitionTable) {
        // no NULLs allowed
        Objects.requireNonNull(labelsMap);
        Objects.requireNonNull(transitionTable);
        for (int[] aTransitionTable : transitionTable) {
            Objects.requireNonNull(aTransitionTable);
        }

        // defensive copies (against TOCTOU)
        labelsMap = new HashMap<>(labelsMap);
        transitionTable = copyTable(transitionTable);

        // Validate these:
        //  numberOfStates > 0
        //  alphabetSize > 0
        //  initialState in [0, numberOfStates - 1]
        //  labelsMap [0, numberOfStates - 1] -> StateTag (must be defined at these)
        //  transitionTable [numberOfStates][alphabetSize]
        //  transitionTable elements in [0, numberOfStates - 1]

        if (!(numberOfStates > 0)) {
            throw new IllegalArgumentException("Number of states must be non-negative!");
        }
        if (!(alphabetSize > 0)) {
            throw new IllegalArgumentException("Alphabet size must be non-negative!");
        }
        if (!isInRange(initialState, 0, numberOfStates - 1)) {
            throw new IllegalArgumentException("Initial state must be in range [0, numberOfStates-1]!");
        }
        // TODO: Might change this to automatically label unlabeled states with NOT_FINAL.
        for (int i = 0; i < numberOfStates; i++) {
            if (!labelsMap.containsKey(i)) {
                throw new IllegalArgumentException("All states should be labeled!");
            }
        }
        if (transitionTable.length != numberOfStates) {
            throw new IllegalArgumentException("Transition table should have 'numberOfStates' rows!");
        }
        for (int i = 0; i < numberOfStates; i++) {
            if (transitionTable[i].length != alphabetSize) {
                throw new IllegalArgumentException("Transition table should have 'alphabetSize' columns!");
            }
        }
        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < alphabetSize; j++) {
                int state = transitionTable[i][j];
                if (!isInRange(state, 0, numberOfStates - 1)) {
                    throw new IllegalArgumentException(
                            "Transition table should point to states in range [0, numberOfStates-1]!");
                }
            }
        }

        // assume that all is validated
        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.initialState = initialState;
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

    public List<StateTag> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    public int[][] getTransitionTable() {
        return copyTable(transitionTable);
    }

    public NFA toNFA() {
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            labelsMap.put(i, this.labels.get(i));
        }

        // knowing that the method doesn't modify array
        NFAStateGraph edges = Utility.computeEdgeLabels(this.transitionTable);

        return new NFA(
                this.numberOfStates,
                this.alphabetSize,
                this.initialState,
                labelsMap,
                edges
        );
    }

    public String toGraphvizDotString() {
        return toGraphvizDotString(Utility::defaultAlphabetInterpretation, false);
    }

    // simple delegation to analogous method in NFA
    public String toGraphvizDotString(
            Function<Integer, String> alphabetInterpretation,
            boolean prefixFinalStatesWithTagName
    ) {
        return this.toNFA().toGraphvizDotString(alphabetInterpretation, prefixFinalStatesWithTagName);
    }

    /*
    private static DisjointSetForest finestPartition(int n) {
        DisjointSetForest result = new DisjointSetForest();
        for (int i = 0; i < n; i++) {
            result.makeSet(i);
        }
        return result;
    }

    private static boolean areEquivalent(int state1, int state2, int alphabetSize,
                                         int[][] transitionFunction, DisjointSetForest dsf) {
        for (int i = 0; i < alphabetSize; i++) {
            int r1 = transitionFunction[state1][i];
            int r2 = transitionFunction[state2][i];
            if (!dsf.areEquivalent(r1, r2)) {
                return false;
            }
        }
        return true;
    }

    private static DisjointSetForest refine(DisjointSetForest dsf, int nElements,
                                            int alphabetSize, int[][] transitionFunction) {
        DisjointSetForest result = finestPartition(nElements);
        for (int i = 0; i < nElements; i++) {
            for (int j = i; j < nElements; j++) {
                if (dsf.areEquivalent(i, j)) {
                    if (areEquivalent(i, j, alphabetSize, transitionFunction, dsf)) {
                        result.union(i, j);
                    }
                }
            }
        }
        return result;
    }

    public NFA minimize() {
        NFA dfa = this.determinize();

        boolean[] accepting = new boolean[dfa.numberOfStates];
        dfa.acceptingStates.forEach(n -> accepting[n] = true);

        DisjointSetForest dsf = finestPartition(dfa.numberOfStates);

        int firstAccepting = 0;
        int firstNonAccepting = 0;
        for (int i = 0; i < dfa.numberOfStates; i++) {
            if (accepting[i]) {
                firstAccepting = i;
                break;
            }
        }
        for (int i = 0; i < dfa.numberOfStates; i++) {
            if (!accepting[i]) {
                firstNonAccepting = i;
                break;
            }
        }
        for (int i = 0; i < dfa.numberOfStates; i++) {
            if (accepting[i]) {
                dsf.union(i, firstAccepting);
            } else {
                dsf.union(i, firstNonAccepting);
            }
        }
        // Now we have two classes (0-equivalent)

        int[][] transitionFunction = new int[dfa.numberOfStates][dfa.alphabetSize];
        for (int i = 0; i < dfa.numberOfStates; i++) {
            for (int j = 0; j < dfa.numberOfStates; j++) {
                if (dfa.edges.edgeExists(i, j)) {
                    Set<Integer> marker = dfa.edges.getEdgeMarker(i, j);
                    for (Integer letter : marker) {
                        transitionFunction[i][letter] = j;
                    }
                }
            }
        }

        int alphabetSize = dfa.alphabetSize;
        int numberOfStates;
        do {
            numberOfStates = dsf.numberOfClasses();
            dsf = NFA.refine(dsf, dfa.numberOfStates, alphabetSize, transitionFunction);
        } while (dsf.numberOfClasses() > numberOfStates);

        List<Integer> states = dsf.getRepresentatives();
        // Now we need to rename our states
        Map<Integer, Integer> renaming = new HashMap<>();
        for (int i = 0; i < states.size(); i++) {
            renaming.put(states.get(i), i);
        }

        int initialState = renaming.get(dsf.find(dfa.initialState));
        Set<Integer> acceptingStates = new HashSet<Integer>();
        for (Integer state : dfa.acceptingStates) {
            acceptingStates.add(
                    renaming.get(dsf.find(state))
            );
        }

        NFAStateGraph edges = new NFAStateGraph(numberOfStates);

        for (int i = 0; i < numberOfStates; i++) {
            int dfaStateNo = states.get(i);
            for (int j = 0; j < alphabetSize; j++) {
                int targetState = transitionFunction[dfaStateNo][j];
                targetState = dsf.find(targetState);
                targetState = renaming.get(targetState);
                if (!edges.edgeExists(i, targetState)) {
                    edges.setEdge(i, targetState, new HashSet<>());
                }
                Set<Integer> marker = new HashSet<>(edges.getEdgeMarker(i, targetState));
                marker.add(j);
                edges.setEdge(i, targetState, marker);
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, acceptingStates, edges);
    }
*/

    /**
     * Accepting states of the result are labeled with FINAL_DUMMY!
     */
    public DFA complement() {
        Map<Integer, StateTag> labelsMap = new HashMap<>();

        for (int i = 0; i < this.labels.size(); i++) {
            StateTag original = this.labels.get(i);
            StateTag flipped = StateTag.isFinal(original) ? StateTag.NOT_FINAL : StateTag.FINAL_DUMMY;
            labelsMap.put(i, flipped);
        }

        return new DFA(
                this.numberOfStates,
                this.alphabetSize,
                this.initialState,
                labelsMap,
                this.getTransitionTable()
        );
    }
}
