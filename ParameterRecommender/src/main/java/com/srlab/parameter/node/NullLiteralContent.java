package com.srlab.parameter.node;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;

public class NullLiteralContent extends ParameterContent{
	
	public NullLiteralContent(NullLiteralExpr nl){
		super(nl);
		this.rawStringRep = nl.toString();
		this.absStringRep = this.getAbsStringRep(nl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(nl);
		this.parent = null;
	}

	public void print(){
		System.out.print("NULL LITERAL: Name: "+this.getRawStringRep()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}
