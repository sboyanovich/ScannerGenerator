package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.token.Token;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;

public class TWhitespace extends Token {
    public TWhitespace(Fragment coords) {
        super(coords, SimpleDomains.WHITESPACE);
    }
}
