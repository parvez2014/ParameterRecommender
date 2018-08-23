package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class UnknownContent extends ParameterContent{
	
	public UnknownContent(Expression expression){
		super(expression);
		this.absStringRep ="unknown";
		this.absStringRepWithLiteral = "unknown";
	}

	@Override
	public String toString() {
		return "UnknownContent [rawStringRep=" + rawStringRep + ", parent=" + parent + ", absStringRep=" + absStringRep
				+ ", absStringRepWithLiteral=" + absStringRepWithLiteral + "]";
	}
}
