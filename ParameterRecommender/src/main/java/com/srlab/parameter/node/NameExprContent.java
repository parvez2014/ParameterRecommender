package com.srlab.parameter.node;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.binding.TypeResolver;

public class NameExprContent extends ParameterContent{

	String identifier;
	String typeQualifiedName; //qualifiedName of the type
	public NameExprContent(NameExpr nameExpr){
		super(nameExpr);
		this.identifier=null;
		this.typeQualifiedName=null;
	
		SimpleName sn = nameExpr.asNameExpr().getName();
		this.identifier = sn.getIdentifier();
		this.absStringRep = this.getAbsStringRep(nameExpr);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(nameExpr);
		this.typeQualifiedName = TypeResolver.resolve(nameExpr);
		this.parent = null;		
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}

	public void print(){
		System.out.println("Name: "+this.getRawStringRep()+" Identifier: "+this.getIdentifier()+" AbsStrRep: "+this.absStringRep);
	}
}
