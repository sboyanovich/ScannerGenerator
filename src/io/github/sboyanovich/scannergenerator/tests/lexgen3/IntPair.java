package io.github.sboyanovich.scannergenerator.tests.lexgen3;

import io.github.sboyanovich.scannergenerator.utility.Pair;

public class IntPair extends Pair<Integer, Integer> {
    public IntPair(int first, int second) {
        super(first, second);
    }

    @Override
    public String toString() {
        int first = this.getFirst();
        int second = this.getSecond();

        if (second == -1) {
            return first + "+";
        }
        if (second == first) {
            return first + "";
        }
        return first + "-" + second;
    }
}
