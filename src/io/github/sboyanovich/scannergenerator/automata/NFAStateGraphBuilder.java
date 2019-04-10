package io.github.sboyanovich.scannergenerator.automata;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

public final class NFAStateGraphBuilder extends AbstractNFAStateGraph {

    public NFAStateGraphBuilder(int numberOfStates, int alphabetSize) {
        // number of states > 0
        // alphabet size > 0
        if (!(numberOfStates > 0)) {
            throw new IllegalArgumentException("Number of states must be non-negative!");
        }
        if (!(alphabetSize > 0)) {
            throw new IllegalArgumentException("Alphabet size must be non-negative!");
        }

        this.numberOfStates = numberOfStates;
        this.alphabetSize = alphabetSize;
        this.edges = new ArrayList<>();

        /* Initializing edge matrix. */
        for (int i = 0; i < numberOfStates; i++) {
            this.edges.add(new ArrayList<>(numberOfStates));
            for (int j = 0; j < numberOfStates; j++) {
                this.edges.get(i).add(null);
            }
        }
    }

    /// BUILDER
    public NFAStateGraph build() {
        return new NFAStateGraph(this.numberOfStates, this.alphabetSize, this.edges);
    }

    private boolean isValidSymbol(int symbol) {
        return isInRange(symbol, 0, this.alphabetSize - 1);
    }

    private void validateSymbol(int symbol) {
        if (!isValidSymbol(symbol)) {
            throw new IllegalArgumentException(
                    "Edge marker can only contain symbols in range [0, alphabetSize-1]!"
            );
        }
    }

    // only called if we're sure marker is not null
    private void validateMarker(Set<Integer> marker) {
        for (int symbol : marker) {
            validateSymbol(symbol);
        }
    }

    /// MUTATORS
    public void setEdge(int from, int to, Set<Integer> marker) {
        // null marker not allowed
        Objects.requireNonNull(marker);
        // validate
        validateEdge(from, to);
        validateMarker(marker);
        // defensive copy
        this.edges.get(from).set(to, new HashSet<>(marker));
    }

    public void addSymbolToEdge(int from, int to, int symbol) {
        // validate
        validateEdge(from, to);
        validateSymbol(symbol);
        if (!edgeExists(from, to)) {
            this.edges.get(from).set(to, new HashSet<>());
        }
        this.edges.get(from).get(to).add(symbol);
    }

    public void removeEdge(int from, int to) {
        // validate
        validateEdge(from, to);
        this.edges.get(from).set(to, null);
    }
}
