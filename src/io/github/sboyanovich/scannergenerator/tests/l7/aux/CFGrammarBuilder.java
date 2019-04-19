package io.github.sboyanovich.scannergenerator.tests.l7.aux;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// no validation at all for now
public class CFGrammarBuilder {
    private int nonTerminalAlphabetSize;
    private int terminalAlphabetSize;
    private int axiom;

    private Map<Integer, Set<UAString>> rules;

    public CFGrammarBuilder(
            int nonTerminalAlphabetSize,
            int terminalAlphabetSize,
            int axiom
    ) {
        this.nonTerminalAlphabetSize = nonTerminalAlphabetSize;
        this.terminalAlphabetSize = terminalAlphabetSize;
        this.axiom = axiom;
        this.rules = new HashMap<>();
    }

    public int getNonTerminalAlphabetSize() {
        return nonTerminalAlphabetSize;
    }

    public int getTerminalAlphabetSize() {
        return terminalAlphabetSize;
    }

    public int getAxiom() {
        return axiom;
    }

    public void addProduction(CFGProduction production) {
        int nonTerminal = production.getNonTerminal();
        if (!this.rules.containsKey(nonTerminal)) {
            this.rules.put(nonTerminal, new LinkedHashSet<>());
        }
        Set<UAString> rhss = this.rules.get(nonTerminal);
        rhss.add(production.getRhs());
    }

    public CFGrammar build(Function<Integer, String> nativeTai,
                           Function<Integer, String> nativeNtai) {
        return new CFGrammar(
                this.nonTerminalAlphabetSize,
                this.terminalAlphabetSize,
                this.axiom,
                this.rules,
                nativeTai,
                nativeNtai
        );
    }

    public CFGrammar build() {
        return new CFGrammar(
                this.nonTerminalAlphabetSize,
                this.terminalAlphabetSize,
                this.axiom,
                this.rules
        );
    }

    public String toString(
            Function<Integer, String> terminalAlphabetInterpretation,
            Function<Integer, String> nonTerminalAlphabetInterpretation
    ) {
        StringBuilder result = new StringBuilder();
        result.append("axiom: ")
                .append(nonTerminalAlphabetInterpretation.apply(this.axiom))
                .append("\n");
        for (int nonTerminal : this.rules.keySet()) {
            result.append(
                    nonTerminalAlphabetInterpretation.apply(nonTerminal)
            );
            result.append(" = ");
            List<UAString> productions = new ArrayList<>(this.rules.get(nonTerminal));
            List<UAString> notNullProductions = productions.stream()
                    .filter(prod -> !prod.isEmpty())
                    .collect(Collectors.toList());

            boolean isNullable = productions.size() > notNullProductions.size();
            result.append(
                    notNullProductions.get(0)
                            .toString(
                                    terminalAlphabetInterpretation,
                                    nonTerminalAlphabetInterpretation
                            )
            );
            for (int i = 1; i < notNullProductions.size(); i++) {
                result.append("| ");
                result.append(
                        notNullProductions.get(i)
                                .toString(
                                        terminalAlphabetInterpretation,
                                        nonTerminalAlphabetInterpretation
                                )
                );
            }
            if (isNullable && (productions.size() > 1)) {
                result.append("| ");
            }
            result.append(".\n");
        }
        return result.toString();
    }
}
