package io.github.sboyanovich.scannergenerator.utility;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraph;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;
import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.sboyanovich.scannergenerator.scanner.StateTag.FINAL_DUMMY;

public class Utility {

    public static final String SPACE = " ";
    public static final String LAMBDA = "\u03BB";
    public static final String NEWLINE = "\n";
    public static final String TAB = "\t";
    public static final String SEMICOLON = ";";
    public static final String COMMA = ",";
    public static final String ARROW = "-->";
    public static final String EMPTY = "";
    public static final String BNF_OR = " | ";
    public static final String NONTERMINAL_NAME_PREFIX = "Q_";
    public static final String DOT_ARROW = "->";

    public static int asCodePoint(String symbol) {
        return Character.codePointAt(symbol, 0);
    }

    public static String asString(int codePoint) {
        return new String(new int[]{codePoint}, 0, 1);
    }

    /**
     * Assigns distinct equivalence classes to all pivots. Each interval between two closest pivots
     * (left and right alphabet border act as implicit pivots) is assigned its own equivalence class.
     */
    public static EquivalenceMap getCoarseSymbolClassMap(List<Integer> pivots, int alphabetSize) {
        int[] resultMap = new int[alphabetSize];
        List<Integer> sortedPivots = new ArrayList<>(pivots);
        Collections.sort(sortedPivots);

        int classCounter = 0;
        int start = 0;
        for (int pivot : sortedPivots) {
            int i;
            for (i = start; i < pivot; i++) {
                resultMap[i] = classCounter;
            }
            // covers if there is nothing between prev and curr pivot
            if (i > start) {
                classCounter++;
            }
            resultMap[pivot] = classCounter;
            classCounter++;
            start = pivot + 1;
        }
        // last one
        for (int i = start; i < resultMap.length; i++) {
            resultMap[i] = classCounter;
        }
        int classNo = resultMap[alphabetSize - 1] + 1;

        return new EquivalenceMap(alphabetSize, classNo, resultMap);
    }

    public static EquivalenceMap getCoarseSymbolClassMap(List<Integer> pivots) {
        return getCoarseSymbolClassMap(pivots, Character.MAX_CODE_POINT + 1);
    }

    private static boolean areSymbolsEquivalent(int a, int b, int[][] transitionTable) {
        for (int[] aTransitionTable : transitionTable) {
            if (aTransitionTable[a] != aTransitionTable[b]) {
                return false;
            }
        }
        return true;
    }

    // numbers distinct elements from 0 and renames (returns new array)
    private static int[] normalizeMapping(int[] map) {
        int n = map.length;
        int[] result = new int[n];
        Map<Integer, Integer> known = new HashMap<>();

        int c = 0;
        for (int i = 0; i < map.length; i++) {
            int elem = map[i];
            if (!known.containsKey(elem)) {
                known.put(elem, c);
                c++;
            }
            result[i] = known.get(elem);
        }

        return result;
    }

    public static EquivalenceMap composeEquivalenceMaps(EquivalenceMap map1, EquivalenceMap map2) {
        // not checking parameters for validity for now
        int m = map1.getDomain();
        int[] resultMap = new int[m];
        for (int i = 0; i < resultMap.length; i++) {
            resultMap[i] = map2.getEqClass(map1.getEqClass(i));
        }
        return new EquivalenceMap(m, map2.getEqClassDomain(), resultMap);
    }

