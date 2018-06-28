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

public class MethodInvocationContent extends ParameterContent{
	
	private String methodName;
	private String stringReceiver;
	private String receiverQualifiedName;
	private String absStringRep;
	private ArrayList<String> receiverTypeHierarchy;
	
	public MethodInvocationContent(MethodCallExpr mi, MethodDeclaration md){
		
		super(mi);
		this.stringReceiver = null;
		this.receiverQualifiedName = null;
		this.methodName=null;
		this.absStringRep = null;		
		
		this.methodName = mi.getName().getIdentifier();
		if(mi.getScope().isPresent()) {
			this.stringReceiver = mi.getScope().toString();
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(mi.getScope().get());
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				this.receiverQualifiedName = typeDescriptor.getName(resolvedType);
				this.absStringRep = null;
			}
		}
		else {
			this.stringReceiver = null;
			this.receiverQualifiedName = null;
			this.absStringRep = this.methodName;
		}
	}
	
	public String getMethodName() {
		return methodName;
	}

	public String getStringReceiver() {
		return stringReceiver;
	}


	public String getReceiverQualifiedName() {
		return receiverQualifiedName;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public ArrayList<String> getReceiverTypeHierarchy() {
		return receiverTypeHierarchy;
	}

	public void print(){
		super.print();
		System.out.println("METHOD CALL: Method Name: "+this.getMethodName()+" String Receiver: "+this.getStringReceiver()+" Type Qualified Name: "+this.getReceiverQualifiedName());
	}
}
