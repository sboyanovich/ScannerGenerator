package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum Operators implements Domain {
    ASSIGNMENT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, ASSIGNMENT);
        }
    },
    GREATER {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, GREATER);
        }
    },
    LESS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LESS);
        }
    },
    NOT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, NOT);
        }
    },
    COMPLEMENT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, COMPLEMENT);
        }
    },
    QUESTION_MARK {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, QUESTION_MARK);
        }
    },
    COLON {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, COLON);
        }
    },
    ARROW {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, ARROW);
        }
    },
    EQUALS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, EQUALS);
        }
    },
    GREQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, GREQ);
        }
    },
    LEQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LEQ);
        }
    },
    NEQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, NEQ);
        }
    },
    AND {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, AND);
        }
    },
    OR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, OR);
        }
    },
    INC {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, INC);
        }
    },
    DEC {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DEC);
        }
    },
    PLUS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, PLUS);
        }
    },
    MINUS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MINUS);
        }
    },
    MUL {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MUL);
        }
    },
    DIV {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DIV);
        }
    },
    BW_AND {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, BW_AND);
        }
    },
    BW_OR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, BW_OR);
        }
    },
    BW_XOR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, BW_XOR);
        }
    },
    MOD {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MOD);
        }
    },
    LSHIFT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LSHIFT);
        }
    },
    RSHIFT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RSHIFT);
        }
    },
    LOG_RSHIFT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LOG_RSHIFT);
        }
    },
    PLUS_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, PLUS_EQ);
        }
    },
    MINUS_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MINUS_EQ);
        }
    },
    MUL_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MUL_EQ);
        }
    },
    DIV_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DIV_EQ);
        }
    },
    BW_AND_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, BW_AND_EQ);
        }
    },
    BW_OR_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, BW_OR_EQ);
        }
    },
    BQ_XOR_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, BQ_XOR_EQ);
        }
    },
    MOD_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MOD_EQ);
        }
    },
    LSHIFT_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LSHIFT_EQ);
        }
    },
    RSHIFT_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RSHIFT_EQ);
        }
    },
    LOG_RSHIFT_EQ {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LOG_RSHIFT_EQ);
        }
    }
}
