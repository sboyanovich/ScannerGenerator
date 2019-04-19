package io.github.sboyanovich.scannergenerator.tests.l7.aux;

import java.util.function.Function;

public class UnifiedAlphabetSymbol {
    private int symbol;
    private boolean isTerminal;

    public UnifiedAlphabetSymbol(int symbol, boolean isTerminal) {
        this.symbol = symbol;
        this.isTerminal = isTerminal;
    }

    public int getSymbol() {
        return symbol;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnifiedAlphabetSymbol) {
            UnifiedAlphabetSymbol cobj = (UnifiedAlphabetSymbol) obj;
            return (this.isTerminal == cobj.isTerminal) && (this.symbol == cobj.symbol);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * (1 + 31 * (this.isTerminal ? 1 : 0)) + this.symbol;
    }

    public String toString(
            Function<Integer, String> terminalAlphabetInterpretation,
            Function<Integer, String> nonterminalAlphabetInterpretation
    ) {
        if (this.isTerminal) {
            return terminalAlphabetInterpretation.apply(this.symbol);
        } else {
            return nonterminalAlphabetInterpretation.apply(this.symbol);
        }
    }

    @Override
    public String toString() {
        return toString(
                String::valueOf,
                String::valueOf
        );
    }
}
