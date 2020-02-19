package io.github.sboyanovich.scannergenerator.tests.mockjava.data.tokens;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.mockjava.data.domains.DomainsWithStringAttribute;

public class TComment extends TokenWithAttribute<String> {
    public TComment(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.COMMENT, attribute);
    }
}
