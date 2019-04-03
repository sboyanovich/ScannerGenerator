package lab.token;

import lab.Fragment;

public class TEndOfProgram extends Token {

    public TEndOfProgram(Fragment coords) {
        super(coords, DomainEOP.END_OF_PROGRAM);
    }
}
