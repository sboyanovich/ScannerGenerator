package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public enum DomainsWithIntegerAttribute implements DomainWithAttribute<Integer> {
    CHAR {
        @Override
        public Integer attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<Integer> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, CHAR, attribute(text, fragment));
        }
    }
}