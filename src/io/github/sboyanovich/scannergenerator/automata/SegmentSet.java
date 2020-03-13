package io.github.sboyanovich.scannergenerator.automata;

import io.github.sboyanovich.scannergenerator.utility.Pair;

import java.util.*;

import static io.github.sboyanovich.scannergenerator.utility.Utility.compressIntoSegments;
import static io.github.sboyanovich.scannergenerator.utility.Utility.isInRange;

/// Immutable
// Holds a subset of [0, maxCapacity) as (inclusive or exclusive) segments to save memory
public class SegmentSet extends AbstractSet<Integer> {
    private List<Pair<Integer, Integer>> sortedSegments;
    private int size;
    private int maxCapacity;
    private boolean inverted;

    public static SegmentSet thisRange(int a, int b, int maxCapacity) {
        return new SegmentSet(List.of(new Pair<>(a, b)), maxCapacity, false);
    }

    public static SegmentSet notThisRange(int a, int b, int maxCapacity) {
        return new SegmentSet(List.of(new Pair<>(a, b)), maxCapacity, true);
    }

    public static SegmentSet thisElem(int a, int maxCapacity) {
        return new SegmentSet(List.of(new Pair<>(a, a)), maxCapacity, false);
    }

    public static SegmentSet notThisElem(int a, int maxCapacity) {
        return new SegmentSet(List.of(new Pair<>(a, a)), maxCapacity, true);
    }

    public static SegmentSet fromCollection(Collection<Integer> collection, int maxCapacity) {
        if (collection instanceof Set) {
            @SuppressWarnings("unchecked")
            SegmentSet s = fromSet((Set) collection, maxCapacity);
            return s;
        } else {
            Set<Integer> set = new HashSet<>(collection);
            /// CHECK elements for [0, maxCapacity)

            return new SegmentSet(compressIntoSegments(set), maxCapacity, false);
        }
    }

    public static SegmentSet fromSet(Set<Integer> set, int maxCapacity) {
        /// CHECK elements for [0, maxCapacity)

        if (set instanceof SegmentSet) {
            // immutable
            return (SegmentSet) set;
        }
        return new SegmentSet(compressIntoSegments(set), maxCapacity, false);
    }

    public static SegmentSet nothing(int maxCapacity) {
        return new SegmentSet(List.of(), maxCapacity, false);
    }

    public static SegmentSet all(int maxCapacity) {
        return new SegmentSet(List.of(), maxCapacity, true);
    }

    private SegmentSet(List<Pair<Integer, Integer>> sortedSegments, int maxCapacity, boolean inverted) {
        this.sortedSegments = sortedSegments;
        this.maxCapacity = maxCapacity;
        this.inverted = inverted;

        int acc = 0;
        for (Pair<Integer, Integer> segment : sortedSegments) {
            int a = segment.getFirst();
            int b = segment.getSecond();
            int len = b + 1 - a;
            acc += len;
        }

        if (inverted) {
            this.size = maxCapacity - acc;
        } else {
            this.size = acc;
        }
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            int oi = (Integer) o;

            if (!isInRange(oi, 0, this.maxCapacity - 1)) {
                return false;
            }

            int bsa = 0;
            int bsb = this.sortedSegments.size() - 1;

            // binary search
            while (bsb + 1 - bsa > 0) {
                int mid = (bsa + bsb) / 2;
                Pair<Integer, Integer> segment = this.sortedSegments.get(mid);
                int a = segment.getFirst();
                if (oi < a) {
                    bsb = mid - 1;
                } else {
                    int b = segment.getSecond();
                    if (oi > b) {
                        bsa = mid + 1;
                    } else {
                        return !this.inverted;
                    }
                }
            }
            return this.inverted;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public Iterator<Integer> iterator() {
        if (!inverted) {
            return new Iterator<Integer>() {
                int i = 0;
                int j = 0;

                @Override
                public boolean hasNext() {
                    if (i < sortedSegments.size()) {
                        Pair<Integer, Integer> segment = sortedSegments.get(i);
                        int a = segment.getFirst();
                        int b = segment.getSecond();
                        int len = b + 1 - a;
                        if (j < len) {
                            return true;
                        } else {
                            i++;
                            j = 0;
                            return i < sortedSegments.size();
                        }
                    }
                    return false;
                }

                @Override
                public Integer next() {
                    Pair<Integer, Integer> segment = sortedSegments.get(i);
                    int a = segment.getFirst();
                    return a + j++;
                }
            };
        } else {
            return new Iterator<Integer>() {
                int i = 0;
                int j = 0;

                @Override
                public boolean hasNext() {
                    if (i < sortedSegments.size()) {
                        Pair<Integer, Integer> segment = sortedSegments.get(i);
                        int a = segment.getFirst();
                        if (j == a) {
                            int b = segment.getSecond();
                            j = b + 1;
                            i++; // prepare next segment
                        }
                    }
                    return j < maxCapacity;
                }

                @Override
                public Integer next() {
                    return j++;
                }
            };
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    public SegmentSet invert() {
        return new SegmentSet(this.sortedSegments, this.maxCapacity, !this.inverted);
    }
}