    // map eqDomain == transitionTable alphabet
    public static EquivalenceMap refineEquivalenceMap(EquivalenceMap map, int[][] transitionTable) {
        int n = map.getEqClassDomain();

        int[] auxMap = new int[n];
        // everyone is equivalent to themselves
        for (int i = 0; i < auxMap.length; i++) {
            auxMap[i] = i;
        }

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if ((auxMap[i] != auxMap[j]) && areSymbolsEquivalent(i, j, transitionTable)) {
                    auxMap[j] = auxMap[i];
                }
            }
        }

        auxMap = normalizeMapping(auxMap);

        List<Integer> aux = new ArrayList<>();
        for (int elem : auxMap) {
            aux.add(elem);
        }
        int c = Collections.max(aux) + 1;

        return new EquivalenceMap(n, c, auxMap);
    }

    // map maps alphabetSize -> eqDomain, where alphabetSize = transitionTable[0].length
    public static int[][] compressTransitionTable(int[][] transitionTable, EquivalenceMap map) {
        Objects.requireNonNull(transitionTable);
        Objects.requireNonNull(map);
        for (int i = 0; i < transitionTable.length; i++) {
            Objects.requireNonNull(transitionTable[i]);
            if (transitionTable[i].length != map.getDomain()) {
                throw new IllegalArgumentException("Map domain must be  [0, alphabetSize-1]!");
            }
        }

        int n = transitionTable.length; // number of states
        int m = map.getEqClassDomain();

        int[][] result = new int[n][m];

        for (int i = 0; i < transitionTable.length; i++) {
            for (int j = 0; j < transitionTable[i].length; j++) {
                int state = i;
                int symbol = map.getEqClass(j);
                result[state][symbol] = transitionTable[i][j];
            }
        }
        return result;
    }

    // EXPERIMENTAL
    //  hint domain must be equal to alphabetSize
    public static Pair<EquivalenceMap, DFA> compressAutomaton(EquivalenceMap hint, DFA automaton) {
        int[][] transitionTable = automaton.getTransitionTable();

        int numberOfStates = automaton.getNumberOfStates();
        int initialState = automaton.getInitialState();

        int[][] table = compressTransitionTable(transitionTable, hint);

        EquivalenceMap rmap = refineEquivalenceMap(
                hint,
                table
        );

        int newAlphabetSize = rmap.getEqClassDomain();
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int i = 0; i < numberOfStates; i++) {
            labelsMap.put(i, automaton.getStateTag(i));
        }

        EquivalenceMap emap = composeEquivalenceMaps(hint, rmap);

        int[][] newTransitionTable = compressTransitionTable(
                table,
                rmap
        );

        DFA dfa = new DFA(numberOfStates, newAlphabetSize, initialState, labelsMap, newTransitionTable);

        return new Pair<>(emap, dfa);
    }

    public static Pair<EquivalenceMap, DFA> compressAutomaton(DFA automaton) {
        return compressAutomaton(
                EquivalenceMap.identityMap(automaton.getAlphabetSize()),
                automaton
        );
    }

    public static <T> Set<T> union(Set<T> s1, Set<T> s2) {
        Set<T> result = new HashSet<>();
        result.addAll(s1);
        result.addAll(s2);
        return result;
    }

    public static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
        Set<T> result = new HashSet<>(s1);
        result.removeAll(s2);
        return result;
    }

    public static String getTextFragmentAsString(Text text, Fragment span) {
        return text.subtext(
                span.getStarting().getIndex(),
                span.getFollowing().getIndex()
        )
                .toString();
    }

    public static int[][] copyTable(int[][] table) {
        int[][] result = new int[table.length][];
        for (int i = 0; i < result.length; i++) {
            result[i] = Arrays.copyOf(table[i], table[i].length);
        }
        return result;
    }

    public static boolean isInRange(int x, int n, int m) {
        return (x >= n && x <= m);
    }

    /**
     * reads text from file res/filename
     */
    public static String getText(String filename) {
        StringBuilder lines = new StringBuilder();

        FileReader fr;
        try {
            fr = new FileReader("res/" + filename);
            BufferedReader br = new BufferedReader(fr);
            String currLine = br.readLine();
            while (currLine != null) {
                lines.append(currLine).append("\n");
                currLine = br.readLine();
            }

            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FILE NOT FOUND");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines.toString().substring(0, lines.length() - 1);
    }

    // for DEBUG
    public static void printTransitionTable(int[][] transitionTable, int paddingTo) {
        for (int[] aTransitionTable : transitionTable) {
            for (int anATransitionTable : aTransitionTable) {
                System.out.print(pad(anATransitionTable, paddingTo) + " ");
            }
            System.out.println();
        }
    }

    private static String pad(int arg, int paddingTo) {
        StringBuilder result = new StringBuilder();
        int n = paddingTo - String.valueOf(arg).length();
        result.append(arg);
        for (int i = 0; i < n; i++) {
            result.append(" ");
        }
        return result.toString();
    }

    /// EXPERIMENTAL METHODS SECTION

    public static NFA acceptsAllTheseSymbols(int alphabetSize, Set<String> symbols) {
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(2, alphabetSize);
        Set<Integer> codePoints = symbols.stream().map(Utility::asCodePoint).collect(Collectors.toSet());
        edges.setEdge(0, 1, codePoints);
        return new NFA(2, alphabetSize, 0, Map.of(1, FINAL_DUMMY), edges.build());
    }

    public static NFA acceptThisWord(int alphabetSize, List<String> symbols) {
        int n = symbols.size();
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(n + 1, alphabetSize);
        for (int i = 0; i < n; i++) {
            int codePoint = asCodePoint(symbols.get(i));
            edges.addSymbolToEdge(i, i + 1, codePoint);
        }
        return new NFA(n + 1, alphabetSize, 0, Map.of(n, StateTag.FINAL_DUMMY), edges.build());
    }

    public static void addEdge(NFAStateGraphBuilder edges, int from, int to, Set<String> edge) {
        for (String symbol : edge) {
            edges.addSymbolToEdge(from, to, asCodePoint(symbol));
        }
    }

    public static void addEdgeSubtractive(NFAStateGraphBuilder edges, int from, int to, Set<String> edge) {
        int alphabetSize = edges.getAlphabetSize();
        Set<Integer> codePoints = edge.stream().map(Utility::asCodePoint).collect(Collectors.toSet());
        for (int i = 0; i < alphabetSize; i++) {
            if (!codePoints.contains(i)) {
                edges.addSymbolToEdge(from, to, i);
            }
        }
    }

    static boolean isSubtractive(Set<Integer> marker, int alphabetSize, int limit) {
        return (alphabetSize - marker.size()) < limit;
    }

    static boolean isMentioned(NFAStateGraph edges, int symbol) {
        int numberOfStates = edges.getNumberOfStates();
        int alphabetSize = edges.getAlphabetSize();

        for (int j = 0; j < numberOfStates; j++) {
            for (int k = 0; k < numberOfStates; k++) {
                Optional<Set<Integer>> marker = edges.getEdgeMarker(j, k);
                if (marker.isPresent()) {
                    Set<Integer> markerSet = marker.get();
                    if (isSubtractive(markerSet, alphabetSize, 5)) {
                        if (!markerSet.contains(symbol)) {
                            return true;
                        }
                    } else if (markerSet.contains(symbol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<Integer> mentioned(NFA nfa) {
        int alphabetSize = nfa.getAlphabetSize();
        List<Integer> result = new ArrayList<>();
        NFAStateGraph edges = nfa.getEdges();

        for (int i = 0; i < alphabetSize; i++) {
            if (isMentioned(edges, i)) {
                result.add(i);
            }
        }

        return result;
    }

    // with hint heuristic
    public static LexicalRecognizer createRecognizer(NFA lang, Map<StateTag, Integer> priorityMap) {
        int alphabetSize = lang.getAlphabetSize();
        lang = lang.removeLambdaSteps();
        EquivalenceMap hint = getCoarseSymbolClassMap(mentioned(lang), alphabetSize);
        DFA dfa = lang.determinize(priorityMap);
        return new LexicalRecognizer(hint, dfa);
    }
}
