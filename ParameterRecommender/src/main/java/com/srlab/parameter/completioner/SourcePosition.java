package com.srlab.parameter.completioner;

import java.io.Serializable;

public class SourcePosition implements Comparable<SourcePosition>, Serializable {
    public final int line;
    public final int column;

    /**
     * The first position in the file
     */
    public static final SourcePosition HOME = new SourcePosition(1, 1);

    public SourcePosition(int line, int column) {
        this.line = line;
        this.column = column;
    }

    /**
     * Convenient factory method.
     */
    public static SourcePosition pos(int line, int column) {
        return new SourcePosition(line, column);
    }

    public SourcePosition withColumn(int column) {
        return new SourcePosition(this.line, column);
    }

    public SourcePosition withLine(int line) {
        return new SourcePosition(line, this.column);
    }

    /**
     * Check if the position is usable. Does not know what it is pointing at, so it can't check if the position is after
     * the end of the source.
     */
    public boolean valid() {
        return line > 0 && column > 0;
    }

    public boolean invalid() {
        return !valid();
    }

    public SourcePosition orIfInvalid(SourcePosition anotherPosition) {
         if (valid() || anotherPosition.invalid()) {
            return this;
        }
        return anotherPosition;
    }

    public boolean isAfter(SourcePosition position) {
        if (line > position.line) {
            return true;
        } else if (line == position.line) {
            return column > position.column;
        }
        return false;

    }

    public boolean isBefore(SourcePosition position) {
        if (line < position.line) {
            return true;
        } else if (line == position.line) {
            return column < position.column;
        }
        return false;
    }

    public int compareTo(SourcePosition o) {
         if (isBefore(o)) {
            return -1;
        }
        if (isAfter(o)) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourcePosition position = (SourcePosition) o;

        return line == position.line && column == position.column;
    }

    @Override
    public int hashCode() {
        return 31 * line + column;
    }

    @Override
    public String toString() {
        return "(line " + line + ",col " + column + ")";
    }
}