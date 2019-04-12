package io.github.sboyanovich.scannergenerator.tests.data.domains;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.lex.Text;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.*;
import io.github.sboyanovich.scannergenerator.token.Domain;
import io.github.sboyanovich.scannergenerator.token.Token;

public enum SimpleDomains implements Domain {
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TWhitespace(fragment);
        }
    },
    KEYWORD_IF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TKwdIf(fragment);
        }
    },
    KEYWORD_ELIF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TKwdElif(fragment);
        }
    },
    OP_DIVIDE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TOpDivide(fragment);
        }
    },
    OP_MULTIPLY {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TOpMultiply(fragment);
        }
    }
}