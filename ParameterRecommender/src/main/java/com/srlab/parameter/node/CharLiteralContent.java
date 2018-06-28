package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class CharLiteralContent extends ParameterContent{
	private String name;
	private String absStringRep;
	public CharLiteralContent(MethodCallExpr mi, MethodDeclaration md, CharLiteralExpr cl){
		super(cl);
		name = cl.toString();
		this.absStringRep = this.getStringRep(cl);
	}
	
	public String getStringRep(CharLiteralExpr expr) {
		return expr.toString();
	}
	
	public String getName() {
		return name;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("Name: "+this.getName()+" Abstract String Rep: "+this.getAbsStringRep());
	}
}
