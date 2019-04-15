package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;
import io.github.sboyanovich.scannergenerator.token.TokenWithAttribute;

public class TIdentifier extends TokenWithAttribute<String> {
    public TIdentifier(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.IDENTIFIER, attribute);
    }
}
