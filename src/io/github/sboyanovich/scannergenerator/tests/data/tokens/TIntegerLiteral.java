package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithIntegerAttribute;
import io.github.sboyanovich.scannergenerator.token.TokenWithAttribute;

public class TIntegerLiteral extends TokenWithAttribute<Integer> {
    public TIntegerLiteral(Fragment coords, Integer attribute) {
        super(coords, DomainsWithIntegerAttribute.INTEGER_LITERAL, attribute);
    }
}
