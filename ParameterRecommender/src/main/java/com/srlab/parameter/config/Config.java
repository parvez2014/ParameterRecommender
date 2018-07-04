package com.srlab.parameter.config;

import java.io.File;

public class Config {

	public static final String ROOT_PATH = "/home/parvez/research/historic_evaluation/repositories";//"E:\\research\\parameter_recommendation";
	
	public static final String REPOSITORY_NAME = "jhotdraw"; 
	public static final String REPOSITORY_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME;
	public static final String REPOSITORY_REVISION_PATH = ROOT_PATH + File.separator + REPOSITORY_NAME + "_revisions";
	public static final String EXTERNAL_DEPENDENCY_PATH = ROOT_PATH + File.separator+ REPOSITORY_NAME + "_dependencies";
	
	public static final String[] FILE_EXTENSIONS = {".java"};
	public static final String FRAMEWORKS[] = {"javax.swing.","java.awt."};
	
	public static boolean isInteresting(String qualifiedTypeName) {
		for(String prefix:FRAMEWORKS) {
			if(qualifiedTypeName.startsWith(prefix)) return true;
			else return false;
		}
		return false;
	}
	public static String getRepositoryFolderName() {
		File file = new File(Config.REPOSITORY_PATH);
		return file.getName();
	}
}
