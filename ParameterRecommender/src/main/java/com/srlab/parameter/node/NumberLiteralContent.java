package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class NumberLiteralContent extends ParameterContent{
	private String name;
	private String absStringRep;
	public NumberLiteralContent(MethodCallExpr mi, MethodDeclaration md, CharLiteralExpr cl){
		super(cl);
		name = cl.toString();
		this.absStringRep = this.getStringRep(cl);
	}
	public NumberLiteralContent(MethodCallExpr mi, MethodDeclaration md, DoubleLiteralExpr dl){
		super(dl);
		name = dl.toString();
		this.absStringRep = this.getStringRep(dl);
	}
	public NumberLiteralContent(MethodCallExpr mi, MethodDeclaration md, LongLiteralExpr ll){
		super(ll);
		name = ll.toString();
		this.absStringRep = this.getStringRep(ll);
	}
	public NumberLiteralContent(MethodCallExpr mi, MethodDeclaration md, IntegerLiteralExpr il){
		super(il);
		name = il.toString();
		this.absStringRep = this.getStringRep(il);
	}
	
	public String getStringRep(LiteralStringValueExpr expr) {
		if(expr instanceof DoubleLiteralExpr) {
			return expr.toString();
		}
		else if(expr instanceof LongLiteralExpr) {
			return expr.toString();
		}
		else if(expr instanceof IntegerLiteralExpr) {
			return expr.toString();
		}
		else {
			throw new RuntimeException("Could not match number literal content: "+expr.toString());
		}
	}
	private String getStringRep(CharLiteralExpr cl) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	public String getName() {
		return name;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("Name: "+this.getName());
		System.out.println("Abstract String Rep: "+this.getAbsStringRep());
	}
}
