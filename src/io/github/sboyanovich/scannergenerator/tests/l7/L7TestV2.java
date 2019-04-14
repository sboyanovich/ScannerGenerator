package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.Position;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.lex.Compiler;
import io.github.sboyanovich.scannergenerator.lex.*;
import io.github.sboyanovich.scannergenerator.lex.Scanner;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.token.*;
import io.github.sboyanovich.scannergenerator.utility.Pair;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.alphanumerics;
import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.letters;
import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class L7TestV2 {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        //alphabetSize = 2 * Short.MAX_VALUE + 1;
        alphabetSize = 255; // special case hack for faster recognizer generation

        NFA spaceNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(" "));
        NFA tabNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\t"));
        NFA newlineNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\n"));
        NFA carretNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\r"));

        NFA slWhitespaceNFA = spaceNFA
                .union(tabNFA)
                .positiveIteration();

        NFA whitespaceNFA = spaceNFA
                .union(tabNFA)
                .union(carretNFA.concatenation(newlineNFA))
                .union(newlineNFA)
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA lettersNFA = acceptsAllTheseSymbols(alphabetSize, letters);
        NFA alphanumericsNFA = acceptsAllTheseSymbols(alphabetSize, alphanumerics);

        NFA identifierNFA = lettersNFA
                .concatenation(alphanumericsNFA.iteration())
                .setAllFinalStatesTo(IDENTIFIER);

        NFA kwAxiomNFA = acceptThisWord(alphabetSize, List.of("a", "x", "i", "o", "m"));
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

        NFA opPlusNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("+"))
                .setAllFinalStatesTo(OP_PLUS);
        NFA opMultiplyNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .setAllFinalStatesTo(OP_MULTIPLY);

        NFA equalsNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("="))
                .setAllFinalStatesTo(EQUALS);

        NFA vbarNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("|"))
                .setAllFinalStatesTo(VERTICAL_BAR);

        NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("."))
                .setAllFinalStatesTo(DOT);

        NFA eslpNFA = acceptThisWord(alphabetSize, List.of("\\", "("))
                .setAllFinalStatesTo(ESCAPED_LPAREN);

        NFA esrpNFA = acceptThisWord(alphabetSize, List.of("\\", ")"))
                .setAllFinalStatesTo(ESCAPED_RPAREN);

        NFA lang = whitespaceNFA
                .union(identifierNFA)
                .union(axmDeclNFA)
                .union(nonTerminalNFA)
                .union(opPlusNFA)
                .union(opMultiplyNFA)
                .union(equalsNFA)
                .union(vbarNFA)
                .union(dotNFA)
                .union(eslpNFA)
                .union(esrpNFA);

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                IDENTIFIER,
                AXM_DECL,
                NON_TERMINAL,
                OP_MULTIPLY,
                OP_PLUS,
                DOT,
                VERTICAL_BAR,
                EQUALS,
                ESCAPED_LPAREN,
                ESCAPED_RPAREN
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        System.out.println(lang.getNumberOfStates());

        LexicalRecognizer recognizer = Utility.createRecognizer(lang, priorityMap);
        System.out.println("Recognizer built!");

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);

        String text = Utility.getText("l7test.txt");

        Compiler compiler = new Compiler(recognizer);
        Scanner scanner = compiler.getScanner(text);

        Set<Domain> ignoredTokenTypes = Set.of(
                SimpleDomains.WHITESPACE,
                DomainEOP.END_OF_PROGRAM,
                DomainError.ERROR
        );

        int errCount = 0;

        List<Token> tokensToParse = new ArrayList<>();

        Token t = scanner.nextToken();
        while (t.getTag() != DomainEOP.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                tokensToParse.add(t);
                System.out.println(t);
            }
            if (t.getTag() == DomainError.ERROR) {
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

        List<Domain> columns = List.of(
                DomainsWithStringAttribute.AXM_DECL,
                DomainsWithStringAttribute.IDENTIFIER,
                DomainsWithStringAttribute.NON_TERMINAL,
                SimpleDomains.EQUALS,
                SimpleDomains.VERTICAL_BAR,
                SimpleDomains.DOT,
                SimpleDomains.OP_PLUS,
                SimpleDomains.OP_MULTIPLY,
                SimpleDomains.ESCAPED_LPAREN,
                SimpleDomains.ESCAPED_RPAREN,
                DomainEOP.END_OF_PROGRAM
        );

        List<String> nonTerminalNames = List.of(
                "<lang>",
                "<rule_list>",
                "<rule>",
                "<rhs_list>",
                "<rhs_list_c>",
                "<rhs>",
                "<tl>",
                "<t>",
                "<lhs>"
        );

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
                DomainEOP.END_OF_PROGRAM, 2
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
                        new Pair<>(8, null),
                        new Pair<>(null, SimpleDomains.EQUALS),
                        new Pair<>(3, null),
                        new Pair<>(null, SimpleDomains.DOT)
                )
        ));

        table.put(3, Map.of(
                DomainsWithStringAttribute.IDENTIFIER, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                SimpleDomains.VERTICAL_BAR, 1,
                SimpleDomains.DOT, 1,
                SimpleDomains.OP_PLUS, 1,
                SimpleDomains.OP_MULTIPLY, 1,
                SimpleDomains.ESCAPED_LPAREN, 1,
                SimpleDomains.ESCAPED_RPAREN, 1
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
                DomainsWithStringAttribute.IDENTIFIER, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                SimpleDomains.VERTICAL_BAR, 2,
                SimpleDomains.DOT, 2,
                SimpleDomains.OP_PLUS, 1,
                SimpleDomains.OP_MULTIPLY, 1,
                SimpleDomains.ESCAPED_LPAREN, 1,
                SimpleDomains.ESCAPED_RPAREN, 1
        ));

        rules.put(5, Map.of(
                1, List.of(
                        new Pair<>(7, null),
                        new Pair<>(6, null)
                ),
                2, List.of()
        ));

        table.put(6, Map.of(
                DomainsWithStringAttribute.IDENTIFIER, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                SimpleDomains.VERTICAL_BAR, 2,
                SimpleDomains.DOT, 2,
                SimpleDomains.OP_PLUS, 1,
                SimpleDomains.OP_MULTIPLY, 1,
                SimpleDomains.ESCAPED_LPAREN, 1,
                SimpleDomains.ESCAPED_RPAREN, 1
        ));

        rules.put(6, Map.of(
                1, List.of(
                        new Pair<>(7, null),
                        new Pair<>(6, null)
                ),
                2, List.of()
        ));

        table.put(7, Map.of(
                DomainsWithStringAttribute.IDENTIFIER, 1,
                DomainsWithStringAttribute.NON_TERMINAL, 2,
                SimpleDomains.OP_PLUS, 5,
                SimpleDomains.OP_MULTIPLY, 6,
                SimpleDomains.ESCAPED_LPAREN, 3,
                SimpleDomains.ESCAPED_RPAREN, 4
        ));

        rules.put(7, Map.of(
                1, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.IDENTIFIER)
                ),
                2, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.NON_TERMINAL)
                ),
                3, List.of(
                        new Pair<>(null, SimpleDomains.ESCAPED_LPAREN)
                ),
                4, List.of(
                        new Pair<>(null, SimpleDomains.ESCAPED_RPAREN)
                ),
                5, List.of(
                        new Pair<>(null, SimpleDomains.OP_PLUS)
                ),
                6, List.of(
                        new Pair<>(null, SimpleDomains.OP_MULTIPLY)
                )
        ));

        table.put(8, Map.of(
                DomainsWithStringAttribute.AXM_DECL, 2,
                DomainsWithStringAttribute.NON_TERMINAL, 1
        ));

        rules.put(8, Map.of(
                1, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.NON_TERMINAL)
                ),
                2, List.of(
                        new Pair<>(null, DomainsWithStringAttribute.AXM_DECL)
                )
        ));

        List<String> derivation = parse(tokensToParse, nonTerminalNames, table, rules);
        System.out.println(derivation);
    }

    static boolean isTerminal(Pair<Integer, Domain> genSymbol) {
        return genSymbol.getFirst() == null;
    }

    static List<String> parse(
            List<Token> tokens,
            List<String> nonTerminalNames,
            Map<Integer, Map<Domain, Integer>> predictionTable,
            Map<Integer, Map<Integer, List<Pair<Integer, Domain>>>> rules
    ) {
        List<String> result = new ArrayList<>();
        Deque<Pair<Integer, Domain>> stack = new ArrayDeque<>();
        stack.push(new Pair<>(null, DomainEOP.END_OF_PROGRAM));
        stack.push(new Pair<>(0, null));
        int cnt = 0;
        while (!stack.isEmpty()) {
            Pair<Integer, Domain> genSymbol = stack.pop();
            Token curr = tokens.get(cnt);
            if (isTerminal(genSymbol)) {
                Domain expected = genSymbol.getSecond();
                if (expected != curr.getTag()) {
                    throw new IllegalArgumentException("Unexpected token encountered: expected type " +
                            expected + ", got " + curr
                    );
                } else {
                    cnt++;
                }
            } else {
                Integer nonTerminal = genSymbol.getFirst();
                Integer ruleNo = predictionTable.get(nonTerminal).get(curr.getTag());
                if (ruleNo == null) {
                    throw new IllegalArgumentException("Token type " + curr.getTag() +
                            " not allowed here!\n" +
                            "Violating (token, nonTerminal) pair: " +
                            "(" + curr + ", " + nonTerminalNames.get(nonTerminal) + ")");
                } else {
                    String ruleApplied = nonTerminalNames.get(nonTerminal) + "_" + ruleNo;
                    System.out.println(ruleApplied);
                    result.add(ruleApplied);
                    List<Pair<Integer, Domain>> rhs = rules.get(nonTerminal).get(ruleNo);
                    for (int i = rhs.size() - 1; i >= 0; i--) {
                        stack.push(rhs.get(i));
                    }
                }
            }
        }

        return result;
    }
}
