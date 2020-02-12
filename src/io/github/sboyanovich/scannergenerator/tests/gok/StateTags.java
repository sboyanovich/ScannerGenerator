package io.github.sboyanovich.scannergenerator.tests.gok;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

public enum StateTags implements StateTag {
    WHITESPACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE;
        }
    },
    ID {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.ID;
        }
    },
    NUM {
        @Override
        public Domain getDomain() {
            return DomainsWithIntegerAttribute.NUM;
        }
    },
    KW_RETURN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KW_RETURN;
        }
    },
    KW_FOR {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KW_FOR;
        }
    },
    KW_IF {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KW_IF;
        }
    },
    KW_ELSE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.KW_ELSE;
        }
    },
    PLUS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.PLUS;
        }
    },
    MINUS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.MINUS;
        }
    },
    MUL {
        @Override
        public Domain getDomain() {
            return SimpleDomains.MUL;
        }
    },
    CMP {
        @Override
        public Domain getDomain() {
            return SimpleDomains.CMP;
        }
    },
    SEMICOLON {
        @Override
        public Domain getDomain() {
            return SimpleDomains.SEMICOLON;
        }
    },
    COMMA {
        @Override
        public Domain getDomain() {
            return SimpleDomains.COMMA;
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
    LBRACKET {
        @Override
        public Domain getDomain() {
            return SimpleDomains.LBRACKET;
        }
    },
    RBRACKET {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RBRACKET;
        }
    },
    INC {
        @Override
        public Domain getDomain() {
            return SimpleDomains.INC;
        }
    },
    DEC {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DEC;
        }
    },
    EQUALS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.EQUALS;
        }
    },
    ASSIGN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.ASSIGN;
        }
    }
}
