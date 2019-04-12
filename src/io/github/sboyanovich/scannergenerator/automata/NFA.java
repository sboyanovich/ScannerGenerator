package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.lex.StateTag;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.sboyanovich.scannergenerator.automata.Utility.FINAL_DUMMY_PRIORITY_RANK;
import static io.github.sboyanovich.scannergenerator.automata.Utility.NOT_FINAL_PRIORITY_RANK;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

// TODO: Make separate class for DFA. determinize() should return a DFA then
// TODO: A lot of work here

public class NFA {
    private static final String DOT_ARROW = "->";
    private static final String DOT_ACCEPTING_STATE_SHAPE = "doublecircle";
    private static final String DOT_REGULAR_STATE_SHAPE = "circle";
    private static final String DOT_MAXSIZE_INCHES = "70,0";
    private static final String DOT_AUX_INPUT_STATE_NAME = "input";

    private int numberOfStates;
    private int alphabetSize; //at least 1
    private int initialState;
    // all states are accepting, except those labeled with NOT_FINAL

    private NFAStateGraph edges;

    private List<StateTag> labels;

    public NFA(int numberOfStates, int alphabetSize, int initialState,
               Map<Integer, StateTag> labelsMap, NFAStateGraph edges) {
        // no NULLs allowed
        Objects.requireNonNull(edges);
        Objects.requireNonNull(labelsMap);

        // TOCTOU PROOFING
        //  edges is immutable
        labelsMap = new HashMap<>(labelsMap);

        // Validate inputs
        //  numberOfStates > 0
        //  alphabetSize > 0
        //  initialState in [0, numberOfStates - 1]
        //  edges.numberOfStates = numberOfStates
        //  edges.alphabetSize  = alphabetSize
        if (!(numberOfStates > 0)) {
            throw new IllegalArgumentException("Number of states must be non-negative!");
        }
        if (!(alphabetSize > 0)) {
            throw new IllegalArgumentException("Alphabet size must be non-negative!");
        }
        if (!isInRange(initialState, 0, numberOfStates - 1)) {
            throw new IllegalArgumentException("Initial state must be in range [0, numberOfStates-1]!");
        }
        if (edges.numberOfStates != numberOfStates) {
            throw new IllegalArgumentException("Edge graph must have the same number of states!");
        }
        if (edges.alphabetSize != alphabetSize) {
            throw new IllegalArgumentException("Edge graph must have the same alphabet size!");
        }

        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.initialState = initialState;
        this.edges = edges;
        this.labels = new ArrayList<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            StateTag tag = labelsMap.get(i);
            tag = (tag != null) ? tag : StateTag.NOT_FINAL;
            this.labels.add(tag);
        }
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

    public NFAStateGraph getEdges() {
        return edges;
    }

    public StateTag getStateTag(int state) {
        // validate
        return this.labels.get(state);
    }

    public static NFA emptyLanguage(int alphabetSize) {
        return new NFA(
                1, alphabetSize, 0,
                Map.of(0, StateTag.NOT_FINAL), new NFAStateGraphBuilder(1, alphabetSize).build()
        );
    }

    public static NFA emptyStringLanguage(int alphabetSize) {
        return new NFA(1, alphabetSize, 0,
                Map.of(0, StateTag.FINAL_DUMMY), new NFAStateGraphBuilder(1, alphabetSize).build()
        );
    }

    public static NFA singleLetterLanguage(int alphabetSize, int letter) {
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(2, alphabetSize);
        edges.setEdge(0, 1, Set.of(letter));
        return new NFA(2, alphabetSize, 0,
                Map.of(
                        0, StateTag.NOT_FINAL,
                        1, StateTag.FINAL_DUMMY
                ), edges.build()
        );
    }

    public NFA relabelStates(Map<Integer, StateTag> relabel) {
        return new NFA(
                this.numberOfStates,
                this.alphabetSize,
                this.initialState,
                relabel, this.edges // thanks to immutability,
        );
    }

    public NFA setAllFinalStatesTo(StateTag tag) {
        Map<Integer, StateTag> relabel = new HashMap<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            StateTag stag = this.labels.get(i);
            relabel.put(i, StateTag.isFinal(stag) ? tag : stag);
        }
        return relabelStates(relabel);
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

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
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

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    public NFA concatenation(NFA second) {
        int numberOfStates = this.numberOfStates + second.numberOfStates;
        int alphabetSize = this.alphabetSize;
        int initialState = this.initialState;

        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < this.labels.size(); i++) {
            labels.put(i, StateTag.NOT_FINAL);
        }
        for (int i = 0; i < second.labels.size(); i++) {
            labels.put(this.numberOfStates + i, second.labels.get(i));
        }

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
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

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public NFA iteration() {
        Set<Integer> acceptingStates = acceptingStates();
        // handle automaton with no accepting states
        if (acceptingStates.isEmpty()) {
            return emptyStringLanguage(this.alphabetSize);
        }

        int numberOfStates = this.numberOfStates + 1; // new start state
        int alphabetSize = this.alphabetSize;
        int initialState = numberOfStates - 1;

        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < this.labels.size(); i++) {
            labels.put(i, this.labels.get(i));
        }

