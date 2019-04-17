package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;

public class TEndOfProgram extends Token {

    public TEndOfProgram(Fragment coords) {
        super(coords, DomainEOP.END_OF_PROGRAM);
    }
}
