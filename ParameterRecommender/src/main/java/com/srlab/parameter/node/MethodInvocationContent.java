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
import com.srlab.parameter.binding.TypeResolver;

public class MethodInvocationContent extends ParameterContent{
	
	private String methodName;
	private String receiver;
	private String receiverTypeQualifiedName;
	
	public MethodInvocationContent(MethodCallExpr mi){
		super(mi);
		this.parent = null;
		this.receiver = null;
		this.receiverTypeQualifiedName = null;

		this.methodName = mi.getName().getIdentifier();
		this.absStringRep = this.getAbsStringRep(mi);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(mi);
		
		if(mi.getScope().isPresent()) {
			this.receiver = mi.getScope().toString();
			this.receiverTypeQualifiedName = TypeResolver.resolve( mi.getScope().get());
			this.parent = ParameterContent.get(mi.getScope().get());
		}
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

	@Override
	public String toString() {
		return "MethodInvocationContent [name=" + this.getRawStringRep() + ", methodName=" + methodName + ", receiver=" + receiver
				+ ", receiverTypeQualifiedName=" + receiverTypeQualifiedName + ", absStringRep=" + absStringRep + "]";
	}
	
	
}
