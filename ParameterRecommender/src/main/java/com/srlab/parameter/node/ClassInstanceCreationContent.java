package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.binding.TypeResolver;

public class ClassInstanceCreationContent extends ParameterContent{
	private String scope;
	private String qualifiedName;
	private String className; 
	public ClassInstanceCreationContent(ObjectCreationExpr objectCreationExpression){
		super(objectCreationExpression);
		this.parent = null;
		this.scope = null;
		this.className = null;
		this.qualifiedName = null;
		
		this.absStringRep = this.getAbsStringRep(objectCreationExpression);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(objectCreationExpression);
	
		SymbolReference<ResolvedConstructorDeclaration> srResolvedConstructorDeclaration = JSSConfigurator.getInstance().getJpf().solve(objectCreationExpression);
		if(srResolvedConstructorDeclaration.isSolved()) {
			this.qualifiedName = srResolvedConstructorDeclaration.getCorrespondingDeclaration().getQualifiedName();
			this.className = srResolvedConstructorDeclaration.getCorrespondingDeclaration().getClassName();
		}
		else throw new RuntimeException("Error in resolving object creation expression: "+ objectCreationExpression);
		if(objectCreationExpression.getScope().isPresent()) {
			Expression expression = objectCreationExpression.getScope().get();
			this.scope = objectCreationExpression.getScope().get().toString();
			parent = ParameterContent.get(expression);
		}
		
		
		/*ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(objectCreationExpression.getType());
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
		
		if(objectCreationExpression.getScope().isPresent()) {
			Expression expression = objectCreationExpression.getScope().get();
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration>  srResolvedValueDeclaration  = jpf.solve(expression);
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				resolvedType = resolvedValueDeclaration.getType();
				typeDescriptor = new TypeDescriptor(resolvedType);
				this.scope = objectCreationExpression.getScope().get().toString();
				this.scopeTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
				this.parent = ParameterContent.get(objectCreationExpression.getScope().get());
			}
		}
		else {
			this.parent = null;
			this.scope = null;
			this.scopeTypeQualifiedName =null;
		}*/
	}
	public String getScope() {
		return scope;
	}
	public String getQualifiedName() {
		return qualifiedName;
	}
	public String getClassName() {
		return className;
	}
	@Override
	public String toString() {
		return "ClassInstanceCreationContent [scope=" + scope + ", qualifiedName=" + qualifiedName + ", className="
				+ className + ", rawStringRep=" + rawStringRep + ", parent=" + parent + ", absStringRep=" + absStringRep
				+ ", absStringRepWithLiteral=" + absStringRepWithLiteral + "]";
	}
}
