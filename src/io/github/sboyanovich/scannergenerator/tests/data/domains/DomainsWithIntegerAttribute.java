package io.github.sboyanovich.scannergenerator.tests.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.data.tokens.TIntegerLiteral;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainsWithIntegerAttribute implements DomainWithAttribute<Integer> {
    INTEGER_LITERAL {
        @Override
        public Integer attribute(Text text, Fragment fragment) {
            return Integer.parseInt(
                    Utility.getTextFragmentAsString(text, fragment)
            );
        }

        @Override
        public TokenWithAttribute<Integer> createToken(Text text, Fragment fragment) {
            return new TIntegerLiteral(fragment, attribute(text, fragment));
        }
    }
}