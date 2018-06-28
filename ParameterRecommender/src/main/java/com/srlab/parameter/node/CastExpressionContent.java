package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class CastExpressionContent extends ParameterContent{
	private String name;
	private String absStringRep;
	private String castQualifier;
	private String castTypeQualifiedName;
	
	public CastExpressionContent(MethodCallExpr mi, MethodDeclaration md, CastExpr ce){
		super(ce);
		this.name = ce.toString();
		this.absStringRep = this.getStringRep(ce);
		this.castQualifier = null;
		this.castTypeQualifiedName = null;
		
		if(ce.getType()!=null) {
			this.castQualifier = ce.getType().toString();
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convert(ce.getType(),ce);
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			this.castTypeQualifiedName = typeDescriptor.getName();
		}
		else {
			this.castQualifier = null;
			this.castTypeQualifiedName = null;
		}
		
		if(ce.getExpression()!=null) {
			Expression expression = ce.getExpression();
			this.absStringRep = "("+this.castTypeQualifiedName+")"+this.getStringRep(expression);
		}
	}
	
	public String getName() {
		return name;
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
