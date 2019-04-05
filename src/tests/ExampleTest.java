package tests;

import lab.Position;
import lab.lex.Compiler;
import lab.lex.*;
import lab.token.*;
import tests.data.TransitionTableExample;
import utility.EquivalenceMap;
import utility.Utility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static tests.data.states.StateTags.*;
import static utility.Utility.*;

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
