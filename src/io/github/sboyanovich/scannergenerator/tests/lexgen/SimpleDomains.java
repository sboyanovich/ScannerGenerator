package io.github.sboyanovich.scannergenerator.tests.lexgen;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleDomains implements Domain {
    CLASS_MINUS_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CLASS_MINUS_OP);
        }
    },
    LPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LPAREN);
        }
    },
    RPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RPAREN);
        }
    },
    DOT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DOT);
        }
    },
    ITERATION_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, ITERATION_OP);
        }
    },
    POS_ITERATION_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, POS_ITERATION_OP);
        }
    },
    UNION_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, UNION_OP);
        }
    },
    OPTION_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, OPTION_OP);
        }
    },
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.WHITESPACE);
        }
    }
}
