package com.srlab.parameter.ast;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.config.Config;

public class AstDefFinderTestVisitor extends VoidVisitorAdapter<Void> {

	private CompilationUnit cu;
	private HashMap<String,String> hmKeyword;
	
	public AstDefFinderTestVisitor(CompilationUnit _cu) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
		cu.accept(this,null);
		this.hmKeyword = new HashMap();
		this.initKeywords();
	}
	private void initKeywords(){
		
		String keywords[]={"String","abstract",	"continue",	"goto",	"package",	"switch",
		"assert",	"default",	"if",	"this",
		"boolean",	"do","implements"	,"throw",
		"break",	"double",	"import",	"throws",
		"byte",	"else",	"instanceof",	"return",	"transient",
		"case",	"extends",	"int",	"short",	"try",
		"catch",	"final",	"interface",	"static",	"void",
		"char",	"finally",	"long",	"strictfp",	"volatile",
		"class",	"native",	"super",	"while",
		"const",	"for",	"new",	"synchronized"};
		for(String k:keywords){
			hmKeyword.put(k, k);
		}
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
	/*public List<String> getTokens(Expression expression){
		List<String> tokenList = new ArrayList();
		if(expression.getTokenRange().isPresent()) {
			Iterator<JavaToken> iterator = 	expression.getTokenRange().get().iterator();
			while(iterator.hasNext()) {
				JavaToken javaToken = iterator.next();
				if(hmKeyword.containsKey(javaToken.asString())) {
					tokenList.add(javaToken.asString());
				}
				else if(javaToken.getKind()==JavaToken.Kind.IDENTIFIER.getKind()) {
					if(Character.isUpperCase(javaToken.asString().charAt(0))) {
						tokenList.add(javaToken.asString());
					}
					else if(javaToken.getNextToken().isPresent()&& javaToken.getKind()==JavaToken.Kind.LBRACE.getKind()) {
						tokenList.add(javaToken.asString());	
					}
				}
			}
		}
		return tokenList;
	}
	public List<String> collectAstContext(MethodCallExpr methodCallExpr) {
		List<String> astContextList = new ArrayList();
		Node  parent = methodCallExpr;
		while(parent!=null) {
			if(parent instanceof MethodCallExpr) {
				astContextList.add(methodCallExpr.getName().getIdentifier());
			}
			else if(parent instanceof MethodDeclaration) {
				
				MethodDeclaration methodDeclaration = (MethodDeclaration)parent;
				astContextList.add(methodDeclaration.getName().getIdentifier());
				
				for(Parameter parameter:methodDeclaration.getParameters()) {
					astContextList.add(parameter.getType().toString());
				}
			}
			else if(parent instanceof IfStmt) {
				astContextList.add("if");
				IfStmt ifStmt = ((IfStmt) parent);
				Expression expression = ifStmt.getCondition();
				astContextList.addAll(this.getTokens(expression));
			}
			else if(parent instanceof ForeachStmt) {
				astContextList.add("for");
				ForeachStmt foreachStmt = ((ForeachStmt) parent);
				astContextList.addAll(this.getTokens(foreachStmt.getVariable()));
			}
			else if(parent instanceof ForStmt) {
				astContextList.add("for");
				ForStmt forStmt = ((ForStmt) parent);
				for(Expression expression:forStmt.getInitialization()) {
					astContextList.addAll(this.getTokens(expression));
				}
				if(forStmt.getCompare().isPresent()) {
					astContextList.addAll(this.getTokens(forStmt.getCompare().get()));		
				}
				for(Expression expression:forStmt.getUpdate()) {
					astContextList.addAll(this.getTokens(expression));		
				}
			}
			else if(parent instanceof WhileStmt) {
				astContextList.add("while");
				WhileStmt whileStmt = ((WhileStmt) parent);
				astContextList.addAll(this.getTokens(whileStmt.getCondition()));
			}
			else if(parent instanceof TryStmt) {
				astContextList.add("Try");
				TryStmt tryStmt = ((TryStmt) parent);
				if(tryStmt.getResources().size()>0) {
					for(Expression expression:tryStmt.getResources()) {
						astContextList.addAll(this.getTokens(expression));
					}
				}
			}
			else if(parent instanceof CatchClause) {
				CatchClause catchClause = (CatchClause)parent;
				astContextList.add(catchClause.getParameter().getType().toString());
			}
			else if(parent instanceof ObjectCreationExpr) {
				ObjectCreationExpr objectCreationExpr = ((ObjectCreationExpr) parent);
				astContextList.addAll(this.getTokens(objectCreationExpr));
			}
			else if(parent instanceof VariableDeclarationExpr) {
				VariableDeclarationExpr variableDeclarationExpr = ((VariableDeclarationExpr) parent);
				astContextList.addAll(this.getTokens(variableDeclarationExpr));
			}
			else {
				System.out.println("Current: "+parent);
			}
			if(parent.getParentNode().isPresent()) {
				parent = parent.getParentNode().get();
			}
			else {
				parent = null;
			}
		}
		return astContextList;
	}*/
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
			System.out.println("M: "+m+"   Parent: "+m.getParentNode().get());
			try {
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = JSSConfigurator.getInstance()
						.getJpf().solve(m);
				if (resolvedMethodDeclaration.isSolved()) {
					String methodQualifiedName = resolvedMethodDeclaration.getCorrespondingDeclaration()
							.getQualifiedName();
					// if this is a framework method call and the method has parameter we process it
					if (Config.isInteresting(methodQualifiedName)) {
						AstContextCollector astContextCollector = new AstContextCollector();
						List<String> list = astContextCollector.collectAstContext(m,m.getName().getBegin().get());
						System.out.println("MethodCallExpr: "+m+ "    AST Context List: " + list);
						
						/*if (m.getScope().get() instanceof NameExpr) {
							String varname = m.getScope().get().asNameExpr().getName().getIdentifier();
							HashSet<String> varnameSet = new HashSet();
							varnameSet.add(varname);
							varnameSet.add("label");
							Position position = m.getScope().get().asNameExpr().getBegin().get();
							System.out.println("+++++++++Method Call Expression: " + m + " +++++++++++++++++++");
							AstDefFinderWithoutBinding astDefFinder = new AstDefFinderWithoutBinding(varnameSet, position,
									methodDeclaration, JSSConfigurator.getInstance().getJpf());
							astDefFinder.print();
						}*/
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSSConfigurator.getInstance().init("/home/parvez/research/ParameterCompletion/repository/jedit-svn", Config.EXTERNAL_DEPENDENCY_PATH);
		CompilationUnitCollector cuc = new CompilationUnitCollector();
		List<CompilationUnit> cuList = cuc.collectCompilationUnits(new File("/home/parvez/research/ParameterCompletion/repository/jedit-svn"));
		for(int i=0;i<cuList.size();i++) {
			System.out.println("Progress: " + i+" "+cuList.size());
			AstDefFinderTestVisitor astDefFinder = new AstDefFinderTestVisitor(cuList.get(i));
		}
	}

}
