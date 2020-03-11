package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states;

import io.github.sboyanovich.scannergenerator.scanner.DomainTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains.*;

public enum StateTags implements DomainTag {
    /// OPERATORS
    // In separate enum
    /// OPERATORS#

    STRING_LITERAL {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.STRING_LITERAL;
        }
    },
    DOUBLE_COLON {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DOUBLE_COLON;
        }
    },
    AT {
        @Override
        public Domain getDomain() {
            return SimpleDomains.AT;
        }
    },
    ELLIPSIS {
        @Override
        public Domain getDomain() {
            return SimpleDomains.ELLIPSIS;
        }
    },
    DOT {
        @Override
        public Domain getDomain() {
            return SimpleDomains.DOT;
        }
    },
    COMMA {
        @Override
        public Domain getDomain() {
            return SimpleDomains.COMMA;
        }
    },
    SEMICOLON {
        @Override
        public Domain getDomain() {
            return SimpleDomains.SEMICOLON;
        }
    },
    RSQ_BRACKET {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RSQ_BRACKET;
        }
    },
    LSQ_BRACKET {
        @Override
        public Domain getDomain() {
            return SimpleDomains.LSQ_BRACKET;
        }
    },
    RBRACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RBRACE;
        }
    },
    LBRACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.LBRACE;
        }
    },
    RPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.RPAREN;
        }
    },
    LPAREN {
        @Override
        public Domain getDomain() {
            return SimpleDomains.LPAREN;
        }
    },
    INTEGER_LITERAL {
        @Override
        public Domain getDomain() {
            return DomainsWithIntegerAttribute.INTEGER_LITERAL;
        }
    },
    IDENTIFIER {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.IDENTIFIER;
        }
    },
    TRUE {
        @Override
        public Domain getDomain() {
            return SimpleLiterals.TRUE;
        }
    },
    FALSE {
        @Override
        public Domain getDomain() {
            return SimpleLiterals.FALSE;
        }
    },
    NULL {
        @Override
        public Domain getDomain() {
            return SimpleLiterals.NULL;
        }
    },
    COMMENT {
        @Override
        public Domain getDomain() {
            return DomainsWithStringAttribute.COMMENT;
        }
    },
    KEYWORD_ABSTRACT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_ABSTRACT;
        }
    },

    KEYWORD_ASSERT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_ASSERT;
        }
    },

    KEYWORD_BOOLEAN {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_BOOLEAN;
        }
    },

    KEYWORD_BREAK {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_BREAK;
        }
    },

    KEYWORD_BYTE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_BYTE;
        }
    },

    KEYWORD_CASE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_CASE;
        }
    },

    KEYWORD_CATCH {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_CATCH;
        }
    },

    KEYWORD_CHAR {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_CHAR;
        }
    },

    KEYWORD_CLASS {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_CLASS;
        }
    },

    KEYWORD_CONST {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_CONST;
        }
    },

    KEYWORD_CONTINUE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_CONTINUE;
        }
    },

    KEYWORD_DEFAULT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_DEFAULT;
        }
    },

    KEYWORD_DO {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_DO;
        }
    },

    KEYWORD_DOUBLE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_DOUBLE;
        }
    },

    KEYWORD_ELSE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_ELSE;
        }
    },

    KEYWORD_ENUM {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_ENUM;
        }
    },

    KEYWORD_EXTENDS {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_EXTENDS;
        }
    },

    KEYWORD_FINAL {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_FINAL;
        }
    },

    KEYWORD_FINALLY {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_FINALLY;
        }
    },

    KEYWORD_FLOAT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_FLOAT;
        }
    },

    KEYWORD_FOR {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_FOR;
        }
    },

    KEYWORD_IF {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_IF;
        }
    },

    KEYWORD_GOTO {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_GOTO;
        }
    },

    KEYWORD_IMPLEMENTS {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_IMPLEMENTS;
        }
    },

    KEYWORD_IMPORT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_IMPORT;
        }
    },

    KEYWORD_INSTANCEOF {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_INSTANCEOF;
        }
    },

    KEYWORD_INT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_INT;
        }
    },

    KEYWORD_INTERFACE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_INTERFACE;
        }
    },

    KEYWORD_LONG {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_LONG;
        }
    },

    KEYWORD_NATIVE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_NATIVE;
        }
    },

    KEYWORD_NEW {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_NEW;
        }
    },

    KEYWORD_PACKAGE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_PACKAGE;
        }
    },

    KEYWORD_PRIVATE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_PRIVATE;
        }
    },

    KEYWORD_PROTECTED {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_PROTECTED;
        }
    },

    KEYWORD_PUBLIC {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_PUBLIC;
        }
    },

    KEYWORD_RETURN {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_RETURN;
        }
    },

    KEYWORD_SHORT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_SHORT;
        }
    },

    KEYWORD_STATIC {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_STATIC;
        }
    },

    KEYWORD_STRICTFP {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_STRICTFP;
        }
    },

    KEYWORD_SUPER {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_SUPER;
        }
    },

    KEYWORD_SWITCH {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_SWITCH;
        }
    },

    KEYWORD_SYNCHRONIZED {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_SYNCHRONIZED;
        }
    },

    KEYWORD_THIS {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_THIS;
        }
    },

    KEYWORD_THROW {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_THROW;
        }
    },

    KEYWORD_THROWS {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_THROWS;
        }
    },

    KEYWORD_TRANSIENT {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_TRANSIENT;
        }
    },

    KEYWORD_TRY {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_TRY;
        }
    },

    KEYWORD_VOID {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_VOID;
        }
    },

    KEYWORD_VOLATILE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_VOLATILE;
        }
    },

    KEYWORD_WHILE {
        @Override
        public Domain getDomain() {
            return KeywordDomains.KEYWORD_WHILE;
        }
    },
    WHITESPACE {
        @Override
        public Domain getDomain() {
            return SimpleDomains.WHITESPACE;
        }
    }
}
