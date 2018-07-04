package com.srlab.parameter.node;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class QualifiedNameContent extends ParameterContent {

	private String name;
	private String identifier;
	private String typeQualifiedName;
	private String scope;
	private String scopeTypeQualifiedName;
	private String absStringRep;

	public QualifiedNameContent(MethodCallExpr mi, MethodDeclaration md, FieldAccessExpr fieldAccessExpr) {
		super(fieldAccessExpr);
		this.name = fieldAccessExpr.toString();
		this.scope = null;
		this.scopeTypeQualifiedName = null;

		JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
				.solve(fieldAccessExpr.getScope());
		if (srResolvedValueDeclaration.isSolved()) {
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration
					.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			this.scope = fieldAccessExpr.getScope().toString();
			this.scopeTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
		}
		
		this.identifier = fieldAccessExpr.getName().getIdentifier();
		srResolvedValueDeclaration = JSSConfigurator.getInstance().getJpf().solve(fieldAccessExpr.getName());
		ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
		ResolvedType resolvedType = resolvedValueDeclaration.getType();
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
		
		this.absStringRep = this.getStringRep(fieldAccessExpr); 

	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}

	public String getScope() {
		return scope;
	}

	public String getScopeTypeQualifiedName() {
		return scopeTypeQualifiedName;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public void print() {
		System.out.println("QualifiedNameContent [name=" + name + ", identifier=" + identifier + ", typeQualifiedName="
				+ typeQualifiedName + ", scope=" + scope + ", scopeTypeQualifiedName=" + scopeTypeQualifiedName
				+ ", absStringRep=" + absStringRep + "]");
	}
}
