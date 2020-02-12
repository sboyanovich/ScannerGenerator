package io.github.sboyanovich.scannergenerator.tests.gok;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import static io.github.sboyanovich.scannergenerator.tests.gok.DomainsWithIntegerAttribute.NUM;

public class TNum extends TokenWithAttribute<Integer> {
    public TNum(Fragment coords, Integer attribute) {
        super(coords, NUM, attribute);
    }
}
