package lab.lex;

import utility.EquivalenceMap;

import java.util.ArrayList;
import java.util.List;

public class LexicalRecognizer {
    static final int DEAD_END_STATE = -1;

    private EquivalenceMap generalizedSymbolsMap;
    private int[][] transitionTable;
    private List<StateTag> labels;

    public LexicalRecognizer(EquivalenceMap generalizedSymbolsMap, int[][] transitionTable,
                             List<StateTag> labels) {
        this.generalizedSymbolsMap = generalizedSymbolsMap;
        this.transitionTable = transitionTable;
        this.labels = new ArrayList<>(labels);
    }

    /**
     *  fromState != -1
     * */
    public int transition(int fromState, int codePoint) {
        if (codePoint == Text.EOI) {
            return DEAD_END_STATE;
        }
        int symbol = this.generalizedSymbolsMap.getEqClass(codePoint);
        return this.transitionTable[fromState][symbol];
    }

    public StateTag getStateTag(int state) {
        return this.labels.get(state);
    }

    /*
    public List<StateTag> getLabels() {
        return Collections.unmodifiableList(this.labels);
    }
    */
}
