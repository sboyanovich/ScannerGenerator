package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.automata.StateTag;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import static io.github.sboyanovich.scannergenerator.automata.StateTag.isFinal;
import static io.github.sboyanovich.scannergenerator.utility.Utility.ensurePathExists;
import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

/**
 * Lexical recognizer represents an optimized DFA for the language it recognizes.
 * This means that it has the minimum possible number of states, exactly one drain state
 * (dead-end state, non-final state that leads only to itself), which enables scanning to stop
 * as early as possible
 * if the string certainly doesn't belong to the recognizer's language.
 * <p>
 * Also, its input alphabet is compressed to compress the size of the transition table. This is why
 * lexical recognizer must know how to map natural input symbols to their equivalence classes.
 * <p>
 * All states are labeled, and their label can be queried by client.
 * <p>
 * End of input (EOI) special symbol leads to dead-end state from any state.
 * <p>
 * <p>
 * A lexical recognizer is completely defined by:
 * <p>
 * EquivalenceMap map: (inputAlphabet) => (compressedAlphabet)
 * <p>
 * int[][] transitionTable: [states][compressedSymbols], dead-end state isn't counted
 * but the transition table can refer to it
 * <p>
 * List&lt;StateTag&gt; labels: labels assigned to the states (dead-end state is by definition NOT_FINAL.
 * <p>
 * int initialState: marks which state is initial
 * <p>
 * <p>
 * For the sake of better presentation, dead-end state and edges leading to it are not shown in the diagram.
 */
public final class LexicalRecognizer {
    public static final int DEAD_END_STATE = -1;

    private EquivalenceMap generalizedSymbolsMap;
    private int[][] transitionTable;

    // stores only accepting states
    private Map<Integer, StateTag> labels;
    private int initialState;

    // as of now, hint maps precisely automaton domain to something smaller
    public LexicalRecognizer(DFA automaton) {
        Objects.requireNonNull(automaton);

        // this logic should be here
        automaton = automaton.compress();

        // minimization should go faster now (on average much smaller alphabet)
        automaton = automaton.minimize();

        // aux => final
        automaton = automaton.compress(); // Is this compression really necessary? It appears to be so.
        // original alphabet => final

        this.generalizedSymbolsMap = automaton.getTransitionTable().getEquivalenceMap();
        //System.out.println(emap.getDomain());
        //System.out.println(emap.getEqClassDomain());

        int numberOfStates = automaton.getNumberOfStates();
        int alphabetSize = this.generalizedSymbolsMap.getEqClassDomain();

        // Minimal automaton has one drain at most.
        OptionalInt maybeDrain = getDrainState(automaton);

        int[][] transitionTable = automaton.getTransitionTable().getTable();

        if (maybeDrain.isPresent()) {
            int drain = maybeDrain.getAsInt();
            numberOfStates--;
            IntUnaryOperator renaming = n -> {
                if (n < drain) {
                    return n;
                } else if (n == drain) {
                    return DEAD_END_STATE;
                } else {
                    return n - 1;
                }
            };

            this.transitionTable = new int[numberOfStates][alphabetSize];
            this.initialState = renaming.applyAsInt(automaton.getInitialState());
            for (int j = 0; j < alphabetSize; j++) {
                for (int i = 0; i < drain; i++) {
                    this.transitionTable[i][j] = renaming.applyAsInt(transitionTable[i][j]);
                }
                for (int i = drain + 1; i < numberOfStates + 1; i++) {
                    this.transitionTable[i - 1][j] = renaming.applyAsInt(transitionTable[i][j]);
                }
            }
            this.labels = new HashMap<>();
            for (int i = 0; i < numberOfStates; i++) {
                int state = (i < drain) ? i : i + 1; // reverse renaming (we know i != -1)
                StateTag tag = automaton.getStateTag(state);
                if (isFinal(tag)) {
                    this.labels.put(i, tag);
                }
            }
        } else {
            this.initialState = automaton.getInitialState();
            this.transitionTable = transitionTable;
            this.labels = new HashMap<>();
            for (int state : automaton.getLabels().keySet()) {
                this.labels.put(state, automaton.getStateTag(state));
            }
        }
    }

    // is called only on dfa known to be minimal
    private static OptionalInt getDrainState(DFA dfa) {
        List<Integer> drainStates = dfa.getDrainStates();
        if (!drainStates.isEmpty()) {
            // should have exactly one
            return OptionalInt.of(drainStates.get(0));
        }
        return OptionalInt.empty();
    }

    /**
     * Looks up transition table.
     *
     * @param fromState state from which the transition is to be made
     * @param codePoint code point (symbol) of the natural alphabet
     * @return number of the state to which the transition is to be made
     */
    public int transition(int fromState, int codePoint) {
        if (codePoint == Text.EOI || fromState == DEAD_END_STATE) {
            return DEAD_END_STATE;
        }
        if (!isInRange(fromState, 0, getNumberOfStates() - 1)) {
            throw new IllegalArgumentException("Invalid fromState number!");
        }
        // TODO: Add check for code point. Probably define that this class is for Unicode mainly.
        int symbol = this.generalizedSymbolsMap.getEqClass(codePoint);
        return this.transitionTable[fromState][symbol];
    }


    /**
     * @return number of states for this recognizer (not counting dead-end state)
     */
    public int getNumberOfStates() {
        return this.transitionTable.length;
    }

