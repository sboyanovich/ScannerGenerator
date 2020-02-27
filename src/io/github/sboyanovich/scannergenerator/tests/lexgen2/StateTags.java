package io.github.sboyanovich.scannergenerator.tests.lexgen2;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

public enum StateTags implements StateTag {
    CLASS_CHAR {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.CLASS_CHAR;
        }
    },
    CHAR_CLASS_OPEN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.CHAR_CLASS_OPEN;
        }
    },
    CHAR_CLASS_CLOSE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.CHAR_CLASS_CLOSE;
        }
    },
    CHAR_CLASS_NEG {
        @Override
        public Domain getDomain() {
            return SimpleDomains.CHAR_CLASS_NEG;
        }
    },
    CHAR_CLASS_RANGE_OP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.CHAR_CLASS_RANGE_OP;
        }
    },
    REPETITION_OP {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.REPETITION_OP;
        }
    },
    CLASS_MINUS_OP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.CLASS_MINUS_OP;
        }
    },
    NAMED_EXPR {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.NAMED_EXPR;
        }
    },
    LPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.LPAREN;
        }
    },
    RPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RPAREN;
        }
    },
    CHAR {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.CHAR;
        }
    },
    DOT {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DOT;
        }
    },
    ITERATION_OP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.ITERATION_OP;
        }
    },
    POS_ITERATION_OP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.POS_ITERATION_OP;
        }
    },
    UNION_OP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.UNION_OP;
        }
    },
    OPTION_OP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.OPTION_OP;
        }
    },
    WHITESPACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE;
        }
    }
}
