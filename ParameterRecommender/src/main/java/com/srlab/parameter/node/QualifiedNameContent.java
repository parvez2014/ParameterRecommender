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
import com.srlab.parameter.binding.TypeResolver;

public class QualifiedNameContent extends ParameterContent {

	private String identifier;
	private String typeQualifiedName;
	private String scope;

	public QualifiedNameContent(FieldAccessExpr fieldAccessExpr) {
		super(fieldAccessExpr);
		this.typeQualifiedName = null;
		this.parent = null;
		
		this.identifier = fieldAccessExpr.getName().getIdentifier();
		this.scope = fieldAccessExpr.getScope().toString();

		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(fieldAccessExpr);
		this.absStringRep = this.getAbsStringRep(fieldAccessExpr);
		try {
			this.typeQualifiedName = TypeResolver.resolve(fieldAccessExpr);
		}catch(Exception e) {
			
		}

		/*
		 * SymbolReference<? extends ResolvedValueDeclaration>
		 * srResolvedValueDeclaration =
		 * JSSConfigurator.getInstance().getJpf().solve(fieldAccessExpr.getName());
		 * ResolvedValueDeclaration resolvedValueDeclaration =
		 * srResolvedValueDeclaration.getCorrespondingDeclaration(); ResolvedType
		 * resolvedType = resolvedValueDeclaration.getType(); TypeDescriptor
		 * typeDescriptor = new TypeDescriptor(resolvedType); this.typeQualifiedName =
		 * typeDescriptor.getTypeQualifiedName();
		 * //System.out.println("Abstract String Rep in QualifiedNameContent: "
		 * +fieldAccessExpr.getScope());
		 * 
		 * JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
		 * srResolvedValueDeclaration =
		 * jpf.solve(fieldAccessExpr.getScope().asNameExpr()); if
		 * (srResolvedValueDeclaration.isSolved()) { resolvedValueDeclaration =
		 * srResolvedValueDeclaration .getCorrespondingDeclaration(); resolvedType =
		 * resolvedValueDeclaration.getType(); typeDescriptor = new
		 * TypeDescriptor(resolvedType); this.scope =
		 * fieldAccessExpr.getScope().toString(); this.scopeTypeQualifiedName =
		 * typeDescriptor.getTypeQualifiedName();
		 * System.out.println("TypeDescriptor is called..."); this.parent =
		 * ParameterContent.get(fieldAccessExpr.getScope()); }else {
		 * this.scopeTypeQualifiedName = fieldAccessExpr.getScope().toString(); NameExpr
		 * nameExpr = fieldAccessExpr.getScope().asNameExpr(); }
		 */
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

	@Override
	public String toString() {
		return "QualifiedNameContent [identifier=" + identifier + ", typeQualifiedName=" + typeQualifiedName
				+ ", scope=" + scope + ", rawStringRep=" + rawStringRep + ", parent=" + parent + ", absStringRep="
				+ absStringRep + ", absStringRepWithLiteral=" + absStringRepWithLiteral + "]";
	}

}
