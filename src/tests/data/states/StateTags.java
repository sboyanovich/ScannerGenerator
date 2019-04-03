package tests.data.states;

import lab.lex.StateTag;
import lab.token.Domain;
import tests.data.domains.DomainsWithIntegerAttribute;
import tests.data.domains.DomainsWithStringAttribute;
import tests.data.domains.SimpleDomains;

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
    KEYWORD {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.KEYWORD;
        }
    },
    OPERATION {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.OPERATION;
        }
    },
    COMMENT {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.COMMENT;
        }
    },
    NOT_FINAL {
        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public Domain getDomain() {
            throw new Error("This is never supposed to be called!");
        }
    };

    @Override
    public boolean isFinal() {
        return true;
    }
}