package io.github.sboyanovich.scannergenerator.automata;

import java.util.*;

/**
 * Represent state graph for automaton with 'numberOfStates' states.
 */
public final class NFAStateGraph extends AbstractNFAStateGraph {

    // created only through builder
    NFAStateGraph(int numberOfStates, int alphabetSize, List<List<Set<Integer>>> edges) {
        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.edges = new ArrayList<>();
        for (int i = 0; i < numberOfStates; i++) {
            this.edges.add(new ArrayList<>());
            for (int j = 0; j < numberOfStates; j++) {
                Set<Integer> edge = edges.get(i).get(j);
                if (edge != null) {
                    // defensive copy
                    edge = new HashSet<>(edge);
                }
                this.edges.get(i).add(
                        edge
                );
            }
        }
    }
}
