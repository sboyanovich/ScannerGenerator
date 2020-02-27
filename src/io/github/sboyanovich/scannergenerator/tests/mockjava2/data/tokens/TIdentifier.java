package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.tokens;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains.DomainsWithStringAttribute;

public class TIdentifier extends TokenWithAttribute<String> {
    public TIdentifier(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.IDENTIFIER, attribute);
    }
}
