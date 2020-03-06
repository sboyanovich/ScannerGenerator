package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleDomains implements Domain {
    RULE_END {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RULE_END);
        }
    },
    DEFINER {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DEFINER);
        }
    },
    COMMA {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, COMMA);
        }
    },
    DOT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, DOT);
        }
    },
    MODES_SECTION_MARKER {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, MODES_SECTION_MARKER);
        }
    },
    RULES_SECTION_MARKER {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, RULES_SECTION_MARKER);
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
    L_ANGLE_BRACKET {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, L_ANGLE_BRACKET);
        }
    },
    R_ANGLE_BRACKET {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, R_ANGLE_BRACKET);
        }
    },
    CHAR_CLASS_OPEN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CHAR_CLASS_OPEN);
        }
    },
    CHAR_CLASS_CLOSE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CHAR_CLASS_CLOSE);
        }
    },
    CHAR_CLASS_RANGE_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CHAR_CLASS_RANGE_OP);
        }
    },
    CHAR_CLASS_NEG {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CHAR_CLASS_NEG);
        }
    },
    CLASS_MINUS_OP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, CLASS_MINUS_OP);
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
    }
}