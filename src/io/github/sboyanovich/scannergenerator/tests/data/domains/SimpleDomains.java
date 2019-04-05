package io.github.sboyanovich.scannergenerator.tests.data.domains;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.lex.Text;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.TWhitespace;
import io.github.sboyanovich.scannergenerator.token.Domain;
import io.github.sboyanovich.scannergenerator.token.Token;

public enum SimpleDomains implements Domain {
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TWhitespace(fragment);
        }
    }
}