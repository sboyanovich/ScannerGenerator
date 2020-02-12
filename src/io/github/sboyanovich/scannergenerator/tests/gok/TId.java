package io.github.sboyanovich.scannergenerator.tests.gok;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import static io.github.sboyanovich.scannergenerator.tests.gok.DomainsWithStringAttribute.ID;

public class TId extends TokenWithAttribute<String> {
    public TId(Fragment coords, String attribute) {
        super(coords, ID, attribute);
    }
}
