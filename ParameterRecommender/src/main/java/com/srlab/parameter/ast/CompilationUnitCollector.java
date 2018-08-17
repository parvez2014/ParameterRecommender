package com.srlab.parameter.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.omg.CORBA.RepositoryIdHelper;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class CompilationUnitCollector {

	private HashMap<String,List<CompilationUnit>> fileCompilationUnitMap;
	public CompilationUnitCollector() {
		this.fileCompilationUnitMap = new HashMap();
	}
	

	public HashMap<String, List<CompilationUnit>> getFileCompilationUnitMap() {
		return fileCompilationUnitMap;
	}

	public List<CompilationUnit> collectCompilationUnits(File file) {
		
		List<CompilationUnit> cuList = new ArrayList();
		if(file.isFile() && file.getName().endsWith(".java")) {
			CompilationUnit cu;
			try {
				cu = JavaParser.parse(file);
				if(fileCompilationUnitMap.containsKey(file.getAbsolutePath())) {
					List<CompilationUnit> list = fileCompilationUnitMap.get(file.getAbsolutePath());
					list.add(cu);
				}
				else {
				
					List<CompilationUnit> list = new ArrayList();
					list.add(cu);
					fileCompilationUnitMap.put(file.getAbsolutePath(),list);
				}
				cuList.add(cu);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Error in parsing a file");
			}
		}
		else {
			File childFiles[] = file.listFiles();
			if(childFiles!=null) {
				for(File child:childFiles) {
					cuList.addAll(this.collectCompilationUnits(child));
				}
			}
		}
		return cuList;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}