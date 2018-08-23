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
		this.absStringRep = this.getAbsStringRep(dl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(dl);
		this.parent = null;
	}

	public NumberLiteralContent(LongLiteralExpr ll) {
		super(ll);
		this.absStringRep = this.getAbsStringRep(ll);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(ll);
		this.parent = null;
	}

	public NumberLiteralContent(IntegerLiteralExpr il) {
		super(il);
		this.absStringRep = this.getAbsStringRep(il);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(il);
		this.parent = null;
	}
	
	public void print() {
		System.out.println("NumberLiteralContent [name=" + this.getRawStringRep() + "]");
	}
}
