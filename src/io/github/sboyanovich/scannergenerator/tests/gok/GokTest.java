package io.github.sboyanovich.scannergenerator.tests.gok;

import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.*;
import static io.github.sboyanovich.scannergenerator.tests.gok.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class GokTest {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        //alphabetSize = 2 * Short.MAX_VALUE + 1;
        alphabetSize = 256; // special case hack for faster recognizer generation

        NFA whitespaceNFA = acceptsAllTheseSymbols(alphabetSize, Set.of(" ", "\t", "\n"))
                .union(acceptThisWord(alphabetSize, List.of("\r", "\n")))
                .positiveIteration()
                .setAllFinalStatesTo(WHITESPACE);

        NFA lettersNFA = acceptsAllTheseSymbols(alphabetSize, letters);
        NFA alphanumericsNFA = acceptsAllTheseSymbols(alphabetSize, alphanumerics);

        NFA identifierNFA = lettersNFA
                .concatenation(alphanumericsNFA.iteration())
                .setAllFinalStatesTo(ID);

        NFA numNFA = acceptsAllTheseSymbols(alphabetSize, digits)
                .positiveIteration()
                .setAllFinalStatesTo(NUM);

        NFA kwReturnNFA = acceptThisWord(alphabetSize, List.of("r", "e", "t", "u", "r", "n"))
                .setAllFinalStatesTo(KW_RETURN);

        NFA lparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("("))
                .setAllFinalStatesTo(LPAREN);
        NFA rparenNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(")"))
                .setAllFinalStatesTo(RPAREN);

        NFA kwIfNFA = acceptThisWord(alphabetSize, List.of("i", "f"))
                .setAllFinalStatesTo(KW_IF);
        NFA kwElseNFA = acceptThisWord(alphabetSize, List.of("e", "l", "s", "e"))
                .setAllFinalStatesTo(KW_ELSE);
        NFA kwForNFA = acceptThisWord(alphabetSize, List.of("f", "o", "r"))
                .setAllFinalStatesTo(KW_FOR);

        NFA plusNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("+"))
                .setAllFinalStatesTo(PLUS);
        NFA minusNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("-"))
                .setAllFinalStatesTo(MINUS);
        NFA timesNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .setAllFinalStatesTo(MUL);

        NFA cmpNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("<"))
                .setAllFinalStatesTo(CMP);

        NFA eqNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("="))
                .setAllFinalStatesTo(ASSIGN);

        NFA semicolonNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(";"))
                .setAllFinalStatesTo(StateTags.SEMICOLON);

        NFA lbracketNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("{"))
                .setAllFinalStatesTo(LBRACKET);
        NFA rbracketNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("}"))
                .setAllFinalStatesTo(RBRACKET);

        NFA commaNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint(","))
                .setAllFinalStatesTo(StateTags.COMMA);

        NFA dplusNFA = acceptThisWord(alphabetSize, List.of("+", "+"))
                .setAllFinalStatesTo(INC);
        NFA dminusNFA = acceptThisWord(alphabetSize, List.of("-", "-"))
                .setAllFinalStatesTo(DEC);
        NFA deqNFA = acceptThisWord(alphabetSize, List.of("=", "="))
                .setAllFinalStatesTo(EQUALS);

        NFA lang = whitespaceNFA
                .union(identifierNFA)
                .union(numNFA)
                .union(lparenNFA)
                .union(rparenNFA)
                .union(lbracketNFA)
                .union(rbracketNFA)
                .union(semicolonNFA)
                .union(commaNFA)
                .union(plusNFA)
                .union(minusNFA)
                .union(timesNFA)
                .union(cmpNFA)
                .union(eqNFA)
                .union(kwReturnNFA)
                .union(kwIfNFA)
                .union(kwElseNFA)
                .union(kwForNFA)
                .union(dplusNFA)
                .union(dminusNFA)
                .union(deqNFA);

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                ID,
                NUM,
                LPAREN,
                RPAREN,
                LBRACKET,
                RBRACKET,
                StateTags.SEMICOLON,
                StateTags.COMMA,
                PLUS,
                MINUS,
                MUL,
                CMP,
                EQUALS,
                ASSIGN,
                INC,
                DEC,
                EQUALS,
                KW_RETURN,
                KW_FOR,
                KW_IF,
                KW_ELSE
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

        String text = Utility.getText("gok.txt");

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

        IRNode ast = Parser.parse(tokensToParse.iterator());

        String astdot = ast.toGraphVizDotString();
        System.out.println();
        System.out.println(astdot);
    }
}