package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.scanner.Compiler;
import io.github.sboyanovich.scannergenerator.scanner.*;
import io.github.sboyanovich.scannergenerator.scanner.Scanner;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.tests.data.CommonCharClasses.*;
import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class ExampleTestNew {
    public static void main(String[] args) {
        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        // alphabetSize = 2 * Short.MAX_VALUE + 1;

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

        NFA digitNFA = acceptsAllTheseSymbols(alphabetSize, digits);
        NFA integerLiteralNFA = digitNFA.positiveIteration()
                .setAllFinalStatesTo(INTEGER_LITERAL);

        NFA kwIfNFA = acceptThisWord(alphabetSize, List.of("i", "f"))
                .setAllFinalStatesTo(KEYWORD_IF);

        NFA kwElifNFA = acceptThisWord(alphabetSize, List.of("e", "l", "i", "f"))
                .setAllFinalStatesTo(KEYWORD_ELIF);

        NFA opDivideNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("/"))
                .setAllFinalStatesTo(OP_DIVIDE);
        NFA opMultiplyNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .setAllFinalStatesTo(OP_MULTIPLY);

        NFAStateGraphBuilder commentNFAEdges = new NFAStateGraphBuilder(6, alphabetSize);
        addEdge(commentNFAEdges, 0, 1, Set.of("/"));
        addEdge(commentNFAEdges, 1, 2, Set.of("*"));
        addEdge(commentNFAEdges, 2, 4, Set.of("*"));
        addEdgeSubtractive(commentNFAEdges, 2, 3, Set.of("*"));
        addEdgeSubtractive(commentNFAEdges, 3, 3, Set.of("*"));
        addEdge(commentNFAEdges, 3, 4, Set.of("*"));
        addEdgeSubtractive(commentNFAEdges, 4, 3, Set.of("*", "/"));
        addEdge(commentNFAEdges, 4, 4, Set.of("*"));
        addEdge(commentNFAEdges, 4, 5, Set.of("/"));

        NFA commentNFA = new NFA(6, alphabetSize, 0, Map.of(5, COMMENT), commentNFAEdges.build());

        List<StateTag> priorityList = List.of(
                WHITESPACE,
                COMMENT,
                OP_DIVIDE,
                OP_MULTIPLY,
                INTEGER_LITERAL,
                IDENTIFIER,
                KEYWORD_ELIF,
                KEYWORD_IF
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        NFA lang = whitespaceNFA
                .union(identifierNFA)
                .union(integerLiteralNFA)
                .union(kwIfNFA)
                .union(kwElifNFA)
                .union(opDivideNFA)
                .union(opMultiplyNFA)
                .union(commentNFA);

        System.out.println(lang.getNumberOfStates());

        lang = lang.removeLambdaSteps();

        // EXPERIMENTAL
        List<Integer> mentioned = mentioned(lang);
        System.out.println(mentioned.size() + " mentioned symbols");
        EquivalenceMap hint = Utility.getCoarseSymbolClassMap(mentioned, alphabetSize);
        System.out.println(hint.getDomain() + " -> " + hint.getEqClassDomain());

        DFA dfa = lang.determinize(priorityMap);

        System.out.println("Determinized!");
        System.out.println(dfa.getNumberOfStates());

        LexicalRecognizer recognizer = new LexicalRecognizer(hint, dfa);
        System.out.println("Recognizer built!");

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);

        String text = Utility.getText("testWin.txt");

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
