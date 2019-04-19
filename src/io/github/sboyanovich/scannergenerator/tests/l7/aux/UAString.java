package io.github.sboyanovich.scannergenerator.tests.l7.aux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class UAString {
    private List<UnifiedAlphabetSymbol> symbols;

    public UAString(List<UnifiedAlphabetSymbol> symbols) {
        this.symbols = new ArrayList<>(symbols);
    }

    public List<UnifiedAlphabetSymbol> getSymbols() {
        return Collections.unmodifiableList(symbols);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UAString) {
            UAString cobj = (UAString) obj;
            return this.symbols.equals(cobj.symbols);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.symbols.hashCode();
    }

    public boolean isEmpty() {
        return this.symbols.isEmpty();
    }

    public UAString prepend(UnifiedAlphabetSymbol symbol) {
        List<UnifiedAlphabetSymbol> symbols = new ArrayList<>();
        symbols.add(symbol);
        symbols.addAll(this.symbols);
        return new UAString(symbols);
    }

    public UAString append(UAString other) {
        List<UnifiedAlphabetSymbol> symbols = new ArrayList<>(this.symbols);
        symbols.addAll(other.symbols);
        return new UAString(symbols);
    }

    public UAString append(UnifiedAlphabetSymbol symbol) {
        List<UnifiedAlphabetSymbol> symbols = new ArrayList<>(this.symbols);
        symbols.add(symbol);
        return new UAString(symbols);
    }

    public UAString removeFirst() {
        List<UnifiedAlphabetSymbol> symbols = new ArrayList<>(this.symbols);
        symbols = symbols.subList(1, symbols.size());
        return new UAString(symbols);
    }

    public UAString subsequence(int from) {
        List<UnifiedAlphabetSymbol> symbols = new ArrayList<>(this.symbols);
        symbols = symbols.subList(from, symbols.size());
        return new UAString(symbols);
    }

    public String toString(
            Function<Integer, String> terminalAlphabetInterpretation,
            Function<Integer, String> nonterminalAlphabetInterpretation
    ) {
        StringBuilder result = new StringBuilder();
        for (UnifiedAlphabetSymbol x : this.symbols) {
            result.append(
                    x.toString(terminalAlphabetInterpretation, nonterminalAlphabetInterpretation)
            )
                    .append(" ");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return toString(
                String::valueOf,
                String::valueOf
        );
    }
}
