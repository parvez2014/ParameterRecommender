package com.srlab.parameter.ast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/*** Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */

import com.github.javaparser.Position;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;

//Given a list of variable names, it return all methods that are called on those variables in sorted order including the definition or constructor calls
public class AstDefFinderWithoutBinding extends VoidVisitorAdapter<Void> {

	//definingMethodCallEntity  is a method call, super method call, or class instance creation that create the method
	private String definingMethodCall;
	private final HashMap<String,Position> hmMethodCallPosition;
	private Position definingMethodCallPosition;
	private DefinitionType definitionType; // definition type
	private HashSet<String> methodCalls; // methods that are called on the receiver variable

	private final HashSet<String> varnameSet; // variable name
	private Position position;	
	private final CallableDeclaration<?> callableDeclaration; // method declaration containing the method call
	private final JavaParserFacade javaParserFacade;
	
	public DefinitionType getDefinitionType() {
		return definitionType;
	}

	public AstDefFinderWithoutBinding(final HashSet<String> _varnameSet, Position _position, CallableDeclaration<?> _callableDeclaration,
			JavaParserFacade _javaParserFacade) {
		this.varnameSet = _varnameSet;
		this.position = _position; // position of the node that triggers this AstdefFinder
		this.javaParserFacade = _javaParserFacade;
		this.callableDeclaration = _callableDeclaration;
		this.methodCalls = new HashSet();

		this.definingMethodCall = null;
		this.definingMethodCallPosition = null;
		this.definitionType = DefinitionType.UNKNOWN;
		this.hmMethodCallPosition = new HashMap();
		//System.out.print("MethodDeclaration: "+methodDeclaration);
		this.callableDeclaration.accept(this, null);
	}

	public String getDefiningMethodCall() {
		return definingMethodCall;
	}

	public HashSet<String> getVarnameSet() {
		return this.varnameSet;
	}

	public Position getPosition() {
		return position;
	}

	public CallableDeclaration<?> getMethodDeclaration() {
		return callableDeclaration;
	}

	public JavaParserFacade getJavaParserFacade() {
		return javaParserFacade;
	}

	//we ignore duplicate method calls
	public List<String> getMethodCalls() {
		//convert hash set to string
		List<String> methodCallList = new ArrayList(this.methodCalls);
		methodCallList.sort(new Comparator<String>() {

			public int compare(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Position p1 = hmMethodCallPosition.get(arg0);
				Position p2 = hmMethodCallPosition.get(arg1);
				if(p1.isAfter(p2)) return 1;
				else if(p2.isAfter(p1))return -1;
				else return 0;
			}
		});
		return methodCallList;
	}

	private void registerMethodCall(String _methodCall, Position newPosition) {
	
		if(this.position.isAfter(newPosition) && this.methodCalls.contains(_methodCall)==false) {
			this.methodCalls.add(_methodCall);
			hmMethodCallPosition.put(_methodCall,newPosition);
		}
	}

	private void registerMethodCallAndDefinition(DefinitionType _definitionType, String _methodCall,
			Position newPosition) {
		if (_definitionType == DefinitionType.PARAMETER) {
			this.definitionType = _definitionType;
		} else if (this.definingMethodCall == null && newPosition.isBefore(position)) {
			this.definitionType = _definitionType;
			this.definingMethodCall = _methodCall;
			this.definingMethodCallPosition = newPosition;
			if(_methodCall!=null) this.registerMethodCall(_methodCall, newPosition);
		} else if (this.definingMethodCall != null && newPosition.isBefore(position)) {
			if (newPosition.isAfter(definingMethodCallPosition)) {
				this.definingMethodCall = _methodCall;
				this.definitionType = _definitionType;
				this.definingMethodCallPosition = newPosition;
				if(_methodCall!=null) this.registerMethodCall(_methodCall, newPosition);
			}
		}
	}

	private boolean receiverExpressionMatchesVarname(final Expression exp) {
		if (exp == null) {
			return false;
		}

		// both simple name and qualified name are name expressions
		if (exp.isNameExpr()) {
			NameExpr nameExpr = (NameExpr) exp;
			return matchesVarName(nameExpr.getName());
		} else if (exp.isThisExpr()) {
			// do we look for this?
			return isThis();
		} else
			return false;
	}

	private boolean matchesVarName(final SimpleName node) {
		if (node == null) {
			return false;
		}
		final String name = node.asString();
		return varnameSet.contains(name);
	}

	private boolean isThis() {
		return varnameSet.contains("this") || varnameSet.contains("");
	}

	private boolean isReceiverThis(final MethodCallExpr mi) {
		// standard case:
		if (mi.getScope().isPresent() == false && !isStatic(mi)) {
			return true;
		}
		// qualified call: this.method()
		if (mi.getScope().isPresent() == true && mi.getScope().get() instanceof ThisExpr) {
			return true;
		}
		return false;
	}

	private boolean isStatic(final MethodCallExpr methodCallExpr) {
		if(Character.isUpperCase(methodCallExpr.getName().getIdentifier().charAt(0)))
			return true;
		else return false;
	}
	
	@Override
	public void visit(AssignExpr assignExpr, Void arg) {
		// TODO Auto-generated method stub
		
		//System.out.println("Assignment Expr: "+assignExpr);
		super.visit(assignExpr, arg);

		final Expression lhs = assignExpr.getTarget();
		if (lhs == null) {
			return;
		}

		if (lhs.isNameExpr()) {
			final SimpleName n = lhs.asNameExpr().getName();
			if (!matchesVarName(n)) {
				return;
			}
			return;
		}
		final Expression rhs = assignExpr.getValue();
		evaluateRightHandSideExpression(rhs);
	}

