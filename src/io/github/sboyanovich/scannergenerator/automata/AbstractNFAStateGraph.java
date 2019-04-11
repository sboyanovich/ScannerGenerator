package io.github.sboyanovich.scannergenerator.automata;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

abstract class AbstractNFAStateGraph {
    int numberOfStates;
    int alphabetSize;
    List<List<Set<Integer>>> edges;

    /*
     * null means no edge
     * empty set means lambda-step
     * nonempty set represents letters that enable transition
     */

    /// GETTERS
    public int getNumberOfStates() {
        return numberOfStates;
    }

    public int getAlphabetSize() {
        return alphabetSize;
    }

    public final boolean edgeExists(int from, int to) {
        // validate
        validateEdge(from, to);
        return this.edges.get(from).get(to) != null;
    }

    public final boolean isNonTrivialEdge(int from, int to) {
        // validate
        validateEdge(from, to);
        return edgeExists(from, to) && !isLambdaEdge(from, to);
    }

    public final boolean isLambdaEdge(int from, int to) {
        // validate
        validateEdge(from, to);
        Optional<Set<Integer>> marker = getEdgeMarkerAux(from, to);
        return marker.isPresent() && marker.get().isEmpty();
    }

    public final Optional<Set<Integer>> getEdgeMarker(int from, int to) {
        // validate
        validateEdge(from, to);
        Optional<Set<Integer>> marker = getEdgeMarkerAux(from, to);
        return marker.map(Collections::unmodifiableSet);
    }

    // is only ever called with valid parameters
    private Optional<Set<Integer>> getEdgeMarkerAux(int from, int to) {
        return Optional.ofNullable(this.edges.get(from).get(to));
    }

    void validateEdge(int from, int to) {
        if (!isInRange(from, 0, this.numberOfStates - 1) || !isInRange(to, 0, this.numberOfStates - 1)) {
            throw new IllegalArgumentException("States must be in range [0, numberOfStates-1]!");
        }
    }

    // TODO: Add toString() (with interpretation)
}
