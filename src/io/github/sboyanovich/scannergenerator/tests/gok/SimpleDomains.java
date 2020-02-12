package io.github.sboyanovich.scannergenerator.tests.gok;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleDomains implements Domain {
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains.WHITESPACE);
        }
    },
    KW_RETURN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KW_RETURN);
        }
    },
    KW_FOR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KW_FOR);
        }
    },
    KW_IF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KW_IF);
        }
    },
    KW_ELSE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KW_ELSE);
        }
    },
    PLUS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, PLUS);
        }
    },
    MUL {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MUL);
        }
    },
    MINUS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MINUS);
        }
    },
    CMP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CMP);
        }
    },
    SEMICOLON {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SEMICOLON);
        }

    },
    COMMA {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, COMMA);
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
    LBRACKET {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, LBRACKET);
        }
    },
    RBRACKET {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RBRACKET);
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
    EQUALS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, EQUALS);
        }
    },
    ASSIGN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, ASSIGN);
        }
    }
}
