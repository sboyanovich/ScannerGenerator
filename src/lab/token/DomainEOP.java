package lab.token;

import lab.Fragment;
import lab.lex.Text;

public enum DomainEOP implements Domain {
    END_OF_PROGRAM {
        @Override
        public Token createToken(Text text, Fragment fragment) {
            return new TEndOfProgram(fragment);
        }
    }
}
