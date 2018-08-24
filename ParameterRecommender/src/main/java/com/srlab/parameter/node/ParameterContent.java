package com.srlab.parameter.node;

import java.io.Serializable;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.binding.TypeResolver;
import com.srlab.parameter.category.ParameterExpressionCategorizer;

public class ParameterContent implements Serializable{
	protected String rawStringRep;
	protected ParameterContent parent;
	private String parameterExpressionType;
	protected String absStringRep; //this one does not transform/encode literal values
	protected String absStringRepWithLiteral; //this one encode literal values
	
	public ParameterContent(Expression expression) {
		this.rawStringRep = expression.toString();
		this.parameterExpressionType = ParameterExpressionCategorizer.getParameterExpressionType(expression);
	}
	
	public void print() {
		if(this instanceof StringLiteralContent) {
			 ((StringLiteralContent)this).print();
		}
		else if(this instanceof CharLiteralContent) {
			((StringLiteralContent)this).print();
		}
		else if(this instanceof NumberLiteralContent) {
			((CharLiteralContent)this).print();
		}
		else if(this instanceof NullLiteralContent) {
			((NullLiteralContent)this).print();
		}
		else if(this instanceof BooleanLiteralContent) {
			((BooleanLiteralContent)this).print();
		}
		else if(this instanceof ThisExpressionContent) {
		    ((ThisExpressionContent)this).print();
		}
		else if(this instanceof NameExprContent) {
		    ((NameExprContent)this).print();
		}
		else System.out.println("ParameterContent [stringParamNode=" + rawStringRep + ", parent=" + parent
				+ ", parameterExpressionType=" + parameterExpressionType + ", absStringRep=" + absStringRep
				+ ", partlyAbsStringRep=" + absStringRepWithLiteral + "]");
		
	}
	
	public String getAbsStringRepWithLiteral() {
		return absStringRepWithLiteral;
	}

	public String getRawStringRep() {
		return rawStringRep;
	}

	public String getAbsStringRep() {
		return absStringRep;
	}

	public ParameterContent getParent() {
		return parent;
	}

	public String getParameterExpressionType() {
		return parameterExpressionType;
	}

	public String getPartlyAbsStringRep() {
		return absStringRepWithLiteral;
	}

