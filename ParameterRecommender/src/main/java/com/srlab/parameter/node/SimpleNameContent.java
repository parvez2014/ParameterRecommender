package com.srlab.parameter.node;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class SimpleNameContent extends ParameterContent{

	String name;     //the qualified name
	String identifier;
	String typeQualifiedName; //qualifiedName of the type
	int bindingKind;
	String absStringRep;
	
	public SimpleNameContent(MethodCallExpr mi, MethodDeclaration md, SimpleName sn){
		super(sn);
		this.name=null;
		this.identifier=null;
		this.typeQualifiedName=null;
		this.bindingKind=-1;
		this.absStringRep=null;
		
		JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(sn);
		if(srResolvedValueDeclaration.isSolved()) {
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			this.identifier = sn.getIdentifier();
			this.absStringRep = typeDescriptor.getName(resolvedType);
			this.typeQualifiedName = typeDescriptor.getName(resolvedType);
			this.absStringRep = this.typeQualifiedName;
			this.name = sn.toString();
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

	public int getBindingKind() {
		return bindingKind;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public void print(){
		System.out.println("Name: "+this.getName()+" Identifier: "+this.getIdentifier()+" AbsStrRep: "+this.getAbsStringRep());
	}
}
