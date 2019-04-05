package io.github.sboyanovich.scannergenerator.automata;

import java.util.*;

public class NFAStateGraphBuilder {
    private int numberOfStates;
    private List<List<Optional<Set<Integer>>>> edges;

    public NFAStateGraphBuilder(int numberOfStates) {
        this.numberOfStates = numberOfStates;
        this.edges = new ArrayList<>(numberOfStates);
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

    public int getNumberOfStates() {
        return numberOfStates;
    }

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

    public boolean edgeExists(int i, int j) {
        // validate
        return this.edges.get(i).get(j).isPresent();
    }

    public boolean isLambdaEdge(int i, int j) {
        // validate
        Optional<Set<Integer>> marker = getEdgeMarkerAux(i, j);
        return marker.isPresent() && marker.get().isEmpty();
    }

    public boolean isNonTrivialEdge(int i, int j) {
        // validate
        return edgeExists(i, j) && !isLambdaEdge(i, j);
    }

    public Optional<Set<Integer>> getEdgeMarker(int i, int j) {
        // validate
        Optional<Set<Integer>> marker = getEdgeMarkerAux(i, j);
        return marker.map(Collections::unmodifiableSet);
    }

    private Optional<Set<Integer>> getEdgeMarkerAux(int i, int j) {
        // validate
        return this.edges.get(i).get(j);
    }

    public NFAStateGraph build() {
        return new NFAStateGraph(this.numberOfStates, this.edges);
    }
}
