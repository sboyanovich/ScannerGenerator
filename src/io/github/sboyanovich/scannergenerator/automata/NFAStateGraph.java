package io.github.sboyanovich.scannergenerator.automata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/* Mutable for now */
public class NFAStateGraph {
    private int numberOfStates;
    private List<List<Set<Integer>>> edges;

    public NFAStateGraph(int numberOfStates) {
        this.numberOfStates = numberOfStates;
        this.edges = new ArrayList<>(numberOfStates);
        /* Initializing edge matrix. */
        /*
         * null means no edge
         * empty set means lambda-step
         * nonempty set represents letters that enable transition
         */
        for (int i = 0; i < numberOfStates; i++) {
            this.edges.add(new ArrayList<>(numberOfStates));
            for (int j = 0; j < numberOfStates; j++) {
                this.edges.get(i).add(null);
            }
        }
    }

    public void setEdge(int i, int j, Set<Integer> markers) {
        // validate
        // null markers not allowed
        this.edges.get(i).set(j, markers);
    }

    public void removeEdge(int i, int j) {
        //validate
        this.edges.get(i).set(j, null);
    }

    public boolean edgeExists(int i, int j) {
        //validate
        return this.edges.get(i).get(j) != null;
    }

    public boolean isLambdaEdge(int i, int j) {
        return edgeExists(i, j) && getEdgeMarker(i, j).isEmpty();
    }

    public boolean isNonTrivialEdge(int i, int j) {
        return edgeExists(i, j) && !isLambdaEdge(i, j);
    }

    public Set<Integer> getEdgeMarker(int i, int j) {
        //validate
        //can return null
        return this.edges.get(i).get(j);
    }
}
