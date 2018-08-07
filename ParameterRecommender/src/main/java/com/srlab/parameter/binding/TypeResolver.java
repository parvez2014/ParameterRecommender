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
		
		/*if(expression instanceof AnnotationExpr) {
			
		}*/
		if(expression instanceof ArrayAccessExpr) {
			System.out.println("Type of Array Access Expr: "+JSSConfigurator.getInstance().getJpf().getType(expression.asArrayAccessExpr()));
			System.out.println("ArrayAccessExpr: "+expression+"  Name: "+expression.asArrayAccessExpr().getName()+"  Index:"+expression.asArrayAccessExpr().getIndex());
			return TypeResolver.resolve(((ArrayAccessExpr) expression).getName());
		}
		else if(expression instanceof ArrayCreationExpr) {
			ArrayCreationExpr arrayCreationExpr = (ArrayCreationExpr)expression;
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convert(arrayCreationExpr.createdType(),arrayCreationExpr);
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			return typeDescriptor.getTypeQualifiedName();
			//System.out.println("ArrayAccessExpr: "+expression+" Resolved Type Type: "+typeDescriptor.getTypeQualifiedName());
		}
		else if(expression instanceof ArrayInitializerExpr) {
			ResolvedType rt = JSSConfigurator.getInstance().getJpf().getType(expression.asArrayInitializerExpr());
			TypeDescriptor typeDescriptor = new TypeDescriptor(rt);
			return typeDescriptor.getTypeQualifiedName();
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
		else if(expression instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = expression.asMethodCallExpr();
			SymbolReference<ResolvedMethodDeclaration>  sr = JSSConfigurator.getInstance().getJpf().solve(methodCallExpr);
			ResolvedMethodDeclaration resolvedMethodDeclaration = sr.getCorrespondingDeclaration();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedMethodDeclaration.getReturnType());
			return typeDescriptor.getTypeQualifiedName();
		}
		else if(expression instanceof NameExpr) {
			System.out.println("Calling Name Expr: "+expression.asNameExpr());
			//System.out.println("Resolved Type: "+JSSConfigurator.getInstance().getJpf().getType(expression.asNameExpr()));
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
			//System.out.println("Super Expr: "+superExpr+JSSConfigurator.getInstance().getJpf().getType(superExpr));
		}
		else if(expression instanceof ThisExpr) {
			SymbolReference<? extends ResolvedTypeDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(expression.asThisExpr());
			ResolvedTypeDeclaration resolvedTypeDeclaration = sr.getCorrespondingDeclaration();
			return resolvedTypeDeclaration.getQualifiedName();
		}
		//else if(expression instanceof VariableDeclarationExpr) {
		//	
		//}
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
			TypeDescriptor typeDescriptor = new TypeDescriptor(JSSConfigurator.getInstance().getJpf().getType(expression));
			return typeDescriptor.getTypeQualifiedName();
			
			/*SymbolReference<? extends ResolvedValueDeclaration> sr = JSSConfigurator.getInstance().getJpf().solve(expression);
			ResolvedValueDeclaration resolvedValueDeclaration = sr.getCorrespondingDeclaration();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedValueDeclaration.getType());
			return typeDescriptor.getTypeQualifiedName();*/
		}
		//else {
		//	throw new RuntimeException("Cannot Resolve Expression Type");
		//}
		return null;
	}
}
