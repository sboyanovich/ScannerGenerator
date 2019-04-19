package io.github.sboyanovich.scannergenerator.tests.l7.aux;

public class CFGProduction {
    private int nonTerminal;
    private UAString rhs;

    public CFGProduction(int nonTerminal, UAString rhs) {
        this.nonTerminal = nonTerminal;
        this.rhs = rhs;
    }

    public int getNonTerminal() {
        return nonTerminal;
    }

    public UAString getRhs() {
        return rhs;
    }
}
