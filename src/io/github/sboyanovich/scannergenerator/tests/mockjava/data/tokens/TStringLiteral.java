package io.github.sboyanovich.scannergenerator.tests.mockjava.data.tokens;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public class TStringLiteral extends TokenWithAttribute<String> {
    public TStringLiteral(Fragment coords, DomainWithAttribute<String> tag, String attribute) {
        super(coords, tag, attribute);
    }
}
