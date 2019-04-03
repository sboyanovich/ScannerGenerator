package lab.token;

import lab.Fragment;

public class TError extends Token {
    private String contents;

    public TError(Fragment coords, String contents) {
        super(coords, DomainError.ERROR);
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return super.toString() + contents;
    }
}
