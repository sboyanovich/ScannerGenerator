package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;

public class TError extends TokenWithAttribute<String> {

    public TError(Fragment coords, String contents) {
        super(coords, Domain.ERROR, contents);
    }

}
