package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.tokens.TComment;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.tokens.TStringLiteral;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainsWithStringAttribute implements DomainWithAttribute<String> {
    STRING_LITERAL {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TStringLiteral(fragment, STRING_LITERAL, attribute(text, fragment));
        }

        /// NOT ENTIRELY CORRECT WAY TO DEAL WITH ESCAPES
        /// WILL PROBABLY WRITE BY HAND LATER
        @Override
        public String attribute(Text text, Fragment fragment) {
            String literal = super.attribute(text, fragment);
/*            literal = literal.replaceAll("\\\\b", "\b");
            literal = literal.replaceAll("\\\\t", "\t");
            literal = literal.replaceAll("\\\\n", "\n");
            literal = literal.replaceAll("\\\\f", "\f");
            literal = literal.replaceAll("\\\\r", "\r");
            literal = literal.replaceAll("\\\\\"", "\"");
            literal = literal.replaceAll("\\\\'", "'");
            literal = literal.replaceAll("\\\\", "\\");*/
            return literal;
        }
    },
    IDENTIFIER {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, IDENTIFIER, attribute(text, fragment));
        }
    },
    COMMENT {
        @Override
        public TokenWithAttribute<String> createToken(Text text, Fragment fragment) {
            return new TComment(fragment, attribute(text, fragment));
        }
    };

    @Override
    public String attribute(Text text, Fragment fragment) {
        return Utility.getTextFragmentAsString(text, fragment);
    }
}