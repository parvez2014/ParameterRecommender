package com.srlab.parameter.completioner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.srlab.parameter.node.ParameterContent;

public class ModelEntry implements Serializable{
	private MethodCallEntity methodCallEntity;
	private List<ParameterContent> parameterContentList;
	private String neighborList;
	private String lineContent;
	private String astContext;
	private String slpContext;
	private String receiverOrArgumentMethodCalls;
	private String nameBasedReceiverMethodCalls;
	private String nameBasedArgumentMethodCalls;
	private int line;
	private int column;
	private String path;

	public ModelEntry(MethodCallEntity _methodCallEntity, List<ParameterContent> _parameterContentList,
			String _neighborList, String _lineContent, String _astContext, String _receiverOrArgumentMethodCalls, String _nameBasedReceiverMethodCalls, String _nameBasedArgumentMethodCalls, String _slpContext, int _column,int _line, String _path) {
		super();
		this.methodCallEntity = _methodCallEntity;
		this.parameterContentList = _parameterContentList;
		this.neighborList = _neighborList;
		this.lineContent = _lineContent;
		this.receiverOrArgumentMethodCalls = _receiverOrArgumentMethodCalls;
		this.nameBasedReceiverMethodCalls = _nameBasedReceiverMethodCalls;
		this.nameBasedArgumentMethodCalls = _nameBasedArgumentMethodCalls;
		this.slpContext = _slpContext;
		this.astContext = _astContext;
		this.line = _line;
		this.column = _column;
		this.path = _path;
	}


	public String getNameBasedReceiverMethodCalls() {
		return nameBasedReceiverMethodCalls;
	}


	public String getNameBasedArgumentMethodCalls() {
		return nameBasedArgumentMethodCalls;
	}


	public String getSlpContext() {
		return slpContext;
	}


	public String getAstContext() {
		return astContext;
	}


	public String getReceiverOrArgumentMethodCalls() {
		return receiverOrArgumentMethodCalls;
	}



	public int getLine() {
		return line;
	}


	public int getColumn() {
		return column;
	}


	public String getPath() {
		return path;
	}

	public MethodCallEntity getMethodCallEntity() {
		return methodCallEntity;
	}

	public void setMethodCallEntity(MethodCallEntity methodCallEntity) {
		this.methodCallEntity = methodCallEntity;
	}

	public List<ParameterContent> getParameterContentList() {
		return parameterContentList;
	}

	public void setParameterContentList(List<ParameterContent> parameterContentList) {
		this.parameterContentList = parameterContentList;
	}

	public String getNeighborList() {
		return neighborList;
	}

	public void setNeighborList(String neighborList) {
		this.neighborList = neighborList;
	}

	public String getLineContent() {
		return lineContent;
	}


	@Override
	public String toString() {
		return "ModelEntry [methodCallEntity=" + methodCallEntity + ", parameterContentList=" + parameterContentList
				+ ", neighborList=" + neighborList + ", lineContent=" + lineContent + ", astContext=" + astContext
				+ ", slpContext=" + slpContext + ", receiverOrArgumentMethodCalls=" + receiverOrArgumentMethodCalls
				+ ", nameBasedReceiverMethodCalls=" + nameBasedReceiverMethodCalls + ", nameBasedArgumentMethodCalls="
				+ nameBasedArgumentMethodCalls + ", line=" + line + ", column=" + column + ", path=" + path + "]";
	}


	public void setLineContent(String lineContent) {
		this.lineContent = lineContent;
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
