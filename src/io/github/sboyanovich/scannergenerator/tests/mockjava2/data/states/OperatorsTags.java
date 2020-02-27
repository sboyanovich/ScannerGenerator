package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains.Operators;

public enum OperatorsTags implements StateTag {
    ASSIGNMENT {
        @Override
        public Domain getDomain() {
            return Operators.ASSIGNMENT;
        }
    },
    GREATER {
        @Override
        public Domain getDomain() {
            return Operators.GREATER;
        }
    },
    LESS {
        @Override
        public Domain getDomain() {
            return Operators.LESS;
        }
    },
    NOT {
        @Override
        public Domain getDomain() {
            return Operators.NOT;
        }
    },
    COMPLEMENT {
        @Override
        public Domain getDomain() {
            return Operators.COMPLEMENT;
        }
    },
    QUESTION_MARK {
        @Override
        public Domain getDomain() {
            return Operators.QUESTION_MARK;
        }
    },
    COLON {
        @Override
        public Domain getDomain() {
            return Operators.COLON;
        }
    },
    ARROW {
        @Override
        public Domain getDomain() {
            return Operators.ARROW;
        }
    },
    EQUALS {
        @Override
        public Domain getDomain() {
            return Operators.EQUALS;
        }
    },
    GREQ {
        @Override
        public Domain getDomain() {
            return Operators.GREQ;
        }
    },
    LEQ {
        @Override
        public Domain getDomain() {
            return Operators.LEQ;
        }
    },
    NEQ {
        @Override
        public Domain getDomain() {
            return Operators.NEQ;
        }
    },
    AND {
        @Override
        public Domain getDomain() {
            return Operators.AND;
        }
    },
    OR {
        @Override
        public Domain getDomain() {
            return Operators.OR;
        }
    },
    INC {
        @Override
        public Domain getDomain() {
            return Operators.INC;
        }
    },
    DEC {
        @Override
        public Domain getDomain() {
            return Operators.DEC;
        }
    },
    PLUS {
        @Override
        public Domain getDomain() {
            return Operators.PLUS;
        }
    },
    MINUS {
        @Override
        public Domain getDomain() {
            return Operators.MINUS;
        }
    },
    MUL {
        @Override
        public Domain getDomain() {
            return Operators.MUL;
        }
    },
    DIV {
        @Override
        public Domain getDomain() {
            return Operators.DIV;
        }
    },
    BW_AND {
        @Override
        public Domain getDomain() {
            return Operators.BW_AND;
        }
    },
    BW_OR {
        @Override
        public Domain getDomain() {
            return Operators.BW_OR;
        }
    },
    BW_XOR {
        @Override
        public Domain getDomain() {
            return Operators.BW_XOR;
        }
    },
    MOD {
        @Override
        public Domain getDomain() {
            return Operators.MOD;
        }
    },
    LSHIFT {
        @Override
        public Domain getDomain() {
            return Operators.LSHIFT;
        }
    },
    RSHIFT {
        @Override
        public Domain getDomain() {
            return Operators.RSHIFT;
        }
    },
    LOG_RSHIFT {
        @Override
        public Domain getDomain() {
            return Operators.LOG_RSHIFT;
        }
    },
    PLUS_EQ {
        @Override
        public Domain getDomain() {
            return Operators.PLUS_EQ;
        }
    },
    MINUS_EQ {
        @Override
        public Domain getDomain() {
            return Operators.MINUS_EQ;
        }
    },
    MUL_EQ {
        @Override
        public Domain getDomain() {
            return Operators.MUL_EQ;
        }
    },
    DIV_EQ {
        @Override
        public Domain getDomain() {
            return Operators.DIV_EQ;
        }
    },
    BW_AND_EQ {
        @Override
        public Domain getDomain() {
            return Operators.BW_AND_EQ;
        }
    },
    BW_OR_EQ {
        @Override
        public Domain getDomain() {
            return Operators.BW_OR_EQ;
        }
    },
    BQ_XOR_EQ {
        @Override
        public Domain getDomain() {
            return Operators.BQ_XOR_EQ;
        }
    },
    MOD_EQ {
        @Override
        public Domain getDomain() {
            return Operators.MOD_EQ;
        }
    },
    LSHIFT_EQ {
        @Override
        public Domain getDomain() {
            return Operators.LSHIFT_EQ;
        }
    },
    RSHIFT_EQ {
        @Override
        public Domain getDomain() {
            return Operators.RSHIFT_EQ;
        }
    },
    LOG_RSHIFT_EQ {
        @Override
        public Domain getDomain() {
            return Operators.LOG_RSHIFT_EQ;
        }
    }
}
