package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public enum DomainsWithIntPairAttribute implements DomainWithAttribute<IntPair> {
    REPETITION_OP {
        @Override
        public IntPair attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<IntPair> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, REPETITION_OP, attribute(text, fragment));
        }
    }
}