package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.scanner.LexicalRecognizer;
import io.github.sboyanovich.scannergenerator.scanner.Message;
import io.github.sboyanovich.scannergenerator.scanner.Position;
import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RecognizerGenTest {
    public static void main(String[] args) {
        String text = Utility.getText("testInputM1.txt");

        int alphabetSize = Character.MAX_CODE_POINT + 1;

        LexGenScanner scanner = new LexGenScanner(text);
        MockCompiler compiler = scanner.getCompiler();

        Set<Domain> ignoredTokenTypes = Set.of(
                Domain.END_OF_PROGRAM,
                Domain.ERROR
        );

        List<Token> allTokens = new ArrayList<>();

        int errCount = 0;

        Token t = scanner.nextToken();
        allTokens.add(t);
        while (t.getTag() != Domain.END_OF_PROGRAM) {
            if (!ignoredTokenTypes.contains(t.getTag())) {
                System.out.println(t);
            }
            if (t.getTag() == Domain.ERROR) {
                errCount++;
                System.out.println(t.getCoords());
            }
            t = scanner.nextToken();
            allTokens.add(t);
        }

        System.out.println();
        System.out.println("Errors: " + errCount);
        System.out.println("Compiler messages: ");
        SortedMap<Position, Message> messages = compiler.getSortedMessages();
        for (Map.Entry<Position, Message> entry : messages.entrySet()) {
            System.out.println(entry.getValue() + " at " + entry.getKey());
        }

        if (errCount == 0) {
            AST ast = Parser.parse(allTokens.iterator());
            /*String dotAST = ast.toGraphVizDotString();
            System.out.println();
            System.out.println(dotAST);*/
            AST.Spec spec = (AST.Spec) ast;

            Map<String, NFA> definitions = new HashMap<>();

            for (AST.Definitions.Def def : spec.definitions.definitions) {
                String name = def.identifier.identifier;
                NFA auto = buildNFAFromRegex(def.regex, definitions, alphabetSize);
                definitions.put(name, auto);
            }

/*            System.out.println();
            for(String key : definitions.keySet()) {
                NFA auto = definitions.get(key);
                System.out.println(key + ": ");
                String dot = auto.toGraphvizDotString(Utility::defaultUnicodeInterpretation, true);
                System.out.println(dot);
                System.out.println();
            }*/

            Map<String, NFA> modeNFAs = new HashMap<>();
            List<StateTag> priorityList = new ArrayList<>();

            List<AST.Rules.Rule> rules = spec.rules.rules;
            for (AST.Rules.Rule rule : rules) {
                String stateName = rule.stateName;
                NFA nfa = buildNFAFromRegex(rule.regex, definitions, alphabetSize);

                StateTag stateTag = new StateTag() {
                    String name = stateName;

                    @Override
                    public String toString() {
                        return name;
                    }
                };

                nfa = nfa.setAllFinalStatesTo(stateTag);

                priorityList.add(stateTag);

                List<AST.Identifier> modeNames = rule.modeList.modeNames;
                for (var mode : modeNames) {
                    String modeName = mode.identifier;
                    if (modeNFAs.containsKey(modeName)) {
                        NFA val = modeNFAs.get(modeName);
                        modeNFAs.put(modeName, val.union(nfa));
                    } else {
                        modeNFAs.put(modeName, nfa);
                    }
                }
            }

            Map<StateTag, Integer> priorityMap = new HashMap<>();

            for (int i = 0; i < priorityList.size(); i++) {
                priorityMap.put(priorityList.get(i), priorityList.size() - (i + 1));
            }

            Map<String, LexicalRecognizer> modes = new HashMap<>();

            for (String modeName : modeNFAs.keySet()) {
                NFA nfa = modeNFAs.get(modeName);
                modes.put(modeName, buildRecognizer(nfa, priorityMap));
            }

            String prefix = "generated/recognizers/";

            for (String modeName : modes.keySet()) {
                LexicalRecognizer recognizer = modes.get(modeName);
                String dot = recognizer.toGraphvizDotString(
                        Objects::toString, true
                );
                System.out.println();
                System.out.println(modeName + ": ");
                System.out.println(dot);
                System.out.println();
                recognizer.writeToFile(prefix + modeName + ".reco", priorityMap);
            }

            System.out.println();
            Collections.reverse(priorityList);
            StringBuilder finalTags = new StringBuilder();
            finalTags.append("List<StateTag> finalTags = new ArrayList<>();\n");
            for (int i = 0; i < priorityList.size(); i++) {
                StateTag tag = priorityList.get(i);
                finalTags.append("finalTags.add(").append(tag).append(");\n");
            }
            System.out.println(finalTags.toString());
        }

    }

    static NFA buildNFAFromRegex(AST.Regex regex, Map<String, NFA> namedExpressions, int alphabetSize) {
        return regex.buildNFA(namedExpressions, alphabetSize);
    }

    static LexicalRecognizer buildRecognizer(NFA lang, Map<StateTag, Integer> priorityMap) {
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
        System.out.println("\n" + factorization + "\n");

        return recognizer;
    }

}
