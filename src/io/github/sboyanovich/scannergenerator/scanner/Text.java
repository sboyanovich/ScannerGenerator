package io.github.sboyanovich.scannergenerator.scanner;

import io.github.sboyanovich.scannergenerator.utility.Utility;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// UTF32 text
public class Text {
    public static final int EOI = -1;

    private List<Integer> codePoints;

    private Text(List<Integer> codePoints, int maxCodePoint) {
        this.codePoints = new ArrayList<>(codePoints);
        this.codePoints.add(maxCodePoint + 1);
    }

    private Text(List<Integer> codePoints) {
        this(codePoints, Character.MAX_CODE_POINT);
    }

    public Text(String text) {
        this(Normalizer.normalize(text, Normalizer.Form.NFC)
                .codePoints()
                .boxed()
                .collect(Collectors.toList())
        );
    }

    public int getAltEoi() {
        return this.codePoints.get(this.codePoints.size() - 1);
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
        int end = follow;
        int size = this.codePoints.size();
        // ensuring there's only one EOF in sequence
        if (size - 1 < end) {
            end = size - 1;
        }
        return new Text(this.codePoints.subList(start, end), getAltEoi());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < this.codePoints.size() - 1; i++) {
            int codePoint = this.codePoints.get(i);
            result.append(Utility.asString(codePoint));
        }

        return result.toString();
    }
}
