package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.util.List;
import java.util.stream.Collectors;

// UTF32 text
public class Text {
    static final int EOI = -1;

    private List<Integer> codePoints;

    private Text(List<Integer> codePoints) {
        this.codePoints = codePoints;
    }

    public Text(String text) {
        this(text
                .codePoints()
                .boxed()
                .collect(Collectors.toList())
        );
    }

    public int size() {
        return this.codePoints.size();
    }

    public int codePointAt(int index) {
        if (index < this.codePoints.size()) {
            return this.codePoints.get(index);
        }
        return EOI;
    }

    public Text subtext(int start, int follow) {
        return new Text(this.codePoints.subList(start, follow));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int codePoint : this.codePoints) {
            result.append(Utility.asString(codePoint));
        }

        return result.toString();
    }
}