	// called only if left hand side was a match.
	private void evaluateRightHandSideExpression(final Expression expression) {
		if (expression.isCastExpr()) {
			// re-evaluate using the next expression:
			evaluateRightHandSideExpression(expression.asCastExpr().getExpression());
		} else if (expression.isMethodCallExpr()) {
			// x = some().method().call()
			MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
			
			boolean isSuper = false;
				boolean isThis = false;
				if (methodCallExpr.getScope().isPresent()) {
					if (methodCallExpr.getScope().get() instanceof SuperExpr) {
						isSuper = true;
					} else if (methodCallExpr.getScope().get() instanceof ThisExpr) {
						isThis = true;
					}
				} else {
					isThis = true;
				}
			
		} else if (expression.isObjectCreationExpr()) {
			this.registerMethodCallAndDefinition(DefinitionType.NEW, expression.asObjectCreationExpr().getType().asString(),
					expression.asObjectCreationExpr().getType().getBegin().get());
		} else if (expression.isNameExpr()) {
			// e.g. int j=anotherValue;
			// some alias thing...
			// it might be that we found an assignment before and this simpleName is just
			// "$missing". Then ignore this
			if (definitionType == null) {
				SimpleName simpleName = (SimpleName) expression.asNameExpr().getName();
				this.registerMethodCallAndDefinition(DefinitionType.LOCAL, null, expression.getBegin().get());
			}
		}
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		if (n.isThis() && varnameSet.contains("this") || varnameSet.contains("")) {
			if(n.getBegin().isPresent()) this.registerMethodCall("this",n.getBegin().get());
		} else if (varnameSet.equals("super")) {
			if(n.getBegin().isPresent()) this.registerMethodCall("super",n.getBegin().get());
		}
	}

	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		boolean isSuper = false;
		boolean isThis = false;

		//System.out.println("MethodCallExpr:::: "+m+ " "+m.getName().getBegin().get());
		
		if (m.getScope().isPresent() && m.getScope().get() instanceof NameExpr) {
			NameExpr nameExpr = m.getScope().get().asNameExpr();
			if (this.matchesVarName(nameExpr.getName())) {
				//definitionType = DefinitionType.METHOD_RETURN;
				if (m.getScope().get() instanceof SuperExpr) {
					isSuper = true;
				} else if (m.getScope().get() instanceof ThisExpr) {
					isThis = true;
				} else {
					isThis = true;
				}
				if(m.getName().getBegin().isPresent())
					this.registerMethodCall(m.getName().getIdentifier(),m.getName().getBegin().get());
			}
		} else if (m.getScope().isPresent() && m.getScope().get() instanceof SuperExpr && varnameSet.equals("super")) {
			//definitionType = DefinitionType.METHOD_RETURN;
			if (m.getScope().get() instanceof SuperExpr) {
				isSuper = true;
			} else if (m.getScope().get() instanceof ThisExpr) {
				isThis = true;
			} else {
				isThis = true;
			}
			if(m.getName().getBegin().isPresent())
			this.registerMethodCall(m.getName().getIdentifier(),m.getName().getBegin().get());

		} else if (isThis()) {
			//definitionType = DefinitionType.METHOD_RETURN;
			if(m.getName().getBegin().isPresent())
			this.registerMethodCall(m.getName().getIdentifier(),m.getName().getBegin().get());
			
		}
	}

	@Override
	public void visit(Parameter n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		if (this.matchesVarName(n.getName())) {
			//System.out.println("Its a parameter: for " + varnameSet);
			this.definitionType = DefinitionType.PARAMETER;
		}
	}

	private void evaluateVariableDeclarationFragment(final VariableDeclarator f) {

		final SimpleName name = f.getName();
		if (matchesVarName(name) && f.getInitializer().isPresent()) {
			final Expression expression = f.getInitializer().get();
			if (expression != null) {
				evaluateRightHandSideExpression(expression);
			}
		}
	}

	public void visit(VariableDeclarationExpr node, Void arg) {
		super.visit(node, arg);
		for (VariableDeclarator v : node.getVariables()) {
			evaluateVariableDeclarationFragment(v);
		}
	}
	
	public void print() {
		System.out.println("Variable Name: " + this.varnameSet + "Position: "+this.getPosition());
		System.out.println("Defining Method Call Entity: "+definingMethodCall);
		System.out.println("Definition Type: "+this.definitionType);
		System.out.println("Method Called On Receiver Variables:" );
		for(String mc:this.getMethodCalls()) {
			System.out.println(mc);
		}
	}
	/**
	 * Specifies how the variable under examination was defined (field, parameter,
	 * by method return...).
	 */
	public enum DefinitionType {
		/**
		 * indicates that the variable was defined by a method return value, e.g, int x
		 * = p.getX();
		 */
		METHOD_RETURN,
		/**
		 * indicates that the variable was defined by a constructor call, e.g, Point p =
		 * new Point(x,y);
		 */
		NEW,
		/**
		 * indicates that the variable was defined by a field declaration
		 */
		FIELD,
		/**
		 * indicates that the variable was declared as a parameter of the enclosing
		 * method.
		 */
		PARAMETER,
		/**
		 * indicates that the variable represents "this"
		 */
		THIS,
		/**
		 * indicates that the variable was defined as a local variable. Usually this
		 * value gets replaced by more specific values {@link #NEW}, or
		 * {@link #METHOD_RETURN} if possible.
		 */
		LOCAL,
		/**
		 * indicates that the variable was defined in an unexpected or unsupported and
		 * yet unhandled way.
		 */
		UNKNOWN,
	}

}
