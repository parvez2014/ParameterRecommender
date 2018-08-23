package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class BooleanLiteralContent extends ParameterContent {

	public BooleanLiteralContent(BooleanLiteralExpr nl) {
		super(nl);
		this.absStringRep = this.getAbsStringRep(nl);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(nl);
		this.parent = null;
	}

	@Override
	public String toString() {
		return "BooleanLiteralContent [rawStringRep=" + rawStringRep + ", parent=" + parent + ", absStringRep="
				+ absStringRep + ", absStringRepWithLiteral=" + absStringRepWithLiteral + "]";
	}
}
