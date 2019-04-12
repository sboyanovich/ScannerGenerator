package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.Position;
import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraph;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.lex.Compiler;
import io.github.sboyanovich.scannergenerator.lex.*;
import io.github.sboyanovich.scannergenerator.lex.Scanner;
import io.github.sboyanovich.scannergenerator.token.Domain;
import io.github.sboyanovich.scannergenerator.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.token.DomainError;
import io.github.sboyanovich.scannergenerator.token.Token;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class ExampleTestNew {
    public static void main(String[] args) {

        Set<String> digits = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            digits.add(String.valueOf(i));
        }

        Set<String> capitalLatins = new HashSet<>();
        int capA = asCodePoint("A");
        int capZ = asCodePoint("Z");
        for (int i = capA; i <= capZ; i++) {
            capitalLatins.add(asString(i));
        }

        Set<String> lowercaseLatins = new HashSet<>();
        int lcA = asCodePoint("a");
        int lcZ = asCodePoint("z");
        for (int i = lcA; i <= lcZ; i++) {
            lowercaseLatins.add(asString(i));
        }

        Set<String> letters = union(capitalLatins, lowercaseLatins);
        Set<String> alphanumerics = union(letters, digits);

        int alphabetSize = Character.MAX_CODE_POINT + 1;
        /*
            basically, n^2 complexity of brute force emap building is prohibitive
            when dealing with entire Unicode span
        */
        alphabetSize = 128;

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

        NFA kwIfNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("i"))
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("f")));

        NFA kwElifNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("e"))
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("l")))
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("i")))
                .concatenation(NFA.singleLetterLanguage(alphabetSize, asCodePoint("f")));

        NFA keywordNFA = kwIfNFA.union(kwElifNFA)
                .setAllFinalStatesTo(KEYWORD);

        NFA operationNFA = NFA.singleLetterLanguage(alphabetSize, asCodePoint("*"))
                .union(NFA.singleLetterLanguage(alphabetSize, asCodePoint("/")))
                .setAllFinalStatesTo(OPERATION);

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
                OPERATION,
                INTEGER_LITERAL,
                IDENTIFIER,
                KEYWORD
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        NFA lang = whitespaceNFA
                .union(identifierNFA)
                .union(integerLiteralNFA)
                .union(keywordNFA)
                .union(operationNFA)
                .union(commentNFA);

        System.out.println(lang.getNumberOfStates());

        lang = lang.removeLambdaSteps();

        // EXPERIMENTAL
        List<Integer> mentioned = mentioned(lang);
        System.out.println(mentioned.size());
        EquivalenceMap hint = Utility.getCoarseSymbolClassMap(mentioned, alphabetSize);
        System.out.println(hint.getDomain() + " -> " + hint.getEqClassDomain());
        // hint = EquivalenceMap.identityMap(alphabetSize); // MW

        DFA dfa = lang.determinize(priorityMap);

        System.out.println("Determinized!");
        System.out.println(dfa.getNumberOfStates());

        dfa = dfa.minimize();
        System.out.println("Minimized!");
        System.out.println(dfa.getNumberOfStates());
        System.out.println();


        LexicalRecognizer recognizer = new LexicalRecognizer(hint, dfa);
        System.out.println("Recognizer built!");

        String dot = recognizer.toGraphvizDotString(Object::toString, true);
        System.out.println(dot);

        String text = Utility.getText("testWin.txt");

        Compiler compiler = new Compiler(recognizer);
        Scanner scanner = compiler.getScanner(text);

        Set<Domain> ignoredTokenTypes = Set.of(
                DomainEOP.END_OF_PROGRAM,
                DomainError.ERROR
        );

        int errCount = 0;

        Token t = scanner.nextToken();
        while (t.getTag() != DomainEOP.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == DomainError.ERROR) {
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

    static NFA acceptsAllTheseSymbols(int alphabetSize, Set<String> symbols) {
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(2, alphabetSize);
        Set<Integer> codePoints = symbols.stream().map(Utility::asCodePoint).collect(Collectors.toSet());
        edges.setEdge(0, 1, codePoints);
        return new NFA(2, alphabetSize, 0, Map.of(1, FINAL_DUMMY), edges.build());
    }

    static void addEdge(NFAStateGraphBuilder edges, int from, int to, Set<String> edge) {
        for (String symbol : edge) {
            edges.addSymbolToEdge(from, to, asCodePoint(symbol));
        }
    }

    static void addEdgeSubtractive(NFAStateGraphBuilder edges, int from, int to, Set<String> edge) {
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

    static boolean isMentioned(NFAStateGraph edges, int symbol) {
        int numberOfStates = edges.getNumberOfStates();
        int alphabetSize = edges.getAlphabetSize();

        for (int j = 0; j < numberOfStates; j++) {
            for (int k = 0; k < numberOfStates; k++) {
                Optional<Set<Integer>> marker = edges.getEdgeMarker(j, k);
                if (marker.isPresent()) {
                    Set<Integer> markerSet = marker.get();
                    if (isSubtractive(markerSet, alphabetSize, 5)) {
                        if (!markerSet.contains(symbol)) {
                            return true;
                        }
                    } else if (markerSet.contains(symbol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static List<Integer> mentioned(NFA nfa) {
        int alphabetSize = nfa.getAlphabetSize();
        List<Integer> result = new ArrayList<>();
        NFAStateGraph edges = nfa.getEdges();

        for (int i = 0; i < alphabetSize; i++) {
            if (isMentioned(edges, i)) {
                result.add(i);
            }
        }

        return result;
    }
}
