package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class BooleanLiteralContent extends ParameterContent{
	private String name;
	private String absStringRep;
	public BooleanLiteralContent(MethodCallExpr mi, MethodDeclaration md, BooleanLiteralExpr nl){
		name = nl.toString();
		this.absStringRep = this.getStringRep(nl);
	}
	public String getName() {
		return name;
	}
	public String getStringRep(BooleanLiteralExpr booleanLiteralExpr) {
		return booleanLiteralExpr.toString();
	}

	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("BOOLEAN LITERAL: " + "Name: "+this.getName()+" Abstract Rep: "+this.getAbsStringRep());
	}
}
