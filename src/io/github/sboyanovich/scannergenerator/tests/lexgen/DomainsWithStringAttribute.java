package io.github.sboyanovich.scannergenerator.tests.lexgen;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;

import static io.github.sboyanovich.scannergenerator.utility.Utility.getTextFragmentAsString;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    REPETITION_OP {
        @Override
        public String attribute(Text text, Fragment fragment) {
            String s = getTextFragmentAsString(text, fragment);
            return s.substring(1, s.length() - 1);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            String s = attribute(text, fragment);
            String attribute;

            int pos = s.indexOf(",");

            if (pos == -1) {
                attribute = s;
            } else if (pos + 1 < s.length()) {
                String a = s.substring(0, pos);
                String b = s.substring(pos + 1);
                attribute = a + "-" + b;
            } else {
                String a = s.substring(0, pos);
                attribute = a + "+";
            }

            return new TokenWithAttribute<>(fragment, REPETITION_OP, attribute);
        }
    },
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
    CHAR_CLASS {
        @Override
        public String attribute(Text text, Fragment fragment) {
            return getTextFragmentAsString(text, fragment);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, CHAR_CLASS, attribute(text, fragment));
        }
    },
    CHAR {
        @Override
        public String attribute(Text text, Fragment fragment) {
            return getTextFragmentAsString(text, fragment);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, CHAR, attribute(text, fragment));
        }
    }
}
