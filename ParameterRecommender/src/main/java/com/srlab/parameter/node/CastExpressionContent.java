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
import com.srlab.parameter.binding.TypeResolver;

public class CastExpressionContent extends ParameterContent {
	private String castQualifier;
	private String castTypeQualifiedName;
	
	public CastExpressionContent(CastExpr ce) {
		super(ce);
		this.castQualifier = ce.getType().toString();
		ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convertToUsage(ce.getType());
		this.castTypeQualifiedName = TypeDescriptor.resolveTypeQualifiedName(resolvedType);
		this.absStringRep = this.getAbsStringRep(ce);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(ce);
		this.parent = ParameterContent.get(ce.getExpression());
	}

	public String getCastQualifier() {
		return castQualifier;
	}

	public String getCastTypeQualifiedName() {
		return castTypeQualifiedName;
	}

	@Override
	public String toString() {
		return "CastExpressionContent [castQualifier=" + castQualifier + ", castTypeQualifiedName="
				+ castTypeQualifiedName + ", rawStringRep=" + rawStringRep + ", parent=" + parent + ", absStringRep="
				+ absStringRep + ", absStringRepWithLiteral=" + absStringRepWithLiteral + "]";
	}
}
