package io.github.sboyanovich.scannergenerator.tests.l7.aux;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CFGrammar {
    private int nonTerminalAlphabetSize;
    private int terminalAlphabetSize;
    private int axiom;

    private Map<Integer, List<UAString>> rules;

    // kind of a hack
    private Function<Integer, String> nativeTai;
    private Function<Integer, String> nativeNtai;

    // possibly just pass Builder
    public CFGrammar(
            int nonTerminalAlphabetSize,
            int terminalAlphabetSize,
            int axiom,
            Map<Integer, Set<UAString>> rules,
            Function<Integer, String> nativeTai,
            Function<Integer, String> nativeNtai
    ) {
        this.nonTerminalAlphabetSize = nonTerminalAlphabetSize;
        this.terminalAlphabetSize = terminalAlphabetSize;
        this.axiom = axiom;
        this.rules = new HashMap<>();
        for (int nonTerminal : rules.keySet()) {
            Set<UAString> productions = rules.get(nonTerminal);
            List<UAString> productionList = new ArrayList<>(productions);
            this.rules.put(nonTerminal, productionList);
        }
        this.nativeTai = nativeTai;
        this.nativeNtai = nativeNtai;
    }

    public CFGrammar(
            int nonTerminalAlphabetSize,
            int terminalAlphabetSize,
            int axiom,
            Map<Integer, Set<UAString>> rules
    ) {
        this(
                nonTerminalAlphabetSize,
                terminalAlphabetSize,
                axiom,
                rules,
                String::valueOf,
                String::valueOf
        );
    }

    public Function<Integer, String> getNativeTai() {
        return nativeTai;
    }

    public Function<Integer, String> getNativeNtai() {
        return nativeNtai;
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

    public List<UAString> getProductions(int nonTerminal) {
        return Collections.unmodifiableList(this.rules.get(nonTerminal));
    }

    // shamelessly copypasted from builder
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

    @Override
    public String toString() {
        return toString(
                this.nativeTai,
                this.nativeNtai
        );
    }
}
