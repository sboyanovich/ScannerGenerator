package io.github.sboyanovich.scannergenerator.tests.mockjava.data.tokens;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public class TIntegerLiteral extends TokenWithAttribute<Integer> {
    public TIntegerLiteral(Fragment coords, DomainWithAttribute<Integer> tag, Integer attribute) {
        super(coords, tag, attribute);
    }
}
