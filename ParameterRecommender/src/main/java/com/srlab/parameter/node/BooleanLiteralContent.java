package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class BooleanLiteralContent extends ParameterContent {
	private String name;

	public BooleanLiteralContent(BooleanLiteralExpr nl) {
		super(nl);
		name = nl.toString();
		this.absStringRep = this.getAbsStringRep(nl);
		this.partlyAbsStringRep = this.getStringRep(nl);
		this.parent = null;
	}

	public String getName() {
		return name;
	}

	public void print() {
		System.out.print("BOOLEAN LITERAL: " + "Name: " + this.getName() + " Abstract Rep: " + this.getAbsStringRep());
	}
}
