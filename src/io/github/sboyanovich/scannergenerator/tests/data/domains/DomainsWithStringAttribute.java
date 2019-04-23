package io.github.sboyanovich.scannergenerator.tests.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.TAxmDecl;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.TComment;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.TIdentifier;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.TNonTerminal;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    IDENTIFIER {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TIdentifier(fragment, attribute(text, fragment));
        }
    },
    COMMENT {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TComment(fragment, attribute(text, fragment));
        }
    },
    AXM_DECL {
        @Override
        public String attribute(Text text, Fragment fragment) {
            String all = super.attribute(text, fragment);
            String[] split = all.split("[ \t]+");
            String name = split[1];
            return name.substring(0, name.length() - 1);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TAxmDecl(fragment, attribute(text, fragment));
        }
    },
    NON_TERMINAL {
        @Override
        public String attribute(Text text, Fragment fragment) {
            String all = super.attribute(text, fragment);
            return all.substring(1, all.length() - 1);
        }

        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TNonTerminal(fragment, attribute(text, fragment));
        }
    },
    TERMINAL {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, TERMINAL, attribute(text, fragment));
        }
    };

    @Override
    public String attribute(Text text, Fragment fragment) {
        return Utility.getTextFragmentAsString(text, fragment);
    }
}