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

	public CharLiteralContent(CharLiteralExpr cl) {
		super(cl);
		this.rawStringRep = cl.toString();
		this.absStringRep = this.getAbsStringRep(cl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(cl);
		this.parent = null;
	}

	public void print() {
		System.out.print("Name: " + this.getRawStringRep() + " Abstract String Rep: " + this.getAbsStringRep());
	}
}
