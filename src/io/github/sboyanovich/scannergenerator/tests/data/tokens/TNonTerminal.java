package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.token.Token;

import static io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute.NON_TERMINAL;

public class TNonTerminal extends Token {
    private String attribute;

    public TNonTerminal(Fragment coords, String attribute) {
        super(coords, NON_TERMINAL);
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public String toString() {
        return super.toString() + attribute;
    }
}
