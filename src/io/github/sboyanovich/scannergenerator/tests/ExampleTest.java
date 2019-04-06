package io.github.sboyanovich.scannergenerator.tests;

import io.github.sboyanovich.scannergenerator.Position;
import io.github.sboyanovich.scannergenerator.lex.Compiler;
import io.github.sboyanovich.scannergenerator.lex.*;
import io.github.sboyanovich.scannergenerator.tests.data.TransitionTableExample;
import io.github.sboyanovich.scannergenerator.token.Domain;
import io.github.sboyanovich.scannergenerator.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.token.DomainError;
import io.github.sboyanovich.scannergenerator.token.Token;
import io.github.sboyanovich.scannergenerator.utility.EquivalenceMap;
import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static io.github.sboyanovich.scannergenerator.lex.STNotFinal.NOT_FINAL;
import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;
import static io.github.sboyanovich.scannergenerator.utility.Utility.*;

public class ExampleTest {
    public static void main(String[] args) {
        /* TODO: Add automata handling classes and chain  */

        EquivalenceMap map1 = TransitionTableExample.map1;
        int[][] transitionTable = TransitionTableExample.get();

        EquivalenceMap map2 = refineEquivalenceMap(map1, transitionTable);
        EquivalenceMap map = composeEquivalenceMaps(map1, map2);

        int[][] finalTransitionTable = compressTransitionTable(transitionTable, map2);

        List<StateTag> stateLabels = List.of(
                NOT_FINAL,          // 0
                WHITESPACE,         // 1
                NOT_FINAL,          // 2
                IDENTIFIER,         // 3
                IDENTIFIER,         // 4
                IDENTIFIER,         // 5
                IDENTIFIER,         // 6
                KEYWORD,            // 7
                INTEGER_LITERAL,    // 8
                OPERATION,          // 9
                OPERATION,          // 10
                NOT_FINAL,          // 11
                NOT_FINAL,          // 12
                NOT_FINAL,          // 13
                COMMENT             // 14
        );

        // note, Integer.parseInt guards against overflow

        LexicalRecognizer dfa = new LexicalRecognizer(map, finalTransitionTable, stateLabels);

        String text = Utility.getText("testWin.txt");
        //text = text.replace("\n","\r");

        Compiler compiler = new Compiler(dfa);
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

    public static void printTransitionTable(int[][] transitionTable, int paddingTo) {
        for (int[] aTransitionTable : transitionTable) {
            for (int anATransitionTable : aTransitionTable) {
                System.out.print(pad(anATransitionTable, paddingTo) + " ");
            }
            System.out.println();
        }
    }

    private static String pad(int arg, int paddingTo) {
        StringBuilder result = new StringBuilder();
        int n = paddingTo - String.valueOf(arg).length();
        result.append(arg);
        for (int i = 0; i < n; i++) {
            result.append(" ");
        }
        return result.toString();
    }
}
