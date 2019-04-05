package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.token.Token;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;

public class TOperation extends Token {
    private String attribute;

    public TOperation(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.OPERATION);
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
