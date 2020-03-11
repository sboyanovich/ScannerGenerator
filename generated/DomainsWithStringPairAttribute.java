package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public enum DomainsWithStringPairAttribute implements DomainWithAttribute<StringPair> {
    ACTION_SWITCH_RETURN {
        @Override
        public StringPair attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<StringPair> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, ACTION_SWITCH_RETURN, attribute(text, fragment));
        }
    }
}