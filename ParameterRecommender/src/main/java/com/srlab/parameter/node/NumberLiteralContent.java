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
	private String name;
	public NumberLiteralContent(DoubleLiteralExpr dl) {
		super(dl);
		name = dl.toString();
		this.absStringRep = this.getStringRep(dl);
		this.partlyAbsStringRep = dl.toString();
		this.parent = null;
	}

	public NumberLiteralContent(LongLiteralExpr ll) {
		super(ll);
		name = ll.toString();
		this.absStringRep = this.getStringRep(ll);
		this.partlyAbsStringRep = ll.toString();
		this.parent = null;
	}

	public NumberLiteralContent(IntegerLiteralExpr il) {
		super(il);
		name = il.toString();
		this.absStringRep = this.getStringRep(il);
		this.partlyAbsStringRep = il.toString();
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

	private String getStringRep(CharLiteralExpr cl) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return name;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public void print() {
		System.out.print("Name: " + this.getName());
		System.out.println("Abstract String Rep: " + this.getAbsStringRep());
	}
}
