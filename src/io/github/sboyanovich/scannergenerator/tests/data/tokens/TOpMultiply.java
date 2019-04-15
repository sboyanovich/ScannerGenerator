package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.token.Token;

public class TOpMultiply extends Token {
    public TOpMultiply(Fragment coords) {
        super(coords, SimpleDomains.OP_MULTIPLY);
    }
}
