package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class NumberLiteralContent extends ParameterContent {
	
	public NumberLiteralContent(DoubleLiteralExpr dl) {
		super(dl);
		this.rawStringRep = dl.toString();
		this.absStringRep = this.getAbsStringRep(dl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(dl);
		this.parent = null;
	}

	public NumberLiteralContent(LongLiteralExpr ll) {
		super(ll);
		this.rawStringRep = ll.toString();
		this.absStringRep = this.getAbsStringRep(ll);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(ll);
		this.parent = null;
	}

	public NumberLiteralContent(IntegerLiteralExpr il) {
		super(il);
		this.rawStringRep = il.toString();
		this.absStringRep = this.getAbsStringRep(il);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(il);
		this.parent = null;
	}

	/*public String getStringRep(LiteralStringValueExpr expr) {
		if (expr instanceof DoubleLiteralExpr) {
			return expr.toString();
		} else if (expr instanceof LongLiteralExpr) {
			return expr.toString();
		} else if (expr instanceof IntegerLiteralExpr) {
			return expr.toString();
		} else {
			throw new RuntimeException("Could not match number literal content: " + expr.toString());
		}
	}*/

	public void print() {
		System.out.println("NumberLiteralContent [name=" + this.getRawStringRep() + "]");
	}
}
