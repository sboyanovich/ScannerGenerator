package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.lex.StateTag;
import io.github.sboyanovich.scannergenerator.token.Domain;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.lex.STNotFinal.NOT_FINAL;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

// TODO: Make separate class for DFA. determinize() should return a DFA then
// TODO: A lot of work here

public class NFA {
    private static final String DOT_ARROW = "->";
    private static final String DOT_ACCEPTING_STATE_SHAPE = "doublecircle";
    private static final String DOT_REGULAR_STATE_SHAPE = "circle";
    private static final String DOT_MAXSIZE_INCHES = "50,0";
    private static final String DOT_AUX_INPUT_STATE_NAME = "input";

    // TODO: Review this later. There might be a more elegant way.
    //  maybe having client provide their own state tag (other than NOT_FINAL)
    private static final StateTag dummySTFinal = new StateTag() {
        @Override
        public Domain getDomain() {
            throw new Error("Dummy tag doesn't correspond to any domain!");
        }

        @Override
        public String toString() {
            return "DUMMY";
        }
    };

    private int numberOfStates;
    private int alphabetSize; //at least 1
    private int initialState;
    // all states are accepting, except those labeled with NOT_FINAL

    private NFAStateGraph edges;

    private List<StateTag> labels;

    public NFA(int numberOfStates, int alphabetSize, int initialState, NFAStateGraph edges,
               Map<Integer, StateTag> labelsMap) {
        // Validate inputs

        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.initialState = initialState;
        this.edges = edges;
        this.labels = new ArrayList<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            this.labels.add(labelsMap.get(i));
        }
    }

    public static NFA emptyLanguage(int alphabetSize) {
        return new NFA(
                1, alphabetSize, 0,
                new NFAStateGraphBuilder(1).build(),
                Map.of(0, NOT_FINAL)
        );
    }

    public static NFA emptyStringLanguage(int alphabetSize) {
        return new NFA(1, alphabetSize, 0,
                new NFAStateGraphBuilder(1).build(),
                Map.of(0, dummySTFinal)
        );
    }

    public static NFA singleLetterLanguage(int alphabetSize, int letter) {
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(2);
        edges.setEdge(0, 1, Set.of(letter));
        return new NFA(2, alphabetSize, 0,
                edges.build(),
                Map.of(
                        0, NOT_FINAL,
                        1, dummySTFinal
                )
        );
    }

    public NFA relabelStates(Map<Integer, StateTag> relabel) {
        return new NFA(
                this.numberOfStates,
                this.alphabetSize,
                this.initialState,
                this.edges, // thanks to immutability,
                relabel
        );
    }

    public NFA union(NFA second) {
        // It is assumed both automatons are over the exactly same alphabet.

        int numberOfStates = this.numberOfStates + second.numberOfStates + 1; // with new initial state
        int alphabetSize = this.alphabetSize;

        //rename all states of Second to oldName + this.numberOfStates
        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < this.labels.size(); i++) {
            labels.put(i, this.labels.get(i));
        }
        for (int i = 0; i < second.labels.size(); i++) {
            labels.put(this.numberOfStates + i, second.labels.get(i));
        }
        int initialState = numberOfStates - 1;

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    // noinspection OptionalGetWithoutIsPresent (because edgeExists == true guarantees it)
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j).get()));
                }
            }
        }
        // preserving edges from second automaton
        for (int i = 0; i < second.numberOfStates; i++) {
            for (int j = 0; j < second.numberOfStates; j++) {
                if (second.edges.edgeExists(i, j)) {
                    //noinspection OptionalGetWithoutIsPresent
                    edges.setEdge(
                            i + this.numberOfStates,
                            j + this.numberOfStates,
                            new HashSet<>(second.edges.getEdgeMarker(i, j).get()));
                }
            }
        }
        // linking old initial states with new (lambda-steps)
        edges.setEdge(initialState, this.initialState, Set.of());
        edges.setEdge(initialState, second.initialState + this.numberOfStates, Set.of());

        return new NFA(numberOfStates, alphabetSize, initialState, edges.build(), labels);
    }

    public NFA concatenation(NFA second) {
        int numberOfStates = this.numberOfStates + second.numberOfStates;
        int alphabetSize = this.alphabetSize;
        int initialState = this.initialState;

        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < this.labels.size(); i++) {
            labels.put(i, NOT_FINAL);
        }
        for (int i = 0; i < second.labels.size(); i++) {
            labels.put(this.numberOfStates + i, second.labels.get(i));
        }

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    //noinspection OptionalGetWithoutIsPresent
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j).get()));
                }
            }
        }
        // preserving edges from second automaton
        for (int i = 0; i < second.numberOfStates; i++) {
            for (int j = 0; j < second.numberOfStates; j++) {
                if (second.edges.edgeExists(i, j)) {
                    //noinspection OptionalGetWithoutIsPresent
                    edges.setEdge(
                            i + this.numberOfStates,
                            j + this.numberOfStates,
                            new HashSet<>(second.edges.getEdgeMarker(i, j).get()));
                }
            }
        }

        // linking this automaton's accepting states with second automaton's initial state (lambda-steps)
        for (int i = 0; i < this.labels.size(); i++) {
            if (StateTag.isFinal(this.labels.get(i))) {
                edges.setEdge(i, second.initialState + this.numberOfStates, Set.of());
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, edges.build(), labels);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public NFA iteration() {
        int numberOfStates = this.numberOfStates + 2; // new start state and additional accepting state
        int alphabetSize = this.alphabetSize;
        int initialState = numberOfStates - 2;
        int acceptingState = numberOfStates - 1;

        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < this.labels.size(); i++) {
            labels.put(i, this.labels.get(i));
        }
        // For practical purposes, one would call iteration() only on NFA with only one kind
        // of final state, so we shall take the first encountered final state for default
        // or dummy if none is present
        // TODO: In the context of LexicalRecognizers, a slightly modified algorithm makes more sense

        StateTag defaultFinalStateTag = this.labels.stream()
                .filter(StateTag::isFinal)
                .findFirst()
                .orElse(dummySTFinal);

        labels.put(acceptingState, defaultFinalStateTag);
        labels.put(initialState, NOT_FINAL); // should fix NFATest1

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j).get()));
                }
            }
        }
        // adding lambda-step from new initial to new accepting state
        edges.setEdge(initialState, acceptingState, Set.of());
        // adding lambda-step from new initial to old initial state
        edges.setEdge(initialState, this.initialState, Set.of());
        // linking this automaton's accepting states with old initial and new accepting state(lambda-steps)
        // TODO: IS THIS SUPERFLUOUS?

        for (int i = 0; i < this.labels.size(); i++) {
            if (StateTag.isFinal(this.labels.get(i))) {
                edges.setEdge(i, this.initialState, Set.of());
                edges.setEdge(i, acceptingState, Set.of());
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, edges.build(), labels);
    }

    public NFA positiveIteration() {
        return this.concatenation(this.iteration());
    }

    // EXPERIMENTAL
    public NFA power(int n) {
        if (n == 0) {
            return NFA.emptyStringLanguage(this.alphabetSize);
        } else {
            NFA result = this;
            while (n > 1) {
                result = result.concatenation(this);
                n--;
            }
            return result;
        }
    }

    @Override
    public String toString() {
        return toString(Utility::defaultAlphabetInterpretation);
    }

    private boolean isStateAccepting(int n) {
        return StateTag.isFinal(this.labels.get(n));
    }

    private Set<Integer> acceptingStates() {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < this.labels.size(); i++) {
            if (isStateAccepting(i)) {
                result.add(i);
            }
        }
        return result;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public String toString(Function<Integer, String> alphabetInterpretation) {

        StringBuilder result = new StringBuilder();

        int alphabetLastIndex = this.alphabetSize - 1;
        int stateLastIndex = this.numberOfStates - 1;

        Set<Integer> acceptingStates = acceptingStates();

        result.append("Alphabet: a_0 - a_");
        result.append(alphabetLastIndex);
        result.append(NEWLINE);

        result.append("States: q_0 - q_");
        result.append(stateLastIndex);
        result.append(NEWLINE);

        result.append("Initial state: q_");
        result.append(this.initialState);
        result.append(NEWLINE);

        result.append("Accepting states: ");
        result.append(acceptingStates);
        result.append(NEWLINE);

        result.append("Rules: ");
        result.append(NEWLINE);

        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    if (this.edges.getEdgeMarker(i, j).get().isEmpty()) {
                        result.append(TAB + "q_");
                        result.append(i);
                        result.append(SPACE + LAMBDA + SPACE + ARROW + SPACE + "q_");
                        result.append(j);
                        result.append(NEWLINE);
                    } else {
                        for (Integer letter : this.edges.getEdgeMarker(i, j).get()) {
                            result.append(TAB + "q_");
                            result.append(i);
                            result.append(SPACE);
                            result.append(alphabetInterpretation.apply(letter));
                            result.append(SPACE + ARROW + SPACE + "q_");
                            result.append(j);
                            result.append(NEWLINE);
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    public String toGraphvizDotString() {
        return toGraphvizDotString(Utility::defaultAlphabetInterpretation, false);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public String toGraphvizDotString(
            Function<Integer, String> alphabetInterpretation,
            boolean prefixFinalStatesWithTagName
    ) {
        StringBuilder result = new StringBuilder();

        Set<Integer> acceptingStates = acceptingStates();

        result.append("digraph automaton {");
        result.append(NEWLINE);

        // SPACING FIX
        result.append(TAB + "rankdir=LR;" + NEWLINE);
        result.append(TAB + "size=\"" + DOT_MAXSIZE_INCHES + "\";" + NEWLINE);

        // "Fake" input state
        result.append(TAB + "node [shape = point ]; "
                + DOT_AUX_INPUT_STATE_NAME + SEMICOLON + NEWLINE);

        // Accepting states
        result.append(TAB + "node [shape = " + DOT_ACCEPTING_STATE_SHAPE + "];" + NEWLINE);
        for (Integer state : acceptingStates) {
            result.append(TAB);
            result.append(state);
            if (prefixFinalStatesWithTagName) {
                result.append(TAB);
                result.append("[label=\"");
                result.append(state).append("_").append(this.labels.get(state));
                result.append("\"]");
            }
            result.append(SEMICOLON + NEWLINE);
        }

        // The rest
        result.append(TAB + "node [shape = " + DOT_REGULAR_STATE_SHAPE + "];" + NEWLINE);

        // Marking initial state with input edge
        result.append(TAB + DOT_AUX_INPUT_STATE_NAME + SPACE + DOT_ARROW + SPACE);
        result.append(this.initialState);
        result.append(SEMICOLON + NEWLINE);

        // Edges
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    result.append(TAB);
                    result.append(i);
                    result.append(SPACE + DOT_ARROW + SPACE);
                    result.append(j);
                    result.append(SPACE);
                    result.append(SPACE + "[label=\"");
                    Set<Integer> marker = this.edges.getEdgeMarker(i, j).get();
                    String markerString = Utility.edgeLabelAsString(marker, alphabetInterpretation);
                    result.append(markerString);
                    result.append("\"]");
                    result.append(SEMICOLON + NEWLINE);
                }
            }
        }
        result.append("}" + NEWLINE);

        return result.toString();
    }
    // EXPERIMENTAL
    /* NOTE: If there are lambda-cycles in the state graph and the string doesn't belong to the
       automaton language, this method WILL get stuck. This is because it brute force searches
       all possible paths from the initial state and repeated edge traversal is allowed.
    */

/*    public boolean isAccepted(List<Integer> string) {
        class StateLetterNoPair {
            private int state;
            private int letterNo;

            public StateLetterNoPair(int state, int letterNo) {
                this.state = state;
                this.letterNo = letterNo;
            }

            public int getState() {
                return state;
            }

            public int getLetterNo() {
                return letterNo;
            }
        }

        Queue<StateLetterNoPair> bfs = new LinkedList<>();
        bfs.add(new StateLetterNoPair(this.initialState, 0));

        while (!bfs.isEmpty()) {
            StateLetterNoPair curr = bfs.remove();
            int letterNo = curr.getLetterNo();
            int state = curr.getState();
            if (letterNo == string.size() && this.acceptingStates.contains(state)) {
                return true;
            }
            int currLetter = (letterNo < string.size()) ? string.get(letterNo) : -1;
            for (int i = 0; i < this.numberOfStates; i++) {
                if (this.edges.edgeExists(state, i)) {
                    Set<Integer> marker = this.edges.getEdgeMarker(state, i);
                    if (marker.isEmpty()) {
                        bfs.add(new StateLetterNoPair(i, letterNo));
                    } else if (currLetter > -1 && marker.contains(currLetter)) {
                        bfs.add(new StateLetterNoPair(i, letterNo + 1));
                    }
                }
            }
        }
        return false;
    }*/

    // This is a TRAINWRECK, might need to rewrite according to lecture materials in the future
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public NFA removeLambdaSteps() {
        ///DFS
        boolean[] filteredStates;
        {
            boolean[] visited = new boolean[this.numberOfStates];
            Deque<Integer> dfs = new ArrayDeque<>();
            for (int i = 0; i < this.numberOfStates; i++) {
                if (!visited[i]) {
                    dfs.push(i);
                }
                while (!dfs.isEmpty()) {
                    int curr = dfs.pop();
                    for (int j = 0; j < this.numberOfStates; j++) {
                        if (!visited[j]
                                && this.edges.edgeExists(curr, j)
                                && !this.edges.isLambdaEdge(curr, j)) {
                            visited[j] = true;
                            dfs.push(j);
                        }
                    }
                }
            }
            visited[this.initialState] = true;
            // Filtered states
            filteredStates = Arrays.copyOf(visited, visited.length);
        }

        // Accepting states
        Set<Integer> currAcceptingStates = acceptingStates();
        boolean[] acceptingStates = new boolean[this.numberOfStates];
        currAcceptingStates.forEach(i -> acceptingStates[i] = true);

        boolean[] newAcceptingStates = new boolean[this.numberOfStates];
        // addition
        StateTag[] nasLabels = new StateTag[this.numberOfStates];
        for (int i = 0; i < nasLabels.length; i++) {
            nasLabels[i] = NOT_FINAL;
        }

        // Computing new accepting states
        for (int i = 0; i < this.numberOfStates; i++) {
            if (filteredStates[i]) {
                Deque<Integer> bfs = new ArrayDeque<>(); // idiomatic Stack
                boolean[] visited = new boolean[this.numberOfStates];
                bfs.add(i);
                while (!bfs.isEmpty()) {
                    int curr = bfs.remove();
                    if (acceptingStates[curr] || newAcceptingStates[curr]) {
                        newAcceptingStates[i] = true;
                        // addition
                        // here is the problem: we need to find out the label of reached final state
                        if (acceptingStates[curr]) {
                            nasLabels[i] = this.labels.get(curr);
                        } else {
                            nasLabels[i] = nasLabels[curr];
                        } // this should fix it

                        bfs.clear(); // hope empty will return true
                    } else {
                        visited[curr] = true;
                        for (int j = 0; j < this.numberOfStates; j++) {
                            if (this.edges.isLambdaEdge(curr, j) && !visited[j]) {
                                bfs.add(j);
                            }
                        }
                    }
                }
            }
        }

        List<Integer> newStates = new ArrayList<>();
        Set<Integer> tempAcceptingStatesSet = new HashSet<>();
        for (int i = 0; i < filteredStates.length; i++) {
            if (filteredStates[i]) {
                newStates.add(i);
                if (newAcceptingStates[i]) {
                    tempAcceptingStatesSet.add(i);
                }
            }
        }

        int numberOfStates = newStates.size();
        int alphabetSize = this.alphabetSize;

        NFAStateGraphBuilder tempEdges = new NFAStateGraphBuilder(this.numberOfStates); // has more entries!

        // Edges
        {
            for (int i = 0; i < this.numberOfStates; i++) {
                if (filteredStates[i]) {
                    // Adding neighbours
                    for (int j = 0; j < this.numberOfStates; j++) {
                        if (filteredStates[j] && this.edges.isNonTrivialEdge(i, j)) {
                            tempEdges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j).get()));
                        }
                    }

                    Deque<Integer> bfs = new ArrayDeque<>();
                    boolean[] visited = new boolean[this.numberOfStates];
                    for (int j = 0; j < this.numberOfStates; j++) {
                        if (this.edges.isLambdaEdge(i, j)) {
                            bfs.add(j);
                        }
                    }
                    while (!bfs.isEmpty()) {
                        int curr = bfs.remove();
                        visited[curr] = true;
                        for (int j = 0; j < this.numberOfStates; j++) {
                            if (this.edges.isNonTrivialEdge(curr, j)) {
                                if (!tempEdges.edgeExists(i, j)) {
                                    tempEdges.setEdge(i, j, new HashSet<>());
                                }
                                Set<Integer> newMarker = new HashSet<>(tempEdges.getEdgeMarker(i, j).get());
                                newMarker.addAll(this.edges.getEdgeMarker(curr, j).get());
                                tempEdges.setEdge(i, j, newMarker);
                            } else if (this.edges.isLambdaEdge(curr, j) && !visited[j]) {
                                bfs.add(j);
                            }
                        }
                    }
                }
            }
        }

        // Now we need to rename our states and size down tempAcceptingStatesSet and tempEdges
        Map<Integer, Integer> renaming = new HashMap<>();
        for (int i = 0; i < newStates.size(); i++) {
            renaming.put(newStates.get(i), i);
        }

        int initialState = renaming.get(this.initialState);

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates);

        // trying to keep track of state labels
        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < numberOfStates; i++) {
            labels.put(i, NOT_FINAL);
        }
        for (int state : tempAcceptingStatesSet) {
            int renamed = renaming.get(state);
            labels.put(renamed, nasLabels[state]);
        }

        for (int i = 0; i < numberOfStates; i++) {
            int mappedI = newStates.get(i);
            for (int j = 0; j < numberOfStates; j++) {
                int mappedJ = newStates.get(j);
                if (tempEdges.edgeExists(mappedI, mappedJ)) {
                    edges.setEdge(i, j, new HashSet<>(tempEdges.getEdgeMarker(mappedI, mappedJ).get()));
                }
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, edges.build(), labels);
    }

    // TODO: Lot of work here too.
    /*
    // EXPERIMENTAL
    private Set<Integer> superState(int state, int letter) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            if (this.edges.isNonTrivialEdge(state, i)) {
                if (this.edges.getEdgeMarker(state, i).contains(letter)) {
                    result.add(i);
                }
            }
        }
        return result;
    }
    // EXPERIMENTAL

    private Set<Integer> closure(Set<Integer> superstate, int letter) {
        Set<Integer> result = new HashSet<>();

        for (Integer state : superstate) {
            result.addAll(superState(state, letter));
        }

        return result;
    }
    // VERY DIRTY HACK

    private Set<Integer> get(Set<Set<Integer>> family, int k) {
        int i = 0;
        for (Set<Integer> set : family) {
            if (i == k) {
                return set;
            }
            i++;
        }
        return Set.of();
    }
    // EXPERIMENTAL

    public NFA determinize() {
        NFA lambdaless = this.removeLambdaSteps();
        Set<Set<Integer>> superstates = new LinkedHashSet<>();
        Set<Integer> initialSuperstate = Set.of(lambdaless.initialState);
        superstates.add(initialSuperstate);

        Map<Set<Integer>, Integer> names = new HashMap<>();
        names.put(initialSuperstate, 0);

        List<List<Integer>> transitionFunction = new ArrayList<>();

        {
            int i = 0;
            int k = 1;
            do {
                Set<Integer> currentSuperstate = get(superstates, i);

                transitionFunction.add(new ArrayList<>(alphabetSize));

                for (int j = 0; j < alphabetSize; j++) {
                    Set<Integer> image = lambdaless.closure(currentSuperstate, j);
                    boolean newState = superstates.add(image);
                    if (newState) {
                        names.put(image, k);
                        k++;
                    }
                    transitionFunction.get(i).add(names.get(image));
                }
                i++;
            } while (i < superstates.size());
        }
        List<Set<Integer>> statesList = new ArrayList<>(superstates);

        int alphabetSize = this.alphabetSize;
        int numberOfStates = superstates.size();
        int initialState = 0;
        Set<Integer> acceptingStates = new HashSet<>();
        for (int j = 0; j < numberOfStates; j++) {
            Set<Integer> intersection = new HashSet<>(lambdaless.acceptingStates);
            intersection.retainAll(statesList.get(j));
            if (!intersection.isEmpty()) {
                acceptingStates.add(j);
            }
        }

        NFAStateGraph edges = new NFAStateGraph(numberOfStates);

        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < alphabetSize; j++) {
                int targetState = transitionFunction.get(i).get(j);
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

    // Experimental and not necessarily correct (only call on determinized automata)
    public NFA complement() {
        Set<Integer> accepting = new HashSet<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            accepting.add(i);
        }
        accepting.removeAll(this.acceptingStates);
        return new NFA(this.numberOfStates, this.alphabetSize, this.initialState, accepting, this.edges);
    }
    */
}
