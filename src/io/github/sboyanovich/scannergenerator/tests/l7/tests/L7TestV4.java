package io.github.sboyanovich.scannergenerator.tests.l7.tests;

import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainError;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.tests.data.states.StateTags;
import io.github.sboyanovich.scannergenerator.tests.l7.ParseException;
import io.github.sboyanovich.scannergenerator.tests.l7.ParseTree;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.UnifiedAlphabetSymbol;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

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
        alphabetSize = 256; // special case hack for faster recognizer generation

        NFA slWhitespaceNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t"))
                .positiveIteration();

        NFA whitespaceNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t", "\n"))
                .union(acceptThisWord(alphabetSize, List.of("\r", "\n")))
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA lettersNFA = acceptsAllTheseSymbols(alphabetSize, letters);
        NFA alphanumericsNFA = acceptsAllTheseSymbols(alphabetSize, alphanumerics);
        NFA underscoreNFA = acceptsAllTheseSymbols(alphabetSize, Set.of("_"));

        NFA identifierNFA = lettersNFA
                .concatenation(alphanumericsNFA.union(underscoreNFA).iteration())
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

        NFA equalsNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("="))
                .setAllFinalStatesTo(EQUALS);

        NFA vbarNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("|"))
                .setAllFinalStatesTo(VERTICAL_BAR);

        NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("."))
                .setAllFinalStatesTo(DOT);

        NFA specialNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(".", "|", "=", "(", ")"));

        NFA arithmOpNFA = acceptsAllTheseSymbols(alphabetSize, Set.of("*", "+", "-", "/"));
        NFA escapeNFA = acceptsAllTheseSymbols(alphabetSize, Set.of("\\"));

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
        LexicalRecognizer recognizer = Utility.createRecognizer(lang, priorityMap);
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

        List<String> nonTerminalNamesList = List.of(
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

        Function<Integer, String> nonTerminalNames = nonTerminalNamesList::get;

        Map<Domain, Integer> tNumMap = Map.of(
                DomainsWithStringAttribute.TERMINAL, 0,
                DomainsWithStringAttribute.NON_TERMINAL, 1,
                DomainsWithStringAttribute.AXM_DECL, 2,
                SimpleDomains.DOT, 3,
                SimpleDomains.VERTICAL_BAR, 4,
                SimpleDomains.EQUALS, 5,
                DomainEOP.END_OF_PROGRAM, 6
        );
        Map<Integer, Domain> tNumMapInv = io.github.sboyanovich.scannergenerator.tests.l7.Utility
                .inverseMap(tNumMap);
        Function<Domain, Integer> terminalNumbering = tNumMap::get;
        Function<Integer, Domain> interpretation = tNumMapInv::get;

        int[][] table = new int[nonTerminalNamesList.size()][tNumMap.size()];
        Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> rules = new HashMap<>();

        table[0][terminalNumbering.apply(DomainsWithStringAttribute.AXM_DECL)] = 1;
        table[0][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 1;

        rules.put(0, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(2, false),
                        new UnifiedAlphabetSymbol(1, false)
                )
        ));

        table[1][terminalNumbering.apply(DomainsWithStringAttribute.AXM_DECL)] = 1;
        table[1][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 1;
        table[1][terminalNumbering.apply(DomainEOP.END_OF_PROGRAM)] = 2;

        rules.put(1, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(2, false),
                        new UnifiedAlphabetSymbol(1, false)
                ),
                2, List.of()
        ));

        table[2][terminalNumbering.apply(DomainsWithStringAttribute.AXM_DECL)] = 1;
        table[2][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 1;

        rules.put(2, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(7, false),
                        new UnifiedAlphabetSymbol(terminalNumbering.apply(SimpleDomains.EQUALS), true),
                        new UnifiedAlphabetSymbol(3, false),
                        new UnifiedAlphabetSymbol(terminalNumbering.apply(SimpleDomains.DOT), true)
                )
        ));

        table[3][terminalNumbering.apply(DomainsWithStringAttribute.TERMINAL)] = 1;
        table[3][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 1;
        table[3][terminalNumbering.apply(SimpleDomains.VERTICAL_BAR)] = 1;
        table[3][terminalNumbering.apply(SimpleDomains.DOT)] = 1;

        rules.put(3, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(5, false),
                        new UnifiedAlphabetSymbol(4, false)
                )
        ));

        table[4][terminalNumbering.apply(SimpleDomains.VERTICAL_BAR)] = 1;
        table[4][terminalNumbering.apply(SimpleDomains.DOT)] = 2;

        rules.put(4, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(terminalNumbering.apply(SimpleDomains.VERTICAL_BAR), true),
                        new UnifiedAlphabetSymbol(3, false)
                ),
                2, List.of()
        ));

        table[5][terminalNumbering.apply(DomainsWithStringAttribute.TERMINAL)] = 1;
        table[5][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 1;
        table[5][terminalNumbering.apply(SimpleDomains.VERTICAL_BAR)] = 2;
        table[5][terminalNumbering.apply(SimpleDomains.DOT)] = 2;

        rules.put(5, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(6, false),
                        new UnifiedAlphabetSymbol(5, false)
                ),
                2, List.of()
        ));

        table[6][terminalNumbering.apply(DomainsWithStringAttribute.TERMINAL)] = 1;
        table[6][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 2;

        rules.put(6, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(terminalNumbering.apply(DomainsWithStringAttribute.TERMINAL), true)
                ),
                2, List.of(
                        new UnifiedAlphabetSymbol(terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL), true)
                )
        ));

        table[7][terminalNumbering.apply(DomainsWithStringAttribute.AXM_DECL)] = 2;
        table[7][terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL)] = 1;

        rules.put(7, Map.of(
                1, List.of(
                        new UnifiedAlphabetSymbol(
                                terminalNumbering.apply(DomainsWithStringAttribute.NON_TERMINAL), true
                        )
                ),
                2, List.of(
                        new UnifiedAlphabetSymbol(terminalNumbering.apply(DomainsWithStringAttribute.AXM_DECL), true)
                )
        ));

        if (errCount == 0) {
            try {
                ParseTree derivation =
                        io.github.sboyanovich.scannergenerator.tests.l7.Utility
                                .parse(
                                        tokensToParse,
                                        nonTerminalNames,
                                        0,
                                        terminalNumbering,
                                        interpretation,
                                        table,
                                        rules
                                );
                dot = derivation.toGraphvizDotString(nonTerminalNames);
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
}