package io.github.sboyanovich.scannergenerator.tests.lexgen;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.lexgen.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class Test {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        alphabetSize = 256;

/*
        NFA spaceNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(" "));
        NFA tabNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\t"));
        NFA newlineNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\n"));
        NFA carretNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("\r"));

        NFA whitespaceNFA = spaceNFA
                .union(tabNFA)
                .union(carretNFA.concatenation(newlineNFA))
                .union(newlineNFA)
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);
*/

        Set<Integer> chars = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            if (i != asCodePoint("\r") &&
                    i != asCodePoint("\n") &&
                    i != asCodePoint("\t") &&
                    i != asCodePoint("-") &&
                    i != asCodePoint("\\") &&
                    i != asCodePoint("^") &&
                    i != asCodePoint("[") &&
                    i != asCodePoint("]") &&
                    i != asCodePoint("\b") &&
                    i != asCodePoint("\f")
            ) {
                chars.add(i);
            }
        }

        NFA classSingleCharNFA = NFA.acceptsAllTheseCodePoints(alphabetSize, chars);

        NFA decimalDigitsNFA = NFA.acceptsAllTheseSymbols(
                alphabetSize, Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        );

        NFA hexDigitsNFA = decimalDigitsNFA
                .union(
                        NFA.acceptsAllTheseSymbols(alphabetSize, Set.of("A", "B", "C", "D", "E", "F"))
                );

        NFA decimalNumberNFA = decimalDigitsNFA.positiveIteration();
        NFA hexNumberNFA = hexDigitsNFA.positiveIteration();

        NFA decimalEscapeNFA = NFA.acceptsThisWord(alphabetSize, "\\U+#")
                .concatenation(decimalNumberNFA);
        NFA hexEscapeNFA = NFA.acceptsThisWord(alphabetSize, "\\U+")
                .concatenation(hexNumberNFA);

        NFA uEscapeNFA = decimalEscapeNFA.union(hexEscapeNFA);

        NFA classEscapeNFA = uEscapeNFA
                .union(NFA.acceptsThisWord(alphabetSize, "\\b"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\t"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\n"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\f"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\r"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\\\"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\-"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\^"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\["))
                .union(NFA.acceptsThisWord(alphabetSize, "\\]"));

        NFA classCharNFA = classSingleCharNFA.union(classEscapeNFA);

        NFA escapeNFA = uEscapeNFA
                .union(NFA.acceptsThisWord(alphabetSize, "\\b"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\t"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\n"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\f"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\r"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\\""))
                .union(NFA.acceptsThisWord(alphabetSize, "\\'"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\\\"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\*"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\+"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\|"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\?"))
                .union(NFA.acceptsThisWord(alphabetSize, "\\."))
                .union(NFA.acceptsThisWord(alphabetSize, "\\("))
                .union(NFA.acceptsThisWord(alphabetSize, "\\)"));

        chars = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            if (i != asCodePoint("\r") && i != asCodePoint("\n")) {
                chars.add(i);
            }
        }
        NFA inputCharNFA = NFA.acceptsAllTheseCodePoints(alphabetSize, chars);

        chars = new HashSet<>();
        for (int i = asCodePoint("A"); i <= asCodePoint("Z"); i++) {
            chars.add(i);
        }
        for (int i = asCodePoint("a"); i <= asCodePoint("z"); i++) {
            chars.add(i);
        }

        NFA underscoreNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("_"));
        NFA latinLettersNFA = NFA.acceptsAllTheseCodePoints(alphabetSize, chars);
        NFA idenStartNFA = latinLettersNFA.union(underscoreNFA);
        NFA idenPartNFA = idenStartNFA.union(decimalDigitsNFA);
        NFA identifierNFA = idenStartNFA.concatenation(idenPartNFA.iteration());

        NFA namedExprNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("{"))
                .concatenation(identifierNFA)
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("}")))
                .setAllFinalStatesTo(NAMED_EXPR);

        NFA charNFA = inputCharNFA.union(escapeNFA)
                .setAllFinalStatesTo(CHAR);

        NFA segmentNFA = classCharNFA
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("-")))
                .concatenation(classCharNFA);

        NFA charsAndSegmentsNFA = classCharNFA.union(segmentNFA).positiveIteration();
        NFA charClassNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("["))
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("^")).optional())
                .concatenation(charsAndSegmentsNFA)
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("]")))
                .setAllFinalStatesTo(CHAR_CLASS);

        NFA dotNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("."))
                .setAllFinalStatesTo(DOT);
        NFA iterationOpNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .setAllFinalStatesTo(ITERATION_OP);
        NFA posIterationOpNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("+"))
                .setAllFinalStatesTo(POS_ITERATION_OP);
        NFA unionOpNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("|"))
                .setAllFinalStatesTo(UNION_OP);
        NFA optionOpNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("?"))
                .setAllFinalStatesTo(OPTION_OP);

        NFA lParenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("("))
                .setAllFinalStatesTo(LPAREN);
        NFA rParenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(")"))
                .setAllFinalStatesTo(RPAREN);

        NFA classMinusOpNFA = NFA.acceptsThisWord(alphabetSize, "{-}")
                .setAllFinalStatesTo(CLASS_MINUS_OP);

        NFA repetitionOpNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("{"))
                .concatenation(decimalNumberNFA)
                .concatenation(
                        NFA.singleLetterLanguage(alphabetSize, asCodePoint(","))
                                .concatenation(decimalNumberNFA.optional()).optional()
                )
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("}")))
                .setAllFinalStatesTo(REPETITION_OP);

        List<StateTag> priorityList = new ArrayList<>(
                List.of(
                        //WHITESPACE,
                        CHAR,
                        CHAR_CLASS,
                        DOT,
                        ITERATION_OP,
                        POS_ITERATION_OP,
                        UNION_OP,
                        OPTION_OP,
                        LPAREN,
                        RPAREN
                )
        );


        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        NFA lang = charNFA
                //.union(whitespaceNFA)
                .union(charClassNFA)
                .union(dotNFA)
                .union(iterationOpNFA)
                .union(posIterationOpNFA)
                .union(unionOpNFA)
                .union(optionOpNFA)
                .union(lParenNFA)
                .union(rParenNFA)
                .union(namedExprNFA)
                .union(classMinusOpNFA)
                .union(repetitionOpNFA);

        System.out.println(lang.getNumberOfStates());

        // This appears to be necessary for determinization to work properly. It shouldn't be.
        lang = lang.removeLambdaSteps();
        System.out.println("Lambda steps removed.");

        Instant start = Instant.now();
        DFA dfa = lang.determinize(priorityMap);
        Instant stop = Instant.now();
        long timeElapsed = Duration.between(start, stop).toMillis();

        System.out.println("Determinized!");
        System.out.println("\tin " + timeElapsed + "ms");
        System.out.println("States: " + dfa.getNumberOfStates());
        System.out.println("Classes: " + dfa.getTransitionTable().getEquivalenceMap().getEqClassDomain());

        start = Instant.now();
        LexicalRecognizer recognizer = new LexicalRecognizer(dfa);
        stop = Instant.now();
        timeElapsed = Duration.between(start, stop).toMillis();
        System.out.println("Recognizer built!");
        System.out.println("\tin " + timeElapsed + "ms");
        System.out.println("States: " + recognizer.getNumberOfStates());
        System.out.println("Classes: " + recognizer.getNumberOfColumns());

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);
        String factorization = recognizer.displayEquivalenceMap(Utility::defaultUnicodeInterpretation);
        System.out.println(NEWLINE + factorization + NEWLINE);

        String text = Utility.getText("LGTest1.txt");

        Compiler compiler = new Compiler(recognizer);
        Scanner scanner = compiler.getScanner(text);

        Set<Domain> ignoredTokenTypes = Set.of(
                SimpleDomains.WHITESPACE,
                Domain.END_OF_PROGRAM,
                Domain.ERROR
        );

        int errCount = 0;

        Token t = scanner.nextToken();
        while (t.getTag() != Domain.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
        }

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }

    }
}
