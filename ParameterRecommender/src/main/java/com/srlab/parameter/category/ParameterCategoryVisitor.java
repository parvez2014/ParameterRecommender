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
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.config.Config;


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
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		try {
			if(m.getScope().isPresent()) {
				//resolved the method binding
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = 
						JSSConfigurator.getInstance().getJpf().solve(m);
				if(resolvedMethodDeclaration.isSolved()) {
					String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName();
					
					//if this is a framework method call and the method has parameter we process it
					if(Config.isInteresting(methodQualifiedName)&&m.getArguments().size()>0) {
						for(int i=0;i<m.getArguments().size();i++) {
							parameterCategorizer.add(m, m.getArguments().get(i),i);
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
