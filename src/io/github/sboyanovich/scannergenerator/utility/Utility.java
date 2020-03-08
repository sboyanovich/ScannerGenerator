package io.github.sboyanovich.scannergenerator.utility;

import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraph;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public static final String MINUS = "-";
    public static final String DOT_ARROW = "->";
    public static final String EQDEF = ":=";

    public static int asCodePoint(String symbol) {
        return Character.codePointAt(symbol, 0);
    }

    public static String asString(int codePoint) {
        return new String(new int[]{codePoint}, 0, 1);
    }

    public static String defaultUnicodeInterpretation(int codepoint, int maxCodePoint) {
        switch (codepoint) {
            case 9:
                return "TAB";
            case 10:
                return "NEWLINE";
            case 13:
                return "CR";
            case 32:
                return "SPACE";
            case 352:
                return "Š";
            case 353:
                return "š";
            case 272:
                return "Đ";
            case 273:
                return "đ";
            case 268:
                return "Č";
            case 269:
                return "č";
            case 262:
                return "Ć";
            case 263:
                return "ć";
            case 381:
                return "Ž";
            case 382:
                return "ž";
        }
        if (codepoint == maxCodePoint + 1) {
            return "<<EOF>>";
        }
        if (isInRange(codepoint, 33, 126) ||
                isInRange(codepoint, 162, 165)) {
            return asString(codepoint);
        }
        if (isInRange(codepoint, asCodePoint("А"), asCodePoint("Я")) ||
                isInRange(codepoint, asCodePoint("а"), asCodePoint("я"))
        ) {
            return asString(codepoint);
        }

        return "U+#" + codepoint;
    }

    public static String defaultUnicodeInterpretation(int codepoint) {
        return defaultUnicodeInterpretation(codepoint, Character.MAX_CODE_POINT);
    }

    /**
     * Assigns distinct equivalence classes to all pivots. All unmentioned symbols are in class 0.
     */
    public static EquivalenceMap getCoarseSymbolClassMap(List<Integer> pivots, int alphabetSize) {
        int[] resultMap = new int[alphabetSize];
        List<Integer> sortedPivots = new ArrayList<>(pivots);
        Collections.sort(sortedPivots);

        int classNo;

        if (pivots.size() < alphabetSize) {
            int classCounter = 1;
            for (int pivot : sortedPivots) {
                resultMap[pivot] = classCounter;
                classCounter++;
            }
            classNo = pivots.size() + 1;
        } else {
            for (int i = 0; i < alphabetSize; i++) {
                resultMap[i] = i;
            }
            classNo = alphabetSize;
        }

        return new EquivalenceMap(alphabetSize, classNo, resultMap);
    }

    public static EquivalenceMap getCoarseSymbolClassMap(List<Integer> pivots) {
        return getCoarseSymbolClassMap(pivots, Character.MAX_CODE_POINT + 1);
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

    /**
     * reads text from file res/filename
     */
    public static String getTextFromFile(String filename) {
        try (InputStream is = new FileInputStream("res/" + filename)) {
            return getTextFromInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getTextFromResourceFile(String resourcePath) {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath)) {
            return getTextFromInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String getTextFromInputStream(InputStream is) {
        StringBuilder lines = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String currLine = br.readLine();
            while (currLine != null) {
                lines.append(currLine).append("\n");
                currLine = br.readLine();
            }
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

    /**
     * Represents a collection of integers into a list of sorted segments, i.e.
     * <p>
     * 0, 1, 2, 3, 6, 7, 8, 10, 11, 14, 15, 16 ==> 0-3, 6-8, 10, 11, 14-16
     */
    public static List<Pair<Integer, Integer>> compressIntoSegments(Collection<Integer> data) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        List<Integer> sortedData = new ArrayList<>(data);
        Collections.sort(sortedData);

        int n = sortedData.size();
        int segstart = 0;
        int seglen = 1;

        while (segstart < n) {
            while (segstart + seglen < n &&
                    (sortedData.get(segstart + seglen) - sortedData.get(segstart) == seglen)) {
                seglen++;
            }

            int a = sortedData.get(segstart);
            int b = sortedData.get(segstart + seglen - 1);

            result.add(new Pair<>(a, b));

            segstart += seglen;
            seglen = 1;
        }

        return result;
    }

    private static String displaySegment(
            Pair<Integer, Integer> segment, Function<Integer, String> interpretation
    ) {
        int a = segment.getFirst();
        int b = segment.getSecond();

        int seglen = b - a + 1;

        if (seglen > 2) {
            return interpretation.apply(a) + MINUS + interpretation.apply(b);
        } else if (seglen > 1) {
            return interpretation.apply(a) + COMMA + SPACE + interpretation.apply(b);
        } else {
            return interpretation.apply(a);
        }
    }

    public static String displayAsSegments(Collection<Integer> data, Function<Integer, String> interpretation) {
        List<Pair<Integer, Integer>> segments = compressIntoSegments(data);
        StringBuilder result = new StringBuilder();

        if (!segments.isEmpty()) {
            result.append(displaySegment(segments.get(0), interpretation));
        }
        for (int i = 1; i < segments.size(); i++) {
            result.append(COMMA).append(SPACE);
            result.append(displaySegment(segments.get(i), interpretation));
        }

        return result.toString();
    }

    public static File ensurePathExists(String path) {
        File filePath = new File(path);
        File parentFile = filePath.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        return filePath;
    }

    public static void writeTextToFile(String text, String path) {
        File filePath = ensurePathExists(path);
        try (Writer writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateSimpleDomain(String domainName) {
        return "    " +
                domainName +
                " {\n" + "        @Override\n" +
                "        public Token createToken(Text text, Fragment fragment) {\n" +
                "            return new BasicToken(fragment, " +
                domainName + ");\n" + "        }\n" + "    }";
    }

    public static String generateSimpleDomainsEnum(List<String> domainNames, String packageName) {
        StringBuilder result = new StringBuilder();

        result.append("package ")
                .append(packageName)
                .append(";\n\n");
        result.append("import io.github.sboyanovich.scannergenerator.scanner.Fragment;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.Text;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.Domain;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.Token;\n\n");
        result.append("public enum SimpleDomains implements Domain {\n");

        if (domainNames.size() > 0) {
            result.append(generateSimpleDomain(domainNames.get(0)));
        }
        for (int i = 1; i < domainNames.size(); i++) {
            result.append(",\n")
                    .append(generateSimpleDomain(domainNames.get(i)));
        }

        result.append("\n}");

        return result.toString();
    }

    private static String generateDomainWithAttribute(String domainName, String attributeType) {
        return "    " + domainName + " {\n" +
                "        @Override\n" +
                "        public " + attributeType + " attribute(Text text, Fragment fragment) {\n\n" +
                "        }\n" +
                "\n" +
                "        @Override\n" +
                "        public TokenWithAttribute<" + attributeType +
                "> createToken(Text text, Fragment fragment) {\n" +
                "            return new TokenWithAttribute<>(fragment, " +
                domainName + ", attribute(text, fragment));\n" +
                "        }\n" +
                "    }";
    }

    public static String generateDomainWithAttributeEnum(
            String attributeType, List<String> domainNames, String packageName
    ) {
        StringBuilder result = new StringBuilder();

        result.append("package ")
                .append(packageName)
                .append(";\n\n");
        result.append("import io.github.sboyanovich.scannergenerator.scanner.Fragment;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.Text;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;\n\n");
        result.append("public enum DomainsWith")
                .append(attributeType)
                .append("Attribute implements DomainWithAttribute<").append(attributeType).append("> {\n");

        if (domainNames.size() > 0) {
            result.append(generateDomainWithAttribute(domainNames.get(0), attributeType));
        }
        for (int i = 1; i < domainNames.size(); i++) {
            result.append(",\n")
                    .append(generateDomainWithAttribute(domainNames.get(i), attributeType));
        }

        result.append("\n}");

        return result.toString();
    }

    public static String generateStateTagsEnum(List<String> stateNames, String packageName) {
        StringBuilder result = new StringBuilder();

        result.append("package ")
                .append(packageName)
                .append(";\n\n");
        result.append("import io.github.sboyanovich.scannergenerator.automata.StateTag;\n\n");
        result.append("public enum StateTags implements StateTag {\n");

        if (stateNames.size() > 0) {
            result.append("    ").append(stateNames.get(0));
        }
        for (int i = 1; i < stateNames.size(); i++) {
            result.append(",\n")
                    .append("    ").append(stateNames.get(i));
        }

        result.append("\n}");

        return result.toString();
    }

    /// EXPERIMENTAL METHODS SECTION

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

    public static List<Integer> mentioned(NFA nfa) {
        int alphabetSize = nfa.getAlphabetSize();
        int n = nfa.getNumberOfStates();
        NFAStateGraph edges = nfa.getEdges();

        Set<Integer> aux = new HashSet<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Optional<Set<Integer>> marker = edges.getEdgeMarker(i, j);
                if (marker.isPresent()) {
                    Set<Integer> markerSet = marker.get();
                    if (!isSubtractive(markerSet, alphabetSize, 15)) {
                        aux.addAll(markerSet);
                    } else {
                        for (int k = 0; k < alphabetSize; k++) {
                            if (!markerSet.contains(k)) {
                                aux.add(k);
                            }
                        }
                    }
                }
            }
        }

        List<Integer> result = new ArrayList<>(aux);

        return result;
    }
}