        labels.put(initialState, StateTag.NOT_FINAL); // should fix NFATest1

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j).get()));
                }
            }
        }
        // adding lambda-step from new initial to old initial state
        edges.setEdge(initialState, this.initialState, Set.of());

        // linking this automaton's accepting states with new initial state (lambda-steps)
        // linking this automaton's new initial state with accepting states (lambda-steps)
        for (int i = 0; i < this.labels.size(); i++) {
            if (StateTag.isFinal(this.labels.get(i))) {
                edges.setEdge(i, initialState, Set.of());
                edges.setEdge(initialState, i, Set.of());
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
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
        return toString(io.github.sboyanovich.scannergenerator.automata.Utility::defaultAlphabetInterpretation);
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
        return toGraphvizDotString(io.github.sboyanovich.scannergenerator.automata.Utility::defaultAlphabetInterpretation, false);
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
        result.append(TAB + "node [shape = point];" + NEWLINE + TAB
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
                    String markerString = io.github.sboyanovich.scannergenerator.automata.Utility.edgeLabelAsString(marker, alphabetInterpretation);
                    result.append(markerString);
                    result.append("\"]");
                    result.append(SEMICOLON + NEWLINE);
                }
            }
        }
        result.append("}" + NEWLINE);

        return result.toString();
    }

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
            nasLabels[i] = StateTag.NOT_FINAL;
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

        NFAStateGraphBuilder tempEdges = new NFAStateGraphBuilder(this.numberOfStates, alphabetSize); // has more entries!

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

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);

        // trying to keep track of state labels
        Map<Integer, StateTag> labels = new HashMap<>();
        for (int i = 0; i < numberOfStates; i++) {
            labels.put(i, StateTag.NOT_FINAL);
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

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Set<Integer> superState(int state, int letter) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            if (this.edges.isNonTrivialEdge(state, i)) {
                if (this.edges.getEdgeMarker(state, i).get().contains(letter)) {
                    result.add(i);
                }
            }
        }
        return result;
    }

    private Set<Integer> closure(Set<Integer> superstate, int letter) {
        Set<Integer> result = new HashSet<>();

        for (Integer state : superstate) {
            result.addAll(superState(state, letter));
        }

        return result;
    }

    public DFA determinize(Map<StateTag, Integer> priorities) {
        // priorities map must contain mapping for every present StateTag (bigger number -> higher priority)
        // (except NOT_FINAL and FINAL_DUMMY) (these are most likely going to be overwritten anyway)
        // client-defined priority ranks must be non-negative

        priorities = new HashMap<>(priorities);
        // TODO: Validate priorities Map
        priorities.put(StateTag.NOT_FINAL, NOT_FINAL_PRIORITY_RANK);
        priorities.put(StateTag.FINAL_DUMMY, FINAL_DUMMY_PRIORITY_RANK);

        // first we make sure there are no lambda steps
        NFA lambdaless = this.removeLambdaSteps();

        // initial superstate
        // maybe replace this with ordinary HashSet later
        Set<Set<Integer>> superstates = new LinkedHashSet<>();
        Set<Integer> initialSuperstate = Set.of(lambdaless.initialState);
        superstates.add(initialSuperstate);

        Map<Set<Integer>, Integer> names = new HashMap<>();
        names.put(initialSuperstate, 0);

        List<Set<Integer>> statesList = new ArrayList<>(superstates);
        List<List<Integer>> transitionFunction = new ArrayList<>();

        {
            int i = 0;
            int k = 1; // current free name to be assigned to a new superstate
            do {
                Set<Integer> currentSuperstate = statesList.get(i);

                transitionFunction.add(new ArrayList<>());

                for (int j = 0; j < alphabetSize; j++) {
                    Set<Integer> image = lambdaless.closure(currentSuperstate, j);
                    boolean newState = superstates.add(image);
                    if (newState) {
                        statesList.add(image);
                        names.put(image, k);
                        k++;
                    }
                    transitionFunction.get(i).add(names.get(image));
                }
                i++;
            } while (i < superstates.size());
        }

        int alphabetSize = this.alphabetSize;
        int numberOfStates = superstates.size();
        int initialState = 0;

        // time to handle accepting states (state tags)
        // maybe use copy of priorities, to prevent race conditions
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int j = 0; j < numberOfStates; j++) {
            Set<Integer> currentSuperstate = statesList.get(j);
            List<StateTag> candidateTags = List.of(StateTag.NOT_FINAL);
            if (!currentSuperstate.isEmpty()) {
                candidateTags =
                        currentSuperstate.stream()
                                .map(s -> lambdaless.labels.get(s))
                                .collect(Collectors.toList());
            }
            StateTag label =
                    Collections.max(
                            candidateTags,
                            Comparator.comparingInt(priorities::get)
                    );
            labelsMap.put(j, label);
        }

        // writing transition table
        int[][] transitionTable = new int[numberOfStates][alphabetSize];
        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < alphabetSize; j++) {
                transitionTable[i][j] = transitionFunction.get(i).get(j);
            }
        }

        return new DFA(numberOfStates, alphabetSize, initialState, labelsMap, transitionTable);
    }
}
