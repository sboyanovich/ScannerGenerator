package io.github.sboyanovich.scannergenerator.tests.mockjava.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum KeywordDomains implements Domain {
    // It would be nice to be able to generate all this boilerplate.

    KEYWORD_WHILE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_WHILE);
        }
    },
    KEYWORD_VOLATILE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_VOLATILE);
        }
    },
    KEYWORD_VOID {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_VOID);
        }
    },
    KEYWORD_TRY {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_TRY);
        }
    },
    KEYWORD_TRANSIENT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_TRANSIENT);
        }
    },
    KEYWORD_THROWS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_THROWS);
        }
    },
    KEYWORD_THROW {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_THROW);
        }
    },
    KEYWORD_THIS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_THIS);
        }
    },
    KEYWORD_SYNCHRONIZED {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_SYNCHRONIZED);
        }
    },
    KEYWORD_SWITCH {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_SWITCH);
        }
    },
    KEYWORD_SUPER {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_SUPER);
        }
    },
    KEYWORD_STRICTFP {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_STRICTFP);
        }
    },
    KEYWORD_STATIC {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_STATIC);
        }
    },
    KEYWORD_SHORT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_SHORT);
        }
    },
    KEYWORD_RETURN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_RETURN);
        }
    },
    KEYWORD_PUBLIC {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_PUBLIC);
        }
    },
    KEYWORD_PROTECTED {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_PROTECTED);
        }
    },
    KEYWORD_PRIVATE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_PRIVATE);
        }
    },
    KEYWORD_PACKAGE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_PACKAGE);
        }
    },
    KEYWORD_NEW {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_NEW);
        }
    },
    KEYWORD_NATIVE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_NATIVE);
        }
    },
    KEYWORD_LONG {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_LONG);
        }
    },
    KEYWORD_INTERFACE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_INTERFACE);
        }
    },
    KEYWORD_INT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_INT);
        }
    },
    KEYWORD_INSTANCEOF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_INSTANCEOF);
        }
    },
    KEYWORD_IMPORT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_IMPORT);
        }
    },
    KEYWORD_IMPLEMENTS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_IMPLEMENTS);
        }
    },
    KEYWORD_GOTO {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_GOTO);
        }
    },
    KEYWORD_IF {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_IF);
        }
    },
    KEYWORD_FOR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_FOR);
        }
    },
    KEYWORD_FLOAT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_FLOAT);
        }
    },
    KEYWORD_FINALLY {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_FINALLY);
        }
    },
    KEYWORD_FINAL {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_FINAL);
        }
    },
    KEYWORD_EXTENDS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_EXTENDS);
        }
    },
    KEYWORD_ENUM {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_ENUM);
        }
    },
    KEYWORD_ELSE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_ELSE);
        }
    },
    KEYWORD_DOUBLE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_DOUBLE);
        }
    },
    KEYWORD_DO {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_DO);
        }
    },
    KEYWORD_DEFAULT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_DEFAULT);
        }
    },
    KEYWORD_CONTINUE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_CONTINUE);
        }
    },
    KEYWORD_CONST {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_CONST);
        }
    },
    KEYWORD_CLASS {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_CLASS);
        }
    },
    KEYWORD_CHAR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_CHAR);
        }
    },
    KEYWORD_CATCH {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_CATCH);
        }
    },
    KEYWORD_CASE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_CASE);
        }
    },
    KEYWORD_BYTE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_BYTE);
        }
    },
    KEYWORD_BREAK {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_BREAK);
        }
    },
    KEYWORD_BOOLEAN {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_BOOLEAN);
        }
    },
    KEYWORD_ASSERT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_ASSERT);
        }
    },
    KEYWORD_ABSTRACT {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, KeywordDomains.KEYWORD_ABSTRACT);
        }
    }
}
