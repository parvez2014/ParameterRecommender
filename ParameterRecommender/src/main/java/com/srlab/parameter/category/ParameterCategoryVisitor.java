package com.srlab.parameter.category;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.binding.TypeResolver;
import com.srlab.parameter.config.Config;
import com.srlab.parameter.node.ParameterContent;


public class ParameterCategoryVisitor extends VoidVisitorAdapter<Void>{

	private CompilationUnit cu;
	private ParameterExpressionCategorizer parameterCategorizer;

	public ParameterCategoryVisitor(CompilationUnit _cu, ParameterExpressionCategorizer _paramCategorizer) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
		this.parameterCategorizer = _paramCategorizer;
	}

	public CompilationUnit getCu() {
		return cu;
	}
		
	public ParameterExpressionCategorizer getParamCategorizer() {
		return parameterCategorizer;
	}


	@Override
	public void visit(ArrayCreationExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
	}


	@Override
	public void visit(SuperExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
	}


	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);System.out.println("MethodCallExpr: "+m);
		try {
			if(m.getScope().isPresent()) {
				//resolved the method binding
				System.out.println("M: "+m);
				System.out.println("Scope: "+m.getScope().get()+"  Is Array Access "+(m.getScope().get() instanceof ArrayAccessExpr));
				System.out.println("Scope: "+m.getScope().get()+"  Resolve "+TypeResolver.resolve(m.getScope().get()));
				
				
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = 
						JSSConfigurator.getInstance().getJpf().solve(m);
				if(resolvedMethodDeclaration.isSolved()) {
					String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName();
					
					//if this is a framework method call and the method has parameter we process it
					if(m.getArguments().size()>0) {
						for(int i=0;i<m.getArguments().size();i++) {
							Expression expression = m.getArguments().get(i);
							System.out.println("**********Expression: "+expression+" TQN:"+TypeResolver.resolve(expression));
							if(expression instanceof FieldAccessExpr) {
								System.out.println("Field Access Expression: "+expression+" Type: "+TypeResolver.resolve(expression.asFieldAccessExpr().getScope()));	
								/*FieldAccessExpr fieldAccessExpr = (FieldAccessExpr)expression;
								SymbolReference<? extends ResolvedValueDeclaration> srReceiver = JSSConfigurator.getInstance().getJpf().solve(fieldAccessExpr.getScope());
								//ResolvedTypeDeclaration resolvedTypeDeclaration = srReceiver.getCorrespondingDeclaration().declaringType();
								System.out.println("Field ResolvedTypeDeclaration: "+srReceiver.isSolved());
								System.out.println("Field Scope is NameExpr: "+(fieldAccessExpr.getScope() instanceof NameExpr) +" Parent: "+fieldAccessExpr.getScope().getParentNode().get());
							*/	
								//System.out.println("Field Receiver QN: "+resolvedTypeDeclaration.getQualifiedName());
							}
							parameterCategorizer.add(m, m.getArguments().get(i),i);
						}
						
						for(Expression expression:m.getArguments()) {
							ParameterContent parameterContent = ParameterContent.get(expression);
							System.out.println("+++++++++++++++++===Expression: "+expression+"  Parameter Content: "+parameterContent.getStringRep(expression));
							System.out.println("Expression Type: "+parameterContent.getParameterExpressionType()+ "  Parameter Content: "+parameterContent.getAbsStringRep());
							
						}
					}
				}
			}
		}
		catch(java.lang.RuntimeException e) {
			System.out.println("Error in binding method: "+m);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
