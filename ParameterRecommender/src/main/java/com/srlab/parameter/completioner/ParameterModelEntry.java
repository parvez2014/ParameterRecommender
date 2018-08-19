package com.srlab.parameter.completioner;

import java.io.Serializable;

import com.srlab.parameter.node.ParameterContent;

public class ParameterModelEntry implements Serializable{

	private ModelEntry modelEntry;
	private String receiverType;
	private int parameterPosition;
	private String parameterName;
	private ParameterContent parameterContent;
	private String filePath;

	public ParameterModelEntry(ModelEntry _modelEntry, String _receiverType, int _parameterPosition, String _filePath) {
		super();
		this.modelEntry = _modelEntry;
		this.receiverType = _receiverType;
		this.parameterPosition = _parameterPosition;
		this.parameterContent = modelEntry.getParameterContentList().get(parameterPosition);
		this.parameterName = modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getParameterList()
				.get(parameterPosition).getName();
		this.filePath = _filePath;
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

	public String getParameterName() {
		return parameterName;
	}

	public ParameterContent getParameterContent() {
		return parameterContent;
	}

	public String getFilePath() {
		return filePath;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
