package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.unionfind.DisjointSetForest;

import java.util.*;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

public class DFA {
    private int numberOfStates;
    private int alphabetSize;
    private int initialState;
    private List<StateTag> labels;
    private DFATransitionTable transitionTable;

    public DFA(int numberOfStates, int alphabetSize, int initialState,
               Map<Integer, StateTag> labelsMap, DFATransitionTable transitionTable) {
        // no NULLs allowed
        Objects.requireNonNull(labelsMap);
        Objects.requireNonNull(transitionTable);

        // defensive copies (against TOCTOU)
        labelsMap = new HashMap<>(labelsMap);

        // Validate these:
        //  numberOfStates > 0
        //  alphabetSize > 0
        //  initialState in [0, numberOfStates - 1]
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
        if (transitionTable.getNumberOfStates() != numberOfStates) {
            throw new IllegalArgumentException("Transition table should have 'numberOfStates' rows!");
        }
        if (transitionTable.getAlphabetSize() != alphabetSize) {
            throw new IllegalArgumentException("Transition table should have 'alphabetSize' columns!");
        }

        // TODO: Move this logic into DFATransitionTable
        /*
        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < alphabetSize; j++) {
                int state = transitionTable[i][j];
                if (!isInRange(state, 0, numberOfStates - 1)) {
                    throw new IllegalArgumentException(
                            "Transition table should point to states in range [0, numberOfStates-1]!");
                }
            }
        }
        */

        // assume that all is validated
        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.initialState = initialState;
        this.labels = new ArrayList<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            StateTag tag = labelsMap.get(i);
            tag = (tag != null) ? tag : StateTag.NOT_FINAL;
            this.labels.add(tag);
        }
        this.transitionTable = transitionTable;
    }

    public boolean isStateADrain(int state) {
        if (StateTag.isFinal(getStateTag(state))) {
            return false;
        }

        int alphabetSize = this.transitionTable.getEquivalenceMap().getEqClassDomain();
        for (int i = 0; i < alphabetSize; i++) {
            int to = this.transitionTable.generalizedTransition(state, i);
            if (to != state) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getDrainStates() {
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < this.numberOfStates; i++) {
            if (isStateADrain(i)) {
                result.add(i);
            }
        }

        return result;
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

    public DFATransitionTable getTransitionTable() {
        return this.transitionTable;
    }

    public NFA toNFA() {
        Set<Integer> drainStates = new TreeSet<>(getDrainStates());
        if (drainStates.contains(initialState)) {
            return NFA.emptyLanguage(this.alphabetSize);
        }

        int numberOfStates = this.numberOfStates - drainStates.size();

        Function<Integer, Integer> lessThan = n -> {
            int cnt = 0;
            for (var state : drainStates) {
                if (state < n) {
                    cnt++;
                } else {
                    break;
                }
            }
            return cnt;
        };

        Function<Integer, Integer> renaming = n -> {
            if (drainStates.contains(n)) {
                return -1;
            } else {
                return n - lessThan.apply(n);
            }
        };

        //NFAStateGraph edges = Utility.computeEdgeLabels(this.transitionTable);

        int initialState = renaming.apply(this.initialState);
        int alphabetSize = transitionTable.getAlphabetSize();

        NFAStateGraphBuilder edgeBuilder = new NFAStateGraphBuilder(numberOfStates, alphabetSize);

        for (int state = 0; state < this.numberOfStates; state++) {
            if (drainStates.contains(state)) {
                continue;
            }
            int stateNewName = renaming.apply(state);
            for (int symbol = 0; symbol < alphabetSize; symbol++) {
                int to = transitionTable.transition(state, symbol);
                if (!drainStates.contains(to)) {
                    int toNewName = renaming.apply(to);
                    edgeBuilder.addSymbolToEdge(stateNewName, toNewName, symbol);
                }
            }
        }

        NFAStateGraph edges = edgeBuilder.build();

        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            if (drainStates.contains(i)) {
                continue;
            }
            int stateNewName = renaming.apply(i);
            labelsMap.put(stateNewName, this.labels.get(i));
        }

        return new NFA(
                numberOfStates,
                this.alphabetSize,
                initialState,
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

    public StateTag getStateTag(int state) {
        // validate
        return this.labels.get(state);
    }

    public DFA compress() {
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int i = 0; i < this.labels.size(); i++) {
            StateTag tag = labels.get(i);
            if (!tag.equals(StateTag.NOT_FINAL)) {
                labelsMap.put(i, labels.get(i));
            }
        }
        return new DFA(
                this.numberOfStates, this.alphabetSize, this.initialState, labelsMap, this.transitionTable.compress()
        );
    }

    /// MINIMIZATION
    //  Methods in this section prioritize correctness over performance.

    //  Therefore, their implementations are subject to further improvement.
    // returns DisjointSetForest over [0, n-1] with n equivalence classes

    private static DisjointSetForest finestPartition(int n) {
        DisjointSetForest result = new DisjointSetForest();
        for (int i = 0; i < n; i++) {
            result.makeSet(i);
        }
        return result;
    }

    // checks if two states are equivalent
    private static boolean areEquivalent(
            int state1, int state2, DFATransitionTable transitionFunction, DisjointSetForest dsf
    ) {
        int genAlphabetSize = transitionFunction.getEquivalenceMap().getEqClassDomain();

        for (int i = 0; i < genAlphabetSize; i++) {
            int r1 = transitionFunction.generalizedTransition(state1, i);
            int r2 = transitionFunction.generalizedTransition(state2, i);
            if (!dsf.areEquivalent(r1, r2)) {
                return false;
            }
        }
        return true;
    }

    // breaking up some coarse equivalence classes
    private static DisjointSetForest refine(
            DisjointSetForest dsf, int nElements, DFATransitionTable transitionFunction
    ) {
        DisjointSetForest result = finestPartition(nElements);
        for (int i = 0; i < nElements; i++) {
            for (int j = i; j < nElements; j++) {
                if (dsf.areEquivalent(i, j)) {
                    if (areEquivalent(i, j, transitionFunction, dsf)) {
                        result.union(i, j);
                    }
                }
            }
        }
        return result;
    }

    public DFA minimize() {

        DisjointSetForest dsf = finestPartition(this.numberOfStates);

        Set<StateTag> allTags = new HashSet<>(this.labels);

        // find first state for all tags
        Map<StateTag, Integer> represents = new HashMap<>();
        for (StateTag tag : allTags) {
            for (int i = 0; i < this.numberOfStates; i++) {
                if (getStateTag(i) == tag) {
                    represents.put(tag, i);
                    break;
                }
            }
        }

        // all states with same tag are now equivalent
        for (int i = 0; i < this.numberOfStates; i++) {
            StateTag tag = getStateTag(i);
            dsf.union(i, represents.get(tag));
        }

        DFATransitionTable transitionFunction = this.transitionTable;

        int alphabetSize = this.alphabetSize;
        int numberOfStates;
        do {
            numberOfStates = dsf.numberOfClasses();
            dsf = refine(dsf, this.numberOfStates, transitionFunction);
        } while (dsf.numberOfClasses() > numberOfStates);

        List<Integer> states = dsf.getRepresentatives();
        // Now we need to rename our states
        Map<Integer, Integer> renaming = new HashMap<>();
        for (int i = 0; i < states.size(); i++) {
            renaming.put(states.get(i), i);
        }

        // initial state
        int initialState = renaming.get(dsf.find(this.initialState));

        // state labels
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int i = 0; i < states.size(); i++) {
            int state = states.get(i); // this is a representative
            StateTag tag = getStateTag(state);
            int nState = renaming.get(state); // state in minimal DFA represented
            labelsMap.put(nState, tag);
        }

        EquivalenceMap equivalenceMap = this.transitionTable.getEquivalenceMap();
        int eqcDomain = equivalenceMap.getEqClassDomain();

        int[][] transitionTable = new int[numberOfStates][eqcDomain];

        // writing transition table
        for (int i = 0; i < numberOfStates; i++) {
            int dfaStateNo = states.get(i);
            for (int j = 0; j < eqcDomain; j++) {
                int targetState = transitionFunction.generalizedTransition(dfaStateNo, j);
                targetState = dsf.find(targetState);
                targetState = renaming.get(targetState);
                transitionTable[i][j] = targetState;
            }
        }

        DFATransitionTable dfaTransitionTable =
                new DFATransitionTable(numberOfStates, alphabetSize, transitionTable, equivalenceMap);

        return new DFA(numberOfStates, alphabetSize, initialState, labelsMap, dfaTransitionTable);
    }

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
