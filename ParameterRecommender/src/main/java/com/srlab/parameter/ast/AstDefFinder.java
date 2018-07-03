package com.srlab.parameter.ast;

import java.util.LinkedList;
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

import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
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
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.completioner.MethodCallEntity;
import com.srlab.parameter.completioner.MethodDeclarationEntity;

public class AstDefFinder extends VoidVisitorAdapter<Void> {

	private MethodCallEntity definingMethodCallEntity; // if there is a method call, super method call, or class
														// instance creation that create the method
	private DefinitionType definitionType; // definition type

	private final String varname; // variable name

	private Position position;
	private final MethodDeclaration methodDeclaration; // method declaration containing the method call
	private final JavaParserFacade javaParserFacade;
	private List<MethodCallEntity> methodCallEntities; // methods that are called on the receiver variable

	public DefinitionType getDefinitionType() {
		return definitionType;
	}

	public AstDefFinder(final String _varname, Position _position, MethodDeclaration _methodDeclaration,
			JavaParserFacade _javaParserFacade) {
		this.varname = _varname;
		this.position = _position; // position of the node that triggers this AstdefFinder
		this.javaParserFacade = _javaParserFacade;
		this.methodDeclaration = _methodDeclaration;
		this.methodCallEntities = new LinkedList();

		this.definingMethodCallEntity = null;
		this.definitionType = DefinitionType.UNKNOWN;

		this.methodDeclaration.accept(this, null);
	}

	public MethodCallEntity getDefiningMethodCallEntity() {
		return definingMethodCallEntity;
	}

	public String getVarname() {
		return varname;
	}

