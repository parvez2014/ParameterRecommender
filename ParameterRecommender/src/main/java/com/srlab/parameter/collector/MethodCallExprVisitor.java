package com.srlab.parameter.collector;

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
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.config.Config;


public class MethodCallExprVisitor extends VoidVisitorAdapter<Void>{

	private CompilationUnit cu;
	
	public MethodCallExprVisitor(CompilationUnit _cu, String _path) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
	}
	
	
	public CompilationUnit getCu() {
		return cu;
	}
	
	public MethodDeclaration getMethodDeclarationContainer(Node node) {
		Optional<Node> parent = node.getParentNode();
		while(parent.isPresent() && ((parent.get() instanceof MethodDeclaration))==false){
			parent = parent.get().getParentNode();
		}
		if(parent.isPresent() && ((parent.get())instanceof MethodDeclaration)) {
			return (MethodDeclaration)parent.get();
		}
		else return null;
	}
		
	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		if(m.getScope().isPresent()) {
			
			//resolved the method binding
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = 
					JSSConfigurator.getInstance().getJpf().solve(m);
			if(resolvedMethodDeclaration.isSolved()) {
				String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName();
				
				//if this is a framework method call and the method has parameter we process it
				if(Config.isInteresting(methodQualifiedName)&&m.getArguments().size()>0) {
					
				}
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public List<ParameterModelEntry> getModelEntryList() {
		return modelEntryList;
	}

}
