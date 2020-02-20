package io.github.sboyanovich.scannergenerator.tests.l7;

import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.alphanumerics;
import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.letters;
import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class L7Test {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        alphabetSize = 2 * Short.MAX_VALUE + 1;

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

        NFA lettersNFA = acceptsAllTheseSymbols(alphabetSize, letters);
        NFA alphanumericsNFA = acceptsAllTheseSymbols(alphabetSize, alphanumerics);

        NFA identifierNFA = lettersNFA
                .concatenation(alphanumericsNFA.iteration())
                .setAllFinalStatesTo(IDENTIFIER);

        NFA kwAxiomNFA = acceptThisWord(alphabetSize, List.of("a", "x", "i", "o", "m"))
                .setAllFinalStatesTo(KEYWORD_AXIOM);

        NFA opPlusNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("+"))
                .setAllFinalStatesTo(OP_PLUS);
        NFA opMultiplyNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .setAllFinalStatesTo(OP_MULTIPLY);

        NFA lparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("("))
                .setAllFinalStatesTo(LPAREN);
        NFA rparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(")"))
                .setAllFinalStatesTo(RPAREN);

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
                .union(kwAxiomNFA)
                .union(opPlusNFA)
                .union(opMultiplyNFA)
                .union(equalsNFA)
                .union(vbarNFA)
                .union(dotNFA)
                .union(lparenNFA)
                .union(rparenNFA)
                .union(eslpNFA)
                .union(esrpNFA);

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                IDENTIFIER,
                OP_MULTIPLY,
                OP_PLUS,
                KEYWORD_AXIOM,
                LPAREN,
                RPAREN,
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

        LexicalRecognizer recognizer = new LexicalRecognizer(lang.determinize(priorityMap));
        System.out.println("Recognizer built!");

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);

        String text = Utility.getText("l7test.txt");

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
