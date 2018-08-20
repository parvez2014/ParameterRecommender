package com.srlab.parameter.recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.completioner.ModelEntry;
import com.srlab.parameter.completioner.ModelEntryCollectionDriver;
import com.srlab.parameter.completioner.ParameterModelEntry;
import com.srlab.parameter.config.Config;

public class TrainingTestGenerator {

	private HashMap<String,List<ParameterModelEntry>> hmFileToParameterModelEntries;
	private List<ParameterModelEntry> testParameterModelEntryList;
	private List<ParameterModelEntry> trainingParameterModelEntryList;
	private List<ParameterModelEntry> allParameterModelEntryList;
	private List<String> testSourceFilePath;
	
	public TrainingTestGenerator(HashMap<String,List<ParameterModelEntry>> _hmFileToParameterModelEntryList) {
		this.hmFileToParameterModelEntries = _hmFileToParameterModelEntryList;
		this.testParameterModelEntryList = new ArrayList();
		this.allParameterModelEntryList = new ArrayList();
		this.trainingParameterModelEntryList = new ArrayList();
		this.testSourceFilePath = new ArrayList();
	}
	
	private List<ModelEntry>getModelEntryList(HashMap<String,List<ModelEntry>> hmFileToModelEntryList) {
		List<ModelEntry> modelEntryList = new ArrayList();
		for(String path:hmFileToModelEntryList.keySet()) {
			modelEntryList.addAll(hmFileToModelEntryList.get(path));
		}
		return modelEntryList;
	}
	
	public void genTrainingTestDataSet() {
		this.testParameterModelEntryList.clear();
		this.trainingParameterModelEntryList.clear();
		this.testSourceFilePath.clear();
		this.allParameterModelEntryList.clear();
		
		HashSet<String> testPathSet = new HashSet();
		List<String> javaFileList = new ArrayList(this.hmFileToParameterModelEntries.keySet());
		
		//step-1: collect all model entries
		int totalParameterModelEntries = 0;
		for(String file:javaFileList) {
			totalParameterModelEntries = totalParameterModelEntries + hmFileToParameterModelEntries.get(file).size();
			allParameterModelEntryList.addAll(hmFileToParameterModelEntries.get(file));
		}
		
		System.out.println("Total Parameter Model Entries: "+ totalParameterModelEntries );
		
		//step-2: collect 80% of model entries for training
		Collections.shuffle(allParameterModelEntryList);
		int totalTestModelEntries = (int)(totalParameterModelEntries*(0.20f));
		int remains = totalTestModelEntries;
		for(ParameterModelEntry parameterModelEntry:allParameterModelEntryList) {
			if(remains>0) {
				testPathSet.add(parameterModelEntry.getFilePath());
				testParameterModelEntryList.add(parameterModelEntry);
				remains--;
			}
			else {
				trainingParameterModelEntryList.add(parameterModelEntry);
		
			}
		}
		
		//Step-3: collect all source files that contain at least one test data set 
		this.testSourceFilePath = new ArrayList(testPathSet);
		System.out.println("Total Training Data Set: "+testParameterModelEntryList.size());
		System.out.println("Total Test Data Set: "+trainingParameterModelEntryList.size());
		System.out.println("Total Test Files: "+this.getTestSourceFilePath().size());
	}
	
	public List<ParameterModelEntry> getAllParameterModelEntryList() {
		return allParameterModelEntryList;
	}

	public void genFileBasedTrainingTestDataSet() {
		//to be safe side we clear both training and test list
		this.testParameterModelEntryList.clear();
		this.trainingParameterModelEntryList.clear();
		this.testSourceFilePath.clear();
		
		List<String> javaFileList = new ArrayList(this.hmFileToParameterModelEntries.keySet());
		Collections.shuffle(javaFileList);
		
		//step-1: collect all model entries
		int totalParameterModelEntries = 0;
		for(String file:javaFileList) {
			totalParameterModelEntries = totalParameterModelEntries + hmFileToParameterModelEntries.get(file).size();
		}
		System.out.println("Total Model Entries: "+ totalParameterModelEntries );
		//step-2: collect 80% of model entries for training
		int totalTestModelEntries = (int)(totalParameterModelEntries*(0.40f));
		int remains = totalTestModelEntries;
		
		//collect the testData
		for(String file:javaFileList) {
			if(remains>0) {
				List<ParameterModelEntry> fileModelEntryList = hmFileToParameterModelEntries.get(file);
				testParameterModelEntryList.addAll(fileModelEntryList);
				remains = remains - fileModelEntryList.size();
				testSourceFilePath.add(file);
			}
			else {
				List<ParameterModelEntry> fileModelEntryList = hmFileToParameterModelEntries.get(file);
				trainingParameterModelEntryList.addAll(fileModelEntryList);
			}
		}
		System.out.println("Total Training Data Set: "+testParameterModelEntryList.size());
		System.out.println("Total Test Data Set: "+trainingParameterModelEntryList.size());
	}
	public List<String> getTestSourceFilePath() {
		return testSourceFilePath;
	}
	
	public HashMap<String, List<ParameterModelEntry>> getHmFileToParameterModelEntries() {
		return hmFileToParameterModelEntries;
	}

	public List<ParameterModelEntry> getTestParameterModelEntryList() {
		return testParameterModelEntryList;
	}

	public List<ParameterModelEntry> getTrainingParameterModelEntryList() {
		return trainingParameterModelEntryList;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSSConfigurator.getInstance().init(Config.REPOSITORY_PATH,Config.EXTERNAL_DEPENDENCY_PATH);
		ModelEntryCollectionDriver modelEntryCollectionDriver = new ModelEntryCollectionDriver(Config.REPOSITORY_PATH);
		try {
			modelEntryCollectionDriver.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Total Model Entry Keys: "+modelEntryCollectionDriver.getHmFileToModelEntries().keySet().size());
		TrainingTestGenerator trainingTestGenerator = new TrainingTestGenerator(modelEntryCollectionDriver.getHmFileToParameterModelEntries());
		trainingTestGenerator.genTrainingTestDataSet();
	}
}
