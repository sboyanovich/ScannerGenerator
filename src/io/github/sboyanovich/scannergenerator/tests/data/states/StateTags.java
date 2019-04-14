package io.github.sboyanovich.scannergenerator.tests.data.states;

import io.github.sboyanovich.scannergenerator.lex.StateTag;
import io.github.sboyanovich.scannergenerator.token.Domain;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithIntegerAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.domains.DomainsWithStringAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.domains.SimpleDomains;

public enum StateTags implements StateTag {
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
    INTEGER_LITERAL {
        @Override
        public Domain getDomain() {
            return DomainsWithIntegerAttribute.INTEGER_LITERAL;
        }
    },
    KEYWORD_IF {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KEYWORD_IF;
        }
    },
    KEYWORD_ELIF {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KEYWORD_ELIF;
        }
    },
    OP_DIVIDE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.OP_DIVIDE;
        }
    },
    OP_MULTIPLY {
        @Override
        public Domain getDomain() {
            return SimpleDomains.OP_MULTIPLY;
        }
    },
    COMMENT {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.COMMENT;
        }
    },
    OP_PLUS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.OP_PLUS;
        }
    },
    KEYWORD_AXIOM {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KEYWORD_AXIOM;
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
    DOT {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DOT;
        }
    },
    VERTICAL_BAR {
        @Override
        public Domain getDomain() {
            return SimpleDomains.VERTICAL_BAR;
        }
    },
    EQUALS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.EQUALS;
        }
    },
    ESCAPED_LPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.ESCAPED_LPAREN;
        }
    },
    ESCAPED_RPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.ESCAPED_RPAREN;
        }
    },
    AXM_DECL {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.AXM_DECL;
        }
    },
    NON_TERMINAL {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.NON_TERMINAL;
        }
    }
}