package com.srlab.parameter.simplename;

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


public class SimpleNameRecommendationTestVisitor extends VoidVisitorAdapter<Void>{

	private CompilationUnit cu;
	
	public SimpleNameRecommendationTestVisitor(CompilationUnit _cu, String _path) {
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
					if (Config.isInteresting(methodQualifiedName) && m.getArguments().size() > 0) {
						for (Expression exp : m.getArguments()) {
							if (methodDeclaration != null && exp instanceof NameExpr) {
								NameExpr nameExpr = (NameExpr) exp;
								SimpleName sn = nameExpr.getName();
								SymbolReference<? extends ResolvedValueDeclaration> srResolvedvalueDeclaration = JSSConfigurator
										.getInstance().getJpf().solve(sn);
								if (srResolvedvalueDeclaration.isSolved()) {
									ResolvedValueDeclaration resolvedValueDeclaration = srResolvedvalueDeclaration
											.getCorrespondingDeclaration();
									TypeDescriptor typeDescriptor = new TypeDescriptor(
											resolvedValueDeclaration.getType());
									if (typeDescriptor.isReferenceType()) {
										String qualifiedName = typeDescriptor.getTypeQualifiedName();
										String varName = sn.getIdentifier();

										System.out.println("This is a reference type: " + exp + " " + qualifiedName
												+ "  varName: " + varName);

										SimpleNameCollector snCollector = new SimpleNameCollector(methodDeclaration,
												new SourcePosition(exp.getBegin().get()));
										snCollector.run();
										SimpleNameRecommender snRecommender = new SimpleNameRecommender(snCollector,
												varName, qualifiedName);
										List<String> list = snRecommender.recommend();
										System.out.println("Recommendations: " + list);
									} else if (typeDescriptor.isReference()) {
										System.out.println("This is a reference: " + exp + " " + typeDescriptor);
									}
								}
							}
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
