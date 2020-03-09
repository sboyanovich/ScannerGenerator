package io.github.sboyanovich.scannergenerator.tests.biglexgen;

import io.github.sboyanovich.scannergenerator.scanner.Fragment;
import io.github.sboyanovich.scannergenerator.scanner.Text;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainWithAttribute;
import io.github.sboyanovich.scannergenerator.scanner.token.TokenWithAttribute;
import io.github.sboyanovich.scannergenerator.utility.Utility;

public enum DomainsWithStringPairAttribute implements DomainWithAttribute<StringPair> {
    ACTION_SWITCH_RETURN {
        @Override
        public StringPair attribute(Text text, Fragment fragment) {
            String s = Utility.getTextFragmentAsString(text, fragment);
            int index = s.indexOf("#");
            return new StringPair(s.substring(1, index), s.substring(index + 1));
        }

        @Override
        public TokenWithAttribute<StringPair> createToken(Text text, Fragment fragment) {
            return new TokenWithAttribute<>(fragment, this, attribute(text, fragment));
        }
    }
}