	public static ParameterContent get(Expression expression) {
		if (expression instanceof StringLiteralExpr) {
			return new StringLiteralContent((StringLiteralExpr)expression);
		} else if (expression instanceof NullLiteralExpr) {
			return new NullLiteralContent((NullLiteralExpr)expression);
		} else if (expression instanceof BooleanLiteralExpr) {
			return new BooleanLiteralContent((BooleanLiteralExpr)expression);
		} 
		else if(expression instanceof DoubleLiteralExpr) {
			return new NumberLiteralContent((DoubleLiteralExpr)expression);
		}
		else if(expression instanceof LongLiteralExpr) {
			return new NumberLiteralContent((LongLiteralExpr)expression);	
		}
		
		else if(expression instanceof IntegerLiteralExpr) {
			return new NumberLiteralContent((IntegerLiteralExpr)expression);	
		}
		else if (expression instanceof CharLiteralExpr) {
			return new CharLiteralContent((CharLiteralExpr)expression);
		} 
		else if(expression instanceof NameExpr) {
			return new NameExprContent((NameExpr)expression);
		}
		else if(expression instanceof FieldAccessExpr) {
			return new QualifiedNameContent((FieldAccessExpr)expression);
		}
		else if(expression instanceof ObjectCreationExpr) {
			return new ClassInstanceCreationContent((ObjectCreationExpr)expression);
		}
		else if(expression instanceof CastExpr) {
			return new CastExpressionContent((CastExpr)expression);
		}
		else if(expression instanceof MethodCallExpr) {
			return new MethodInvocationContent((MethodCallExpr)expression);
		}
		else if(expression instanceof ThisExpr) {
			return new ThisExpressionContent((ThisExpr)expression);
		}else {
			return new UnknownContent(expression);
		}
	}
	//The problem with Java symbol solver is that if the NameExpr indicates a type name, such as BufferedReder, it can solve the type of BufferedReader
		public String getAbsStringRep(Expression expression) {
			if (expression instanceof StringLiteralExpr) {
				return expression.toString();
			} else if (expression instanceof NullLiteralExpr) {
				return expression.toString();
			} else if (expression instanceof BooleanLiteralExpr) {
				return expression.toString();
			} 
			else if(expression instanceof DoubleLiteralExpr) {
				return expression.toString();
			}
			else if(expression instanceof LongLiteralExpr) {
				return expression.toString();	
			}
			
			else if(expression instanceof IntegerLiteralExpr) {
				return expression.toString();	
			}
		
			else if(expression instanceof CharLiteralExpr) {
				return expression.toString();
			}
			else if(expression instanceof NameExpr) {
				SimpleName sn = expression.asNameExpr().getName(); 
				String typeQualifiedName = TypeResolver.resolve(expression);
				return "SN:"+typeQualifiedName;
			}
			else if(expression instanceof CastExpr) {
				CastExpr ce = (CastExpr)expression;
				ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(ce.getType());
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
				return "("+typeDescriptor.getTypeQualifiedName()+")"+this.getAbsStringRep(ce.getExpression());
			}
			else if(expression instanceof ObjectCreationExpr) {
				try {
				ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr)expression;
				SymbolReference<ResolvedConstructorDeclaration> srResolvedConstructorDeclaration = JSSConfigurator.getInstance().getJpf().solve(objectCreationExpr);
				return "new_" + srResolvedConstructorDeclaration.getCorrespondingDeclaration().getClassName()+"("+")";
				}catch(Exception e) {
					return expression.toString();
				}
			}
			else if(expression instanceof MethodCallExpr) {
				MethodCallExpr methodCallExpr = (MethodCallExpr)expression;
				if(((MethodCallExpr) expression).getScope().isPresent()) {
					return this.getAbsStringRepWithLiteral(methodCallExpr.getScope().get())+"."+methodCallExpr.getName()+"("+")";
				}
				else {
					return methodCallExpr.getName()+"("+")";
				}
			}
			else if(expression instanceof FieldAccessExpr) {
				return expression.toString();
				
			} else if (expression instanceof ThisExpr) {
				return expression.toString();
			}
			else return null;
		}
	//The problem with Java symbol solver is that if the NameExpr indicates a type name, such as BufferedReder, it can solve the type of BufferedReader
	public String getAbsStringRepWithLiteral(Expression expression) {
		if (expression instanceof StringLiteralExpr) {
			return "String";
		} else if (expression instanceof NullLiteralExpr) {
			return "NULL";
		} else if (expression instanceof BooleanLiteralExpr) {
			return "Boolean";
		} 
		else if(expression instanceof DoubleLiteralExpr) {
			return "Number";
		}
		else if(expression instanceof LongLiteralExpr) {
			return "Number";	
		}
		
		else if(expression instanceof IntegerLiteralExpr) {
			return "Number";	
		}
	
		else if(expression instanceof CharLiteralExpr) {
			return "Char";
		}
		else if(expression instanceof NameExpr) {
			SimpleName sn = expression.asNameExpr().getName(); 
			String typeQualifiedName = TypeResolver.resolve(expression);
			return "SN:"+typeQualifiedName;
		}
		else if(expression instanceof CastExpr) {
			CastExpr ce = (CastExpr)expression;
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(ce.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			return "("+typeDescriptor.getTypeQualifiedName()+")"+this.getAbsStringRepWithLiteral(ce.getExpression());
		}
		else if(expression instanceof ObjectCreationExpr) {
			try {
			ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr)expression;
			SymbolReference<ResolvedConstructorDeclaration> srResolvedConstructorDeclaration = JSSConfigurator.getInstance().getJpf().solve(objectCreationExpr);
			return "new_" + srResolvedConstructorDeclaration.getCorrespondingDeclaration().getClassName()+"("+")";
			}catch(Exception e) {
				return expression.toString();
			}
		}
		else if(expression instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = (MethodCallExpr)expression;
			if(((MethodCallExpr) expression).getScope().isPresent()) {
				return this.getAbsStringRepWithLiteral(methodCallExpr.getScope().get())+"."+methodCallExpr.getName()+"("+")";
			}
			else {
				return methodCallExpr.getName()+"("+")";
			}
		}
		else if(expression instanceof FieldAccessExpr) {
			return expression.toString();
			/*FieldAccessExpr fieldAccessExpr = (FieldAccessExpr)expression;
			SimpleName simpleName = fieldAccessExpr.getName();
			JavaParserFacade jpf = JSSConfigurator.getInstance().getJpf();
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedValueDeclaration = jpf
					.solve(fieldAccessExpr.getName());
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedValueDeclaration.getCorrespondingDeclaration();
			ResolvedType resolvedType = resolvedValueDeclaration.getType();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			//System.out.println("FieldAccessExpr in ParameterContent: "+fieldAccessExpr.getScope());
			if(Character.isUpperCase(fieldAccessExpr.getScope().toString().charAt(0))&& Character.isUpperCase(fieldAccessExpr.getName().getIdentifier().charAt(0))) {
				return fieldAccessExpr.getScope().toString()+"."+fieldAccessExpr.getName().getIdentifier();
			}
			else if (Character.isUpperCase(fieldAccessExpr.getScope().toString().charAt(0))){
				return fieldAccessExpr.getScope().toString()+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
			}
			else return this.getAbsStringRepWithLiteral(fieldAccessExpr.getScope())+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
			*/
			 
			
		} else if (expression instanceof ThisExpr) {
			return expression.toString();
			// TODO Auto-generated method stub
			/*if (thisExpr.getClassExpr().isPresent()) {
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
						return thisExpr.toString();
				} else if (thisExpr.getClassExpr().get() instanceof FieldAccessExpr) { // a.b.this
					FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) thisExpr.getClassExpr().get();
					return this.getAbsStringRepWithLiteral(fieldAccessExpr)+"."+"this";
				} else
					return thisExpr.toString();
			}
			return thisExpr.toString();*/
		}
		else return null;
	}
	
	/*public String getAbsStringRep(Expression expression) {
		if (expression instanceof StringLiteralExpr) {
			return expression.toString();
		} else if(expression instanceof CharLiteralExpr) {
			return expression.toString(); 
		}
		else if (expression instanceof NullLiteralExpr) {
			return expression.toString();
		} else if (expression instanceof BooleanLiteralExpr) {
			return expression.toString();
		} 
		else if(expression instanceof DoubleLiteralExpr) {
			return expression.toString();
		}
		else if(expression instanceof LongLiteralExpr) {
			return expression.toString();	
		}
		else if(expression instanceof IntegerLiteralExpr) {
			return expression.toString();	
		}
		else if(expression instanceof NameExpr) {
			SimpleName sn = expression.asNameExpr().getName(); 
			return sn.getIdentifier();
		}
		else if(expression instanceof CastExpr) {
			CastExpr ce = (CastExpr)expression;
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(ce.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			return "("+typeDescriptor.getTypeQualifiedName()+")"+this.getAbsStringRep(ce.getExpression());
		}
		else if(expression instanceof ObjectCreationExpr) {
			try {
			ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr)expression;
			SymbolReference<ResolvedConstructorDeclaration> srResolvedConstructorDeclaration = JSSConfigurator.getInstance().getJpf().solve(objectCreationExpr);
			return "new " + srResolvedConstructorDeclaration.getCorrespondingDeclaration().getClassName()+"("+")";
			
			//System.out.println("Object Creation Expression: "+objectCreationExpr.getType());
			//if(objectCreationExpr.getScope().isPresent()==false) {
			//	ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(objectCreationExpr.getType());
			//	TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			//	return "new "+typeDescriptor.getTypeQualifiedName()+"("+")";
			//}
			//else {
			//	ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().getType(objectCreationExpr.getType());
			//	TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			//	return "new "+this.getAbsStringRep(objectCreationExpr.getScope().get())+"."+typeDescriptor.getTypeQualifiedName()+"("+")";
			//}
			}catch(Exception e) {
				return expression.toString();
			}
		}
		else if(expression instanceof MethodCallExpr) {
			MethodCallExpr methodCallExpr = (MethodCallExpr)expression;
			if(((MethodCallExpr) expression).getScope().isPresent()) {
				return this.getAbsStringRep(methodCallExpr.getScope().get())+"."+methodCallExpr.getName()+"("+")";
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
			System.out.println("FieldAccessExpr in ParameterContent: "+fieldAccessExpr.getScope());
			if(Character.isUpperCase(fieldAccessExpr.getScope().toString().charAt(0))&& Character.isUpperCase(fieldAccessExpr.getName().getIdentifier().charAt(0))) {
				return fieldAccessExpr.getScope().toString()+"."+fieldAccessExpr.getName().getIdentifier();
			}
			else if (Character.isUpperCase(fieldAccessExpr.getScope().toString().charAt(0))){
				return fieldAccessExpr.getScope().toString()+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
			}
			else return this.getAbsStringRepWithLiteral(fieldAccessExpr.getScope())+"."+"SN:"+typeDescriptor.getTypeQualifiedName();
		}
		
	    else if (expression instanceof ThisExpr) {
			ThisExpr thisExpr = (ThisExpr) expression;
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
						return nameExpr.getName().getIdentifier() + "." + "this";
					} else return thisExpr.toString();
				} else if (thisExpr.getClassExpr().get() instanceof FieldAccessExpr) { // a.b.this
					FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) thisExpr.getClassExpr().get();
					return this.getAbsStringRep(fieldAccessExpr)+"."+"this";
				} else
					return thisExpr.toString();
			}
			else  return thisExpr.toString();
	    }
		else throw new RuntimeException("Error in creating absStringRep of fieldAccessExpr: "+expression);
	}*/
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
