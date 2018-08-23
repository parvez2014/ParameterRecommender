package com.srlab.parameter.binding;

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
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.ast.CompilationUnitCollector;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.binding.TypeResolver;
import com.srlab.parameter.config.Config;
import com.srlab.parameter.node.BooleanLiteralContent;
import com.srlab.parameter.node.CastExpressionContent;
import com.srlab.parameter.node.CharLiteralContent;
import com.srlab.parameter.node.ClassInstanceCreationContent;
import com.srlab.parameter.node.MethodInvocationContent;
import com.srlab.parameter.node.NameExprContent;
import com.srlab.parameter.node.NullLiteralContent;
import com.srlab.parameter.node.NumberLiteralContent;
import com.srlab.parameter.node.ParameterContent;
import com.srlab.parameter.node.QualifiedNameContent;
import com.srlab.parameter.node.StringLiteralContent;
import com.srlab.parameter.node.ThisExpressionContent;
import com.srlab.parameter.node.UnknownContent;


public class BindingTestVisitor extends VoidVisitorAdapter<Void>{

	private CompilationUnit cu;

	public BindingTestVisitor(CompilationUnit _cu) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
		cu.accept(this, null);
	}

	public CompilationUnit getCu() {
		return cu;
	}
		
	@Override
	public void visit(ArrayCreationExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		System.out.println("ArrayCreationExpression: "+n +" Binding: "+TypeResolver.resolve(n));
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
				System.out.println("MethodCallExpr: "+m +" Scope: "+m.getScope().get());
				System.out.println("Receiver Binding: "+TypeResolver.resolve(m.getScope().get()));
			}
		}
		catch(java.lang.RuntimeException e) {
			System.out.println("Error in binding method: "+m);
		}
	}

	@Override
	public void visit(FieldAccessExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		System.out.println("FieldAccessExpr: "+n +" Binding: "+TypeResolver.resolve(n)+" Scope: "+n.getScope()+" Binding: "+TypeResolver.resolve(n.getScope()));

	}

	@Override
	public void visit(ArrayInitializerExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		System.out.println("ArrayInitializerExpression: "+n +" Binding: "+TypeResolver.resolve(n));
	}

	@Override
	public void visit(ObjectCreationExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		System.out.println("ObjectCreationExpression: "+n +" Binding: "+TypeResolver.resolve(n));
	}

	@Override
	public void visit(ThisExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		System.out.println("ThisExpression: "+n +" Binding: "+TypeResolver.resolve(n));
		if(n.getClassExpr().isPresent()) {
			System.out.println("This Expression Class Expression: "+n.getClassExpr().get()+" Binding: "+TypeResolver.resolve(n.getClassExpr().get()));
		}

	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSSConfigurator.getInstance().init("/home/parvez/research/ParameterCompletion/parameterCompletionWorkspace/BindingTest",Config.EXTERNAL_DEPENDENCY_PATH, true);
		CompilationUnitCollector cuc = new CompilationUnitCollector();
		for(CompilationUnit cu:cuc.collectCompilationUnits(new File("/home/parvez/research/ParameterCompletion/parameterCompletionWorkspace/BindingTest"))) {
			BindingTestVisitor bindingTestVisitor = new BindingTestVisitor(cu);
		}
	}
}
