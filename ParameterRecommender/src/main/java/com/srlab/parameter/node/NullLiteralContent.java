package com.srlab.parameter.node;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;

public class NullLiteralContent extends ParameterContent{
	private String name;
	public NullLiteralContent(NullLiteralExpr nl){
		super(nl);
		name = nl.toString();
		this.absStringRep = this.getAbsStringRep(nl);
		this.partlyAbsStringRep = this.getStringRep(nl);
		this.parent = null;
	}
	public String getName() {
		return name;
	}
	
	public void print(){
		System.out.print("NULL LITERAL: Name: "+this.getName()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}
