package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class CharLiteralContent extends ParameterContent {
	private String name;

	public CharLiteralContent(CharLiteralExpr cl) {
		super(cl);
		name = cl.toString();
		this.absStringRep = this.getAbsStringRep(cl);
		this.partlyAbsStringRep = this.getStringRep(cl);
		this.parent = null;
	}

	public String getName() {
		return name;
	}

	public void print() {
		System.out.print("Name: " + this.getName() + " Abstract String Rep: " + this.getAbsStringRep());
	}
}
