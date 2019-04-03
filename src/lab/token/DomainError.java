package lab.token;

import lab.Fragment;
import lab.lex.Text;
import utility.Utility;

public enum DomainError implements DomainWithAttribute<String> {
    ERROR {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TError(fragment, attribute(text, fragment));
        }

        // TODO: Factor the common part somewhere out
        @Override
        public String attribute(Text text, Fragment fragment) {
            return Utility.getTextFragmentAsString(text, fragment);
        }
    }
}
