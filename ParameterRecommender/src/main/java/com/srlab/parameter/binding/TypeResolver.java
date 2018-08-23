package com.srlab.parameter.binding;

import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;

public class TypeResolver {

	public static String resolve(Expression expression) {
		
		if(expression instanceof ArrayAccessExpr) {
			return TypeResolver.resolve(((ArrayAccessExpr) expression).getName());
		}
		else if(expression instanceof ArrayCreationExpr) {
			ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr)expression;
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convert(arrayCreationExpr.createdType(),arrayCreationExpr);
			return TypeDescriptor.resolveTypeQualifiedName(resolvedType);
		}
		else if(expression instanceof ArrayInitializerExpr) {
			//you need to identify the list of array initializer expression and resolve them separately
			throw new RuntimeException("cannot resolve binding of array initializer expression: "+expression);
		}
		else if(expression instanceof FieldAccessExpr) {
			SymbolReference<? extends ResolvedFieldDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(expression.asFieldAccessExpr());
			ResolvedFieldDeclaration resolvedFieldDeclaration = sr.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedFieldDeclaration.getType();
			return TypeDescriptor.resolveTypeQualifiedName(resolvedType);
		}
		else if(expression instanceof LiteralExpr) {
			LiteralExpr literalExpr = expression.asLiteralExpr();
			SymbolReference<? extends ResolvedValueDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(literalExpr);
			ResolvedValueDeclaration resolvedValueDeclaration = sr.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			return TypeDescriptor.resolveTypeQualifiedName(resolvedType);
		}
		else if(expression instanceof MethodCallExpr) { //we resolved the return type of the method. It can be void too
			MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
			SymbolReference<ResolvedMethodDeclaration>  sr = JSSConfigurator.getInstance().getJpf().solve(methodCallExpr);
			ResolvedMethodDeclaration resolvedMethodDeclaration = sr.getCorrespondingDeclaration();
			return TypeDescriptor.resolveTypeQualifiedName(resolvedMethodDeclaration.getReturnType());
		}
		else if(expression instanceof NameExpr) {
			//a name can be i or can be BorderLayout.center. Second one is not supportred so we need to process additional steps 
			NameExpr nameExpr = expression.asNameExpr();
			Optional<Node> parent = nameExpr.getParentNode();
			
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = JSSConfigurator.getInstance().getJpf().solve(expression.asNameExpr());
			if(srResolvedValueDeclaration.isSolved()==false) {
				if(parent.isPresent() && parent.get() instanceof FieldAccessExpr) {
					FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) parent.get();
					SymbolReference<? extends ResolvedFieldDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(fieldAccessExpr);
					ResolvedTypeDeclaration resolvedTypeDeclaration = sr.getCorrespondingDeclaration().declaringType();
					return resolvedTypeDeclaration.getQualifiedName();
				}
				else if(parent.isPresent() && parent.get() instanceof MethodCallExpr) {
					MethodCallExpr methodCallExpr = (MethodCallExpr) parent.get();
					SymbolReference<ResolvedMethodDeclaration>  sr = JSSConfigurator.getInstance().getJpf().solve(methodCallExpr);
					ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration = sr.getCorrespondingDeclaration().declaringType();
					return resolvedReferenceTypeDeclaration.getQualifiedName();
				}
				else {
					throw new RuntimeException("cannot resolve the name expression: " + expression);
				}
			}
			else {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
				ResolvedType resolvedType = resolvedValueDeclaration.getType();
				return TypeDescriptor.resolveTypeQualifiedName(resolvedType);
			}
		}
		else if(expression instanceof SuperExpr) {
			SuperExpr superExpr = expression.asSuperExpr();
			TypeDescriptor typeDescriptor = new TypeDescriptor(JSSConfigurator.getInstance().getJpf().getType(superExpr));
			return typeDescriptor.getTypeQualifiedName();
		}
		else if(expression instanceof ThisExpr) {
			//An occurrence of the "this" keyword.
			//World.this.greet() is a MethodCallExpr of method name greet, and scope "World.super" which is a ThisExpr with classExpr "World".
			//this.name is a FieldAccessExpr of field greet, and a ThisExpr as its scope. The ThisExpr has no classExpr.
			SymbolReference<? extends ResolvedTypeDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(expression.asThisExpr());
			ResolvedTypeDeclaration resolvedTypeDeclaration = sr.getCorrespondingDeclaration();
			return resolvedTypeDeclaration.getQualifiedName();
		}
		
		else if(expression instanceof ObjectCreationExpr) {
			SymbolReference<? extends ResolvedConstructorDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(expression.asObjectCreationExpr());
			ResolvedConstructorDeclaration resolvedConstructorDeclaration = sr.getCorrespondingDeclaration();
			if(resolvedConstructorDeclaration.getPackageName()!=null) {
				return resolvedConstructorDeclaration.getPackageName()+"."+resolvedConstructorDeclaration.getClassName();
			}
			else {
				return resolvedConstructorDeclaration.getClassName();
			}
		}
		else {
			//any other thing we are trying to resolve as is and it may throw exception
			TypeDescriptor typeDescriptor = new TypeDescriptor(JSSConfigurator.getInstance().getJpf().getType(expression));
			return typeDescriptor.getTypeQualifiedName();
		}
	}
}
