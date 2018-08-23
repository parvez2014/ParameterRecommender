package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.binding.TypeResolver;

public class ThisExpressionContent extends ParameterContent{
	private String classQualifier;
	private String typeQualifiedName;
	public ThisExpressionContent(ThisExpr thisExpr) {
		super(thisExpr);
		this.parent = null;
		this.classQualifier = null;
		this.typeQualifiedName = null;
		this.absStringRep = this.getAbsStringRep(thisExpr);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(thisExpr);
		
		TypeResolver.resolve(thisExpr);
		if(thisExpr.getClassExpr().isPresent()) {
			this.classQualifier = thisExpr.getClassExpr().get().toString();
			this.parent = ParameterContent.get(thisExpr.getClassExpr().get());
		}
	}

	public String getClassQualifier() {
		return classQualifier;
	}


	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}

	@Override
	public String toString() {
		return "ThisExpressionContent [classQualifier=" + classQualifier + ", typeQualifiedName=" + typeQualifiedName
				+ ", rawStringRep=" + rawStringRep + ", parent=" + parent + ", absStringRep=" + absStringRep
				+ ", absStringRepWithLiteral=" + absStringRepWithLiteral + "]";
	}
}
