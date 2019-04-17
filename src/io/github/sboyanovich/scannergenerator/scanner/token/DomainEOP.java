package io.github.sboyanovich.scannergenerator.scanner.token;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;

public enum DomainEOP implements Domain {
    END_OF_PROGRAM {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TEndOfProgram(fragment);
        }
    }
}
