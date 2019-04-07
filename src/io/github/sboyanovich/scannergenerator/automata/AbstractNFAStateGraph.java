package io.github.sboyanovich.scannergenerator.automata;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

abstract class AbstractNFAStateGraph {
    int numberOfStates;
    List<List<Optional<Set<Integer>>>> edges;

    /*
     * no set means no edge
     * empty set means lambda-step
     * nonempty set represents letters that enable transition
     */

    /// GETTERS
    public int getNumberOfStates() {
        return numberOfStates;
    }

    public final boolean edgeExists(int i, int j) {
        // validate
        return this.edges.get(i).get(j).isPresent();
    }

    public final boolean isNonTrivialEdge(int i, int j) {
        // validate
        return edgeExists(i, j) && !isLambdaEdge(i, j);
    }

    public final boolean isLambdaEdge(int i, int j) {
        // validate
        Optional<Set<Integer>> marker = getEdgeMarkerAux(i, j);
        return marker.isPresent() && marker.get().isEmpty();
    }

    public final Optional<Set<Integer>> getEdgeMarker(int i, int j) {
        // validate
        Optional<Set<Integer>> marker = getEdgeMarkerAux(i, j);
        return marker.map(Collections::unmodifiableSet);
    }

    private Optional<Set<Integer>> getEdgeMarkerAux(int i, int j) {
        // validate
        return this.edges.get(i).get(j);
    }
}
