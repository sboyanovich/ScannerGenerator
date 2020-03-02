package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

public enum StateTags implements StateTag {
    WHITESPACE_IN_REGEX {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE_IN_REGEX;
        }
    },
    WHITESPACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE;
        }
    },
    IDENTIFIER {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.IDENTIFIER;
        }
    },
    STATE_NAME {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.STATE_NAME;
        }
    },
    DOMAINS_GROUP_MARKER {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.DOMAINS_GROUP_MARKER;
        }
    },
    RULE_END {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RULE_END;
        }
    },
    MODES_SECTION_MARKER {
        @Override
        public Domain getDomain() {
            return SimpleDomains.MODES_SECTION_MARKER;
        }
    },
    DEFINER {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DEFINER;
        }
    },
    RULES_SECTION_MARKER {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RULES_SECTION_MARKER;
        }
    },
    COMMA {
        @Override
        public Domain getDomain() {
            return SimpleDomains.COMMA;
        }
    },
    L_ANGLE_BRACKET {
        @Override
        public Domain getDomain() {
            return SimpleDomains.L_ANGLE_BRACKET;
        }
    },
    R_ANGLE_BRACKET {
        @Override
        public Domain getDomain() {
            return SimpleDomains.R_ANGLE_BRACKET;
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
            return DomainsWithIntPairAttribute.REPETITION_OP;
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
            return DomainsWithIntegerAttribute.CHAR;
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
    }/*,
    WHITESPACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE;
        }
    }*/
}
