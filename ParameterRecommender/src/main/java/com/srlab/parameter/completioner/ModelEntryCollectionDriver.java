package com.srlab.parameter.completioner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.srlab.parameter.config.Config;
import com.srlab.parameter.node.ParameterContent;

public class ModelEntryCollectionDriver {
	
	private String repositoryPath;
	private List<ModelEntry> modelEntryList;
	private HashMap<String,List<ModelEntry>> hmFileToModelEntries;
	private HashMap<String,List<ParameterModelEntry>> hmFileToParameterModelEntries;
	
	
	public List<String> collectSourceFiles(File file){
		List<String> fileList = new LinkedList();
		if(file.isDirectory()) {
			for(File f:file.listFiles()) {
				fileList.addAll(this.collectSourceFiles(f));
			}
		}else if(file.isFile() && file.getName().endsWith(".java")){
			fileList.add(file.getAbsolutePath());
		}
		return fileList;
	}
	
	public void run() {
		List<String> fileList = this.collectSourceFiles(new File(this.repositoryPath));
		System.out.println("Total Collected Files: "+fileList.size());
		int counter = 0;
		for(String file:fileList.subList(0, 10)) {
			//first convert the file to compilation unit
			System.out.println("Progress: "+ (counter++)+"/"+fileList.size());
			CompilationUnit cu;
			List<ModelEntry> fileModelEntryList = new ArrayList();
			List<ParameterModelEntry> fileParameterModelEntryList = new ArrayList();
			try {
				cu = JavaParser.parse(new FileInputStream(file));
				
				for(TypeDeclaration typeDeclaration:cu.getTypes()) {
					for(Object obj:typeDeclaration.getMethods()) {
						if(obj instanceof MethodDeclaration) {
							MethodDeclaration md = (MethodDeclaration)obj;
							MethodCallExprVisitor methodCallExprVisitor = new MethodCallExprVisitor(cu,file);
							md.accept(methodCallExprVisitor,null);
							modelEntryList.addAll(methodCallExprVisitor.getModelEntryList());
							fileModelEntryList.addAll(methodCallExprVisitor.getModelEntryList());
							fileParameterModelEntryList.addAll(methodCallExprVisitor.getParameterModelEntryList());
							
						}
					}	
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
			this.hmFileToModelEntries.put(file,fileModelEntryList);
		}
	}
	
	public String getRepositoryPath() {
		return repositoryPath;
	}

	public List<ModelEntry> getModelEntryList() {
		return modelEntryList;
	}

	public HashMap<String, List<ParameterModelEntry>> getHmFileToParameterModelEntries() {
		return hmFileToParameterModelEntries;
	}

	public ModelEntryCollectionDriver(String _repositoryPath) {
		this.repositoryPath = _repositoryPath;
		this.modelEntryList = new LinkedList();
		this.hmFileToModelEntries = new HashMap();
		this.hmFileToParameterModelEntries = new HashMap();
	}

	public HashMap<String, List<ModelEntry>> getHmFileToModelEntries() {
		return hmFileToModelEntries;
	}

	public static void main(String args[]) {
		ModelEntryCollectionDriver modelEntryCollectionDriver = new ModelEntryCollectionDriver(Config.TEST_REPOSITORY_PATH);
		modelEntryCollectionDriver.run();
		System.out.println("Total Collected Model Entry List: "+modelEntryCollectionDriver.getModelEntryList().size());
		List<ModelEntry> modelEntryList = modelEntryCollectionDriver.getModelEntryList();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Config.MODEL_ENTRY_OUTPUT_PATH));
			for(ModelEntry modelEntry:modelEntryList) {
				StringBuffer sbParameter = new StringBuffer("");
				for(ParameterContent parameterContent: modelEntry.getParameterContentList()) {
					sbParameter.append(parameterContent.getAbsStringRep());
					sbParameter.append(" ");
				}
				
				bw.write("MethodName:ReceiverType> "+modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getName()+":"+modelEntry.getMethodCallEntity().getReceiverQualifiedName());
				bw.newLine();
				bw.write("ReceiverType> "+modelEntry.getMethodCallEntity().getReceiverQualifiedName());
				bw.newLine();
				bw.write("ParameterList> "+sbParameter.toString());
				bw.newLine();
				bw.write("SorroundingContext> "+modelEntry.getNeighborList());
				bw.newLine();
				bw.write("LineContext> "+modelEntry.getLineContent());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
