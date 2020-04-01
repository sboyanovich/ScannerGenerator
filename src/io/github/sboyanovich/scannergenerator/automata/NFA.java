package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.sboyanovich.scannergenerator.automata.StateTag.FINAL_DUMMY;
import static io.github.sboyanovich.scannergenerator.automata.StateTag.isFinal;
import static io.github.sboyanovich.scannergenerator.automata.Utility.FINAL_DUMMY_PRIORITY_RANK;
import static io.github.sboyanovich.scannergenerator.automata.Utility.NOT_FINAL_PRIORITY_RANK;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class NFA {
    private static final String DOT_ACCEPTING_STATE_SHAPE = "doublecircle";
    private static final String DOT_REGULAR_STATE_SHAPE = "circle";
    private static final String DOT_MAXSIZE_INCHES = "70,0";
    private static final String DOT_AUX_INPUT_STATE_NAME = "input";

    private int numberOfStates;
    private int alphabetSize; //at least 1
    private int initialState;
    // all states are accepting, except those labeled with NOT_FINAL

    private NFAStateGraph edges;

    // stores only accepting states
    private Map<Integer, StateTag> labels;

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
        this.labels = new HashMap<>();
        for (int state : labelsMap.keySet()) {
            StateTag tag = labelsMap.get(state);
            if (isFinal(tag)) {
                this.labels.put(state, tag);
            }
        }
    }

    public static NFA acceptsAllTheseSymbols(int alphabetSize, Set<String> symbols) {
        Set<Integer> codePoints = symbols.stream()
                .map(io.github.sboyanovich.scannergenerator.utility.Utility::asCodePoint).collect(Collectors.toSet());
        return acceptsAllTheseCodePoints(alphabetSize, codePoints);
    }

    public static NFA acceptsAllTheseCodePoints(int alphabetSize, Set<Integer> codePoints) {
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(2, alphabetSize);
        edges.setEdge(0, 1, codePoints, true);
        return new NFA(2, alphabetSize, 0, Map.of(1, FINAL_DUMMY), edges.build());
    }

    public static NFA rejectsThisCodePoint(int alphabetSize, int codePoint) {
        return acceptsAllTheseCodePoints(alphabetSize, SegmentSet.notThisElem(codePoint, alphabetSize));
    }

    public static NFA acceptsThisRange(int alphabetSize, int a, int b) {
        return acceptsAllTheseCodePoints(alphabetSize, SegmentSet.thisRange(a, b, alphabetSize));
    }

    public static NFA acceptsThisRange(int alphabetSize, String a, String b) {
        return acceptsThisRange(alphabetSize, asCodePoint(a), asCodePoint(b));
    }

    public static NFA rejectsThisRange(int alphabetSize, int a, int b) {
        return acceptsAllTheseCodePoints(alphabetSize, SegmentSet.notThisRange(a, b, alphabetSize));
    }

    public static NFA rejectsThisRange(int alphabetSize, String a, String b) {
        return rejectsThisRange(alphabetSize, asCodePoint(a), asCodePoint(b));
    }

    public static NFA acceptsAllCodePointsButThese(int alphabetSize, Set<Integer> codePoints) {
        return acceptsAllTheseCodePoints(alphabetSize, SegmentSet.fromSet(codePoints, alphabetSize).invert());
    }

    // won't accept EOF(AEOI). Client likely wants only ordinary symbols
    public static NFA acceptsAllSymbolsButThese(int alphabetSize, Set<String> symbols) {
        int aeoi = alphabetSize - 1;
        Set<Integer> codePoints = new HashSet<>();
        codePoints.add(aeoi);
        symbols.stream()
                .map(io.github.sboyanovich.scannergenerator.utility.Utility::asCodePoint)
                .forEach(codePoints::add);
        return acceptsAllCodePointsButThese(alphabetSize, codePoints);
    }

    public static NFA acceptsThisWord(int alphabetSize, String word) {
        List<Integer> codePoints = word.codePoints().boxed().collect(Collectors.toList());
        int n = codePoints.size();
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(n + 1, alphabetSize);
        for (int i = 0; i < n; i++) {
            int codePoint = codePoints.get(i);
            edges.addSymbolToEdge(i, i + 1, codePoint);
        }
        return new NFA(n + 1, alphabetSize, 0, Map.of(n, StateTag.FINAL_DUMMY), edges.build());
    }

    public static NFA acceptsAllTheseWords(int alphabetSize, Set<String> words) {
        NFA result = NFA.emptyLanguage(alphabetSize);
        for (String word : words) {
            result = result.union(acceptsThisWord(alphabetSize, word));
        }
        return result;
    }

    public static NFA acceptsThisWord(int alphabetSize, List<Integer> symbols) {
        int n = symbols.size();
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(n + 1, alphabetSize);
        for (int i = 0; i < n; i++) {
            int codePoint = symbols.get(i);
            edges.setEdge(i, i + 1, SegmentSet.thisElem(codePoint, alphabetSize));
        }
        return new NFA(n + 1, alphabetSize, 0, Map.of(n, StateTag.FINAL_DUMMY), edges.build());
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
        if (this.labels.containsKey(state)) {
            return this.labels.get(state);
        }
        return StateTag.NOT_FINAL;
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
        edges.setEdge(0, 1, SegmentSet.thisElem(letter, alphabetSize));
        return new NFA(2, alphabetSize, 0,
                Map.of(
                        0, StateTag.NOT_FINAL,
                        1, StateTag.FINAL_DUMMY
                ), edges.build()
        );
    }

    public static NFA singleLetterLanguage(int alphabetSize, String letter) {
        return singleLetterLanguage(alphabetSize, asCodePoint(letter));
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
        for (int state : this.labels.keySet()) {
            relabel.put(state, tag);
        }
        return relabelStates(relabel);
    }

    // assuming list is non empty
    public static NFA unionAll(List<NFA> nfas) {
        // It is assumed all automatons are over the exactly same alphabet.

        if (nfas.size() == 1) {
            return nfas.get(0);
        }

        int alphabetSize = nfas.get(0).alphabetSize;
        int numberOfStates = 1;
        for (NFA nfa : nfas) {
            numberOfStates += nfa.numberOfStates;
        }
        int initialState = numberOfStates - 1;
        int numberingPrefix = 0;

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        SegmentSet nothing = SegmentSet.nothing(alphabetSize);
        Map<Integer, StateTag> labels = new HashMap<>();

        for (NFA nfa : nfas) {
            // preserving edges from this automaton
            for (int i = 0; i < nfa.numberOfStates; i++) {
                // labels
                if (nfa.isStateAccepting(i)) {
                    labels.put(numberingPrefix + i, nfa.getStateTag(i));
                }
                for (int j = 0; j < nfa.numberOfStates; j++) {
                    if (nfa.edges.edgeExists(i, j)) {
                        // noinspection OptionalGetWithoutIsPresent (because edgeExists == true guarantees it)
                        edges.setEdge(
                                numberingPrefix + i,
                                numberingPrefix + j,
                                SegmentSet.fromSet(nfa.edges.getEdgeMarker(i, j).get(), nfa.alphabetSize),
                                true
                        );
                    }
                }
            }
            // linking old initial states with new (lambda-steps)
            edges.setEdge(initialState, numberingPrefix + nfa.initialState, nothing, true);
            numberingPrefix += nfa.numberOfStates;
        }

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    // assuming list is non empty
    public static NFA concatenationAll(List<NFA> nfas) {

        if (nfas.size() == 1) {
            return nfas.get(0);
        }

        int alphabetSize = nfas.get(0).alphabetSize;
        int numberOfStates = 0;
        for (NFA nfa : nfas) {
            numberOfStates += nfa.numberOfStates;
        }
        int initialState = nfas.get(0).initialState;
        int numberingPrefix = 0;

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        SegmentSet nothing = SegmentSet.nothing(alphabetSize);
        Map<Integer, StateTag> labels = new HashMap<>();

        NFA last = nfas.get(nfas.size() - 1);
        int lastPrefix = numberOfStates - last.numberOfStates;
        for (int state : last.labels.keySet()) {
            labels.put(lastPrefix + state, last.getStateTag(state));
        }

        for (NFA nfa : nfas) {
            // preserving edges from this automaton
            for (int i = 0; i < nfa.numberOfStates; i++) {
                for (int j = 0; j < nfa.numberOfStates; j++) {
                    if (nfa.edges.edgeExists(i, j)) {
                        // noinspection OptionalGetWithoutIsPresent (because edgeExists == true guarantees it)
                        edges.setEdge(
                                numberingPrefix + i,
                                numberingPrefix + j,
                                SegmentSet.fromSet(nfa.edges.getEdgeMarker(i, j).get(), nfa.alphabetSize),
                                true
                        );
                    }
                }
            }
            numberingPrefix += nfa.numberOfStates;
        }

        numberingPrefix = 0;
        // linking every non-last automaton's accepting states with next automaton's initial state (lambda-steps)
        for (int i = 0; i < nfas.size() - 1; i++) {
            NFA curr = nfas.get(i);
            NFA next = nfas.get(i + 1);
            for (int state : curr.labels.keySet()) {
                edges.setEdge(
                        numberingPrefix + state,
                        numberingPrefix + curr.numberOfStates + next.initialState,
                        nothing,
                        true
                );
            }
            numberingPrefix += curr.numberOfStates;
        }

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    public NFA union(NFA second) {
        // It is assumed both automatons are over the exactly same alphabet.

        int numberOfStates = this.numberOfStates + second.numberOfStates + 1; // with new initial state
        int alphabetSize = this.alphabetSize;

        //rename all states of Second to oldName + this.numberOfStates
        Map<Integer, StateTag> labels = new HashMap<>();
        for (int state : this.labels.keySet()) {
            labels.put(state, this.labels.get(state));
        }
        for (int state : second.labels.keySet()) {
            labels.put(this.numberOfStates + state, second.labels.get(state));
        }
        int initialState = numberOfStates - 1;

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    // noinspection OptionalGetWithoutIsPresent (because edgeExists == true guarantees it)
                    edges.setEdge(
                            i, j,
                            SegmentSet.fromSet(this.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                            true
                    );
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
                            SegmentSet.fromSet(second.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                            true
                    );
                }
            }
        }

        SegmentSet nothing = SegmentSet.nothing(this.alphabetSize);
        // linking old initial states with new (lambda-steps)
        edges.setEdge(initialState, this.initialState, nothing, true);
        edges.setEdge(initialState, second.initialState + this.numberOfStates, nothing, true);

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    public NFA concatenation(NFA second) {
        int numberOfStates = this.numberOfStates + second.numberOfStates;
        int alphabetSize = this.alphabetSize;
        int initialState = this.initialState;

        Map<Integer, StateTag> labels = new HashMap<>();
        for (int state : second.labels.keySet()) {
            labels.put(this.numberOfStates + state, second.labels.get(state));
        }

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    //noinspection OptionalGetWithoutIsPresent
                    edges.setEdge(
                            i, j,
                            SegmentSet.fromSet(this.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                            true
                    );
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
                            SegmentSet.fromSet(second.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                            true
                    );
                }
            }
        }

        SegmentSet nothing = SegmentSet.nothing(this.alphabetSize);
        // linking this automaton's accepting states with second automaton's initial state (lambda-steps)
        for (int state : this.labels.keySet()) {
            edges.setEdge(state, second.initialState + this.numberOfStates, nothing, true);
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
        for (int state : this.labels.keySet()) {
            labels.put(state, this.labels.get(state));
        }

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(
                            i, j,
                            SegmentSet.fromSet(this.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                            true
                    );
                }
            }
        }
        SegmentSet nothing = SegmentSet.nothing(this.alphabetSize);
        // adding lambda-step from new initial to old initial state
        edges.setEdge(initialState, this.initialState, nothing);

        // linking this automaton's accepting states with new initial state (lambda-steps)
        // linking this automaton's new initial state with accepting states (lambda-steps)
        for (int state : this.labels.keySet()) {
            edges.setEdge(state, initialState, nothing, true);
            edges.setEdge(initialState, state, nothing, true);
        }

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    public NFA positiveIteration() {
        // THIS IS MORE PERFORMANT THAN MATHEMATICAL DEFINITION, ALSO DOESN'T NEED EXTRA STATE
        Set<Integer> acceptingStates = acceptingStates();
        // handle automaton with no accepting states
        if (acceptingStates.isEmpty()) {
            return emptyLanguage(this.alphabetSize);
        }

        int numberOfStates = this.numberOfStates;
        int alphabetSize = this.alphabetSize;
        int initialState = this.initialState;

        Map<Integer, StateTag> labels = new HashMap<>();
        for (int state : this.labels.keySet()) {
            labels.put(state, this.labels.get(state));
        }

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        // preserving edges from this automaton
        for (int i = 0; i < this.numberOfStates; i++) {
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.edgeExists(i, j)) {
                    edges.setEdge(
                            i, j,
                            SegmentSet.fromSet(this.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                            true
                    );
                }
            }
        }
        SegmentSet nothing = SegmentSet.nothing(this.alphabetSize);

        // linking this automaton's accepting states with initial state (lambda-steps)
        for (int state : this.labels.keySet()) {
            edges.setEdge(state, initialState, nothing, true);
        }

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    public NFA optional() {
        return this.union(emptyStringLanguage(this.alphabetSize));
    }

    public NFA power(int n) {
        // TODO: Implement fast power.
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
        return StateTag.isFinal(getStateTag(n));
    }

    private Set<Integer> acceptingStates() {
        return new HashSet<>(this.labels.keySet());
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
                result.append(state).append("_").append(getStateTag(state));
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
                            nasLabels[i] = getStateTag(curr);
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

        SegmentSet nothing = SegmentSet.nothing(this.alphabetSize);

        // Edges
        {
            for (int i = 0; i < this.numberOfStates; i++) {
                if (filteredStates[i]) {
                    // Adding neighbours
                    for (int j = 0; j < this.numberOfStates; j++) {
                        if (filteredStates[j] && this.edges.isNonTrivialEdge(i, j)) {
                            tempEdges.setEdge(
                                    i, j,
                                    SegmentSet.fromSet(this.edges.getEdgeMarker(i, j).get(), this.alphabetSize),
                                    true
                            );
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
                                    tempEdges.setEdge(i, j, nothing, true);
                                }
                                Set<Integer> newMarker = new HashSet<>(tempEdges.getEdgeMarker(i, j).get());
                                newMarker.addAll(this.edges.getEdgeMarker(curr, j).get());
                                tempEdges.setEdge(i, j, newMarker, true);
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
                    edges.setEdge(
                            i, j,
                            SegmentSet.fromSet(tempEdges.getEdgeMarker(mappedI, mappedJ).get(), this.alphabetSize),
                            true
                    );
                }
            }
        }

        return new NFA(numberOfStates, alphabetSize, initialState, labels, edges.build());
    }

    private Set<Integer> lambdaClosure(Set<Integer> states) {
        Set<Integer> result = new HashSet<>(states);

        /// DFS
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i : states) {
            stack.push(i);
        }

        while (!stack.isEmpty()) {
            int q = stack.pop();
            for (int u = 0; u < this.numberOfStates; u++) {
                if (this.edges.isLambdaEdge(q, u)) {
                    if (!result.contains(u)) {
                        result.add(u);
                        stack.push(u);
                    }
                }
            }
        }

        return result;
    }

    public DFA determinize(Map<StateTag, Integer> priorities) {
        return determinize(priorities, List.of());
    }

    // Pivots are not checked for any kind of logical consistency with the NFA.
    // Supply them only if you know what you're doing.
    public DFA determinize(Map<StateTag, Integer> priorities, List<Integer> pivots) {
        // priorities map must contain mapping for every present StateTag (bigger number -> higher priority)
        // (except NOT_FINAL and FINAL_DUMMY) (these are most likely going to be overwritten anyway)
        // client-defined priority ranks must be non-negative

        int lambdaClosureMemoHitCounter = 0;
        int lambdaClosureMemoQueryCounter = 0;

        long totalTime, closureTime = 0, lambdaClosureTime = 0;
        Instant start, end, startTotal, endTotal;

        startTotal = Instant.now();

        EquivalenceMap emap;

        System.out.println("States: " + this.numberOfStates);
        if (pivots.isEmpty()) {
            List<Integer> mentioned = mentioned(this);
            System.out.println("Mentioned :" + mentioned.size());
            emap = getCoarseSymbolClassMap(mentioned, this.alphabetSize);
        } else {
            System.out.println("Pivots: " + pivots.size());
            emap = getCoarseSymbolClassMapRegexBased(pivots, this.alphabetSize);
        }
        System.out.println("Classes: " + emap.getEqClassDomain());

        priorities = new HashMap<>(priorities);
        // TODO: Validate priorities Map
        priorities.put(StateTag.NOT_FINAL, NOT_FINAL_PRIORITY_RANK);
        priorities.put(StateTag.FINAL_DUMMY, FINAL_DUMMY_PRIORITY_RANK);

        // initial superstate
        Set<Set<Integer>> superstates = new HashSet<>();

        Set<Integer> initialSuperstate = this.lambdaClosure(
                Set.of(this.initialState)
        );
        superstates.add(initialSuperstate);

        Map<Set<Integer>, Integer> names = new HashMap<>();
        names.put(initialSuperstate, 0);

        List<Set<Integer>> statesList = new ArrayList<>(superstates);
        List<List<Integer>> transitionFunction = new ArrayList<>();

        int eqcd = emap.getEqClassDomain();
        List<Integer> eqc = emap.getRepresents();

        /// OPTIMIZING STRUCTURES
        Map<Set<Integer>, Set<Integer>> memo = new HashMap<>();
        Map<Integer, List<Integer>> edges = new HashMap<>();
        for (int i = 0; i < this.numberOfStates; i++) {
            List<Integer> states = new ArrayList<>();
            for (int j = 0; j < this.numberOfStates; j++) {
                if (this.edges.isNonTrivialEdge(i, j)) {
                    states.add(j);
                }
            }
            edges.put(i, states);
        }

        {
            int i = 0;
            int k = 1; // current free name to be assigned to a new superstate
            do {
                Set<Integer> currentSuperstate = statesList.get(i);

                transitionFunction.add(new ArrayList<>());

                for (int j = 0; j < eqcd; j++) {
                    int repLetter = eqc.get(j);

                    start = Instant.now();
                    Set<Integer> closure = new HashSet<>();

                    for (int from : currentSuperstate) {
                        List<Integer> toStates = edges.get(from);
                        for (int to : toStates) {
                            if (!closure.contains(to)) {
                                Set<Integer> edge = this.edges.getEdgeMarker(from, to).get();
                                if (edge.contains(repLetter)) {
                                    closure.add(to);
                                }
                            }
                        }
                    }
                    end = Instant.now();
                    closureTime += Duration.between(start, end).toNanos();

                    Set<Integer> image;

                    lambdaClosureMemoQueryCounter++;
                    if (memo.containsKey(closure)) {
                        image = memo.get(closure);
                        lambdaClosureMemoHitCounter++;
                    } else {
                        start = Instant.now();
                        image = this.lambdaClosure(
                                closure
                        );
                        memo.put(closure, image);
                        end = Instant.now();
                        lambdaClosureTime += Duration.between(start, end).toNanos();
                    }

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
                                .map(this::getStateTag)
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
        int[][] transitionTable = new int[numberOfStates][eqcd];
        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < eqcd; j++) {
                transitionTable[i][j] = transitionFunction.get(i).get(j);
            }
        }

        DFATransitionTable newTransitionTable =
                new DFATransitionTable(numberOfStates, alphabetSize, transitionTable, emap);

        closureTime /= 1_000_000;
        lambdaClosureTime /= 1_000_000;

        endTotal = Instant.now();
        totalTime = Duration.between(startTotal, endTotal).toMillis();
        System.out.println();
        System.out.println("DET: Total time taken: " + totalTime + "ms");
        System.out.println("DET: Closure time: " + closureTime + "ms");
        System.out.println("DET: Lambda closure time: " + lambdaClosureTime + "ms");
        System.out.println(lambdaClosureMemoHitCounter + "lambda-closure memo hits.");
        System.out.println(lambdaClosureMemoQueryCounter + "lambda-closure memo queries.");
        System.out.println();

        return new DFA(numberOfStates, alphabetSize, initialState, labelsMap, newTransitionTable);
    }
}
