package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleDomains implements Domain {
    DOUBLE_COLON {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.DOUBLE_COLON);
        }
    },
    AT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.AT);
        }
    },
    ELLIPSIS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.ELLIPSIS);
        }
    },
    DOT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.DOT);
        }
    },
    COMMA {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.COMMA);
        }
    },
    SEMICOLON {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.SEMICOLON);
        }
    },
    RSQ_BRACKET {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.RSQ_BRACKET);
        }
    },
    LSQ_BRACKET {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.LSQ_BRACKET);
        }
    },
    RBRACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.RBRACE);
        }
    },
    LBRACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.LBRACE);
        }
    },
    RPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.RPAREN);
        }
    },
    LPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.LPAREN);
        }
    },
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.WHITESPACE);
        }
    }
}
