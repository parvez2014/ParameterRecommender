package com.srlab.parameter.node;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.TypeExpr;
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
	
	public QualifiedNameContent(FieldAccessExpr fieldAccessExpr) {
		super(fieldAccessExpr);
		this.name = fieldAccessExpr.toString();
		this.scope = null;
		this.scopeTypeQualifiedName = null;
		//System.out.println("FieldAccessScope: "+fieldAccessExpr.getScope()+" "+(fieldAccessExpr.getScope() instanceof ClassExpr));
		
		this.identifier = fieldAccessExpr.getName().getIdentifier();
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = JSSConfigurator.getInstance().getJpf().solve(fieldAccessExpr.getName());
		ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
		ResolvedType resolvedType = resolvedValueDeclaration.getType();
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(fieldAccessExpr);
		this.absStringRep = this.getAbsStringRep(fieldAccessExpr);
		//System.out.println("Abstract String Rep in QualifiedNameContent: "+fieldAccessExpr.getScope());
		
		JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
		srResolvedValueDeclaration = jpf.solve(fieldAccessExpr.getScope().asNameExpr());
		if (srResolvedValueDeclaration.isSolved()) {
			resolvedValueDeclaration = srResolvedValueDeclaration
					.getCorrespondingDeclaration();
			resolvedType = resolvedValueDeclaration.getType();
			typeDescriptor = new TypeDescriptor(resolvedType);
			this.scope = fieldAccessExpr.getScope().toString();
			this.scopeTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
			System.out.println("TypeDescriptor is called...");
			this.parent = ParameterContent.get(fieldAccessExpr.getScope());
		}else {
			this.scopeTypeQualifiedName = fieldAccessExpr.getScope().toString();
			//System.out.println("FieldAccessScope: ::::"+"TypeExpr: "+(fieldAccessExpr.getScope() instanceof TypeExpr));
			//System.out.println("FieldAccessScope: ::::"+"NameExpr: "+(fieldAccessExpr.getScope() instanceof NameExpr));
			//System.out.println("FieldAccessScope: ::::"+"ClassExpr: "+(fieldAccessExpr.getScope() instanceof ClassExpr));
			NameExpr nameExpr = fieldAccessExpr.getScope().asNameExpr();
		}
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
	public void print() {
		System.out.println("QualifiedNameContent [name=" + name + ", identifier=" + identifier + ", typeQualifiedName="
				+ typeQualifiedName + ", scope=" + scope + ", scopeTypeQualifiedName=" + scopeTypeQualifiedName
				+ ", absStringRep=" + absStringRep + "]");
	}
}
