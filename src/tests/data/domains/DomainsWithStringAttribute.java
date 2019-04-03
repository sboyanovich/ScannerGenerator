package tests.data.domains;

import lab.Fragment;
import lab.lex.Text;
import lab.token.DomainWithAttribute;
import lab.token.Token;
import tests.data.tokens.TComment;
import tests.data.tokens.TIdentifier;
import tests.data.tokens.TKeyword;
import tests.data.tokens.TOperation;
import utility.Utility;

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
    },
    KEYWORD {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TKeyword(fragment, attribute(text, fragment));
        }
    },
    OPERATION {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TOperation(fragment, attribute(text, fragment));
        }
    };

    @Override
    public String attribute(Text text, Fragment fragment) {
        return Utility.getTextFragmentAsString(text, fragment);
    }
}