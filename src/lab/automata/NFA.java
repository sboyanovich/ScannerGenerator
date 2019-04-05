package lab.automata;

import utility.Utility;
import utility.unionfind.DisjointSetForest;

import java.util.*;
import java.util.function.Function;

import static utility.Utility.*;

// TODO: Make separate class for DFA. determinize() should return a DFA then

public class NFA {
    private static final String DOT_ARROW = "->";
    private static final String DOT_ACCEPTING_STATE_SHAPE = "doublecircle";
    private static final String DOT_REGULAR_STATE_SHAPE = "circle";
    private static final String DOT_MAXSIZE_INCHES = "50,0";
    private static final String DOT_AUX_INPUT_STATE_NAME = "input";

    private int numberOfStates;
    private int alphabetSize; //at least 1
    private int initialState;
    private Set<Integer> acceptingStates;

    private NFAStateGraph edges;

    public NFA(int numberOfStates, int alphabetSize, int initialState, Set<Integer> acceptingStates,
               NFAStateGraph edges) {
        //Validate inputs

        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.initialState = initialState;
        this.acceptingStates = acceptingStates;
        this.edges = edges;
    }

    public static NFA emptyLanguageNDFA(int alphabetSize) {
        return new NFA(1, alphabetSize, 0, Set.of(), new NFAStateGraph(1));
    }

    public static NFA emptyStringLanguageNDFA(int alphabetSize) {
        return new NFA(1, alphabetSize, 0, Set.of(0), new NFAStateGraph(1));
    }

    public static NFA singleLetterLanguageNDFA(int alphabetSize, int letter) {
        NFAStateGraph edges = new NFAStateGraph(2);
        edges.setEdge(0, 1, Set.of(letter));
        return new NFA(2, alphabetSize, 0, Set.of(1), edges);
    }

