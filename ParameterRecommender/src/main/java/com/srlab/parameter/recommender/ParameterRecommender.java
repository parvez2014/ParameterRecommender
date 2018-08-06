package com.srlab.parameter.recommender;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.completioner.MethodCallEntity;
import com.srlab.parameter.completioner.MethodCallExprVisitor;
import com.srlab.parameter.completioner.ModelEntry;
import com.srlab.parameter.completioner.ModelEntryCollectionDriver;
import com.srlab.parameter.completioner.ParameterEntity;
import com.srlab.parameter.completioner.ParameterModelEntry;
import com.srlab.parameter.completioner.SourcePosition;
import com.srlab.parameter.config.Config;
import com.srlab.parameter.node.BooleanLiteralContent;
import com.srlab.parameter.node.CastExpressionContent;
import com.srlab.parameter.node.CharLiteralContent;
import com.srlab.parameter.node.ClassInstanceCreationContent;
import com.srlab.parameter.node.MethodInvocationContent;
import com.srlab.parameter.node.NameExprContent;
import com.srlab.parameter.node.NullLiteralContent;
import com.srlab.parameter.node.NumberLiteralContent;
import com.srlab.parameter.node.ParameterContent;
import com.srlab.parameter.node.QualifiedNameContent;
import com.srlab.parameter.node.StringLiteralContent;
import com.srlab.parameter.node.ThisExpressionContent;
import com.srlab.parameter.simplename.SimpleNameCollector;
import com.srlab.parameter.simplename.SimpleNameRecommender;

import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;

public class ParameterRecommender {

	private MultiKeyMap mkRecommenderModel;
	private List<ModelEntry> trainingModelEntryList;
	private List<ModelEntry> testModelEntryList;
	
	public ParameterRecommender() {
		this.mkRecommenderModel = new MultiKeyMap();
	}
	//to train we need to index training dataset based on method name, receivertype and parameter position
	public void train(List<ParameterModelEntry> trainingParameterModelEntryList) {
		//index training model entries by receiver type, method name and parameter position
		for(ParameterModelEntry parameterModelEntry:trainingParameterModelEntryList) {
			MethodCallEntity methodCallEntity = parameterModelEntry.getModelEntry().getMethodCallEntity();
			if(mkRecommenderModel.containsKey(parameterModelEntry.getReceiverType(),
					methodCallEntity.getMethodDeclarationEntity().getName(),
					parameterModelEntry.getParameterPosition())){
				List<ParameterModelEntry> list = (List<ParameterModelEntry>)mkRecommenderModel.get(parameterModelEntry.getReceiverType(),
						methodCallEntity.getMethodDeclarationEntity().getName(),
						parameterModelEntry.getParameterPosition());
				list.add(parameterModelEntry);
				
			}else {
				List<ParameterModelEntry> list = new ArrayList();
				list.add(parameterModelEntry);
				mkRecommenderModel.put(parameterModelEntry.getReceiverType(),
						methodCallEntity.getMethodDeclarationEntity().getName(),
						parameterModelEntry.getParameterPosition(),list);
			}
			
		}
	}
	
