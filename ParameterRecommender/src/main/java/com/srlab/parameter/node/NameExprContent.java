package com.srlab.parameter.node;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class NameExprContent extends ParameterContent{

	String name;     //the qualified name
	String identifier;
	String typeQualifiedName; //qualifiedName of the type
	String absStringRep;
	
	public NameExprContent(MethodCallExpr mi, MethodDeclaration md, NameExpr nameExpr){
		super(nameExpr);
		this.name=null;
		this.identifier=null;
		this.typeQualifiedName=null;
		this.absStringRep=null;
		
		SimpleName sn = nameExpr.asNameExpr().getName();
		JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(sn);
		if(srResolvedValueDeclaration.isSolved()) {
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		
			this.name = sn.toString();
			this.identifier = sn.getIdentifier();
			this.typeQualifiedName = typeDescriptor.getTypeQualifiedName();
			this.absStringRep = this.getStringRep(nameExpr);
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

	public void print(){
		System.out.println("Name: "+this.getName()+" Identifier: "+this.getIdentifier()+" AbsStrRep: "+this.absStringRep);
	}
}
