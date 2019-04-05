package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithIntegerAttribute;
import io.github.sboyanovich.scannergenerator.token.Token;

public class TIntegerLiteral extends Token {

    private int attribute;

    public TIntegerLiteral(Fragment coords, int attribute) {
        super(coords, DomainsWithIntegerAttribute.INTEGER_LITERAL);
        this.attribute = attribute;
    }

    public int getAttribute() {
        return attribute;
    }

    @Override
    public String toString() {
        return super.toString() + attribute;
    }
}
