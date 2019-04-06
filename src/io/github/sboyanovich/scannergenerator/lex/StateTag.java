package io.github.sboyanovich.scannergenerator.lex;

import io.github.sboyanovich.scannergenerator.token.Domain;

public interface StateTag {
    static boolean isFinal(StateTag tag) {
        return !tag.equals(STNotFinal.NOT_FINAL);
    }
    Domain getDomain();
}
