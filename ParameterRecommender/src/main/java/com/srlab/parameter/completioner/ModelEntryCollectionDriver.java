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
import com.srlab.parameter.ast.CompilationUnitCollector;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.config.Config;
import com.srlab.parameter.node.ParameterContent;

public class ModelEntryCollectionDriver {
	
	private String repositoryPath;
	private List<ModelEntry> modelEntryList;
	private HashMap<String,List<ModelEntry>> hmFileToModelEntries;
	private HashMap<String,List<ParameterModelEntry>> hmFileToParameterModelEntries;
	
	//list of SLP context
	private static List<List<String>> slp_context;
	
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
	
	public void run() throws IOException {
		List<String> fileList = this.collectSourceFiles(new File(this.repositoryPath));
		System.out.println("Total Collected Files: "+fileList.size());
		int counter = 0;
		BufferedWriter bw = new BufferedWriter(new FileWriter(Config.MODEL_ENTRY_OUTPUT_PATH));
		List<List<String>> slp_context = new ArrayList();

		for(String file:fileList) {
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
							this.modelEntryList.addAll(methodCallExprVisitor.getModelEntryList());
							fileModelEntryList.addAll(methodCallExprVisitor.getModelEntryList());
							fileParameterModelEntryList.addAll(methodCallExprVisitor.getParameterModelEntryList());
							//System.out.println("I am here"+methodCallExprVisitor.getModelEntryList().size());
							//write the information
							for(ModelEntry modelEntry:methodCallExprVisitor.getModelEntryList()) {
								StringBuffer sbParameterAbsStringRep = new StringBuffer("");
								StringBuffer sbParameterAbsStringRepWithLiteral = new StringBuffer("");
								
								for(ParameterContent parameterContent: modelEntry.getParameterContentList()) {
									sbParameterAbsStringRep.append(parameterContent.getAbsStringRep());
									sbParameterAbsStringRep.append(" ");
									sbParameterAbsStringRepWithLiteral.append(parameterContent.getAbsStringRepWithLiteral());
									sbParameterAbsStringRepWithLiteral.append(" ");
								}
								
								bw.write("MethodName: "+modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getName());
								bw.newLine();
								bw.write("ReceiverType: "+modelEntry.getMethodCallEntity().getReceiverQualifiedName());
								bw.newLine();
								bw.write("SourcePosition: "+modelEntry.getSourcePosition().column+" : " + modelEntry.getSourcePosition().line);
								bw.newLine();
								bw.write("Path: "+modelEntry.getPath());
								bw.newLine();
								
								bw.write("AbsStringRep: "+sbParameterAbsStringRep);
								bw.newLine();
								bw.write("AbsStringRepWithLiteral: "+sbParameterAbsStringRepWithLiteral);
								bw.newLine();
								bw.write("Neighborlist: "+modelEntry.getNeighborList());
								bw.newLine();
								bw.write("LineContext: "+modelEntry.getLineContent());
								bw.newLine();
								bw.write("AstContext: "+modelEntry.getAstContext());
								bw.newLine();
								bw.write("SlpContext: "+modelEntry.getSlpContext());
								bw.newLine();
								bw.write("MethodCalledOnReceiverOrArguments: "+modelEntry.getReceiverOrArgumentMethodCalls());
								bw.newLine();
							}
						}
					}	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}catch(java.lang.StackOverflowError e) {
				System.out.println("Stack OverflowError");
			}
			this.hmFileToModelEntries.put(file,fileModelEntryList);
			this.hmFileToParameterModelEntries.put(file,fileParameterModelEntryList);
		}
		bw.close();
	}
	
	public void newRun() {
		File root = new File("/media/parvez/IntelSSD/research/parameter_recommendation/repository/main");
		List<String> filePathList = this.collectSourceFiles(root);
		System.out.println("FilePath: "+filePathList.size());
		File listOfFiles[] = root.listFiles();
		int totalCompilationUnit =0;
		for(File child:listOfFiles) {
			CompilationUnitCollector cuc = new CompilationUnitCollector();
			List<CompilationUnit> cuList = cuc.collectCompilationUnits(child);
			totalCompilationUnit = totalCompilationUnit +cuList.size();
			if(cuList.size()>500)
			System.out.println("File: "+child.getName()+" : "+cuList.size());
		}
		System.out.println("Total CompilationUnit: "+totalCompilationUnit);
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
		JSSConfigurator.getInstance().init(Config.REPOSITORY_PATH,Config.EXTERNAL_DEPENDENCY_PATH);
		ModelEntryCollectionDriver modelEntryCollectionDriver = new ModelEntryCollectionDriver(Config.REPOSITORY_PATH);
		modelEntryCollectionDriver.newRun();
	}
}
