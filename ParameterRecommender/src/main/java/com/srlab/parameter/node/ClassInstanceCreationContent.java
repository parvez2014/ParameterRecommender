package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class ClassInstanceCreationContent extends ParameterContent{
	private String name;
	private String typeQualifiedName;
	private String absStringRep;
	public ClassInstanceCreationContent(MethodCallExpr mi, MethodDeclaration md, ObjectCreationExpr objectCreationExpression){
		super(mi);
		this.typeQualifiedName = null;
		this.name = objectCreationExpression.toString();
		this.absStringRep = this.getStringRep(objectCreationExpression);	
		if(objectCreationExpression.getScope().isPresent()) {
			Expression expression = objectCreationExpression.getScope().get();
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration>  srResolvedValueDeclaration  = jpf.solve(expression);
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				this.typeQualifiedName = typeDescriptor.getName();
			}
		}
		this.absStringRep = "new "+ typeQualifiedName+"( )";
	}
	private String getStringRep(ObjectCreationExpr objectCreationExpression) {
		// TODO Auto-generated method stub
		return this.absStringRep;
	}
	public String getName() {
		return name;
	}
	public String getAbsStringRep() {
		return absStringRep;
	}
	public void print(){
		System.out.print("Name: "+this.getName()+" AbsStrRep: "+this.getAbsStringRep());
	}
}
