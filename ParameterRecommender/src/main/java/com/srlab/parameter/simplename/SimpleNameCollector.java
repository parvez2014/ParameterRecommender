package com.srlab.parameter.simplename;

import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.completioner.SourcePosition;

public class SimpleNameCollector extends VoidVisitorAdapter<Void>{

	private MethodDeclaration methodDeclaration;
	private MethodCallExpr methodCallExpr;
	private List<VariableEntity> localVariableEntities;
	private List<VariableEntity> fieldVariableEntities;
	private List<VariableEntity> inheritedVariableEntities;
	private List<VariableEntity> parameterVariableEntities;
	private TypeDeclaration typeDeclaration;
	
	public SimpleNameCollector(MethodDeclaration _methodDeclaration, MethodCallExpr _methodCallExpr) {
		this.methodDeclaration = _methodDeclaration;
		this.methodCallExpr = _methodCallExpr;
		
		this.localVariableEntities = new LinkedList();
		this.fieldVariableEntities = new LinkedList();
		this.inheritedVariableEntities = new LinkedList();
		this.parameterVariableEntities = new LinkedList();
	}
	
	public void collectParameters() {
		for(Parameter parameter:this.methodDeclaration.getParameters()) {
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convertToUsage(parameter.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			
			SourcePosition sourcePosition = null;
			if(parameter.getBegin().isPresent()) {
				sourcePosition = new SourcePosition(parameter.getBegin().get());
			}
				VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),parameter.getName().getIdentifier(),
					VariableEntityCategory.PARAMETER, VariableLocationCategory.PARAMETER_DECLARATION,sourcePosition); 
				this.parameterVariableEntities.add(variableEntity);
		}
	}
	
	@Override
	public void visit(VariableDeclarationExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);
	}

	@Override
	public void visit(VariableDeclarator variableDeclarator, Void arg) {
		// TODO Auto-generated method stub
		super.visit(variableDeclarator, arg);
		String identifier = variableDeclarator.getName().getIdentifier();
		
		SourcePosition sourcePosition = null;
		ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convertToUsage(variableDeclarator.getType());
		TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
		
		if(variableDeclarator.getBegin().isPresent()) {
			sourcePosition = new SourcePosition(variableDeclarator.getBegin().get());
		}
		VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),identifier,
				VariableEntityCategory.LOCAL, VariableLocationCategory.LOCAL_DECLARATION, sourcePosition); 
		localVariableEntities.add(variableEntity);
		
	}

	@Override
	public void visit(NameExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);

		SimpleName simpleName = n.getName();
		SourcePosition sourcePosition = null;
		SymbolReference<? extends ResolvedValueDeclaration> srResolvedvalueDeclaration = JSSConfigurator.getInstance().getJpf().solve(simpleName);
		if(srResolvedvalueDeclaration.isSolved()) {
			ResolvedValueDeclaration resolvedValueDeclaration = srResolvedvalueDeclaration.getCorrespondingDeclaration();
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedValueDeclaration.getType());
			if(simpleName.getBegin().isPresent()) {
				sourcePosition = new SourcePosition(simpleName.getBegin().get());
			}
			if(n.getParentNode().isPresent() && n.getParentNode().get() instanceof AssignExpr &&
				((AssignExpr)n.getParentNode().get()).getTarget()==n) {
				
				VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),simpleName.getIdentifier(),
						VariableEntityCategory.LOCAL, VariableLocationCategory.ASSIGNMENT,sourcePosition); 	
				this.localVariableEntities.add(variableEntity);
			}
			else {
				VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),simpleName.getIdentifier(),
						VariableEntityCategory.LOCAL, VariableLocationCategory.NAME_EXPR,sourcePosition); 	
				this.localVariableEntities.add(variableEntity);
			}
		}
	}

	@Override
	public void visit(AssignExpr n, Void arg) {
		// TODO Auto-generated method stub
		System.out.println("Asignment: "+n);		
		super.visit(n, arg);
	}

	public void collectFieldVariables() {
		Node parent = this.methodDeclaration;
		while(parent!=null && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParentNode().get();
		}
		if(parent!=null && parent instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration)parent;
			
			List<BodyDeclaration> bdList = td.getMembers();
			for(BodyDeclaration bd:bdList) {
				if(bd instanceof FieldDeclaration) {
					FieldDeclaration fd =(FieldDeclaration)bd;
					for(VariableDeclarator vd:fd.getVariables()) {
						
						String identifier = vd.getName().getIdentifier();
					
						SourcePosition sourcePosition = null;
						ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convertToUsage(vd.getType());
						TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
					
						if(vd.getBegin().isPresent()) {
							sourcePosition = new SourcePosition(vd.getBegin().get());
						}
						VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),identifier,
							VariableEntityCategory.FIELD, VariableLocationCategory.FIELD_DECLARATION,sourcePosition); 
						fieldVariableEntities.add(variableEntity);
					}
				}
			}
			for(VariableEntity ve:this.getFieldVariableEntities()) {
				System.out.println("Field: "+ve.toString());
			}
		}
	}
	public void collectLocalVariables() {
		this.methodDeclaration.accept(this, null);
		for(VariableEntity ve:this.getLocalVariableEntities()) {
			System.out.println("LOCAL: "+ve.toString());
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public List<VariableEntity> getLocalVariableEntities() {
		return localVariableEntities;
	}

	public List<VariableEntity> getFieldVariableEntities() {
		return fieldVariableEntities;
	}

	public List<VariableEntity> getInheritedVariableEntities() {
		return inheritedVariableEntities;
	}

	public List<VariableEntity> getParameterVariableEntities() {
		return parameterVariableEntities;
	}

}
