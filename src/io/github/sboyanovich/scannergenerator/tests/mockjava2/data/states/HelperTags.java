package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.states;

import io.github.sboyanovich.scannergenerator.scanner.StateTag;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;

public enum HelperTags implements StateTag {
    SLC_START,
    MLC_START,
    MLC_NO_ASTERISK_SEQ,
    MLC_ASTERISK,
    STRING_LITERAL_START,
    STRING_LITERAL_ELEM;

    @Override
    public Domain getDomain() {
        throw new Error("This is never supposed to be called!");
    }

    @Override
    public boolean hasDomain() {
        return false;
    }


}
