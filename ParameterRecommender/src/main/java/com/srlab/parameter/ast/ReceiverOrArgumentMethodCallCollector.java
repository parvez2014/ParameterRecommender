package com.srlab.parameter.ast;

import java.util.HashSet;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;

public class ReceiverOrArgumentMethodCallCollector {

	
	public static String getVariableName(Expression expression){
		
		if(expression instanceof NameExpr){
			return expression.asNameExpr().getName().getIdentifier();
		}
		else if(expression instanceof MethodCallExpr){
			MethodCallExpr methodCallExpr = (MethodCallExpr)expression;
			if(methodCallExpr.getScope().isPresent()){
				return getVariableName(methodCallExpr.getScope().get());
			}
			else return "";
		}
		else if(expression instanceof ThisExpr) {
			return "this";
		}
		else if(expression instanceof SuperExpr) {
			return "super";
		}
		else return null;
	}
	
	public static HashSet<String> collectIdentifiers(MethodCallExpr m) {
		HashSet<String> identifierSet = new HashSet();
		
		if(m.getScope().isPresent()) {
			String receiver_varname = getVariableName(m.getScope().get());
			if(receiver_varname!=null) identifierSet.add(receiver_varname);
			if(m.getArguments().size()>0) {
				for(Expression expression:m.getArguments()) {
					//we need to find the receiver variable
					String argumentExpressionVarName = getVariableName(expression);
					if(argumentExpressionVarName!=null && identifierSet.contains(argumentExpressionVarName)==false) {
						identifierSet.add(argumentExpressionVarName);
					}
				}
			}
		}
		return identifierSet;
	}
}
