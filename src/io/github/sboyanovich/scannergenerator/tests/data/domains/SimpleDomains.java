package io.github.sboyanovich.scannergenerator.tests.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleDomains implements Domain {
    WHITESPACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.WHITESPACE);
        }
    },
    KEYWORD_IF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.KEYWORD_IF);
        }
    },
    KEYWORD_ELIF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.KEYWORD_ELIF);
        }
    },
    OP_DIVIDE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.OP_DIVIDE);
        }
    },
    OP_MULTIPLY {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleDomains.OP_MULTIPLY);
        }
    },
    OP_PLUS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, OP_PLUS);
        }
    },
    KEYWORD_AXIOM {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KEYWORD_AXIOM);
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
    VERTICAL_BAR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, VERTICAL_BAR);
        }
    },
    EQUALS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, EQUALS);
        }
    },
    ESCAPED_LPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, ESCAPED_LPAREN);
        }
    },
    ESCAPED_RPAREN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, ESCAPED_RPAREN);
        }
    },
    OPERATION {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, OPERATION);
        }
    },
    KEYWORD {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KEYWORD);
        }
    }
}