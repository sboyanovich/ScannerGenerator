package io.github.sboyanovich.scannergenerator.tests.data.domains;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.lex.Text;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.*;
import io.github.sboyanovich.scannergenerator.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.token.Token;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    IDENTIFIER {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TIdentifier(fragment, attribute(text, fragment));
        }
    },
    COMMENT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TComment(fragment, attribute(text, fragment));
        }
    };

    @Override
    public String attribute(Text text, Fragment fragment) {
        return Utility.getTextFragmentAsString(text, fragment);
    }
}