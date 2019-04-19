package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.UnifiedAlphabetSymbol;

import java.util.*;
import java.util.function.Function;

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

            Map<Integer, Map<Integer, Integer>> predictionTable,
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
                Integer ruleNo = predictionTable.get(nonTerminal).get(
                        terminalNumbering.apply(
                                curr.getTag()
                        )
                );

                // null symbolizes ERR in prediction table
                if (ruleNo == null) {
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
}
