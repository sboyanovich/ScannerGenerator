package io.github.sboyanovich.scannergenerator.tests.automata;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.lex.StateTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.github.sboyanovich.scannergenerator.tests.data.states.StateTags.*;

public class DFATest3 {
    public static void main(String[] args) {
        Map<Integer, String> interpretationMap = new HashMap<>();

        List<String> symbols = List.of(
                "a", "b", "e", "f", "i", "l", "1", "2", "*", "/",
                "SPACE", "NEWLINE", "TAB", "CARRET"
        );

        for (int i = 0; i < symbols.size(); i++) {
            interpretationMap.put(i, symbols.get(i));
        }

        Function<Integer, String> interpretation = interpretationMap::get;

        int alphabetSize = interpretationMap.size();
        List<StateTag> labelsList = List.of(
                StateTag.NOT_FINAL,
                StateTag.NOT_FINAL,
                WHITESPACE,
                StateTag.NOT_FINAL,
                StateTag.NOT_FINAL,
                StateTag.NOT_FINAL,
                IDENTIFIER,
                StateTag.NOT_FINAL,
                INTEGER_LITERAL,
                StateTag.NOT_FINAL,
                StateTag.NOT_FINAL,
                KEYWORD,
                StateTag.NOT_FINAL,
                StateTag.NOT_FINAL,
                StateTag.NOT_FINAL,
                OPERATION
        );
        int numberOfStates = labelsList.size();
        Map<Integer, StateTag> labelsMap = new HashMap<>();
        for (int i = 0; i < labelsList.size(); i++) {
            labelsMap.put(i, labelsList.get(i));
        }

        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        edges.setEdge(0, 1, Set.of());
        edges.setEdge(0, 4, Set.of());
        edges.setEdge(0, 7, Set.of());
        edges.setEdge(0, 9, Set.of());
        edges.setEdge(0, 14, Set.of());

        edges.setEdge(1, 2, Set.of(10, 11, 12));
        edges.setEdge(1, 3, Set.of(13));

        edges.setEdge(2, 1, Set.of());

        edges.setEdge(3, 2, Set.of(11));

        edges.setEdge(4, 5, Set.of(0, 1, 2, 3, 4, 5));

        edges.setEdge(5, 6, Set.of());

        edges.setEdge(6, 6, Set.of(0, 1, 2, 3, 4, 5, 6, 7));

        edges.setEdge(7, 8, Set.of(6, 7));

        edges.setEdge(8, 8, Set.of(6, 7));

        edges.setEdge(9, 10, Set.of(4));
        edges.setEdge(9, 12, Set.of(2));

        edges.setEdge(10, 11, Set.of(3));

        edges.setEdge(12, 13, Set.of(5));

        edges.setEdge(13, 10, Set.of(4));

        edges.setEdge(14, 15, Set.of(8, 9));

        NFA nfa = new NFA(numberOfStates, alphabetSize, 0, labelsMap, edges.build());

        List<StateTag> priorityList = List.of(
                KEYWORD,
                IDENTIFIER,
                INTEGER_LITERAL,
                OPERATION,
                WHITESPACE,
                NOT_FINAL
        );

        Map<StateTag, Integer> priorityMap = new HashMap<>();
        for (int i = 0; i < priorityList.size(); i++) {
            priorityMap.put(priorityList.get(i), i);
        }

        DFA dfa = nfa.determinize(priorityMap);

        String dot = dfa.toGraphvizDotString(interpretation, true);
        System.out.println(dot);
    }
}
