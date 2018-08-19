package com.srlab.parameter.completioner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

public class ModelEntryCollectionDriver implements Serializable {
	
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
	
	public void process(String repo, BufferedWriter bw){
	
		int counter = 0;
		List<String> fileList = this.collectSourceFiles(new File(repo));
		System.out.println("Total Collected Files: "+fileList.size());
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
					
							for(ModelEntry modelEntry:methodCallExprVisitor.getModelEntryList()) {
								StringBuffer sbParameterAbsStringRep = new StringBuffer("");
								StringBuffer sbParameterAbsStringRepWithLiteral = new StringBuffer("");
								
								for(ParameterContent parameterContent: modelEntry.getParameterContentList()) {
									sbParameterAbsStringRep.append(parameterContent.getAbsStringRep());
									sbParameterAbsStringRep.append(" ");
									sbParameterAbsStringRepWithLiteral.append(parameterContent.getAbsStringRepWithLiteral());
									sbParameterAbsStringRepWithLiteral.append(" ");
								}
								bw.write("MethodCallExpression: "+methodCallExprVisitor.getHmModelEntryToMethodCallExpr().get(modelEntry));
								bw.newLine();
								bw.write("MethodName: "+modelEntry.getMethodCallEntity().getMethodDeclarationEntity().getName());
								bw.newLine();
								bw.write("ReceiverType: "+modelEntry.getMethodCallEntity().getReceiverQualifiedName());
								bw.newLine();
								bw.write("SourcePosition: "+modelEntry.getColumn()+" : " + modelEntry.getLine());
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
								bw.write("RecMethodCalls: "+modelEntry.getNameBasedReceiverMethodCalls());
								bw.newLine();
								bw.write("ArgMethodCalls: "+modelEntry.getNameBasedArgumentMethodCalls());
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
	}
	
	public void run() throws IOException {
		File root = new File(Config.REPOSITORY_PATH);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Config.MODEL_ENTRY_OUTPUT_PATH)));	
	    JSSConfigurator.getInstance().init(Config.REPOSITORY_PATH, Config.EXTERNAL_DEPENDENCY_PATH);
	    this.process(Config.REPOSITORY_PATH, bw);
	    bw.close();
	}
	
	public void divideAndRun() throws IOException {
		File root = new File(Config.REPOSITORY_PATH);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Config.MODEL_ENTRY_OUTPUT_PATH)));
		
		File listOfFiles[] = root.listFiles();
		int totalScannedFiles = 0;
		for(int i=0;i<listOfFiles.length;i++) {
			File child = listOfFiles[i];
			System.out.println("Directories Processed: "+i+"/"+listOfFiles.length+" Total Scanned Files: "+totalScannedFiles);
			List<String> filePathList = this.collectSourceFiles(child);
			totalScannedFiles = totalScannedFiles + filePathList.size();
			
			JSSConfigurator.getInstance().init(child.getAbsolutePath(), Config.EXTERNAL_DEPENDENCY_PATH);
			this.process(child.getAbsolutePath(), bw);
			JSSConfigurator.getInstance().clear();
		}
		bw.close();
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

	public void save(File filename) {
	    FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(this);

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
      
	}
	public static ModelEntryCollectionDriver load(File filename) {
	    FileInputStream fos = null;
        ObjectInputStream out = null;
        try {
            fos = new FileInputStream(filename);
            out = new ObjectInputStream(fos);
            ModelEntryCollectionDriver driver = (ModelEntryCollectionDriver)out.readObject();
            out.close();
            return driver;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
      return null;
	}
	public static void main(String args[]) {
		//JSSConfigurator.getInstance().init(Config.REPOSITORY_PATH,Config.EXTERNAL_DEPENDENCY_PATH);
		ModelEntryCollectionDriver modelEntryCollectionDriver = new ModelEntryCollectionDriver(Config.REPOSITORY_PATH);
		try {
			modelEntryCollectionDriver.divideAndRun();
			modelEntryCollectionDriver.save(new File(Config.ROOT_PATH+File.separator+Config.REPOSITORY_NAME+".mec"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ModelEntryCollectionDriver driver = ModelEntryCollectionDriver.load(new File(Config.ROOT_PATH+File.separator+Config.REPOSITORY_NAME+".mec"));
		System.out.println("Driver = "+driver.getModelEntryList().size());
	}
}
