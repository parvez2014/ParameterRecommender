package com.srlab.parameter.completioner;

import com.srlab.parameter.node.ParameterContent;

public class ParameterModelEntry {

	private ModelEntry modelEntry;
	private String receiverType;
	private int parameterPosition;
	private String parameterName;
	private ParameterContent parameterContent;
	
	public ParameterModelEntry(ModelEntry modelEntry, String receiverType, int parameterPosition) {
		super();
		this.modelEntry = modelEntry;
		this.receiverType = receiverType;
		this.parameterPosition = parameterPosition;
		this.parameterContent = modelEntry.getParameterContentList().get(parameterPosition);
		this.parameterName = modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getParameterList().get(parameterPosition).getName();
	}

	public String getParameterName() {
		return parameterName;
	}

	public ParameterContent getParameterContent() {
		return parameterContent;
	}

	public ModelEntry getModelEntry() {
		return modelEntry;
	}

	public String getReceiverType() {
		return receiverType;
	}

	public int getParameterPosition() {
		return parameterPosition;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
