package com.srlab.parameter.completioner;

import java.util.ArrayList;
import java.util.List;

import com.srlab.parameter.node.ParameterContent;

public class ModelEntry {
	private MethodCallEntity methodCallEntity;
	private List<ParameterContent> parameterContentList;
	private String neighborList;
	private String lineContent;
	private String receiverMethodCalls;
	private String astContext;
	private List<String> argumentMethodCallsList;

	private SourcePosition sourcePosition;
	private String path;

	public ModelEntry(MethodCallEntity _methodCallEntity, List<ParameterContent> _parameterContentList,
			String _neighborList, String _lineContent, String astContxt, String _receiverMethodCalls,
			List<String> _argumentMethodCallsList, SourcePosition _sourcePosition, String _path) {
		super();
		this.methodCallEntity = _methodCallEntity;
		this.parameterContentList = _parameterContentList;
		this.neighborList = _neighborList;
		this.lineContent = _lineContent;
		this.receiverMethodCalls = _receiverMethodCalls;
		this.argumentMethodCallsList = _argumentMethodCallsList;
		this.sourcePosition = _sourcePosition;
		this.path = _path;
	}

	public String getReceiverMethodCalls() {
		return receiverMethodCalls;
	}

	public List<String> getArgumentMethodCallsList() {
		return argumentMethodCallsList;
	}

	public SourcePosition getSourcePosition() {
		return sourcePosition;
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

	public void setLineContent(String lineContent) {
		this.lineContent = lineContent;
	}

	@Override
	public String toString() {
		return "ParameterModelEntry [methodCallEntity=" + methodCallEntity + ", parameterContentList="
				+ parameterContentList + ", neighborList=" + neighborList + ", lineContent=" + lineContent + "]";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
