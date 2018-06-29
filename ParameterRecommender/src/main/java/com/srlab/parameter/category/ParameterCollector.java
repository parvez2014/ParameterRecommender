package com.srlab.parameter.category;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.srlab.parameter.config.Config;

public class ParameterCollector {
	
	private String repositoryPath;

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
		ParameterExpressionCategorizer parameterExpressionCategorizer = new ParameterExpressionCategorizer(true);
		for(String file:fileList) {
			//first convert the file to compilation unit
			CompilationUnit cu;
			try {
				cu = JavaParser.parse(new FileInputStream(file));
				ParameterCategoryVisitor visitor = new ParameterCategoryVisitor(cu, parameterExpressionCategorizer);
				cu.accept(visitor,null);
		
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		parameterExpressionCategorizer.print();
	}
	
	public ParameterCollector(String _repositoryPath) {
		this.repositoryPath = _repositoryPath;
	}
	
	public static void main(String args[]) {
		ParameterCollector parameterCollector = new ParameterCollector(Config.REPOSITORY_PATH);
		parameterCollector.run();
	}
}
