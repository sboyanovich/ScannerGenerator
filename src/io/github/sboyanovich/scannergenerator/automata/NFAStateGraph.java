package io.github.sboyanovich.scannergenerator.automata;

import java.util.*;

/**
 * Represent state graph for automaton with 'numberOfStates' states.
 */
public final class NFAStateGraph extends AbstractNFAStateGraph {

    // created only through builder
    NFAStateGraph(int numberOfStates, int alphabetSize, List<List<Optional<Set<Integer>>>> edges) {
        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.edges = new ArrayList<>();
        for (int i = 0; i < numberOfStates; i++) {
            this.edges.add(new ArrayList<>());
            for (int j = 0; j < numberOfStates; j++) {
                this.edges.get(i).add(
                        edges.get(i).get(j).map(HashSet::new)
                ); // defensive copy
            }
        }
    }
}
