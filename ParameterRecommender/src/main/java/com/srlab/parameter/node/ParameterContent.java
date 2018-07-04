package com.srlab.parameter.node;

import java.io.Serializable;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class ParameterContent implements Serializable{
	protected String stringParamNode;

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

	public String getStringRep(Expression expression) {
		if (expression instanceof StringLiteralExpr) {
			return expression.toString();
		} else if (expression instanceof NullLiteralExpr) {
			return expression.toString();
		} else if (expression instanceof BooleanLiteralExpr) {
			return expression.toString();
		} 
		else if(expression instanceof NameExpr) {
			SimpleName sn = expression.asNameExpr().getName(); 
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration  = jpf.solve(sn);
			if(srResolvedValueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				return "SN"+":"+typeDescriptor.getTypeQualifiedName();
			}
			else return null;
		}
		else if(expression instanceof CastExpr) {
			CastExpr ce = (CastExpr)expression;
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(ce.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			return "("+typeDescriptor.getTypeQualifiedName()+")"+this.getStringRep(ce.getExpression());
		}
		else if(expression instanceof ObjectCreationExpr) {
			ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr)expression;
			if(objectCreationExpr.getScope().isPresent()==false) {
				ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(objectCreationExpr.getType());
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				return "new "+typeDescriptor.getTypeQualifiedName()+"("+")";
			}
			else {
				return this.getStringRep(objectCreationExpr.getScope().get())+"."+"";
			}
		}
		else if(expression instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = (MethodCallExpr)expression;
			if(((MethodCallExpr) expression).getScope().isPresent()) {
				return this.getStringRep(methodCallExpr.getScope().get())+"."+methodCallExpr.getName()+"("+")";
			}
			else {
				return methodCallExpr.getName()+"("+")";
			}
		}
		else if(expression instanceof FieldAccessExpr) {
			
			FieldAccessExpr fieldAccessExpr = (FieldAccessExpr)expression;
			SimpleName simpleName = fieldAccessExpr.getName();
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
					.solve(fieldAccessExpr.getName());
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);

			return this.getStringRep(fieldAccessExpr.getScope())+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
		}
		else if(expression instanceof CharLiteralExpr) {
			return expression.toString();
		} else if (expression instanceof ThisExpr) {
			ThisExpr thisExpr = (ThisExpr) expression;
			// TODO Auto-generated method stub
			if (thisExpr.getClassExpr().isPresent()) {
				if (thisExpr.getClassExpr().get() instanceof ClassExpr) { // Example: Worls.this
					return thisExpr.getClassExpr().get().toString() + "." + "this";
				} else if (thisExpr.getClassExpr().get() instanceof NameExpr) { // Example: m.this;
					NameExpr nameExpr = (NameExpr) thisExpr.getClassExpr().get();
					JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
					SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
							.solve(nameExpr);
					if (srResolvedValueDeclaration.isSolved()) {
						ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration
								.getCorrespondingDeclaration();
						ResolvedType resolvedType = resolvedValueDeclaration.getType();
						TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
						return "SN:" + typeDescriptor.getTypeQualifiedName() + "." + "this";
					} else
						return null;
				} else if (thisExpr.getClassExpr().get() instanceof FieldAccessExpr) { // a.b.this
					FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) thisExpr.getClassExpr().get();
				} else
					return null;
			}

			return thisExpr.toString();
		}
		else return null;
	}

	/*public  String getStringRep(Node node) {

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
