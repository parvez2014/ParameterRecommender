package com.srlab.parameter.ast;

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
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.completioner.SourcePosition;
import com.srlab.parameter.config.Config;

public class AstDefFinderTestVisitor extends VoidVisitorAdapter<Void> {

	private CompilationUnit cu;

	public AstDefFinderTestVisitor(CompilationUnit _cu, String _path) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
	}

	public CompilationUnit getCu() {
		return cu;
	}

	public MethodDeclaration getMethodDeclarationContainer(Node node) {
		Optional<Node> parent = node.getParentNode();
		while (parent.isPresent() && ((parent.get() instanceof MethodDeclaration)) == false) {
			parent = parent.get().getParentNode();
		}
		if (parent.isPresent() && ((parent.get()) instanceof MethodDeclaration)) {
			return (MethodDeclaration) parent.get();
		} else
			return null;
	}

	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		MethodDeclaration methodDeclaration = null;
		// collect method declaration
		Optional<Node> parent = m.getParentNode();
		while (parent.isPresent() && !(parent.get() instanceof MethodDeclaration)) {
			parent = parent.get().getParentNode();
		}

		if (parent.isPresent() && parent.get() instanceof MethodDeclaration) {
			methodDeclaration = (MethodDeclaration) parent.get();
		}

		if (m.getScope().isPresent()) {

			// resolved the method binding

			try {
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = JSSConfigurator.getInstance()
						.getJpf().solve(m);
				if (resolvedMethodDeclaration.isSolved()) {
					String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration()
							.getQualifiedName();
					// if this is a framework method call and the method has parameter we process it
					if (Config.isInteresting(methodQualifiedName)) {

						if (m.getScope().get() instanceof NameExpr) {
							String varname = m.getScope().get().asNameExpr().getName().getIdentifier();
							Position position = m.getScope().get().asNameExpr().getBegin().get();
							System.out.println("+++++++++Method Call Expression: " + m + " +++++++++++++++++++");
							AstDefFinderWithoutBinding astDefFinder = new AstDefFinderWithoutBinding(varname, position,
									methodDeclaration, JSSConfigurator.getInstance().getJpf());
							astDefFinder.print();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
