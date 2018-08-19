package com.srlab.parameter.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.github.javaparser.JavaToken;
import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class AstContextCollector {
	private HashMap<String, String> hmKeyword;

	public AstContextCollector() {
		this.hmKeyword = new HashMap<String, String>();
		this.initKeywords();
	}

	private void initKeywords() {

		String keywords[] = { "String", "abstract", "continue", "goto", "package", "switch", "assert", "default", "if",
				"this", "boolean", "do", "implements", "throw", "break", "double", "import", "throws", "byte", "else",
				"instanceof", "return", "transient", "case", "extends", "int", "short", "try", "catch", "final",
				"interface", "static", "void", "char", "finally", "long", "strictfp", "volatile", "class", "native",
				"super", "while", "const", "for", "new", "synchronized" };
		for (String k : keywords) {
			hmKeyword.put(k, k);
		}
	}

	public List<String> getTokens(Node node, Position position) {
		List<String> tokenList = new ArrayList<String>();
		if (node.getTokenRange().isPresent()) {
			Iterator<JavaToken> iterator = node.getTokenRange().get().iterator();
			while (iterator.hasNext()) {
				JavaToken javaToken = iterator.next();
				if (javaToken.getRange().isPresent() && javaToken.getRange().get().begin.isBefore(position)) {
					if (hmKeyword.containsKey(javaToken.asString())) {
						tokenList.add(javaToken.asString());
					} else if (javaToken.getKind() == JavaToken.Kind.IDENTIFIER.getKind()) {
			
						if (Character.isUpperCase(javaToken.asString().charAt(0))) {
							tokenList.add(javaToken.asString());
						
						} else if (javaToken.getNextToken().isPresent()
								&& javaToken.getNextToken().get().getKind() == JavaToken.Kind.LPAREN.getKind()) {
							tokenList.add(javaToken.asString());
						}
					}
				}
			}
		}
		return tokenList;
	}

	public List<String> collectAstContext(MethodCallExpr _methodCallExpr, Position position) {
		List<String> astContextList = new ArrayList<String>();
		Node parent = _methodCallExpr;
		while (parent != null) {
			if (parent instanceof MethodCallExpr) {
				MethodCallExpr methodCallExpr = (MethodCallExpr) parent;
				if(methodCallExpr.getScope().isPresent())
				astContextList.addAll(this.getTokens(methodCallExpr.getScope().get(), position));
				astContextList.addAll(this.getTokens(methodCallExpr.getName(), position));
				for(Expression expression: methodCallExpr.getArguments()) {
					astContextList.addAll(this.getTokens(expression, position));
				}
			} else if (parent instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) parent;
				astContextList.addAll(this.getTokens(methodDeclaration.getName(), position));
				for (Parameter parameter : methodDeclaration.getParameters()) {
					astContextList.addAll(this.getTokens(parameter, position));
				}
			} else if (parent instanceof IfStmt) {
				astContextList.add("if");
				IfStmt ifStmt = ((IfStmt) parent);
				Expression expression = ifStmt.getCondition();
				astContextList.addAll(this.getTokens(expression, position));
			} else if (parent instanceof ForeachStmt) {
				astContextList.add("for");
				ForeachStmt foreachStmt = ((ForeachStmt) parent);
				astContextList.addAll(this.getTokens(foreachStmt.getVariable(), position));
			} else if (parent instanceof ForStmt) {
				astContextList.add("for");
				ForStmt forStmt = ((ForStmt) parent);
				for (Expression expression : forStmt.getInitialization()) {
					astContextList.addAll(this.getTokens(expression, position));
				}
				if (forStmt.getCompare().isPresent()) {
					astContextList.addAll(this.getTokens(forStmt.getCompare().get(), position));
				}
				for (Expression expression : forStmt.getUpdate()) {
					astContextList.addAll(this.getTokens(expression, position));
				}
			} else if (parent instanceof WhileStmt) {
				astContextList.add("while");
				WhileStmt whileStmt = ((WhileStmt) parent);
				astContextList.addAll(this.getTokens(whileStmt.getCondition(), position));
			} else if (parent instanceof TryStmt) {
				astContextList.add("try");
				TryStmt tryStmt = ((TryStmt) parent);
				if (tryStmt.getResources().size() > 0) {
					for (Expression expression : tryStmt.getResources()) {
						astContextList.addAll(this.getTokens(expression, position));
					}
				}
			} else if (parent instanceof CatchClause) {
				CatchClause catchClause = (CatchClause) parent;
				astContextList.add("catch");
				astContextList.addAll(this.getTokens(catchClause.getParameter(), position));
			} else if (parent instanceof ObjectCreationExpr) {
				ObjectCreationExpr objectCreationExpr = ((ObjectCreationExpr) parent);
				
				astContextList.addAll(this.getTokens(objectCreationExpr.getType().getName(), position));
				for(Expression expression:objectCreationExpr.getArguments()) {
					astContextList.addAll(this.getTokens(expression, position));
				}
			} else if (parent instanceof VariableDeclarationExpr) {
				VariableDeclarationExpr variableDeclarationExpr = ((VariableDeclarationExpr) parent);
				astContextList.addAll(this.getTokens(variableDeclarationExpr, position));
			} 
			else if(parent instanceof FieldAccessExpr) {

			}
			else if (parent instanceof ClassOrInterfaceDeclaration) {
				ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration)parent;
				astContextList.add(classOrInterfaceDeclaration.getName().getIdentifier());
				for(ClassOrInterfaceType classOrInterfaceType: classOrInterfaceDeclaration.getExtendedTypes()) {
					astContextList.add(classOrInterfaceType.asString());
				}
				for(ClassOrInterfaceType classOrInterfaceType: classOrInterfaceDeclaration.getImplementedTypes()) {
					astContextList.add(classOrInterfaceType.asString());
				}
			}
			
			//now go to the parent node
			if (parent.getParentNode().isPresent()) {
				parent = parent.getParentNode().get();
			} else {
				parent = null;
			}
		}
		//if the same item appear multiple times, we preserve just one
		/*List<String> filteredAstContextList = new ArrayList();
		for(int i=0;i<astContextList.size();i++) {
			if(i>0 && astContextList.get(i).equals(astContextList.get(i-1))) {
				//do nothing
			}
			else filteredAstContextList.add(astContextList.get(i));		
		}*/
		//remove duplicates
		List<String> filteredAstContextList = new ArrayList(new HashSet(astContextList));
		Collections.reverse(filteredAstContextList);
		return filteredAstContextList;
	}
	
	public static void main(String args[]) {
		List<String> list = new ArrayList();
		list.add("iterator");
		list.add("iterator");
		list.add("iterator1");
		list.add("iterator1");
		list.add("iterator2");
		List<String> filteredAstContextList = new ArrayList(new HashSet(list));
		
		System.out.println("List: "+filteredAstContextList);
	}

}
