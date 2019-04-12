package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.token.Token;

public class TKwdIf extends Token {

    public TKwdIf(Fragment coords) {
        super(coords, SimpleDomains.KEYWORD_IF);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
