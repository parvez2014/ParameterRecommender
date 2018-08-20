package com.srlab.parameter.node;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;

public class ThisExpressionContent extends ParameterContent{
	private String classQualifier;
	private String classQualifiedName;
	private String thisQualifiedName;
	public ThisExpressionContent(ThisExpr thisExpr) {
		super(thisExpr);
		this.absStringRep = this.getAbsStringRep(thisExpr);
		this.absStringRepWithLiteral = this.getAbsStringRepWithLiteral(thisExpr);
		this.parent = null;

		try {
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			if (thisExpr.getClassExpr().isPresent()) {
				this.classQualifier = thisExpr.getClassExpr().get().toString();

				SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
						.solve(thisExpr.getClassExpr().get());
				if (srResolvedValueDeclaration.isSolved()) {
					ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration
							.getCorrespondingDeclaration();
					ResolvedType resolvedType = resolvedValueDeclaration.getType();
					TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
					this.classQualifiedName = typeDescriptor.getTypeQualifiedName();
				}
				this.parent = ParameterContent.get(thisExpr.getClassExpr().get());
			} else {
				this.classQualifier = null;
				this.classQualifiedName = null;
				this.parent = null;
			}

			SymbolReference<? extends ResolvedTypeDeclaration> srResolvedTypeDeclaration = jpf.solve(thisExpr);
			if (srResolvedTypeDeclaration.isSolved()) {
				ResolvedTypeDeclaration resolvedTypeDeclaration = srResolvedTypeDeclaration
						.getCorrespondingDeclaration();
				this.thisQualifiedName = resolvedTypeDeclaration.getQualifiedName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getClassQualifier() {
		return classQualifier;
	}
	public String getClassQualifiedName() {
		return classQualifiedName;
	}
	public String getThisQualifiedName() {
		return thisQualifiedName;
	}
	public void print(){
		System.out.print("THIS Name: "+this.getRawStringRep()+" ClassQualifier: "+this.getClassQualifier()+" ClassQN: "+this.getClassQualifiedName()+" AbsStrRep: "+this.getAbsStringRep());
	}
}
