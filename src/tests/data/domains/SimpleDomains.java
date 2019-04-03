package tests.data.domains;

import lab.Fragment;
import lab.lex.Text;
import lab.token.Domain;
import lab.token.Token;
import tests.data.tokens.TOperation;
import tests.data.tokens.TWhitespace;

public enum SimpleDomains implements Domain {
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TWhitespace(fragment);
        }
    }
}