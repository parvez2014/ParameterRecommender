package com.srlab.parameter.node;

import java.io.Serializable;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;

public class ParameterContent implements Serializable{
	protected ParameterContent receiver;
	protected String stringParamNode;

	public ParameterContent getReceiver() {
		return receiver;
	}

	public String getStringParamNode() {
		return stringParamNode;
	}

	public void print() {
		System.out.println("String Param Node: " + this.stringParamNode);
	}

	public ParameterContent() {

	}
	public ParameterContent(Node node) {
		stringParamNode = node.toString();
	}

	/*public void processReceiver(MethodCallExpr mi, MethodDeclaration md,
			Node node) {
		if (node instanceof SimpleName) {
			receiver = new SimpleNameContent(mi, md, (SimpleName) node);
		} else if (node instanceof Name) {
			receiver = new QualifiedNameContent(mi, md,
					(Name) node);
		} else if (node instanceof MethodCallExpr) {
			receiver = new MethodInvocationContent(mi, md,
					(MethodCallExpr)node);
		} else if (node instanceof ObjectCreationExpr) {
			receiver = new ClassInstanceCreationContent(mi, md,
					(ObjectCreationExpr) node);
		} else if (node instanceof CastExpr) {
			receiver = new CastExpressionContent(mi, md,
					(CastExpr) node);
		} else {
			receiver = null;
		}
	}

	
	
	
	public  String getStringRep(Node node) {

		if (expression instanceof SimpleName &&((SimpleName) expression).resolveTypeBinding()!=null)
			return ((SimpleName) expression).resolveTypeBinding()
					.getQualifiedName();
		else if (expression instanceof QualifiedName) {
			return ((QualifiedName) expression).toString();
		} else if (expression instanceof NumberLiteral
				|| expression instanceof StringLiteral
				|| expression instanceof NullLiteral
				|| expression instanceof ThisExpression) {
			return expression.toString();
		} else if (expression instanceof ClassInstanceCreation) {
			ClassInstanceCreation ci = ((ClassInstanceCreation) expression);
			return "new " + ci.getType().toString();
		} else if (expression instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) expression;

			if (mi.getExpression() != null && mi.getExpression() instanceof SimpleName) {
				SimpleName sn = (SimpleName) mi.getExpression();

				if (sn.resolveBinding() != null && sn.resolveBinding().getKind() == IBinding.TYPE) {
					return sn.resolveTypeBinding().getQualifiedName() + "."
							+ mi.getName().getFullyQualifiedName();

				} else if (sn.resolveBinding() != null && sn.resolveBinding().getKind() == IBinding.METHOD) {
				} else if (sn.resolveTypeBinding() != null
						&& sn.resolveBinding() != null
						&& sn.resolveBinding().getKind() == IBinding.VARIABLE) {
					return (sn.resolveTypeBinding().getQualifiedName() + "." + mi
							.getName());
				}

			} else if (mi.getExpression() != null && mi.getExpression() instanceof MethodInvocation) {
				return "" + this.getStringRep(mi.getExpression()) + "."
						+ mi.getName();
			}

			return mi.toString();
		} else if (expression instanceof CastExpression) {
			CastExpression cast = (CastExpression) expression;
			return cast.getType().toString()
					+ "."
					+ this.getStringRep(((CastExpression) expression)
							.getExpression());
		} else {
			return expression.toString();
		}
	}*/
}
