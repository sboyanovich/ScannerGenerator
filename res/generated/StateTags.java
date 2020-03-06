package io.github.sboyanovich.scannergenerator.generated;

import io.github.sboyanovich.scannergenerator.automata.StateTag;

public enum StateTags implements StateTag {
    ASTERISK,
    COMMENT_CLOSE,
    NO_ASTERISK_SEQ,
    COMMENT_START,
    CLASS_CHAR,
    CHAR,
    CHAR_CLASS_CLOSE,
    CHAR_CLASS_OPEN,
    CHAR_CLASS_NEG,
    CHAR_CLASS_RANGE_OP,
    DOT,
    ITERATION_OP,
    POS_ITERATION_OP,
    UNION_OP,
    OPTION_OP,
    REPETITION_OP,
    CLASS_MINUS_OP,
    RPAREN,
    LPAREN,
    NAMED_EXPR,
    IDENTIFIER,
    DEFINER,
    MODES_SECTION_MARKER,
    DOMAINS_GROUP_MARKER,
    RULES_SECTION_MARKER,
    R_ANGLE_BRACKET,
    L_ANGLE_BRACKET,
    COMMA,
    RULE_END,
    WHITESPACE,
    WHITESPACE_IN_REGEX
}