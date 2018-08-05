package com.srlab.parameter.completioner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.SymbolSolver;

public class MethodCallEntity implements Serializable{

	private String receiverQualifiedName;
	private boolean isSuper;
	private boolean isThis;
	private SourcePosition position;
	private MethodDeclarationEntity methodDeclarationEntity;
	
	public MethodCallEntity(Optional<String> _receiver, boolean _isSuper, boolean _isThis,
			Optional<Position> _position, MethodDeclarationEntity _methodDeclarationEntity) {
		this.receiverQualifiedName = (_receiver.isPresent()?_receiver.get():null);
		this.isSuper = _isSuper;
		this.isThis = _isThis;
		this.position = (_position.isPresent()?new SourcePosition(_position.get()):null);
		this.methodDeclarationEntity = _methodDeclarationEntity;
	}

	public static MethodCallEntity get(MethodCallExpr methodCallExpr, ResolvedMethodDeclaration resolvedMethodDeclaration, JavaParserFacade jpf)throws Exception{
		boolean isSuper = false;
		boolean isThis = false;
		Optional<String> receiver = Optional.empty();
		Optional<String> innerReceiver = Optional.empty();
		MethodDeclarationEntity methodDeclarationEntity = MethodDeclarationEntity.get(resolvedMethodDeclaration, jpf);
	
		if(methodCallExpr.getScope().isPresent()) {
			if(methodCallExpr.getScope().get() instanceof SuperExpr) {
				isSuper = true;
			}
			else if(methodCallExpr.getScope().get() instanceof ThisExpr) {
				isThis = true;
			}
			Expression receiverExpression = methodCallExpr.getScope().get();
			ResolvedType resolvedReceiverType = jpf.getType(receiverExpression);
			//System.out.println("ResolvedReceiverType: "+resolvedReceiverType+"  isReferenceTYpe: "+resolvedReceiverType.isReferenceType());
			if(resolvedReceiverType.isReferenceType()) {
				receiver = Optional.of(resolvedReceiverType.asReferenceType().getQualifiedName());
				MethodCallEntity methodCallEntity = new MethodCallEntity(receiver,isSuper,isThis,methodCallExpr.getBegin(),methodDeclarationEntity);
				return methodCallEntity;
			}
			
		}else {
			isThis = true;
			MethodCallEntity methodCallEntity = new MethodCallEntity(receiver,isSuper,isThis,methodCallExpr.getBegin(),methodDeclarationEntity);
			return methodCallEntity;
		}
		return null;
	}
	
	public static MethodCallEntity get(ExplicitConstructorInvocationStmt n, ResolvedConstructorDeclaration resolvedConstructorDeclaration,  JavaParserFacade jpf) throws Exception{
		boolean isSuper = false;
		boolean isThis = false;
		Optional<String> receiver = Optional.empty();
		Optional<String> innerReceiver = Optional.empty();
		
		MethodDeclarationEntity methodDeclarationEntity = MethodDeclarationEntity.get(resolvedConstructorDeclaration, jpf);
		MethodCallEntity  methodCallEntity = new MethodCallEntity(receiver,false,false,n.getBegin(),methodDeclarationEntity);
		return methodCallEntity;
	}
	
	public boolean isSuper() {
		return isSuper;
	}

	public void setSuper(boolean isSuper) {
		this.isSuper = isSuper;
	}

	public SourcePosition getPosition() {
		return position;
	}

	public void setPosition(SourcePosition position) {
		this.position = position;
	}

	public String getReceiverQualifiedName() {
		return receiverQualifiedName;
	}

	public void setReceiverQualifiedName(String receiverQualifiedName) {
		this.receiverQualifiedName = receiverQualifiedName;
	}

	public MethodDeclarationEntity getMethodDeclarationEntity() {
		return methodDeclarationEntity;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "MethodCallEntity [receiverQualifiedName=" + receiverQualifiedName + ", isSuper=" + isSuper + ", isThis=" + isThis + ", position=" + position
				+ ", methodDeclarationEntity=" + methodDeclarationEntity + "]";
	}
}
