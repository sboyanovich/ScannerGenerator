package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.token.Token;

public class TOpDivide extends Token {
    public TOpDivide(Fragment coords) {
        super(coords, SimpleDomains.OP_DIVIDE);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