    public NFA union(NFA second) {
        // It is assumed both automatons are over the exactly same alphabet.

        int numberOfStates = this.numberOfStates + second.numberOfStates + 1; // with new initial state
        int alphabetSize = this.alphabetSize;

        //rename all states of Second to oldName + this.numberOfStates
        Set<Integer> acceptingStates = new HashSet<>(this.acceptingStates);
        second.acceptingStates.forEach(n -> acceptingStates.add(n + this.numberOfStates));

        int initialState = numberOfStates - 1;

        NFAStateGraph edges = new NFAStateGraph(numberOfStates);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j)));
                }
            }
        }
        // preserving edges from second automaton
        for (int i = 0; i < second.numberOfStates; i++) {
            for (int j = 0; j < second.numberOfStates; j++) {
                if (second.edges.edgeExists(i, j)) {
                    edges.setEdge(
                            i + this.numberOfStates,
                            j + this.numberOfStates,
                            new HashSet<>(second.edges.getEdgeMarker(i, j)));
                }
            }
        }
        // linking old initial states with new (lambda-steps)
        edges.setEdge(initialState, this.initialState, Set.of());
        edges.setEdge(initialState, second.initialState + this.numberOfStates, Set.of());

        return new NFA(numberOfStates, alphabetSize, initialState, acceptingStates, edges);
    }

    public NFA concatenation(NFA second) {
        int numberOfStates = this.numberOfStates + second.numberOfStates;
        int alphabetSize = this.alphabetSize;
        int initialState = this.initialState;
        Set<Integer> acceptingStates = new HashSet<>();
        second.acceptingStates.forEach(n -> acceptingStates.add(n + this.numberOfStates));
        NFAStateGraph edges = new NFAStateGraph(numberOfStates);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j)));
                }
            }
        }
        // preserving edges from second automaton
        for (int i = 0; i < second.numberOfStates; i++) {
            for (int j = 0; j < second.numberOfStates; j++) {
                if (second.edges.edgeExists(i, j)) {
                    edges.setEdge(
                            i + this.numberOfStates,
                            j + this.numberOfStates,
                            new HashSet<>(second.edges.getEdgeMarker(i, j)));
                }
            }
        }
        // linking this automaton's accepting states with second automaton's initial state (lambda-steps)
        for (Integer state : this.acceptingStates) {
            edges.setEdge(state, second.initialState + this.numberOfStates, Set.of());
        }

        return new NFA(numberOfStates, alphabetSize, initialState, acceptingStates, edges);
    }

    public NFA iteration() {
        int numberOfStates = this.numberOfStates + 2; // new start state and additional accepting state
        int alphabetSize = this.alphabetSize;
        int initialState = numberOfStates - 2;
        int acceptingState = numberOfStates - 1;
        Set<Integer> acceptingStates = new HashSet<>();
        acceptingStates.add(acceptingState);
        NFAStateGraph edges = new NFAStateGraph(numberOfStates);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j)));
                }
            }
        }
        // adding lambda-step from new initial to new accepting state
        edges.setEdge(initialState, acceptingState, Set.of());
        // adding lambda-step from new initial to old initial state
        edges.setEdge(initialState, this.initialState, Set.of());
        // linking this automaton's accepting states with old initial and new accepting state(lambda-steps)
        for (Integer state : this.acceptingStates) {
            edges.setEdge(state, this.initialState, Set.of());
            edges.setEdge(state, acceptingState, Set.of());
        }

        return new NFA(numberOfStates, alphabetSize, initialState, acceptingStates, edges);
    }

    public NFA positiveIteration() {
        return this.concatenation(this.iteration());
    }

    // EXPERIMENTAL
    public NFA power(int n) {
        if (n == 0) {
            return NFA.emptyStringLanguageNDFA(this.alphabetSize);
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

    public String toString(Function<Integer, String> alphabetInterpretation) {

        StringBuilder result = new StringBuilder();

        int alphabetLastIndex = this.alphabetSize - 1;
        int stateLastIndex = this.numberOfStates - 1;

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
        result.append(this.acceptingStates);
        result.append(NEWLINE);

        result.append("Rules: ");
        result.append(NEWLINE);

        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    if (this.edges.getEdgeMarker(i, j).isEmpty()) {
                        result.append(TAB + "q_");
                        result.append(i);
                        result.append(SPACE + LAMBDA + SPACE + ARROW + SPACE + "q_");
                        result.append(j);
                        result.append(NEWLINE);
                    } else {
                        for (Integer letter : this.edges.getEdgeMarker(i, j)) {
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
        return toGraphvizDotString(Utility::defaultAlphabetInterpretation);
    }

    public String toGraphvizDotString(Function<Integer, String> alphabetInterpretation) {
        StringBuilder result = new StringBuilder();

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
        for (Integer state : this.acceptingStates) {
            result.append(TAB);
            result.append(state);
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
                    Set<Integer> marker = this.edges.getEdgeMarker(i, j);
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
    public boolean isAccepted(List<Integer> string) {
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
    }

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
        boolean[] acceptingStates = new boolean[this.numberOfStates];
        this.acceptingStates.forEach(i -> acceptingStates[i] = true);

        boolean[] newAcceptingStates = new boolean[this.numberOfStates];

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

        NFAStateGraph tempEdges = new NFAStateGraph(this.numberOfStates); // has more entries!

        // Edges
        {
            for (int i = 0; i < this.numberOfStates; i++) {
                if (filteredStates[i]) {
                    // Adding neighbours
                    for (int j = 0; j < this.numberOfStates; j++) {
                        if (filteredStates[j] && this.edges.isNonTrivialEdge(i, j)) {
                            tempEdges.setEdge(i, j, new HashSet<>(this.edges.getEdgeMarker(i, j)));
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
                                Set<Integer> newMarker = new HashSet<>(tempEdges.getEdgeMarker(i, j));
                                newMarker.addAll(this.edges.getEdgeMarker(curr, j));
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

        NFAStateGraph edges = new NFAStateGraph(numberOfStates);
        Set<Integer> acceptingStatesSet = new HashSet<>();
        tempAcceptingStatesSet.forEach(n -> acceptingStatesSet.add(renaming.get(n)));
        for (int i = 0; i < numberOfStates; i++) {
            int mappedI = newStates.get(i);
            for (int j = 0; j < numberOfStates; j++) {
                int mappedJ = newStates.get(j);
                if (tempEdges.edgeExists(mappedI, mappedJ)) {
                    edges.setEdge(i, j, new HashSet<>(tempEdges.getEdgeMarker(mappedI, mappedJ)));
                }
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, acceptingStatesSet, edges);
    }

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

    private boolean isStateAccepting(int n) {
        return this.acceptingStates.contains(n);
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
}
