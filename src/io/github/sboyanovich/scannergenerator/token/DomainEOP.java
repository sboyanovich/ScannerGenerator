package io.github.sboyanovich.scannergenerator.token;

import io.github.sboyanovich.scannergenerator.Fragment;
import io.github.sboyanovich.scannergenerator.lex.Text;

public enum DomainEOP implements Domain {
    END_OF_PROGRAM {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TEndOfProgram(fragment);
        }
    }
}