    /**
     * @return number of this recognizer's initial state
     */
    public int getInitialState() {
        return this.initialState;
    }


    /**
     * @param state state whose tag is to be queried
     * @return tag of the state
     */
    public StateTag getStateTag(int state) {
        if (state == DEAD_END_STATE) {
            return StateTag.NOT_FINAL;
        }
        if (!isInRange(state, 0, getNumberOfStates() - 1)) {
            throw new IllegalArgumentException("Invalid state number!");
        }
        if (this.labels.containsKey(state)) {
            return this.labels.get(state);
        }
        return StateTag.NOT_FINAL;
    }

    public int getNumberOfColumns() {
        return this.transitionTable[0].length;
    }

    private NFA toNFA() {
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        int numberOfStates = this.transitionTable.length;
        int alphabetSize = this.transitionTable[0].length;

        for (int i = 0; i < numberOfStates; i++) {
            labelsMap.put(i, getStateTag(i));
        }

        // knowing that the method doesn't modify array
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);

        for (int i = 0; i < numberOfStates; i++) {
            for (int j = 0; j < alphabetSize; j++) {
                int state = this.transitionTable[i][j];
                if (state != DEAD_END_STATE) {
                    edges.addSymbolToEdge(i, state, j);
                }
            }
        }

        return new NFA(
                numberOfStates,
                alphabetSize,
                this.initialState,
                labelsMap,
                edges.build()
        );
    }

    public String toGraphvizDotString() {
        return toGraphvizDotString(
                io.github.sboyanovich.scannergenerator.automata.Utility::defaultAlphabetInterpretation,
                false
        );
    }

    /**
     * @param alphabetInterpretation       map, assigning more meaningful names to compressed alphabet symbols
     * @param prefixFinalStatesWithTagName whether to add tag name to final states' label (for better readability)
     * @return string containing recognizer's state diagram in Graphviz dot language
     */
    // simple delegation to analogous method in NFA
    public String toGraphvizDotString(
            Function<Integer, String> alphabetInterpretation,
            boolean prefixFinalStatesWithTagName
    ) {
        return this.toNFA().toGraphvizDotString(alphabetInterpretation, prefixFinalStatesWithTagName);
    }

    public String displayEquivalenceMap(Function<Integer, String> alphabetInterpretation) {
        return generalizedSymbolsMap.displayClasses(alphabetInterpretation);
    }

    /// EXPERIMENTAL
    public void writeToFile(String filename, Map<StateTag, Integer> indices) {
        int domain = this.generalizedSymbolsMap.getDomain();
        int eqcDomain = this.generalizedSymbolsMap.getEqClassDomain();
        int numberOfStates = this.transitionTable.length;

        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(ensurePathExists(filename))))
        ) {
            dos.writeInt(domain);
            dos.writeInt(eqcDomain);
            for (int i = 0; i < domain; i++) {
                dos.writeInt(this.generalizedSymbolsMap.getEqClass(i));
            }
            dos.writeInt(this.initialState);
            dos.writeInt(numberOfStates);
            for (int[] row : this.transitionTable) {
                for (int j = 0; j < eqcDomain; j++) {
                    dos.writeInt(row[j]);
                }
            }
            for (int i = 0; i < numberOfStates; i++) {
                StateTag tag = getStateTag(i);
                if (!tag.equals(StateTag.NOT_FINAL)) {
                    dos.writeInt(i);
                    dos.writeInt(indices.get(tag));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CALL ONLY ON GENERATED FILES
    public LexicalRecognizer(InputStream is, List<StateTag> finalTags) {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(is))
        ) {
            int domain = dis.readInt();
            int eqcDomain = dis.readInt();
            int[] map = new int[domain];

            for (int i = 0; i < domain; i++) {
                map[i] = dis.readInt();
            }

            this.generalizedSymbolsMap = new EquivalenceMap(domain, eqcDomain, map);

            this.initialState = dis.readInt();
            int numberOfStates = dis.readInt();

            this.transitionTable = new int[numberOfStates][eqcDomain];

            for (int i = 0; i < numberOfStates; i++) {
                for (int j = 0; j < eqcDomain; j++) {
                    this.transitionTable[i][j] = dis.readInt();
                }
            }

            this.labels = new HashMap<>();

            while (dis.available() > 0) {
                int state = dis.readInt();
                int index = dis.readInt();
                this.labels.put(state, finalTags.get(index));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LexicalRecognizer(String filename, List<StateTag> finalTags) {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(filename)))
        ) {
            int domain = dis.readInt();
            int eqcDomain = dis.readInt();
            int[] map = new int[domain];

            for (int i = 0; i < domain; i++) {
                map[i] = dis.readInt();
            }

            this.generalizedSymbolsMap = new EquivalenceMap(domain, eqcDomain, map);

            this.initialState = dis.readInt();
            int numberOfStates = dis.readInt();

            this.transitionTable = new int[numberOfStates][eqcDomain];

            for (int i = 0; i < numberOfStates; i++) {
                for (int j = 0; j < eqcDomain; j++) {
                    this.transitionTable[i][j] = dis.readInt();
                }
            }

            this.labels = new HashMap<>();

            while (dis.available() > 0) {
                int state = dis.readInt();
                int index = dis.readInt();
                this.labels.put(state, finalTags.get(index));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
