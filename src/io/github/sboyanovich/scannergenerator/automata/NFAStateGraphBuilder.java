package io.github.sboyanovich.scannergenerator.automata;

import java.util.*;

public final class NFAStateGraphBuilder extends AbstractNFAStateGraph {

    public NFAStateGraphBuilder(int numberOfStates) {
        this.numberOfStates = numberOfStates;
        this.edges = new ArrayList<>();
        /* Initializing edge matrix. */
        /*
         * no set means no edge
         * empty set means lambda-step
         * nonempty set represents letters that enable transition
         */
        for (int i = 0; i < numberOfStates; i++) {
            this.edges.add(new ArrayList<>(numberOfStates));
            for (int j = 0; j < numberOfStates; j++) {
                this.edges.get(i).add(Optional.empty());
            }
        }
    }

    /// BUILDER
    public NFAStateGraph build() {
        return new NFAStateGraph(this.numberOfStates, this.edges);
    }

    /// MUTATORS
    public void setEdge(int i, int j, Set<Integer> marker) {
        // validate
        // null marker not allowed
        // defensive copy
        this.edges.get(i).set(j, Optional.of(new HashSet<>(marker)));
    }

    public void removeEdge(int i, int j) {
        //validate
        this.edges.get(i).set(j, Optional.empty());
    }
}
