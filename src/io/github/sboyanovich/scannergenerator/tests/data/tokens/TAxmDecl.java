package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.token.Token;

import static io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute.AXM_DECL;

public class TAxmDecl extends Token {
    private String attribute;

    public TAxmDecl(Fragment coords, String attribute) {
        super(coords, AXM_DECL);
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
