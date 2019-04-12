package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;
import io.github.sboyanovich.scannergenerator.token.Token;

public class TKwdElif extends Token {

    public TKwdElif(Fragment coords) {
        super(coords, SimpleDomains.KEYWORD_ELIF);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
