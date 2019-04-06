package io.github.sboyanovich.scannergenerator.lex;

import io.github.sboyanovich.scannergenerator.token.Domain;

public enum STNotFinal implements StateTag {
    NOT_FINAL {
        @Override
        public Domain getDomain() {
            throw new Error("This is never supposed to be called!");
        }
    }
}