	//ideally we just need the test parameter model entries. But the problem is that we also need o know the 
	// variables declared in the query context, method declaration and method call expression. These are not captured by
	//parameter model entries. So we save the test files. All framework methods called in those files are being tested
	public void test(List<ParameterModelEntry> testParameterModelEntryList) {
		ResultCollector resultCollector = new ResultCollector();
		int testCaseCounter = 0;
		//Step-1: collect all files that contain the test data
		Set<String> testFilePathSet = new HashSet(); 
		for(ParameterModelEntry testParameterModelEntry:testParameterModelEntryList) {
			testFilePathSet.add(testParameterModelEntry.getFilePath());
		}
		//Step-2: iterate through all test cases 
		for(String filePath:testFilePathSet) {
			try {
				CompilationUnit cu = JavaParser.parse(new FileInputStream(filePath));
				
				for(TypeDeclaration typeDeclaration:cu.getTypes()) {
					for(Object obj:typeDeclaration.getMethods()) {
						if(obj instanceof MethodDeclaration) {
							MethodDeclaration md = (MethodDeclaration)obj;
							MethodCallExprVisitor methodCallExprVisitor = new MethodCallExprVisitor(cu,filePath);
							md.accept(methodCallExprVisitor,null);
							//collect parameter model entries and method call expressions associated to it
							for (ParameterModelEntry parameterModelEntry:methodCallExprVisitor.getParameterModelEntryList()) {
								//we only test parameters of following expression type
								if(parameterModelEntry.getParameterContent() instanceof NullLiteralContent
									|| parameterModelEntry.getParameterContent() instanceof StringLiteralContent
									|| parameterModelEntry.getParameterContent() instanceof CharLiteralContent
									|| parameterModelEntry.getParameterContent() instanceof NumberLiteralContent
									|| parameterModelEntry.getParameterContent() instanceof ThisExpressionContent
									|| parameterModelEntry.getParameterContent() instanceof NameExprContent
								) {
									System.out.println("Test Case Counter: "+(testCaseCounter++)+"/"+testParameterModelEntryList.size());
									MethodCallExpr methodCallExpr = (MethodCallExpr)methodCallExprVisitor.getHmParaMeterModelEntryToMethodCallExpr().get(parameterModelEntry);
									this.testInstance(parameterModelEntry, 	methodCallExpr, md, resultCollector);
								}
							}
						}
					}	
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		resultCollector.print();
	}
	
	@SuppressWarnings("unchecked")
	public void testInstance(ParameterModelEntry query, MethodCallExpr methodCallExpr, 
			MethodDeclaration methodDeclaration, ResultCollector resultCollector) {
		
		List<ParameterModelEntry> possibleCandidateList = new ArrayList();
		final HashMap<ParameterModelEntry, Float> hmParameterModelEntryToSimilarity = new HashMap();
		final HashMap<String, Integer> hmFrequency = new HashMap();
		
		if(mkRecommenderModel.containsKey(query.getReceiverType(),
				query.getModelEntry().getMethodCallEntity().getMethodDeclarationEntity().getName(),
				query.getParameterPosition())) 
		{
			//Step-1: collect possible candidate list based on receiver type, method name and parameter position
			possibleCandidateList = (List<ParameterModelEntry>)mkRecommenderModel.get(query.getReceiverType(),
				query.getModelEntry().getMethodCallEntity().getMethodDeclarationEntity().getName(),
				query.getParameterPosition());
			
			//Step-2: collect simple names
			SourcePosition sourcePosition = query.getModelEntry().getMethodCallEntity().getPosition();
			SimpleNameCollector simpleNameCollector = new SimpleNameCollector(methodDeclaration,sourcePosition);
			simpleNameCollector.run();
			
			//Step-3: determine similarity between query and possible candidate list
			for(ParameterModelEntry parameterModelEntry:possibleCandidateList) {
				float similarity = new CosineSimilarity().getSimilarity(query.getModelEntry().getNeighborList(),parameterModelEntry.getModelEntry().getNeighborList());
				hmParameterModelEntryToSimilarity.put(parameterModelEntry, similarity);
				if(hmFrequency.containsKey(parameterModelEntry.getParameterContent().getAbsStringRep())){
					int count= hmFrequency.get(parameterModelEntry.getParameterContent().getAbsStringRep());
					hmFrequency.put(parameterModelEntry.getParameterContent().getAbsStringRep(), count+1);
				}
				else{
					hmFrequency.put(parameterModelEntry.getParameterContent().getAbsStringRep(),1);
				}
			}
			
			//Step-4: Sort parameter model entries
			Collections.sort(possibleCandidateList, new Comparator<ParameterModelEntry>() {

				public int compare(ParameterModelEntry o1, ParameterModelEntry o2) {
					// TODO Auto-generated method stub
					float f1 = (Float)hmParameterModelEntryToSimilarity.get(o1);
		        	float f2 = (Float)hmParameterModelEntryToSimilarity.get(o2);
				        			
		        	if(f1>f2){
		        		return -1;
		        	}
		        	else if(f1<f2){
		        		return 1;
		        	}
		        	else {
		        		if(hmFrequency.get(o1.getParameterContent().getAbsStringRep())>hmFrequency.get(o2.getParameterContent().getAbsStringRep())){
			        		return -1;
			        	}
			        	else if(hmFrequency.get(o1.getParameterContent().getAbsStringRep())<hmFrequency.get(o2.getParameterContent().getAbsStringRep())){
			        		return 1;
			        	}
			        	else return 0;
		        	}
				}
		    });
			
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			boolean found = false;
			int rank = -1;
			System.out.print("Query: ");
			query.getParameterContent().print();
			System.out.println("Query: "+query.getFilePath()+" "+query.getModelEntry());
			for(int i=0;i<possibleCandidateList.size();i++){
				ParameterModelEntry candidate = possibleCandidateList.get(i);
				if(i<10) {
					System.out.print("["+i+"] ");
					candidate.getParameterContent().print();
				}
				rank++;
				
				if(candidate.getParameterContent() instanceof StringLiteralContent 
						&& query.getParameterContent() instanceof StringLiteralContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep())){
					found=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof NumberLiteralContent
						&& query.getParameterContent() instanceof NumberLiteralContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof ThisExpressionContent 
						&& query.getParameterContent() instanceof ThisExpressionContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof BooleanLiteralContent 
						&& query.getParameterContent() instanceof BooleanLiteralContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().toString().trim())){
					found=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof CharLiteralContent 
						&& query.getParameterContent() instanceof CharLiteralContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().toString().trim())){
					found=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof NullLiteralContent 
						&& query.getParameterContent() instanceof NullLiteralContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof NameExprContent && query.getParameterContent() instanceof NameExprContent) {
					//now replace the variable
					int parameterPosition = candidate.getParameterPosition();
					ParameterEntity parameterEntity = query.getModelEntry().getMethodCallEntity().getMethodDeclarationEntity().getParameterList().get(parameterPosition);
					String parameterName = parameterEntity.getName();
					String typeQualifiedName = parameterEntity.getTypeDescriptor().getTypeQualifiedName();
					NameExprContent nameExprContent =  (NameExprContent)candidate.getParameterContent();
					
					System.out.println("ParameterName: "+parameterName + "Type: "+nameExprContent.getTypeQualifiedName());
					SimpleNameRecommender simpleNameRecommender = new SimpleNameRecommender(simpleNameCollector, "",nameExprContent.getTypeQualifiedName());
					List<String> varList = simpleNameRecommender.recommend();
										
					System.out.println("Var List: "+varList);
					if(varList.size()>0 && varList.get(0).split(":")[0].equals(((NameExprContent)query.getParameterContent()).getIdentifier())){
						found = true;
						break;
					}
					else if(varList.size()>=1 && varList.get(1).split(":")[0].equals(((NameExprContent)query.getParameterContent()).getIdentifier())){
						rank++;
						found = true;
						break;
					}
					else if(varList.size()>=2 && varList.get(2).split(":")[0].equals(((NameExprContent)query.getParameterContent()).getIdentifier())){
						rank++;
						found = true;
						break;
					}
				}
				else if(candidate.getParameterContent() instanceof CastExpressionContent 
						&& query.getParameterContent() instanceof CastExpressionContent 
						&& candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					found=true;
					break;
				}
			}
			if(found==true) {
				resultCollector.add(rank);
			}
			else {
				System.out.println("Recommendation Not Found");
				resultCollector.add(-1);
			}
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		/*	hmFrequency.clear();
			//now remove the duplicated entry before recommendation
			//we do not insert more than three variable name
			int simplenameCounter=0;
			List<ParameterModelEntry> candidateList = new ArrayList();
			
			for(ParameterModelEntry pd:possibleCandidateList){
				
				if(pd.getParameterContent() instanceof NameExprContent ){
					NameExprContent nameExprContent = (NameExprContent)pd.getParameterContent();
					String parameterTypeQualifiedName = nameExprContent.getTypeQualifiedName();
					SimpleNameRecommender simpleNameRecommender = new SimpleNameRecommender(sn,pd.getParameterName(),parameterTypeQualifiedName);
					
					if(simplenameCounter<=2 && sn.>simplenameCounter){
						pd.setForcefulExpressioType(ParameterDescriptor.SimpleNameType);
						pd.setSecondaryObject(csnc.getUniqueVariableList().get(simplenameCounter).getName());
						candidateList.add(pd);
						simplenameCounter++;
					}
				}
				else if(hmFrequency.containsKey(pd.getParameterContent().getAbsStringRep())){
					int count= hmFrequency.get(pd.getParameterContent().getAbsStringRep());
					hmFrequency.put(pd.getParameterContent().getAbsStringRep(), count+1);
					
				}
				else{
					hmFrequency.put(pd.getParameterContent().getAbsStringRep(),1);
						
						if(pd.getParameterContent() instanceof MethodInvocationContent){
							MethodInvocationContent mic = (MethodInvocationContent)pd.getParameterContent();
							
							if(mic.getReceiver()!=null && mic.getParent() instanceof NameExprContent){
								NameExprContent nameExpressionContent = (NameExprContent)mic.getParent();
								if(nameExpressionContent.getTypeQualifiedName()!=null){
									SimpleNameCollector snc = new SimpleNameCollector(methodDeclaration,sourcePosition);
									snc.run();
									SimpleNameRecommender simpleNameRecommender = new SimpleNameRecommender(snc, "",nameExpressionContent.getTypeQualifiedName();
									List<String> varNameList = simpleNameRecommender.recommend();
									if(varNameList.size()>0){
										for(int i=0;i<varNameList.size();i++){
											
											LWParameterDescriptor newPd = pd.createClone();
											newPd.setObject(pd.getObject());
											newPd.setForcefulExpressioType(ParameterDescriptor.MethodInvocation);
											newPd.setSecondaryObject(simpleNameCollector.getUniqueVariableList().get(i).getName());
											candidateList.add(newPd);				
										}
									}
								}
								else{
									candidateList.add(pd);				
								}
							}
							else if(mic.getReceiver()!=null && mic.getParent() instanceof MethodInvocationContent)
							{
									System.out.println("I am on the other part");
									MethodInvocationContent mic2 = (MethodInvocationContent)mic.getParent();
										if(mic2.getReceiver()!=null && mic2.getParent()instanceof NameExprContent){
											NameExprContent nameExprContent2 = (NameExprContent)mic2.getParent());
											if(nameExprContent2.getTypeQualifiedName()!=null){
												SimpleNameCollector snc2 = new SimpleNameCollector(methodDeclaration,sourcePosition);
												snc2.run();
												SimpleNameRecommender simpleNameRecommender = new SimpleNameRecommender(snc2, "",nameExprContent2.getTypeQualifiedName());
												List<String> varNameList2 = simpleNameRecommender.recommend();
												if(varNameList2.size()>0){
													for(int i=0;i<varNameList2.size();i++){
													LWParameterDescriptor newPd = pd.createClone();
													newPd.setObject(pd.getObject());
													newPd.setForcefulExpressioType(ParameterDescriptor.MethodInvocation);
													newPd.setSecondaryObject(snc.getUniqueVariableList().get(i).getName());
													candidateList.add(newPd);			
													}
													}
											}
										}
										else{
											candidateList.add(pd);
										}
							}
							candidateList.add(pd);
						}
						else{
							if(pd.getParameterContent() instanceof NameExprContent){}	
							else candidateList.add(pd);
						}
					}
			}
			
			//now process the result for collection
			ResultCollector partialResultCollector = new ResultCollector();
			boolean partialFound =false;
			for(ParameterModelEntry candidate:candidateList){
				partialRank++;
				if(candidate.getParameterContent() instanceof QualifiedNameContent && query.getParameterContent() instanceof QualifiedNameContent){

					if(((QualifiedNameContent)(candidate.getParameterContent())).getScope().equals(((QualifiedNameContent)(query.getParameterContent())).getScope())){
						partialFound=true;
						break;
					}
				}
				else if(candidate.getParameterContent()instanceof ClassInstanceCreationContent && query.getParameterContent() instanceof ClassInstanceCreationContent){
					ClassInstanceCreationContent cic  = (ClassInstanceCreationContent)candidate.getParameterContent(); 
					ClassInstanceCreationContent qcic = (ClassInstanceCreationContent)query.getParameterContent();
					if(cic.getAbsStringRep().equals(qcic.getAbsStringRep())){
						partialFound =true;
						break;
					}
				}
				else if(candidate.getParameterContent() instanceof StringLiteralContent && query.getParameterContent() instanceof StringLiteralContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep())){
					partialFound=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof NumberLiteralContent && query.getParameterContent() instanceof NumberLiteralContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof ThisExpressionContent && query.getParameterContent() instanceof ThisExpressionContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof BooleanLiteralContent && query.getParameterContent() instanceof BooleanLiteralContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().toString().trim())){
					partialFound=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof CharLiteralContent && query.getParameterContent() instanceof CharLiteralContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().toString().trim())){
					partialFound=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof NullLiteralContent && query.getParameterContent() instanceof NullLiteralContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(candidate.getParameterContent() instanceof CastExpressionContent && query.getParameterContent() instanceof CastExpressionContent && candidate.getParameterContent().getAbsStringRep().equals(query.getParameterContent().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(query.getParameterContent() instanceof NameExprContent ){
					
				}
				
				else if(candidate.getParameterContent() instanceof MethodInvocationContent && query.getParameterContent() instanceof MethodInvocationContent){
					
				}
			}*/
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSSConfigurator.getInstance().init(Config.REPOSITORY_PATH,Config.EXTERNAL_DEPENDENCY_PATH);
		ModelEntryCollectionDriver modelEntryCollectionDriver = new ModelEntryCollectionDriver(Config.REPOSITORY_PATH);
		modelEntryCollectionDriver.run();
		System.out.println("Total Model Entry Keys: "+modelEntryCollectionDriver.getHmFileToModelEntries().keySet().size());
		TrainingTestGenerator trainingTestGenerator = new TrainingTestGenerator(modelEntryCollectionDriver.getHmFileToParameterModelEntries());
		trainingTestGenerator.genTrainingTestDataSet();
		ParameterRecommender parameterRecommender = new ParameterRecommender();
		parameterRecommender.train(trainingTestGenerator.getTrainingParameterModelEntryList());
		parameterRecommender.test(trainingTestGenerator.getTestParameterModelEntryList());
	}
}
