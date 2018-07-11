package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class CastExpressionContent extends ParameterContent {
	private String name;
	private String castQualifier;
	private String castTypeQualifiedName;
	private String expressionTypeQualifiedName;

	public CastExpressionContent(CastExpr ce) {
		super(ce);
		this.name = ce.toString();
		this.castQualifier = null;
		this.castTypeQualifiedName = null;
		this.expressionTypeQualifiedName = null;

		this.absStringRep = this.getStringRep(ce);
		this.castQualifier = ce.getType().toString();

		ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(ce.getType());
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		this.castTypeQualifiedName = typeDescriptor.getTypeQualifiedName();

		Expression expression = ce.getExpression();
		this.parent = ParameterContent.get(expression);
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedvalueDeclaration = JSSConfigurator.getInstance()
				.getJpf().solve(expression);
		if (srResolvedvalueDeclaration.isSolved()) {
			typeDescriptor = new TypeDescriptor(srResolvedvalueDeclaration.getCorrespondingDeclaration().getType());
			this.expressionTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
		}
		this.absStringRep = this.getStringRep(ce);
	}

	public String getName() {
		return name;
	}

	public void print() {

		System.out.println("CastExpressionContent [name=" + name + ", absStringRep=" + absStringRep + ", castQualifier="
				+ castQualifier + ", castTypeQualifiedName=" + castTypeQualifiedName + ", expressionTypeQualifiedName="
				+ expressionTypeQualifiedName + "]");
	}
}
