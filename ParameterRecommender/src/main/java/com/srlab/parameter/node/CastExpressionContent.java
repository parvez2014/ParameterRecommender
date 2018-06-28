package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class CastExpressionContent extends ParameterContent{
	private String name;
	private String absStringRep;
	private String stringReceiver;
	public CastExpressionContent(MethodCallExpr mi, MethodDeclaration md, CastExpr ce){
		super(ce);
		name = ce.getT
		this.absStringRep = this.getStringRep(ce);
		
		if(ce.getExpression()!=null) this.stringReceiver = ce.getExpression().toString();
		else this.stringReceiver = null;
		
		if(ce.getExpression()!=null)
		this.processReceiver(mi, md, ce.getExpression());
	}
	public String getName() {
		return name;
	}
	
	public String getStringReceiver() {
		return stringReceiver;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("Name: "+this.getName());
		System.out.print("Receiver: "+this.getStringReceiver());
		System.out.println("Abstract String Rep: "+this.getAbsStringRep());
	}
}
