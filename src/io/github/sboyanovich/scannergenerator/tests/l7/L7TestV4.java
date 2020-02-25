package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.tests.data.states.StateTags;
import io.github.sboyanovich.scannergenerator.utility.Pair;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.alphanumerics;
import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.letters;
import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class L7TestV4 {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        //alphabetSize = 2 * Short.MAX_VALUE + 1;
        //alphabetSize = 256; // special case hack for faster recognizer generation

        NFA slWhitespaceNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t"))
                .positiveIteration();

        NFA whitespaceNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t", "\n"))
                .union(NFA.acceptsThisWord(alphabetSize, List.of("\r", "\n")))
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA lettersNFA = NFA.acceptsAllTheseSymbols(alphabetSize, letters);
        NFA alphanumericsNFA = NFA.acceptsAllTheseSymbols(alphabetSize, alphanumerics);
        NFA underscoreNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("_"));

        NFA identifierNFA = lettersNFA
                .concatenation(alphanumericsNFA.union(underscoreNFA).iteration())
                .setAllFinalStatesTo(IDENTIFIER);

        NFA kwAxiomNFA = NFA.acceptsThisWord(alphabetSize, List.of("a", "x", "i", "o", "m"));
        NFA lparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("("));
        NFA rparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(")"));

        NFA axmDeclNFA = lparenNFA
                .concatenation(kwAxiomNFA)
                .concatenation(slWhitespaceNFA)
                .concatenation(identifierNFA)
                .concatenation(rparenNFA)
                .setAllFinalStatesTo(AXM_DECL);

        NFA nonTerminalNFA = lparenNFA
                .concatenation(identifierNFA)
                .concatenation(rparenNFA)
                .setAllFinalStatesTo(NON_TERMINAL);

        NFA equalsNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("="))
                .setAllFinalStatesTo(EQUALS);

        NFA vbarNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("|"))
                .setAllFinalStatesTo(VERTICAL_BAR);

        NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("."))
                .setAllFinalStatesTo(DOT);

        NFA specialNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of(".", "|", "=", "(", ")"));

        NFA arithmOpNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("*", "+", "-", "/"));
        NFA escapeNFA = NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("\\"));

        NFA terminalNFA = identifierNFA
                .union(arithmOpNFA)
                .union(escapeNFA.concatenation(specialNFA))
                .setAllFinalStatesTo(StateTags.TERMINAL);

        /// LEXICAL STRUCTURE

        /*
        // AUX
            IDENTIFIER      = ([a-z][A-Z])([a-z][A-Z][0-9][_])*
            SPECIAL         = [.|=()]

        // TOKEN TYPES
            TERMINAL        = {IDENTIFIER} | [*+-/] | (\\{SPECIAL})
            AXM_DECL        = \(axiom[ \t]+{IDENTIFIER}]\)
            NON_TERMINAL    = \({IDENTIFIER}\)
            EQUALS          = =
            VERTICAL_BAR    = |
            DOT             = \.
        */

        NFA lang = whitespaceNFA
                .union(terminalNFA)
                .union(axmDeclNFA)
                .union(nonTerminalNFA)
                .union(equalsNFA)
                .union(vbarNFA)
                .union(dotNFA);

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                TERMINAL,
                AXM_DECL,
                NON_TERMINAL,
                DOT,
                VERTICAL_BAR,
                EQUALS
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        System.out.println("NFA has " + lang.getNumberOfStates() + " states.");

        Instant start = Instant.now();
        LexicalRecognizer recognizer = new LexicalRecognizer(lang.determinize(priorityMap));
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Recognizer built in " + timeElapsed + "ms!\n");

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);

        String text = Utility.getText("l7test3.txt");

        Compiler compiler = new Compiler(recognizer);
        Scanner scanner = compiler.getScanner(text);

        Set<Domain> ignoredTokenTypes = Set.of(
                SimpleDomains.WHITESPACE,
                Domain.END_OF_PROGRAM,
                Domain.ERROR
        );

        int errCount = 0;

        List<Token> tokensToParse = new ArrayList<>();

        Token t = scanner.nextToken();
        while (t.getTag() != Domain.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                tokensToParse.add(t);
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
        }
        tokensToParse.add(t);

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }

        List<String> nonTerminalNames = List.of(
                "<lang>",
                "<rule_list>",
                "<rule>",
                "<rhs_list>",
                "<rhs_list_c>",
                "<rhs>",
                "<t>",
                "<lhs>"
        );

        /// HARDCODING PREDICTION TABLE AND PRODUCTION RULES

        // GRAMMAR
        /*
            0	<lang> 		    := <rule> <rule_list> .
            1	<rule_list>	    := <rule> <rule_list> | .
            2	<rule>		    := <lhs> EQUALS <rhs_list> DOT .
            3	<rhs_list>	    := <rhs> <rhs_list_c> .
            4	<rhs_list_c>	:= VERTICAL_BAR <rhs_list> | .
            5	<rhs>		    := <t> <rhs> | .
            6	<t>		        := TERMINAL | NON_TERMINAL .
            7	<lhs>		    := NON_TERMINAL | AXM_DECL .
        */
        // TERMINALS ORDER
        /*
            0   TERMINAL
            1   NON_TERMINAL
            2   AXM_DECL
            3   DOT
            4   VERTICAL_BAR
            5   EQUALS
            6   $
        */

        Map<Integer, Map<Domain, Integer>> table = new HashMap<>();
        Map<Integer, Map<Integer, List<Pair<Integer, Domain>>>> rules = new HashMap<>();

        table.put(0, Map.of(
                DomainsWithStringAttribute.AXM_DECL, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1
        ));

        rules.put(0, Map.of(
                1, List.of(
                        new Pair<>(2, null),
                        new Pair<>(1, null)
                )
        ));

        table.put(1, Map.of(
                DomainsWithStringAttribute.AXM_DECL, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                Domain.END_OF_PROGRAM, 2
        ));

        rules.put(1, Map.of(
                1, List.of(
                        new Pair<>(2, null),
                        new Pair<>(1, null)
                ),
                2, List.of()
        ));

        table.put(2, Map.of(
                DomainsWithStringAttribute.AXM_DECL, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1
        ));

        rules.put(2, Map.of(
                1, List.of(
                        new Pair<>(7, null),
                        new Pair<>(null, SimpleDomains.EQUALS),
                        new Pair<>(3, null),
                        new Pair<>(null, SimpleDomains.DOT)
                )
        ));

        table.put(3, Map.of(
                DomainsWithStringAttribute.TERMINAL, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                SimpleDomains.VERTICAL_BAR, 1,
                SimpleDomains.DOT, 1
        ));

        rules.put(3, Map.of(
                1, List.of(
                        new Pair<>(5, null),
                        new Pair<>(4, null)
                )
        ));

        table.put(4, Map.of(
                SimpleDomains.VERTICAL_BAR, 1,
                SimpleDomains.DOT, 2
        ));

        rules.put(4, Map.of(
                1, List.of(
                        new Pair<>(null, SimpleDomains.VERTICAL_BAR),
                        new Pair<>(3, null)
                ),
                2, List.of()
        ));

        table.put(5, Map.of(
                DomainsWithStringAttribute.TERMINAL, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                SimpleDomains.VERTICAL_BAR, 2,
                SimpleDomains.DOT, 2
        ));

        rules.put(5, Map.of(
                1, List.of(
                        new Pair<>(6, null),
                        new Pair<>(5, null)
                ),
                2, List.of()
        ));

        table.put(6, Map.of(
                DomainsWithStringAttribute.TERMINAL, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 2
        ));

        rules.put(6, Map.of(
                1, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.TERMINAL)
                ),
                2, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.NON_TERMINAL)
                )
        ));

        table.put(7, Map.of(
                DomainsWithStringAttribute.AXM_DECL, 2,
                DomainsWithStringAttribute.NON_TERMINAL, 1
        ));

        rules.put(7, Map.of(
                1, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.NON_TERMINAL)
                ),
                2, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.AXM_DECL)
                )
        ));

        if (errCount == 0) {
            try {
                ParseTree derivation = parse(tokensToParse, nonTerminalNames, table, rules);
                dot = derivation.toGraphvizDotString(nonTerminalNames::get);
                System.out.println();
                System.out.println(dot);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                System.out.println("There is a syntax error in the input. Parsing cannot proceed.");
            }
        } else {
            System.out.println("There are lexical errors in the input. Parsing cannot begin.");
        }
    }

    static boolean isTerminal(Pair<Integer, Domain> genSymbol) {
        return genSymbol.getFirst() == null;
    }

    static ParseTree parse(
            List<Token> tokens,
            List<String> nonTerminalNames,
            Map<Integer, Map<Domain, Integer>> predictionTable,
            Map<Integer, Map<Integer, List<Pair<Integer, Domain>>>> rules
    ) throws ParseException {
        ParseTree result = new ParseTree(0); // knowing axiom is 0 here

        Deque<Pair<Integer, Domain>> stack = new ArrayDeque<>();
        Deque<ParseTree.Node> nodeStack = new ArrayDeque<>(); // for building parse tree

        stack.push(new Pair<>(null, Domain.END_OF_PROGRAM));
        stack.push(new Pair<>(0, null));

        nodeStack.push(new ParseTree.TerminalNode(null, -1)); // dummy for symmetry
        nodeStack.push(result.getRoot());

        int cnt = 0; // input tracker
        int nodeCnt = 1; // node numbering

        while (!stack.isEmpty()) {
            Pair<Integer, Domain> genSymbol = stack.pop();
            Token curr = tokens.get(cnt);
            if (isTerminal(genSymbol)) {
                ParseTree.TerminalNode leaf = (ParseTree.TerminalNode) nodeStack.pop();
                Domain expected = genSymbol.getSecond();
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
                Integer nonTerminal = genSymbol.getFirst();
                Integer ruleNo = predictionTable.get(nonTerminal).get(curr.getTag());

                // null symbolizes ERR in prediction table
                if (ruleNo == null) {
                    throw new ParseException("Token type " + curr.getTag() + " not allowed here!\n" +
                            "Offending (token, nonTerminal) pair: " +
                            "(" + curr + ", " + nonTerminalNames.get(nonTerminal) + ")");
                } else {
                    ParseTree.NonTerminalNode subtree = (ParseTree.NonTerminalNode) nodeStack.pop();

                    String ruleApplied = nonTerminalNames.get(nonTerminal) + "_" + ruleNo;
                    // System.out.println(ruleApplied);

                    List<ParseTree.Node> nodes = new ArrayList<>();
                    List<Pair<Integer, Domain>> rhs = rules.get(nonTerminal).get(ruleNo);
                    for (int i = rhs.size() - 1; i >= 0; i--) {
                        Pair<Integer, Domain> gs = rhs.get(i);
                        stack.push(gs);
                        if (!isTerminal(gs)) {
                            ParseTree.NonTerminalNode nonTerminalNode =
                                    new ParseTree.NonTerminalNode(gs.getFirst(), nodeCnt);
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
