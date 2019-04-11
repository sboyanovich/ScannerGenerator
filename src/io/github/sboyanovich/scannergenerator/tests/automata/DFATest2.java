package io.github.sboyanovich.scannergenerator.tests.automata;

import io.github.sboyanovich.scannergenerator.automata.DFA;
import io.github.sboyanovich.scannergenerator.automata.NFA;
import io.github.sboyanovich.scannergenerator.automata.NFAStateGraphBuilder;
import io.github.sboyanovich.scannergenerator.lex.StateTag;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DFATest2 {
    public static void main(String[] args) {
        Map<Integer, String> interpretationMap = Map.of(
                0, "a",
                1, "b",
                2, "1",
                3, "2"
        );
        Function<Integer, String> interpretation = interpretationMap::get;

        int alphabetSize = interpretationMap.size();
        int numberOfStates = 2;
        NFAStateGraphBuilder edges = new NFAStateGraphBuilder(numberOfStates, alphabetSize);
        edges.setEdge(0, 1, Set.of(0, 1));
        edges.setEdge(1, 1, Set.of(0, 1, 2, 3));
        Map<Integer, StateTag> labelsMap = Map.of(
                0, StateTag.NOT_FINAL,
                1, StateTag.FINAL_DUMMY
        );
        NFA nfa = new NFA(numberOfStates, alphabetSize, 0, labelsMap, edges.build());

        DFA dfa = nfa.determinize(Map.of());

        String dot = dfa.toGraphvizDotString(interpretation, true);
        System.out.println(dot);
    }
}
