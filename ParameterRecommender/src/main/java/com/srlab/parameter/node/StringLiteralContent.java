package com.srlab.parameter.node;


import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class StringLiteralContent extends ParameterContent{
	
public StringLiteralContent(StringLiteralExpr sl){
		super(sl);
		this.absStringRep = this.getAbsStringRep(sl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(sl);
		this.parent = null;
	}

public void print(){
		System.out.print("STRING LITERAL: Name: "+this.getRawStringRep()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}

