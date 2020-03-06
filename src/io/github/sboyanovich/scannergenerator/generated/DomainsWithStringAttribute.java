package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import static io.github.sboyanovich.scannergenerator.utility.Utility.getTextFragmentAsString;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    NAMED_EXPR {
        @Override
        public String attribute(Text text, Fragment fragment) {
            String s = getTextFragmentAsString(text, fragment);
            return s.substring(1, s.length() - 1);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, NAMED_EXPR, attribute(text, fragment));
        }
    },
    DOMAINS_GROUP_MARKER {
        @Override
        public String attribute(Text text, Fragment fragment) {
            String s = getTextFragmentAsString(text, fragment);
            String prefix = "%DOMAINS";
            if (s.length() == prefix.length()) {
                return "";
            }
            return s.substring(prefix.length() + 1, s.length() - 1);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, DOMAINS_GROUP_MARKER, attribute(text, fragment));
        }
    },
    IDENTIFIER {
        @Override
        public String attribute(Text text, Fragment fragment) {
            return getTextFragmentAsString(text, fragment);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, IDENTIFIER, attribute(text, fragment));
        }
    }
}