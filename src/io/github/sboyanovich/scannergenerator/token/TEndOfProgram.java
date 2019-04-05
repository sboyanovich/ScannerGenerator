package io.github.sboyanovich.scannergenerator.token;

import io.github.sboyanovich.scannergenerator.Fragment;

public class TEndOfProgram extends Token {

    public TEndOfProgram(Fragment coords) {
        super(coords, DomainEOP.END_OF_PROGRAM);
    }
}
