package io.github.sboyanovich.scannergenerator.tests.data.tokens;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public class TComment extends TokenWithAttribute<String> {
    public TComment(Fragment coords, String attribute) {
        super(coords, DomainsWithStringAttribute.COMMENT, attribute);
    }
}
