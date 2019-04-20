package io.github.sboyanovich.scannergenerator.tests.l7.aux;

import io.github.sboyanovich.scannergenerator.tests.l7.PredictionTableCreationException;

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
        for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
            this.rules.put(i, new ArrayList<>());
        }
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
                .append("(")
                .append(nonTerminalAlphabetInterpretation.apply(this.axiom))
                .append(")")
                .append("\n");
        for (int nonTerminal : this.rules.keySet()) {
            result
                    .append("(")
                    .append(nonTerminalAlphabetInterpretation.apply(nonTerminal))
                    .append(")");
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

    // -1 represents epsilon
    private Set<Integer> fst(UAString string, Map<Integer, Set<Integer>> ntsFirst) {
        Set<Integer> result = new HashSet<>();
        if (string.isEmpty()) {
            result.add(-1);
        } else if (string.getSymbols().get(0).isTerminal()) {
            result.add(string.getSymbols().get(0).getSymbol());
        } else {
            int nt = string.getSymbols().get(0).getSymbol();
            Set<Integer> ntFirst = ntsFirst.get(nt);
            if (ntFirst.contains(-1)) {
                result.addAll(ntFirst);
                result.remove(-1);
                result.addAll(fst(string.removeFirst(), ntsFirst));
            } else {
                result.addAll(ntFirst);
            }
        }
        return result;
    }

    public int[][] buildPredictiveAnalysisTable() throws PredictionTableCreationException {
        int[][] table = new int[this.nonTerminalAlphabetSize][this.terminalAlphabetSize + 1];
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                table[i][j] = -1;
            }
        }

        Map<Integer, Set<Integer>> ntsFirst = new HashMap<>();
        for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
            ntsFirst.put(i, new HashSet<>());
        }

        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
                Set<Integer> ntFirst = ntsFirst.get(i);
                List<UAString> rhss = this.rules.get(i);
                for (UAString rhs : rhss) {
                    changed |= ntFirst.addAll(fst(rhs, ntsFirst));
                }
            }
        } while (changed);

        Map<Integer, Set<Integer>> ntsFollow = new HashMap<>();
        for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
            ntsFollow.put(i, new HashSet<>());
        }

        int endMarker = this.terminalAlphabetSize;
        ntsFollow.get(this.axiom).add(endMarker);

        for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
            List<UAString> rhss = this.rules.get(i);
            for (UAString rhs : rhss) {
                for (int j = 0; j < rhs.getSymbols().size(); j++) {
                    UnifiedAlphabetSymbol x = rhs.getSymbols().get(j);
                    if (!x.isTerminal()) {
                        int nt = x.getSymbol();
                        Set<Integer> fv = fst(rhs.subsequence(j + 1), ntsFirst);
                        fv.remove(-1);
                        ntsFollow.get(nt).addAll(fv);
                    }
                }
            }
        }

        // not terribly effective
        do {
            changed = false;
            for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
                List<UAString> rhss = this.rules.get(i);
                for (UAString rhs : rhss) {
                    for (int j = 0; j < rhs.getSymbols().size(); j++) {
                        UnifiedAlphabetSymbol x = rhs.getSymbols().get(j);
                        if (!x.isTerminal()) {
                            int nt = x.getSymbol();
                            Set<Integer> fv = fst(rhs.subsequence(j + 1), ntsFirst);
                            if (fv.contains(-1)) {
                                changed |= ntsFollow.get(nt).addAll(
                                        ntsFollow.get(i)
                                );
                            }
                        }
                    }
                }
            }
        } while (changed);

        for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
            List<UAString> rhss = this.rules.get(i);
            for (int j = 0; j < rhss.size(); j++) {
                Set<Integer> first = fst(rhss.get(j), ntsFirst);
                for (int a : first) {
                    if (a != -1) {
                        if (table[i][a] == -1) {
                            table[i][a] = j;
                        } else {
                            throw new PredictionTableCreationException(
                                    "Grammar is not LL(1)."
                            );
                        }
                    }
                }
                if (first.contains(-1)) {
                    Set<Integer> follow = ntsFollow.get(i);
                    for (int b : follow) {
                        if (table[i][b] == -1) {
                            table[i][b] = j;
                        } else {
                            throw new PredictionTableCreationException(
                                    "Grammar is not LL(1)."
                            );
                        }
                    }
                }
            }
        }
        return table;
    }

    public List<Integer> getExplicitlyUselessNonTerminals() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < this.nonTerminalAlphabetSize; i++) {
            if (this.rules.get(i).isEmpty()) {
                result.add(i);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return toString(
                this.nativeTai,
                this.nativeNtai
        );
    }
}
