package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.CFGrammar;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.UAString;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.UnifiedAlphabetSymbol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.utility.Utility.asCodePoint;
import static io.github.sboyanovich.scannergenerator.utility.Utility.asString;

public class Utility {
    public static <K, V> Map<V, K> inverseMap(Map<K, V> map) {
        Map<V, K> resultMap = new HashMap<>();
        for (K key : map.keySet()) {
            V val = map.get(key);
            resultMap.put(val, key);
        }
        return resultMap;
    }

    public static ParseTree parse(
            List<Token> tokens,
            Function<Integer, String> nonTerminalNames,
            int axiom,

            // these two should be mutually inverse
            // Must contain a mapping for END_OF_PROGRAM
            Function<Domain, Integer> terminalNumbering,
            Function<Integer, Domain> terminalAlphabetInterpretation,

            int[][] predictionTable,
            Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> rules
    ) throws ParseException {
        ParseTree result = new ParseTree(axiom);

        Deque<UnifiedAlphabetSymbol> stack = new ArrayDeque<>();
        Deque<ParseTree.Node> nodeStack = new ArrayDeque<>(); // for building parse tree

        stack.push(new UnifiedAlphabetSymbol(
                terminalNumbering.apply(DomainEOP.END_OF_PROGRAM),
                true
        ));
        stack.push(new UnifiedAlphabetSymbol(axiom, false));

        nodeStack.push(new ParseTree.TerminalNode(null, -1)); // dummy for symmetry
        nodeStack.push(result.getRoot());

        int cnt = 0; // input tracker
        int nodeCnt = 1; // node numbering

        while (!stack.isEmpty()) {
            UnifiedAlphabetSymbol genSymbol = stack.pop();
            Token curr = tokens.get(cnt);
            if (genSymbol.isTerminal()) {
                ParseTree.TerminalNode leaf = (ParseTree.TerminalNode) nodeStack.pop();
                Domain expected = terminalAlphabetInterpretation.apply(genSymbol.getSymbol());
                if (expected != curr.getTag()) {
                    throw new ParseException("Unexpected token encountered: expected type " +
                            expected + ", got " + curr
                    );
                } else {
                    // setting successfully recognized token in the parse tree
                    leaf.setSymbol(curr);
                    cnt++;
                }
            } else {
                int nonTerminal = genSymbol.getSymbol();
                int ruleNo = predictionTable[nonTerminal][terminalNumbering.apply(curr.getTag())];

                // -1 symbolizes ERR in prediction table
                if (ruleNo == -1) {
                    throw new ParseException("Token type " + curr.getTag() + " not allowed here!\n" +
                            "Offending (token, nonTerminal) pair: " +
                            "(" + curr + ", " + nonTerminalNames.apply(nonTerminal) + ")");
                } else {
                    ParseTree.NonTerminalNode subtree = (ParseTree.NonTerminalNode) nodeStack.pop();

                    List<ParseTree.Node> nodes = new ArrayList<>();
                    List<UnifiedAlphabetSymbol> rhs = rules.get(nonTerminal).get(ruleNo);
                    for (int i = rhs.size() - 1; i >= 0; i--) {
                        UnifiedAlphabetSymbol gs = rhs.get(i);
                        stack.push(gs);
                        if (!gs.isTerminal()) {
                            ParseTree.NonTerminalNode nonTerminalNode =
                                    new ParseTree.NonTerminalNode(gs.getSymbol(), nodeCnt);
                            nodeCnt++;
                            nodeStack.push(
                                    nonTerminalNode
                            );
                            nodes.add(nonTerminalNode);
                        } else {
                            // for now unknown token
                            ParseTree.TerminalNode terminalNode = new ParseTree.TerminalNode(null, nodeCnt);
                            nodeCnt++;
                            nodeStack.push(
                                    terminalNode
                            );
                            nodes.add(terminalNode);
                        }
                    }

                    Collections.reverse(nodes);

                    subtree.setChildren(nodes);
                }
            }
        }
        return result;
    }

    public static String asStringLiteral(String s) {
        StringBuilder result = new StringBuilder();
        Text t = new Text(s);
        int backslash = asCodePoint("\\");
        for (int i = 0; i < s.length(); i++) {
            int curr = t.codePointAt(i);
            if (curr == backslash) {
                result.append(asString(backslash));
            }
            result.append(asString(curr));
        }
        return result.toString();
    }

