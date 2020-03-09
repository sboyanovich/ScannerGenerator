package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.utility.Pair;

public class StringPair extends Pair<String, String> {
    public StringPair(String first, String second) {
        super(first, second);
    }

    @Override
    public String toString() {
        return "(" + this.getFirst() + ", " + this.getSecond() + ")";
    }
}
