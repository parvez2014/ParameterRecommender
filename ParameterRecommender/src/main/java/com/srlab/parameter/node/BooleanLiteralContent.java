package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class BooleanLiteralContent extends ParameterContent {

	public BooleanLiteralContent(BooleanLiteralExpr nl) {
		super(nl);
		this.rawStringRep = nl.toString();
		this.absStringRep = this.getAbsStringRep(nl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(nl);
		this.parent = null;
	}

	public void print() {
		System.out.print("BOOLEAN LITERAL: " + "Name: " + this.getRawStringRep() + " Abstract Rep: " + this.getAbsStringRep());
	}
}