    public static String grammarAsClass(CFGrammar grammar, String className) throws PredictionTableCreationException {
        StringBuilder result = new StringBuilder();

        // hardcoded
        result.append("package io.github.sboyanovich.scannergenerator.tests.l7.generated;\n" +
                "\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.Domain;\n" +
                "import io.github.sboyanovich.scannergenerator.scanner.token.DomainEOP;\n" +
                "import io.github.sboyanovich.scannergenerator.tests.l7.aux.UnifiedAlphabetSymbol;\n" +
                "import java.util.*;\n");

        result.append("public class ")
                .append(className).append(" {\n");
        result.append("\tprivate static List<String> terminalNames;\n");
        result.append("\tprivate static List<String> nonTerminalNames;\n");
        result.append("\tstatic {\n");
        result.append("\t\tterminalNames = new ArrayList<>();\n");
        result.append("\t\tnonTerminalNames = new ArrayList<>();\n");
        for (int i = 0; i < grammar.getTerminalAlphabetSize(); i++) {
            result.append("\t\tterminalNames.add(\"")
                    .append(asStringLiteral(grammar.getNativeTai().apply(i)))
                    .append("\");\n");
        }
        for (int i = 0; i < grammar.getNonTerminalAlphabetSize(); i++) {
            result.append("\t\tnonTerminalNames.add(\"")
                    .append(asStringLiteral(grammar.getNativeNtai().apply(i)))
                    .append("\");\n");
        }
        result.append("\t}\n");

        result.append("\tpublic static Map<Integer, Domain> getTermInterpretation(Map<String, Domain> domainNames) {\n");
        result.append("\t\tMap<Integer, Domain> result = new HashMap<>();\n");
        result.append("\t\tfor(int i = 0; i < terminalNames.size(); i++) {\n");
        result.append("\t\t\tresult.put(i, domainNames.get(terminalNames.get(i)));\n");
        result.append("\t\t}\n");
        result.append("\t\tresult.put(terminalNames.size(), DomainEOP.END_OF_PROGRAM);\n");
        result.append("\t\treturn result;\n");
        result.append("\t}\n");

        result.append("\tpublic static List<String> getNonTerminalNames() {\n");
        result.append("\t\treturn Collections.unmodifiableList(nonTerminalNames);\n");
        result.append("\t}\n");

        result.append("\tpublic static int getAxiom() {\n");
        result.append("\t\treturn ").append(grammar.getAxiom()).append(";\n");
        result.append("\t}\n");

        int[][] table = grammar.buildPredictiveAnalysisTable();

        result.append("\tpublic static int[][] getPredictionTable() {\n");
        result.append("\t\tint[][] result = new int[").append(table.length)
                .append("][").append(table[0].length).append("];\n");
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                result.append("\t\tresult[").append(i).append("][").append(j).append("] = ")
                        .append(table[i][j]).append(";\n");
            }
        }
        result.append("\t\treturn result;\n");
        result.append("\t}\n");

        result.append("\tpublic static Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> getRules() {\n");
        result.append("\t\tMap<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> result = new HashMap<>();\n");
        result.append("\t\tHashMap<Integer, List<UnifiedAlphabetSymbol>> productions = new HashMap<>();\n");
        result.append("\t\tList<UnifiedAlphabetSymbol> production = new ArrayList<>();\n");
        for (int i = 0; i < grammar.getNonTerminalAlphabetSize(); i++) {
            List<UAString> productions = grammar.getProductions(i);
            for (int j = 0; j < productions.size(); j++) {
                List<UnifiedAlphabetSymbol> seq = productions.get(j).getSymbols();
                for (UnifiedAlphabetSymbol symbol : seq) {
                    result.append("\t\tproduction.add(new UnifiedAlphabetSymbol(")
                            .append(symbol.getSymbol()).append(", ").append(symbol.isTerminal())
                            .append("));\n");
                }
                result.append("\t\tproductions.put(").append(j).append(", production);\n");
                result.append("\t\tproduction = new ArrayList<>();\n");
            }
            result.append("\t\tresult.put(").append(i).append(", productions);\n");
            result.append("\t\tproductions = new HashMap<>();\n");
        }
        result.append("\t\treturn result;\n");
        result.append("\t}\n");

        result.append("}\n");

        return result.toString();
    }

    public static void writeToFile(String fullFilePath, String text) {
        File f = new File(fullFilePath);
        try {
            FileWriter fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.close();
            fw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
