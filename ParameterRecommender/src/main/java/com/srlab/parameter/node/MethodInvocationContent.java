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
	
	private String name;
	private String methodName;
	private String receiver;
	private String receiverTypeQualifiedName;
	private String absStringRep;
	
	public MethodInvocationContent(MethodCallExpr mmi, MethodDeclaration md, MethodCallExpr mi){
		
		super(mi);
		this.name = mi.toString();
		this.methodName = mi.getName().getIdentifier();
		this.receiver = null;
		this.receiverTypeQualifiedName = null;
		this.absStringRep = null;		

		if(mi.getScope().isPresent()) {
			this.receiver = mi.getScope().toString();
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(mi.getScope().get());
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				this.receiverTypeQualifiedName = typeDescriptor.getTypeQualifiedName();
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getReceiver() {
		return receiver;
	}

	public String getReceiverTypeQualifiedName() {
		return receiverTypeQualifiedName;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}
	
	public void print() {
		System.out.println("MethodInvocationContent [name=" + name + ", methodName=" + methodName + ", receiver=" + receiver
				+ ", receiverTypeQualifiedName=" + receiverTypeQualifiedName + ", absStringRep=" + absStringRep + "]");
	}
}
