package com.srlab.parameter.node;

import java.util.ArrayList;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class QualifiedNameContent extends ParameterContent{
	
	private String typeQualifiedName;
	private String qualifier;
	String name;
	String identifier;
	String absStringRep;
	ArrayList<String> receiverTypeHierarchy;
	public QualifiedNameContent(MethodCallExpr mi, MethodDeclaration md, Name qn){
		super(qn);
		this.typeQualifiedName =null;
		this.qualifier=null;
		this.qualifier="";
		
		if(qn.getQualifier().isPresent()) {
			Name name = qn.getQualifier().get();
			this.qualifier = qn.getQualifier().get().toString();
		}
		
		
		//if(qn.getQualifier()!=null && qn.getQualifier().resolveTypeBinding()!=null){
		//	this.typeQualifiedName = qn.getQualifier().resolveTypeBinding().getQualifiedName();
		//	this.qualifier = qn.getQualifier().toString();
		//}
		
		this.name = qn.toString();
		this.identifier = qn.getIdentifier();
		this.absStringRep = this.getStringRep(qn);
		//this.receiverTypeHierarchy = new ArrayList();
		//OverrideDetector.getInstance().collectOverrideHierarchy(qn.getQualifier().resolveTypeBinding(),this.receiverTypeHierarchy);	
		//this.processReceiver(mi,md,qn.getQualifier());
	}

	private String getStringRep(Name qn) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTypeQualifiedName() {
		return typeQualifiedName;
	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public ArrayList<String> getReceiverTypeHierarchy() {
		return receiverTypeHierarchy;
	}

	public String getQualifier() {
		return qualifier;
	}

	public void print(){
		System.out.println("Name: "+this.getName()+" Identifier: "+this.getIdentifier()+" Qualifier:  "+this.getQualifier()+" AbsStringRep: "+this.getAbsStringRep());
	}
}
