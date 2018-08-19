package com.srlab.parameter.completioner;

import com.github.javaparser.ast.Node;

import static com.github.javaparser.utils.Utils.assertNotNull;

import java.io.Serializable;

import com.github.javaparser.Position;

/**
 * A position in a source file. Lines and columns start counting at 1
 * SPosition is the serialized version of Java position class
 */
public class SourcePosition2 extends Position implements Serializable {
      /**
     * The first position in the file
     */
    public static final SourcePosition2 HOME = new SourcePosition2(1, 1);

    public SourcePosition2(Position position) {
    	super(position.line,position.column);
    }
    public SourcePosition2(int line, int column) {
    	super(line,column);
    }

    /**
     * Convenient factory method.
     */
    public static SourcePosition2 pos(int line, int column) {
        return new SourcePosition2(line, column);
    }

    public SourcePosition2 withColumn(int column) {
        return new SourcePosition2(this.line, column);
    }

    public SourcePosition2 withLine(int line) {
        return new SourcePosition2(line, this.column);
    }
}