	public Position getPosition() {
		return position;
	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public JavaParserFacade getJavaParserFacade() {
		return javaParserFacade;
	}

	public List<MethodCallEntity> getMethodCallEntities() {
		return methodCallEntities;
	}

	private void registerMethodCall(MethodCallEntity _methodCallEntity) {
		this.methodCallEntities.add(_methodCallEntity);
	}

	private void registerMethodCallAndDefinition(DefinitionType _definitionType, MethodCallEntity _methodCallEntity,
			Position newPosition) {
		if (_definitionType == DefinitionType.PARAMETER) {
			this.definitionType = _definitionType;
		} else if (this.definingMethodCallEntity == null && newPosition.isBefore(position)) {
			this.definitionType = _definitionType;
			this.definingMethodCallEntity = _methodCallEntity;
		} else if (this.definingMethodCallEntity != null && newPosition.isBefore(position)) {
			Position curDefiningPosition = this.definingMethodCallEntity.getPosition();

			if (newPosition.isAfter(curDefiningPosition)) {
				this.definingMethodCallEntity = _methodCallEntity;
				this.definitionType = _definitionType;
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
		return varname.equals(name);
	}

	private boolean isThis() {
		return "this".equals(varname) || "".equals(varname);
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

	private boolean isStatic(final MethodCallExpr call) {
		SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = javaParserFacade.solve(call);
		if (resolvedMethodDeclaration.isSolved()) {
			return resolvedMethodDeclaration.getCorrespondingDeclaration().isStatic();
		}
		return false;
	}

	@Override
	public void visit(AssignExpr assignExpr, Void arg) {
		// TODO Auto-generated method stub
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
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade
					.solve(expression.asMethodCallExpr());
			if (resolvedMethodDeclaration.isSolved()) {
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

				MethodDeclarationEntity methodDeclarationEntity;
				try {
					methodDeclarationEntity = MethodDeclarationEntity
							.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade);
					MethodCallEntity methodCallEntity = new MethodCallEntity(, isSuper,
							isThis, expression.asMethodCallExpr().getName().getBegin(), methodDeclarationEntity);
					this.registerMethodCallAndDefinition(DefinitionType.METHOD_RETURN, methodCallEntity,
							expression.asMethodCallExpr().getName().getBegin().get());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} else if (expression.isObjectCreationExpr()) {
			SymbolReference<ResolvedConstructorDeclaration> resolvedConstructorDeclaration = this.javaParserFacade
					.solve(expression.asObjectCreationExpr());
			if (resolvedConstructorDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity = MethodDeclarationEntity
						.get(resolvedConstructorDeclaration.getCorrespondingDeclaration(), javaParserFacade);
				MethodCallEntity methodCallEntity = new MethodCallEntity(Optional.empty(), false,
						false, expression.asMethodCallExpr().getName().getBegin(), methodDeclarationEntity);
				this.registerMethodCallAndDefinition(DefinitionType.METHOD_RETURN, methodCallEntity,
						expression.asMethodCallExpr().getName().getBegin().get());
			}
		} else if (expression.isNameExpr()) {
			// e.g. int j=anotherValue;
			// some alias thing...
			// it might be that we found an assignment before and this simpleName is just
			// "$missing". Then ignore this
			if (definitionType == null) {
				SimpleName simpleName = (SimpleName) expression.asNameExpr().getName();
				SymbolReference<? extends ResolvedValueDeclaration> resolvedValueDeclaration = this.javaParserFacade
						.solve(expression.asNameExpr());
				if (resolvedValueDeclaration.isSolved()) {
					this.registerMethodCallAndDefinition(DefinitionType.LOCAL, null, expression.getBegin().get());
				}
			}
		}
	}

	@Override
	public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		if (n.isThis() && varname.equals("this") || varname.equals("")) {
			SymbolReference<? extends ResolvedConstructorDeclaration> resolvedConstructorDeclaration = this.javaParserFacade
					.solve(n);
			if (resolvedConstructorDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity = MethodDeclarationEntity
						.get(resolvedConstructorDeclaration.getCorrespondingDeclaration(), javaParserFacade);
				MethodCallEntity methodCallEntity = new MethodCallEntity(Optional.empty(), false,
						true, n.getBegin(), methodDeclarationEntity);
				this.registerMethodCall(methodCallEntity);
			}
		} else if (varname.equals("super")) {
			SymbolReference<? extends ResolvedConstructorDeclaration> resolvedConstructorDeclaration = this.javaParserFacade
					.solve(n);
			if (resolvedConstructorDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity = MethodDeclarationEntity
						.get(resolvedConstructorDeclaration.getCorrespondingDeclaration(), javaParserFacade);
				MethodCallEntity methodCallEntity = new MethodCallEntity(Optional.empty(), true,
						false, n.getBegin(), methodDeclarationEntity);
				this.registerMethodCall(methodCallEntity);
			}
		}
	}

	@Override
	public void visit(MethodCallExpr m, Void arg) {
		// TODO Auto-generated method stub
		super.visit(m, arg);
		boolean isSuper = false;
		boolean isThis = false;

		if (m.getScope().isPresent() && m.getScope().get() instanceof NameExpr) {
			NameExpr nameExpr = m.getScope().get().asNameExpr();
			if (this.matchesVarName(nameExpr.getName())) {
				definitionType = DefinitionType.METHOD_RETURN;
				SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade.solve(m);
				if (m.getScope().get() instanceof SuperExpr) {
					isSuper = true;
				} else if (m.getScope().get() instanceof ThisExpr) {
					isThis = true;
				} else {
					isThis = true;
				}
				if (resolvedMethodDeclaration.isSolved()) {
					MethodDeclarationEntity methodDeclarationEntity;
					try {
						methodDeclarationEntity = MethodDeclarationEntity
								.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade);
						MethodCallEntity methodCallEntity = new MethodCallEntity(Optional.empty(),
								isSuper, isThis, m.getName().getBegin(), methodDeclarationEntity);
						this.registerMethodCall(methodCallEntity);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else if (m.getScope().isPresent() && m.getScope().get() instanceof SuperExpr && varname.equals("super")) {
			definitionType = DefinitionType.METHOD_RETURN;
			if (m.getScope().get() instanceof SuperExpr) {
				isSuper = true;
			} else if (m.getScope().get() instanceof ThisExpr) {
				isThis = true;
			} else {
				isThis = true;
			}
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade.solve(m);
			if (resolvedMethodDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity;
				try {
					methodDeclarationEntity = MethodDeclarationEntity
							.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade);
					MethodCallEntity methodCallEntity = new MethodCallEntity(Optional.empty(), isSuper,
							isThis, m.getName().getBegin(), methodDeclarationEntity);
					this.registerMethodCall(methodCallEntity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} else if (isThis()) {
			definitionType = DefinitionType.METHOD_RETURN;
			SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = this.javaParserFacade.solve(m);
			if (resolvedMethodDeclaration.isSolved()) {
				MethodDeclarationEntity methodDeclarationEntity;
				try {
					methodDeclarationEntity = MethodDeclarationEntity
							.get(resolvedMethodDeclaration.getCorrespondingDeclaration(), javaParserFacade);
					MethodCallEntity methodCallEntity = new MethodCallEntity(Optional.empty(), true,
							false, m.getName().getBegin(), methodDeclarationEntity);
					this.registerMethodCall(methodCallEntity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	public void visit(Parameter n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
		if (this.matchesVarName(n.getName())) {
			System.out.println("Its a parameter: for " + varname);
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
		for (VariableDeclarator v : node.getVariables()) {
			evaluateVariableDeclarationFragment(v);
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
