package lab;

public class Position implements Comparable<Position> {
    private int line;
    private int pos;
    private int index;

    public Position(int line, int pos, int index) {
        this.line = line;
        this.pos = pos;
        this.index = index;
    }

    public Position(Position position) {
        this(position.line, position.pos, position.index);
    }

    public Position() {
        this(1, 1, 0);
    }

    public int getLine() {
        return line;
    }

    public int getPos() {
        return pos;
    }

    public int getIndex() {
        return index;
    }

    private String toString(boolean verbose) {
        if (verbose) {
            return "(Line: " + this.line + ", Pos: " + this.pos + ", Index: " + this.index + ")";
        } else {
            return "(" + this.line + ", " + this.pos + ")";
        }
    }


    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public int compareTo(Position o) {
        return Integer.compare(this.index, o.index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            return this.compareTo((Position) obj) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = this.line;
        hashCode = 31 * hashCode + this.pos;
        hashCode = 31 * hashCode + this.index;
        return hashCode;
    }

}
