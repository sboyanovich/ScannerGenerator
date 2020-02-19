package io.github.sboyanovich.scannergenerator.tests.mockjava.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.BasicToken;
import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.Token;

public enum SimpleLiterals implements Domain {
    TRUE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleLiterals.TRUE);
        }
    },
    FALSE {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleLiterals.FALSE);
        }
    },
    NULL {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new BasicToken(fragment, SimpleLiterals.NULL);
        }
    }
}
