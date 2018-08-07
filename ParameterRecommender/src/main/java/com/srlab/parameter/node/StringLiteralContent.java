package com.srlab.parameter.node;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class StringLiteralContent extends ParameterContent{
	
	private String name;
	public StringLiteralContent(StringLiteralExpr sl){
		super(sl);
		name = sl.toString();
		absStringRep = this.getAbsStringRep(sl);
		this.partlyAbsStringRep = this.getStringRep(sl);
		this.parent = null;
	}
	public String getName() {
		return name;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("STRING LITERAL: Name: "+this.getName()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}

