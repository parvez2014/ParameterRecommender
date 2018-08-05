package com.srlab.parameter.completioner;

import java.util.List;

import com.srlab.parameter.node.ParameterContent;

public class ModelEntry {
	private MethodCallEntity methodCallEntity;
	private List<ParameterContent> parameterContentList;
	private String neighborList;
	private String lineContent;
	

	public ModelEntry(MethodCallEntity methodCallEntity, List<ParameterContent> parameterContentList, String neighborList, String lineContent) {
		super();
		this.methodCallEntity = methodCallEntity;
		this.parameterContentList = parameterContentList;
		this.neighborList = neighborList;
		this.lineContent = lineContent;
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
