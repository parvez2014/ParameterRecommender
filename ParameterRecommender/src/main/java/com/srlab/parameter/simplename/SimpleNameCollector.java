package com.srlab.parameter.simplename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.binding.TypeDescriptor;
import com.srlab.parameter.completioner.SourcePosition;

public class SimpleNameCollector extends VoidVisitorAdapter<Void>{

	private MethodDeclaration methodDeclaration;
	private SourcePosition origin;
	private List<VariableEntity> localVariableEntities;
	private List<VariableEntity> fieldVariableEntities;
	private List<VariableEntity> inheritedVariableEntities;
	private List<VariableEntity> parameterVariableEntities;
	private List<VariableEntity> usedVariableEntities;
	
	private TypeDeclaration typeDeclaration;
	
	public SimpleNameCollector(MethodDeclaration _methodDeclaration, SourcePosition _sourcePosition) {
		this.methodDeclaration = _methodDeclaration;
		this.origin = _sourcePosition; //we need to collect variables that appears before this location
		this.localVariableEntities = new LinkedList();
		this.fieldVariableEntities = new LinkedList();
		this.inheritedVariableEntities = new LinkedList();
		this.parameterVariableEntities = new LinkedList();
		this.usedVariableEntities = new LinkedList();
	}
	
	public void collectParameters() {
		for(Parameter parameter:this.methodDeclaration.getParameters()) {
			ResolvedType resolvedType = JSSConfigurator.getInstance().getJpf().convertToUsage(parameter.getType());
			TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedType);
			
			SourcePosition sourcePosition = null;
			if(parameter.getBegin().isPresent() && parameter.getBegin().get().isBefore(origin)) {
				sourcePosition = new SourcePosition(parameter.getBegin().get());
				VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
						parameter.getName().getIdentifier(),
						VariableEntityCategory.PARAMETER, 
						VariableLocationCategory.PARAMETER_DECLARATION,
						sourcePosition); 
				this.parameterVariableEntities.add(variableEntity);
			}
		}
	}
	
	public void collectInheritedVariables(ResolvedReferenceTypeDeclaration resolvedReferenceTypeDeclaration){
		ArrayList<VariableEntity> classFieldVariableEntities = new ArrayList();
		for(ResolvedReferenceType rrt:resolvedReferenceTypeDeclaration.getAncestors()) {
			ResolvedReferenceTypeDeclaration rrtd = rrt.getTypeDeclaration();
			if(rrtd.isClass()) {
				for(ResolvedFieldDeclaration rfd:rrtd.getDeclaredFields()) {
					if(rfd.accessSpecifier()==AccessSpecifier.PUBLIC||rfd.accessSpecifier()==AccessSpecifier.PROTECTED) {
						TypeDescriptor typeDescriptor = new TypeDescriptor(rfd.getType());
						VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
										rfd.getName(),
										VariableEntityCategory.INHERITED_FIELD, 
										VariableLocationCategory.FIELD_DECLARATION, 
										null); 
						classFieldVariableEntities.add(variableEntity);
					}
				}
				if(classFieldVariableEntities.size()>0) {
					Collections.reverse(classFieldVariableEntities);
					this.inheritedVariableEntities.addAll(classFieldVariableEntities);
				}
			}
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
		
		if(variableDeclarator.getBegin().isPresent() && variableDeclarator.getBegin().get().isBefore(origin)) {
			sourcePosition = new SourcePosition(variableDeclarator.getBegin().get());
			VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),
							identifier,
							VariableEntityCategory.LOCAL, 
							VariableLocationCategory.LOCAL_DECLARATION, 
							sourcePosition); 
			localVariableEntities.add(variableEntity);
		}
	}

	@Override
	public void visit(NameExpr n, Void arg) {
		// TODO Auto-generated method stub
		super.visit(n, arg);

		SourcePosition sourcePosition = null;
		SimpleName simpleName = n.getName();
		if(simpleName.getBegin().isPresent() && simpleName.getBegin().get().isBefore(origin)) {
			sourcePosition = new SourcePosition(simpleName.getBegin().get());
			SymbolReference<? extends ResolvedValueDeclaration> srResolvedvalueDeclaration = JSSConfigurator.getInstance().getJpf().solve(simpleName);
			if(srResolvedvalueDeclaration.isSolved()) {
				ResolvedValueDeclaration resolvedValueDeclaration = srResolvedvalueDeclaration.getCorrespondingDeclaration();
				TypeDescriptor typeDescriptor = new TypeDescriptor(resolvedValueDeclaration.getType());
				
				if(n.getParentNode().isPresent() && n.getParentNode().get() instanceof AssignExpr &&
					((AssignExpr)n.getParentNode().get()).getTarget()==n) {
					
					VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),simpleName.getIdentifier(),
							VariableEntityCategory.UNKNOWN, VariableLocationCategory.ASSIGNMENT, sourcePosition); 	
					this.localVariableEntities.add(variableEntity);
				}
				else {
					VariableEntity variableEntity = new VariableEntity(typeDescriptor.getTypeQualifiedName(),simpleName.getIdentifier(),
							VariableEntityCategory.UNKNOWN, VariableLocationCategory.NAME_EXPR, sourcePosition); 	
					this.usedVariableEntities.add(variableEntity);
				}
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
		
		//Step-1: from method declaration collect typedeclaration
		Node parent = this.methodDeclaration;
		while(parent!=null && !(parent instanceof TypeDeclaration)) {
			parent = parent.getParentNode().get();
		}
		
		//Step-2: collect field variables
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
		}
	}
	public void collectLocalVariables() {
		//collect variables that appear in the method body
		this.methodDeclaration.accept(this, null);		
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