package io.github.sboyanovich.scannergenerator.tests.mockjava2.data.domains;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.tests.mockjava2.data.tokens.TIntegerLiteral;

import static io.github.sboyanovich.scannergenerator.utility.Utility.getTextFragmentAsString;

public enum DomainsWithIntegerAttribute implements DomainWithAttribute<Integer> {
    INTEGER_LITERAL {
        @Override
        public Integer attribute(Text text, Fragment fragment) {
            return Integer.parseInt(
                    removeAuxSymbols(getTextFragmentAsString(text, fragment))
            );
        }

        @Override
        public TokenWithAttribute<Integer> createToken(Text text, Fragment fragment) {
            return new TIntegerLiteral(fragment, INTEGER_LITERAL, attribute(text, fragment));
        }
    };

    private static String removeAuxSymbols(String intLiteral) {
        return intLiteral.replaceAll("[_lL]", "");
    }
}
