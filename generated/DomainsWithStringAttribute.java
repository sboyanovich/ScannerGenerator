package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    NAMED_EXPR {
        @Override
        public String attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, NAMED_EXPR, attribute(text, fragment));
        }
    },
    DOMAINS_GROUP_MARKER {
        @Override
        public String attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, DOMAINS_GROUP_MARKER, attribute(text, fragment));
        }
    },
    IDENTIFIER {
        @Override
        public String attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, IDENTIFIER, attribute(text, fragment));
        }
    },
    ACTION_SWITCH {
        @Override
        public String attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, ACTION_SWITCH, attribute(text, fragment));
        }
    },
    ACTION_RETURN {
        @Override
        public String attribute(Text text, Fragment fragment) {

        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, ACTION_RETURN, attribute(text, fragment));
        }
    }
}