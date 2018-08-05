package com.codecompletion.parameter.logic;

import java.awt.Component;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.codecompletion.parameter.category.AdditionalQNDataCollector;
import com.codecompletion.parameter.category.ParamVariable;
import com.codecompletion.parameter.category.ParameterCategorizer;
import com.codecompletion.parameter.category.ParameterCollection;
import com.codecompletion.parameter.category.ParameterDescriptor;
import com.codecompletion.parameter.category.SimpleNameCollector;
import com.codecompletion.parameter.category.SimpleNameHistorian;
import com.codecompletion.parameter.category.SimpleNameUsageRecommender;
import com.codecompletion.parameter.category.VariableFinder;
import com.codecompletion.parameter.evaluation.ParameterFilterer;
import com.codecompletion.parameter.node.BooleanLiteralContent;
import com.codecompletion.parameter.node.ClassInstanceCreationContent;
import com.codecompletion.parameter.node.MethodInvocationContent;
import com.codecompletion.parameter.node.NullLiteralContent;
import com.codecompletion.parameter.node.NumberLiteralContent;
import com.codecompletion.parameter.node.QualifiedNameContent;
import com.codecompletion.parameter.node.SimpleNameContent;
import com.codecompletion.parameter.node.StringLiteralContent;
import com.codecompletion.parameter.node.ThisExpressionContent;

import plubibtest3.handlers.ConfigManager;
import plubibtest3.handlers.LCS;
import simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;

public class LWParameterEvaluation extends ASTVisitor {

	private int ACCEPTED_RANGE = 9;
	private int failedPhaseOne;
	
	private int totalMethodCalls;       //any method call appears in source code
	private int interestingMethodCalls; //method calls that we target for
	private int unboundMethodCalls;     //these are the method calls that are not called within a method body
	private int methodCallWithParameter;
	
	private int totalRank;
	
	private ParameterCollection parameterCollection;
	private MethodDeclaration methodDeclaration;
	
	private TypeDeclaration td;
	private String fileName;
	
	private CompilationUnit compilationUnit;
	private ICompilationUnit iCompilationUnit;

	private MultiKeyMap mkMethodExample;
	private MultiKeyMap mkMethodExampleCtaegorizerCount;
	private MultiKeyMap mkTestData;
	private MultiKeyMap mkAllData;
	private int totalCorrectResult=0;
	private int totalCorrectResultPhaseOne=0;
	
	private long time;
	private int testCases=0;
	private AdditionalQNDataCollector qnDataCollector;
	
	private HashMap hmDataTestMapByExpCategory;
	private HashMap hmCorrectTestMapByExpCategory;
	private HashMap hmCorrectTestMapByExpCategoryTop1;
	private HashMap hmCorrectTestMapByExpCategoryTop3;
	private HashMap hmCorrectTestMapByExpCategoryTop5;
	private HashMap hmCorrectTestMapByExpCategoryTop10;
	
	
	private HashMap hmPartialTestMapByExpCategoryTop1;
	private HashMap hmPartialTestMapByExpCategoryTop3;
	private HashMap hmPartialTestMapByExpCategoryTop5;
	private HashMap hmPartialTestMapByExpCategoryTop10;
	
	private HashMap hmRecommendationMadeTestMapByExpCategory;
	private HashMap hmWholeTestMapByExpCategory;
	
	private SimpleNameHistorian simpleNameHistorian;
	private MultiKeyMap parameterNameMap;
	private boolean testPhase;
	private SimpleNameUsageRecommender simpleNameUsageRecommender; 
	private MultiKeyMap mkGpdByMethod;
	
	private ArrayList<LWParameterModelEntry> parameterModelEntryList;
	private HashMap hmCompilationUnits;
	
	public ArrayList<LWParameterModelEntry> getParameterModelEntryList() {
		return parameterModelEntryList;
	}

	public SimpleNameUsageRecommender getSimpleNameUsageRecommender() {
		return simpleNameUsageRecommender;
	}

	public void setSimpleNameUsageRecommender(
			SimpleNameUsageRecommender simpleNameUsageRecommender) {
		this.simpleNameUsageRecommender = simpleNameUsageRecommender;
	}

	public long getTime() {
		return time;
	}

	public SimpleNameHistorian getSimpleNameHistorian() {
		return simpleNameHistorian;
	}

	public void setSimpleNameHistorian(SimpleNameHistorian simpleNameHistorian) {
		this.simpleNameHistorian = simpleNameHistorian;
	}

	public boolean isTestPhase() {
		return testPhase;
	}

	public void setTestPhase(boolean testPhase) {
		this.testPhase = testPhase;
	}

	public int getInterestingMethodCalls() {
		return interestingMethodCalls;
	}
    
	public void setInterestingMethodCalls(int interestingMethodCalls) {
		this.interestingMethodCalls = interestingMethodCalls;
	}

	public ParameterCollection getParameterCollection() {
		return parameterCollection;
	}

	public AdditionalQNDataCollector getQnDataCollector() {
		return qnDataCollector;
	}

	public void setQnDataCollector(AdditionalQNDataCollector qnDataCollector) {
		this.qnDataCollector = qnDataCollector;
	}
	private void printDebug(ParameterDescriptor query, ParameterDescriptor pd){
		if(pd.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType) return;
		String methodBody = "";
		try {
			methodBody = pd.getiCompilationUnit().getSource().substring
					(pd.getMethodDeclaration().getStartPosition(), pd.getExpression().getStartPosition());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Expected = "+query.getExpression().toString());
		System.out.println("Query NeighborList = "+query.getNeighborList());
		System.out.println("Parameter NeighborList = "+pd.getNeighborList());
		System.out.println("Similarity = "+pd.getObject());
		
		System.out.println("Parameter body = "+methodBody);
	}

	public LWParameterEvaluation(MultiKeyMap mkTestData, MultiKeyMap mkAllData, MultiKeyMap _parameterNameMap,HashMap hmCompilationUnits) {
		this.time=0;
		this.hmCorrectTestMapByExpCategory = new HashMap();
		this.hmCorrectTestMapByExpCategoryTop1 = new HashMap();
		this.hmCorrectTestMapByExpCategoryTop3 = new HashMap();
		this.hmCorrectTestMapByExpCategoryTop5 = new HashMap();
		this.hmCorrectTestMapByExpCategoryTop10 = new HashMap();
		
		this.hmDataTestMapByExpCategory    = new HashMap();
		this.hmPartialTestMapByExpCategoryTop1  = new HashMap();
		this.hmPartialTestMapByExpCategoryTop3  = new HashMap();
		this.hmPartialTestMapByExpCategoryTop5  = new HashMap();
		this.hmPartialTestMapByExpCategoryTop10 = new HashMap();
		
		
		this.hmRecommendationMadeTestMapByExpCategory = new HashMap();
		this.hmWholeTestMapByExpCategory = new HashMap();
		this.hmCompilationUnits = hmCompilationUnits;
		this.parameterNameMap = _parameterNameMap;
		
		totalMethodCalls = 0;
		interestingMethodCalls = 0;
		unboundMethodCalls = 0;
		
		this.simpleNameHistorian = new SimpleNameHistorian();
		this.totalCorrectResultPhaseOne = 0;
		this.failedPhaseOne = 0;
		this.parameterCollection  = new ParameterCollection();
		this.mkMethodExample      = new MultiKeyMap();
		this.mkMethodExampleCtaegorizerCount = new MultiKeyMap();
		this.mkTestData = mkTestData; 
		this.mkAllData  = mkAllData;
		this.testPhase = false;
		this.totalCorrectResult=0;
		this.testCases =0;
		this.parameterModelEntryList = new ArrayList();
		this.qnDataCollector = new AdditionalQNDataCollector();
		this.totalRank = 0;
		this.mkGpdByMethod = new MultiKeyMap();
	}

	public int getTotalMethodCalls() {
		return totalMethodCalls;
	}

	public void setMethodCalls(int methodCalls) {
		totalMethodCalls = methodCalls;
	}

	public void updateTotalRank(int rank){
		totalRank = totalRank+rank;
	}
	
	/*
	 * For each method invocation collect related information so that we can
	 * find a match later
	 */
	
	public int getTotalRank() {
		return totalRank;
	}

	public void setTotalRank(int totalRank) {
		this.totalRank = totalRank;
	}

	public void addExternalJars(IProject project) throws JavaModelException{
		/*Add the external jars*/
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] rawClassPath  = javaProject.getRawClasspath();
		List list = new LinkedList (java.util.Arrays.asList(rawClassPath));
		for(String path:ConfigManager.getInstance().getJarPathList()){
			String jarPath = path.toString();
			boolean isAlreadyAdded = false;
			for(IClasspathEntry cpe: rawClassPath){
				isAlreadyAdded = cpe.getPath().toOSString().equals(jarPath);
				if(isAlreadyAdded) break;
			}
			if(!isAlreadyAdded){
				IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(jarPath),null,null);
				list.add(jarEntry);
			}
		}
		IClasspathEntry[] newClassPath = (IClasspathEntry[]) list.toArray(new IClasspathEntry[0]);
		javaProject.setRawClasspath(newClassPath, null);
		//End of jar adding
	}

	public void run() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {

			try {
				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {

					//add the external jars
					this.addExternalJars(project);
					IPackageFragment[] packages = JavaCore.create(project)
							.getPackageFragments();
					// parse(JavaCore.create(project));
					for (IPackageFragment mypackage : packages) {
						if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
							for (ICompilationUnit unit : mypackage
									.getCompilationUnits()) {
								// Now create the AST for the ICompilationUnits
								CompilationUnit cu = LWParameterEvaluation
										.getCompilationUnit(unit);

								fileName = (unit.getPath().toFile()
										.getAbsolutePath());
								if(cu.getPackage()!=null)
									fileName = cu.getPackage().getName().getFullyQualifiedName()+"."+cu.getJavaElement().getElementName().substring(0, cu.getJavaElement().getElementName().length()-".java".length());
								else{
									fileName = cu.getJavaElement().getElementName().substring(0, cu.getJavaElement().getElementName().length()-".java".length());
								}
								//if(this.hmCompilationUnits.containsKey(fileName)==false) continue;
								iCompilationUnit = unit;
								compilationUnit = cu;
								cu.accept(this);
							}
						}
					}
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}

	/*public boolean visit(QualifiedName node){
		
		if(node.getQualifier().resolveBinding() !=null && node.getQualifier().resolveBinding().getKind()==IBinding.VARIABLE){
	
			String name  = node.getQualifier().resolveTypeBinding().getQualifiedName();
			String variable  = node.getName().getIdentifier();
			//System.out.println("Here: "+name+", "+variable);
		}
		return true;
	}*/
	@Override
	public boolean visit(MethodInvocation node) {

		totalMethodCalls++;
		IMethodBinding mb = node.resolveMethodBinding();
		if (mb == null || mb.getDeclaringClass() == null
				|| mb.getMethodDeclaration() == null) {
			return true;
		}

		Expression exp = node.getExpression();
		ITypeBinding tb = null;

		if (exp != null) {
			tb = exp.resolveTypeBinding();

			if (tb == null) {
				if (exp instanceof FieldAccess) {
					FieldAccess fa = (FieldAccess) exp;
					IVariableBinding vb = fa.resolveFieldBinding();
					if (vb != null) {
						tb = vb.getType();
					} else {
						tb = fa.resolveTypeBinding();
					}
				} else if (exp instanceof SimpleName) {
					SimpleName sn = (SimpleName) exp;
					tb = sn.resolveTypeBinding();
				} else if (exp instanceof MethodInvocation) {
					MethodInvocation mi = (MethodInvocation) exp;
					tb = mi.resolveTypeBinding();
				} else if (exp instanceof QualifiedName) {
					QualifiedName qn = (QualifiedName) exp;
					tb = qn.resolveTypeBinding();
				} else if (exp instanceof TypeLiteral) {
					TypeLiteral tl = (TypeLiteral) exp;
					tb = tl.resolveTypeBinding();
				} else if (exp instanceof ArrayAccess) {
					ArrayAccess aa = (ArrayAccess) exp;
					tb = aa.resolveTypeBinding();
				} else if (exp instanceof ClassInstanceCreation) {
					ClassInstanceCreation cic = (ClassInstanceCreation) exp;
					tb = cic.resolveTypeBinding();
				} else if (exp instanceof ParenthesizedExpression) {
					ParenthesizedExpression pe = (ParenthesizedExpression) exp;
					tb = pe.resolveTypeBinding();
				} else if (exp instanceof ThisExpression) {
					ThisExpression te = (ThisExpression) exp;
					tb = te.resolveTypeBinding();
				} else if (exp instanceof StringLiteral) {
					StringLiteral sl = (StringLiteral) exp;
					tb = sl.resolveTypeBinding();
				} else if (exp instanceof ArrayCreation) {
					ArrayCreation ac = (ArrayCreation) exp;
					tb = ac.resolveTypeBinding();
				} else {
					throw new IllegalStateException(
							"Need to handle Expressions of type: "
									+ exp.getClass().getName());
				}
			}
		} else {
			tb = td.resolveBinding();
		}

		if (tb == null) {
			return true;
		}

		//System.out.println("Total method Cals: " + totalMethodCalls
		//		+ "  Interesting: " + interestingMethodCalls + "  Type: "
		//		+ tb.getQualifiedName());
		
		if (isInMethod(node) == false) {
			unboundMethodCalls++;
		}
		else if (isInMethod(node) && (ConfigManager.getInstance().isInteresting(tb) || ConfigManager.getInstance().isInteresting(tb))) {
			interestingMethodCalls++;

			// Collect the method parameters type
			List<Expression> argumentsExpression = node.arguments();
			if(argumentsExpression.size()>0)
				this.methodCallWithParameter++;
			
			// Collect the method parameters type
			ITypeBinding ptb[] = node.resolveMethodBinding()
								.getParameterTypes();
			String parameters[] = new String[ptb.length];
			for (int i = 0; i < ptb.length; i++) {
				parameters[i] = ptb[i].getQualifiedName();
			}
			
			String variableType = null; //is the variable is a local variable, method parameter, field variable
			if(node.getExpression() instanceof SimpleName){
				SimpleName sn = (SimpleName)node.getExpression();
				variableType = this.getVariableCategory(sn);
			}
			
			for(int position =0; position<argumentsExpression.size();position++){
				Expression paramExp = argumentsExpression.get(position);
				{
					if(this.isTestPhase() && this.mkTestData.containsKey(paramExp.getStartPosition(),this.fileName)){

						LWParameterDescriptor pd = (LWParameterDescriptor)this.mkTestData.get(paramExp.getStartPosition(),this.fileName);
						LWParameterModelEntry  pme = new LWParameterModelEntry(pd);
						if(
								pd.getParameterContent()  instanceof QualifiedNameContent
								||pd.getParameterContent() instanceof MethodInvocationContent
							    ||pd.getParameterContent() instanceof NumberLiteralContent
								||pd.getParameterContent() instanceof ThisExpressionContent
								||pd.getParameterContent() instanceof StringLiteralContent
								||pd.getParameterContent() instanceof NullLiteralContent
								||pd.getParameterContent() instanceof BooleanLiteralContent
								||pd.getParameterContent() instanceof SimpleNameContent
								||pd.getParameterContent() instanceof ClassInstanceCreationContent
							)
						{
							System.out.println("++++++++++++++++++++++++++++Evaluate a test case...   Test case number: "+this.testCases+" :"+"  Test Data Size: "+this.mkTestData.keySet().size());		
							System.out.println("Enclosing Method Expression:"+node.toString());
							System.out.println("Test Parameter Expression Detail: "+paramExp.toString()+"Test Parameter Expression Type: " + ParameterCategorizer.getExpressionType(paramExp));
							
							/*ArrayList<LWParameterDescriptor> possibleCandidates = (ArrayList)this.mkGpdByMethod.get(pd.getMethodName(),pd.getReceiverType());
							
							boolean found=false;
							if(possibleCandidates!=null){ 
							for(LWParameterDescriptor pmd:possibleCandidates){
								if(pmd.getParameterContent() instanceof QualifiedNameContent||
										pmd.getParameterContent() instanceof MethodInvocationContent||
										pmd.getParameterContent() instanceof StringLiteralContent||
										pmd.getParameterContent() instanceof NumberLiteralContent||
										pmd.getParameterContent() instanceof ThisExpressionContent
										){
									found=true;
									break;
								}
							}
							if(found==true)*/
							this.evaluateNov16(paramExp,pme,node,this.getEnclosingMethodName(node),this.iCompilationUnit,parameters[position], tb);
							System.out.println("Available Memory: "+Runtime.getRuntime().freeMemory());
							System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
							
						}
					}
					else if(this.isTestPhase()==false &&  this.mkTestData.containsKey(paramExp.getStartPosition(),this.fileName)==false){
						//This is a training case
						
						LWParameterDescriptor pd = (LWParameterDescriptor)this.mkAllData.get(paramExp.getStartPosition(),this.fileName);
						System.out.println("PD = "+pd+"  File: "+this.fileName+" Start Position: "+paramExp.getStartPosition());
						System.out.println("Training Data Size "+mkAllData.keySet().size()+"  Memory: "+Runtime.getRuntime().freeMemory()+"  ParameterModelEntry: "+this.parameterModelEntryList.size());
						if(pd!=null )
						{
							if(this.mkGpdByMethod.containsKey(pd.getMethodName(),pd.getReceiverType())){
							
								ArrayList<LWParameterDescriptor>	list = (ArrayList)this.mkGpdByMethod.get(pd.getMethodName(),pd.getReceiverType());
								list.add(pd);
							}
							else{
								ArrayList<LWParameterDescriptor>	list = new ArrayList();
								list.add(pd);
								this.mkGpdByMethod.put(pd.getMethodName(),pd.getReceiverType(), list);
							}
						
						LWParameterModelEntry  pme = new LWParameterModelEntry(pd);
						this.parameterModelEntryList.add(pme);
						}
					}
					else{
						//throw new RuntimeException("This is not possible. We found an exception while evaluating parameter data");
					}
				}
			}
			
		}
		return true;
	}
	
	public void load(String file){
		try{
			FileInputStream fin = new FileInputStream(file);
			ObjectInputStream is = new ObjectInputStream(fin);
			this.parameterModelEntryList = (ArrayList)is.readObject();
			is.close();
		}catch(Exception exp){
			exp.printStackTrace();
		}
		
		for(LWParameterModelEntry pme:this.parameterModelEntryList){
			if(this.mkGpdByMethod.containsKey(pme.getPd().getMethodName(),pme.getPd().getReceiverType())){
				
				ArrayList<LWParameterDescriptor>	list = (ArrayList)this.mkGpdByMethod.get(pme.getPd().getMethodName(),pme.getPd().getReceiverType());
				list.add(pme.getPd());
			}
			else{
				ArrayList<LWParameterDescriptor>	list = new ArrayList();
				list.add(pme.getPd());
				this.mkGpdByMethod.put(pme.getPd().getMethodName(),pme.getPd().getReceiverType(), list);
			}
		}
	}
	public void store(String file){
		//now our goal is to serialize parameter model entry list
		try{
			FileOutputStream fout = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this.parameterModelEntryList);
			oos.close();
		}catch(Exception exp){
			exp.printStackTrace();
		}
	}
	
	public void updateWholeTestMapDataByCategory(LWParameterModelEntry query){
		
		if(this.hmWholeTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType())){
			int count = (Integer)this.hmWholeTestMapByExpCategory.get(query.getPd().getParameterExpressionType());
			this.hmWholeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),count+1);
			
		}
		else{
			this.hmWholeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),1);
		}
	}
	
	public void updateRecommendationMadeTestMapDataByCategory(LWParameterModelEntry query){
		
		if(this.hmRecommendationMadeTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType())){
			int count = (Integer)this.hmRecommendationMadeTestMapByExpCategory.get(query.getPd().getParameterExpressionType());
			this.hmRecommendationMadeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),count+1);
			
		}
		else{
			this.hmRecommendationMadeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),1);
		}
	}
	
	public void updatePartialTestMapDataByCategory(LWParameterModelEntry query,int rank){
		System.out.println("Query Expression type:"+query.getPd().getParameterExpressionType());
		System.out.println("Rank = "+rank);
		
		/*if(this.hmPartialTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType())){
			int count = (Integer)this.hmPartialTestMapByExpCategory.get(query.getPd().getParameterExpressionType());
			this.hmPartialTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),count+1);
			
		}
		else{
			this.hmPartialTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),1);
		}*/
		
		
		if(rank>=0 && rank<=0){
			if(this.hmPartialTestMapByExpCategoryTop1.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmPartialTestMapByExpCategoryTop1.get(query.getPd().getParameterExpressionType());
				this.hmPartialTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(),(count+1));
		}
		else{
			this.hmPartialTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(),1);
			}
		}
		
		if(rank>=0 && rank<=2){	
			if(this.hmPartialTestMapByExpCategoryTop3.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmPartialTestMapByExpCategoryTop3.get(query.getPd().getParameterExpressionType());
				this.hmPartialTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(),(count+1));
			}
		else{
			this.hmPartialTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(),1);
			}
		}
		
		if(rank>=0 && rank<=4){	
			if(this.hmPartialTestMapByExpCategoryTop5.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmPartialTestMapByExpCategoryTop5.get(query.getPd().getParameterExpressionType());
				this.hmPartialTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(),(count+1));
			}
		else{
			this.hmPartialTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(),1);
			}
		}
		
			if(rank>=0 && rank<=9){	
				if(this.hmPartialTestMapByExpCategoryTop10.containsKey(query.getPd().getParameterExpressionType())){
					int count = (Integer)this.hmPartialTestMapByExpCategoryTop10.get(query.getPd().getParameterExpressionType());
					this.hmPartialTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(),(count+1));
				}
			else{
				this.hmPartialTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(),1);
				}
			}
	}
	
	public void updateTotalTestMapDataByCategory(LWParameterModelEntry query){
		
		if(this.hmDataTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType())){
			int count = (Integer)this.hmDataTestMapByExpCategory.get(query.getPd().getParameterExpressionType());
			this.hmDataTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),count+1);
			
		}
		else{
			this.hmDataTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),1);
		}
		
	}
	public void updateCorrectTestMapDataByCategory(LWParameterModelEntry query,int rank){
		if(this.hmCorrectTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType())){
			int count = (Integer)this.hmCorrectTestMapByExpCategory.get(query.getPd().getParameterExpressionType());
			this.hmCorrectTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),(count+1));
		}
		else{
			this.hmCorrectTestMapByExpCategory.put(query.getPd().getParameterExpressionType(),1);
		}
		
		if(rank>=0 && rank<=0){
			if(this.hmCorrectTestMapByExpCategoryTop1.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmCorrectTestMapByExpCategoryTop1.get(query.getPd().getParameterExpressionType());
				this.hmCorrectTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(),(count+1));
		}
		else{
			this.hmCorrectTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(),1);
			}
		}
		
		if(rank>=0 && rank<=2){	
			if(this.hmCorrectTestMapByExpCategoryTop3.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmCorrectTestMapByExpCategoryTop3.get(query.getPd().getParameterExpressionType());
				this.hmCorrectTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(),(count+1));
			}
		else{
			this.hmCorrectTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(),1);
			}
		}
		
		if(rank>=0 && rank<=4){	
			if(this.hmCorrectTestMapByExpCategoryTop5.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmCorrectTestMapByExpCategoryTop5.get(query.getPd().getParameterExpressionType());
				this.hmCorrectTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(),(count+1));
			}
		else{
			this.hmCorrectTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(),1);
			}
		}
		
		if(rank>=0 && rank<=9){	
			if(this.hmCorrectTestMapByExpCategoryTop10.containsKey(query.getPd().getParameterExpressionType())){
				int count = (Integer)this.hmCorrectTestMapByExpCategoryTop10.get(query.getPd().getParameterExpressionType());
				this.hmCorrectTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(),(count+1));
			}
		else{
			this.hmCorrectTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(),1);
			}
		}
	}
	
	public void evaluateNov16(Expression queryExpression, final LWParameterModelEntry query, MethodInvocation queryMi, MethodDeclaration queryMd, ICompilationUnit queryICompilationUnit, String queryExpectedType,ITypeBinding rtb){
			
		ArrayList<LWParameterDescriptor> paramerList = new ArrayList();
		final HashMap<String,Integer> fqMap = new HashMap();
		for(LWParameterModelEntry pme:this.parameterModelEntryList){
			int score = 0;
			//System.out.println("PME = "+pme.getPd()+"  Query = "+query.getPd());
			
			if(pme.getPd().getParameterPosition()==query.getPd().getParameterPosition() &&
					pme.getPd().getMethodName().equals(query.getPd().getMethodName())	
					)
			{
				LWParameterDescriptor pd = pme.getPd();
				//float similarity = new Levenshtein().getSimilarity(pd.getNeighborList(),query.getPd().getNeighborList());
				float similarity = new CosineSimilarity().getSimilarity(pd.getNeighborList(),query.getPd().getNeighborList());
				pd.setObject(similarity);
				paramerList.add(pd);
				if(fqMap.containsKey(pd.getParameterExpressionType())){
					int count = (Integer)fqMap.get(pd.getParameterExpressionType());
					fqMap.put(pd.getParameterExpressionType(),count+1);
				}
				else{
					fqMap.put(pd.getParameterExpressionType(),1);
				}
			}
		}
		/*ArrayList<String> frequencyList = new ArrayList(fqMap.keySet());
		Collections.sort(frequencyList, new Comparator<String>() {
	        @Override
	        public int compare(String  fruit1, String  fruit2)
	        {
	        	       			
	        	if(fqMap.get(fruit1)>fqMap.get(fruit2)){
	        		return -1;
	        	}
	        	else if(fqMap.get(fruit1)<fqMap.get(fruit2)){
	        		return 1;
	        	}
	        	else return 0;
	        }
	    });
		*/
		
		/*Collections.sort(paramList, new Comparator<ParameterDescriptor>() {
	        @Override
	        public int compare(ParameterDescriptor  fruit1, ParameterDescriptor  fruit2)
	        {
	        	float f1 = (Float)fruit1.getObject();
	        	float f2 = (Float)fruit2.getObject();
			        			
	        	if(f1>f2){
	        		return -1;
	        	}
	        	else if(f1<f2){
	        		return 1;
	        	}
	        	else return 0;
	        }
	    });*/
		int rank=-1;
		boolean found=false;
		this.updateWholeTestMapDataByCategory(query);
		
		 if(query.getPd().getParameterExpressionType().equals("QualifiedName")||
				 query.getPd().getParameterExpressionType().equals("StringLiteral")||
				 query.getPd().getParameterExpressionType().equals("ThisExpression")||
				 query.getPd().getParameterExpressionType().equals("NumberLiteral")||
				 query.getPd().getParameterExpressionType().equals("BooleanLiteral")||
				 query.getPd().getParameterExpressionType().equals("NullLiteral")||
				 query.getPd().getParameterExpressionType().equals("SimpleName")||
				 query.getPd().getParameterExpressionType().equals("MethodInvocation")
				 ||query.getPd().getParameterExpressionType().equals("ClassInstanceCreation")
				 //||query.getPd().getParameterExpressionType().equals("ArrayAccess")
				 //||query.getPd().getParameterExpressionType().equals("CastExpression")
				 
				 
				 ){
			
			 if(query.getPd().getParameterContent() instanceof MethodInvocationContent){
				 query.getPd().setForcefulExpressioType(ParameterDescriptor.QueryMethodInvocation);
			 }
			 
			 System.out.println("Main Method Invocation: "+query.getPd().getMethodInvocation());
			 System.out.println("String Method Invocation: "+query.getPd().getParameterContent().getStringParamNode());
			 //System.out.println("String Comparison Method Invocation: "+query.getPd().getMethodInvocationComparisonStringRep(query.getPd().getExpression()));
			 
			 
			System.out.println("**I am on qualified Name Query part: ");
			long startTime = System.currentTimeMillis();
			
			// we separately search for item that matches with the expected type 
			final HashMap<String,Integer> hmFrequency = new HashMap();
			
			ArrayList<LWParameterDescriptor> candidateList = new ArrayList();
			ArrayList<LWParameterDescriptor> possibleCandidateList = new ArrayList();
			ArrayList<LWParameterDescriptor> possibleCandidates = (ArrayList)this.mkGpdByMethod.get(query.getPd().getMethodName(),query.getPd().getReceiverType());
			
			//ArrayList<ParameterModelEntry> candidateStringList = new ArrayList();
			//ArrayList<String> qualifierList = new ArrayList();
			
			//ArrayList<String> paramVariableTypeList = new ArrayList();
			//ArrayList<LWParameterModelEntry> paramVariableCandidateNameList = new ArrayList();
			
			
			String parameterName = (String)this.parameterNameMap.get(rtb.getQualifiedName(),queryMi.getName().getFullyQualifiedName(),queryMi.arguments().size(),query.getPd().getParameterPosition());
			SimpleNameCollector csnc;
			if(parameterName==null)
			csnc = new SimpleNameCollector("",queryExpectedType,queryMi,queryICompilationUnit,queryMd,rtb);
			else{
				csnc = new SimpleNameCollector(parameterName,queryExpectedType,queryMi,queryICompilationUnit,queryMd,rtb);		
			}
			ArrayList<ParamVariable> paramVariableList = csnc.getUniqueVariableList();
			int paramVariableListCounter=0;

			//if(possibleCandidates==null ||possibleCandidates.size()>0)
			//	return;
			System.out.println("ParameterModelEntryListSize = "+parameterModelEntryList.size());
			HashMap valueMap = new HashMap();
			if(possibleCandidates==null){
				possibleCandidates = new ArrayList();
			}
			//for(ParameterModelEntry pm:this.parameterModelEntryList){
			for(LWParameterDescriptor pd:possibleCandidates){
				
				{	if(
							//pme.getPd().getParameterExpressionType().equals(query.getPd().getParameterExpressionType()) 
							pd.getMethodName().equals(query.getPd().getMethodName()) &&
							//pme.getPd().getParameterExpressionType().equals(query.getPd().getParameterExpressionType())
						    pd.getParameterPosition()==query.getPd().getParameterPosition()
						    //&& ConfigManager.getInstance().isTypeHierarchyMatches(pd.getTb().getQualifiedName(),query.getPd().getTb())==true
									
						    &&pd.getReceiverType().equals(query.getPd().getReceiverType())
						    && pd.getMethodArguments()==query.getPd().getMethodArguments()
						    //&& pme.getPd().getReceiverType().equals(query.getPd().getReceiverType())
						    //&& pme.getPd().getMethodInvocation().arguments().size()==query.getPd().getMethodInvocation().arguments().size()
						    //&& ConfigManager.getInstance().isTypeHierarchyMatches(pme.getPd().getTb().getQualifiedName(),query.getPd().getTb())==true
						    //&& pd.getParameters().equals(query.getPd().getParameters()) 
						    && (
						    	pd.getParameterExpressionType().equals("QualifiedName")||
						    	pd.getParameterExpressionType().equals("StringLiteral")||
								pd.getParameterExpressionType().equals("NumberLiteral")||
						    	pd.getParameterExpressionType().equals("ThisExpression")||
								pd.getParameterExpressionType().equals("MethodInvocation")
								||pd.getParameterExpressionType().equals("NullLiteral")
								||pd.getParameterExpressionType().equals("SimpleName")	
								||pd.getParameterExpressionType().equals("BooleanLiteral")	    	
								||pd.getParameterExpressionType().equals("ClassInstanceCreation")
								
								
								)
							//&&(ParameterFilterer.isfilter(queryExpression,queryMi,queryMd,new LWParameterModelEntry(pd), query))==false 							 	 
							//pme.getPd().getReceiverType().equals(query.getPd().getReceiverType())
							)
					{
						///locate the variable that matches with
						//if(((Float)pd.getObject())>=0.10)
						possibleCandidateList.add(pd);
						
						if(hmFrequency.containsKey(pd.getAbsStringRep())){
							int count= hmFrequency.get(pd.getAbsStringRep());
							hmFrequency.put(pd.getAbsStringRep(), count+1);
						}
						else{
							hmFrequency.put(pd.getAbsStringRep(),1);
							}
					}
				}
			}
			
			//check whether possible candidate lsit only contain SimpleName
			/*System.out.println("Possible ParameterCandidateList: before"+possibleCandidateList.size());
			if(possibleCandidateList.size()<=3){
				possibleCandidateList.clear();
				hmFrequency.clear();
				for(ParameterModelEntry pme:this.parameterModelEntryList){
					
					//if(pme.getPd().getExpression() instanceof NumberLiteral )
					{ParameterDescriptor pd = pme.getPd();
						if(
								//pme.getPd().getParameterExpressionType().equals(query.getPd().getParameterExpressionType()) 
								pd.getMethodName().equals(query.getPd().getMethodName()) &&
								//pme.getPd().getParameterExpressionType().equals(query.getPd().getParameterExpressionType())
							    pd.getParameterPosition()==query.getPd().getParameterPosition()&&
							    ConfigManager.getInstance().isTypeHierarchyMatches(pme.getPd().getTb().getQualifiedName(),query.getPd().getTb())==true
								&&		
							    pd.getMethodInvocation().arguments().size()==query.getPd().getMethodInvocation().arguments().size()
							    //&& pme.getPd().getReceiverType().equals(query.getPd().getReceiverType())
							    //&& pme.getPd().getMethodInvocation().arguments().size()==query.getPd().getMethodInvocation().arguments().size()
							    //&& ConfigManager.getInstance().isTypeHierarchyMatches(pme.getPd().getTb().getQualifiedName(),query.getPd().getTb())==true
							    //&& pd.getParameters().equals(query.getPd().getParameters()) 
							    && !(pd.getExpression() instanceof PrefixExpression ||
										pd.getExpression() instanceof InfixExpression ||
										pd.getExpression() instanceof ConditionalExpression||
									 	pd.getExpression() instanceof SimpleName||
										pd.getExpression() instanceof Assignment||
										pd.getExpression() instanceof BooleanLiteral||
										pd.getExpression() instanceof NullLiteral
									    
										//pd.getExpression() instanceof ClassInstanceCreation
										)
									&& (ParameterFilterer.isfilter(new ParameterModelEntry(pd), query))==false
										   
								
								//pme.getPd().getReceiverType().equals(query.getPd().getReceiverType())
								)
						{
							//System.out.println("QualifiedParamrte: "+query.getPd().getParameters());
							// System.out.println("Other: "+pme.getPd().getParameters());
							//ParameterModelEntry pme = new ParameterModelEntry(pd);
							//if(((Float)pd.getObject())<0.25)
							//	continue;
								
							if(pd.getExpression()instanceof MethodInvocation){
								MethodInvocation mi = (MethodInvocation)pd.getExpression();
								if(mi.getExpression()!=null && mi.getExpression()instanceof SimpleName){
									SimpleName sn = (SimpleName)mi.getExpression();
									if(sn.resolveTypeBinding()!=null && sn.resolveBinding()!=null && sn.resolveBinding().getKind()==IBinding.VARIABLE)
									{
										System.out.println("Example receiver type: "+sn.resolveTypeBinding().getQualifiedName());
										SimpleNameCollector snc = new SimpleNameCollector(sn.resolveTypeBinding().getQualifiedName(),query.getPd().getMethodInvocation(),query.getPd().getMethodDeclaration(),sn.resolveTypeBinding());
										
										if(snc.getUniqueVariableList().size()>0){
											for(int i=0;i<snc.getUniqueVariableList().size()&& i<3;i++){
												ParameterDescriptor newPd = pd.createClone();
												newPd.setObject(pd.getObject());
												newPd.setForcefulExpressioType(ParameterDescriptor.MethodInvocation);
												newPd.setSecondaryObject(snc.getUniqueVariableList().get(i).getName());
												possibleCandidateList.add(newPd);				
												
											}
										}
									}
								}
								else if(pd.getExpression()!=null && pd.getExpression() instanceof MethodInvocation){
										System.out.println("I am on the other part");
										MethodInvocation mi2 = (MethodInvocation)pd.getExpression();
											if(mi2.getExpression()!=null && mi2.getExpression()instanceof SimpleName){
												SimpleName sn2 = (SimpleName)mi2.getExpression();
												if(sn2.resolveTypeBinding()!=null && sn2.resolveBinding()!=null && sn2.resolveBinding().getKind()==IBinding.VARIABLE){
													SimpleNameCollector snc = new SimpleNameCollector(sn2.resolveTypeBinding().getQualifiedName(),query.getPd().getMethodInvocation(),query.getPd().getMethodDeclaration(),sn2.resolveTypeBinding());
													System.out.println("snc size: "+snc.getUniqueVariableList().size());
													System.out.println("Example receiver type: "+sn2.resolveTypeBinding().getQualifiedName());
													
													if(snc.getUniqueVariableList().size()>0){
														for(int i=0;i<snc.getUniqueVariableList().size() && i<3;i++){
															ParameterDescriptor newPd = pd.createClone();
															newPd.setObject(pd.getObject());
															newPd.setForcefulExpressioType(ParameterDescriptor.MethodInvocation);
															newPd.setSecondaryObject(snc.getUniqueVariableList().get(i).getName());
															possibleCandidateList.add(newPd);				
															
														}
													}
												}
											}
									}
									else{
										possibleCandidateList.add(pd);
									}
											
								
								}	
								
							else
									possibleCandidateList.add(pd);	
							
							//locate the variable that matches with
							if(hmFrequency.containsKey(pme.getPd().getStringRep(pme.getPd().getExpression()))){
								int count= hmFrequency.get(pme.getPd().getStringRep(pme.getPd().getExpression()));
								hmFrequency.put(pme.getPd().getStringRep(pme.getPd().getExpression()), count+1);
								
							}
							else{
								hmFrequency.put(pme.getPd().getStringRep(pme.getPd().getExpression()),1);
							}
						}
					}
				}
			}*/
			
			//for(int j=paramVariableListCounter;j<=4&&j<csnc.getUniqueVariableList().size();j++){
				/*ParameterDescriptor tempPd = new ParameterDescriptor(query.getPd().getStartPosition(),query.getPd().getParameterPosition(),
						query.getPd().getMethodInvocation(),query.getPd().getMethodDeclaration());
				tempPd.setForcefulExpressioType(ParameterDescriptor.SimpleNameType);
				tempPd.setObject(paramVariableList.get(j));
				candidateList.add(tempPd);
				System.out.println("J = "+j+"  "+paramVariableList.get(j).getName());
				*/
				
			//}
			
			Collections.sort(possibleCandidateList, new Comparator<LWParameterDescriptor>() {
		        @Override
		        public int compare(LWParameterDescriptor  fruit1, LWParameterDescriptor  fruit2)
		        {
		        	float f1 = (Float)fruit1.getObject();
		        	float f2 = (Float)fruit2.getObject();
				        			
		        	if(f1>f2){
		        		return -1;
		        	}
		        	else if(f1<f2){
		        		return 1;
		        	}
		        	else {
		        		if(hmFrequency.get(fruit1.getAbsStringRep())>hmFrequency.get(fruit2.getAbsStringRep())){
			        		return -1;
			        	}
			        	else if(hmFrequency.get(fruit1.getAbsStringRep())<hmFrequency.get(fruit2.getAbsStringRep())){
			        		return 1;
			        	}
			        	else return 0;
		        	}
		        }
		    });
			Collections.sort(possibleCandidateList, new Comparator<LWParameterDescriptor>() {
		        @Override
		        public int compare(LWParameterDescriptor  fruit1, LWParameterDescriptor  fruit2)
		        {
		        	
		        	/*if(fruit1.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType && fruit2.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType){
		        		//double value1 = ParameterEvaluation.this.simpleNameUsageRecommender.calculateSimilarity(fruit1.getObject().toString(),query.getPd());
		        		//double value2 = ParameterEvaluation.this.simpleNameUsageRecommender.calculateSimilarity(fruit2.getObject().toString(),query.getPd());
				        
		        		//if(value1>value2) return -1;
		        		//else if(value2>value1) return 1;
		        		//else 
		        		return 0;
		        	}
		        	else if(fruit1.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType){
		        		//double value = ParameterEvaluation.this.simpleNameUsageRecommender.calculateSimilarity(fruit1.getObject().toString(),query.getPd());
			        	//if(value>=0) return -1;
			        	return 0;
		        	}
		        	else if(fruit1.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType){
		        		//double value = ParameterEvaluation.this.simpleNameUsageRecommender.calculateSimilarity(fruit2.getObject().toString(),query.getPd());
			        	//if(value>=0) return -1;
			        	return 0;
		        	}*/
		        	
		        	/*if(query.getPd().getEvidence().size()>0 && (fruit1.getEvidence().size()>0 ||fruit2.getEvidence().size()>0))
		        	{
		        		if(LCS.findLCS(query.getPd().getEvidence().toArray(),fruit1.getEvidence().toArray())
		        				>LCS.findLCS(query.getPd().getEvidence().toArray(),fruit2.getEvidence().toArray())
		        				)
		        			return -1;
		        		else if(LCS.findLCS(query.getPd().getEvidence().toArray(),fruit1.getEvidence().toArray())
		        				<LCS.findLCS(query.getPd().getEvidence().toArray(),fruit2.getEvidence().toArray())
		        				)
		        		return 1;
		        		else return 0;
		        	}*/
		        	
		        	/*if(hmFrequency.get(fruit1.getStringRep(fruit1.getExpression()))>hmFrequency.get(fruit2.getStringRep(fruit2.getExpression()))){
		        		return -1;
		        	}
		        	else if(hmFrequency.get(fruit1.getStringRep(fruit1.getExpression()))<hmFrequency.get(fruit2.getStringRep(fruit2.getExpression()))){
		        		return 1;
		        	}
		        	else return 0;
		        	*/		        	
		        	return 0;
		        }
		    });
			
			hmFrequency.clear();
			//now remove the duplicated entry before recommendation
			//we do not insert more than three variable name
			int simplenameCounter=0;
			for(LWParameterDescriptor pd:possibleCandidateList){
				
				if(pd.getParameterContent() instanceof SimpleNameContent ){
					System.out.println("CSNC is empty:"+csnc.getUniqueVariableList().size());
					if(simplenameCounter<=2 && csnc.getUniqueVariableList().size()>simplenameCounter){
						pd.setForcefulExpressioType(ParameterDescriptor.SimpleNameType);
						pd.setSecondaryObject(csnc.getUniqueVariableList().get(simplenameCounter).getName());
						candidateList.add(pd);
						simplenameCounter++;
					}
				}
				else if(hmFrequency.containsKey(pd.getAbsStringRep())){
					int count= hmFrequency.get(pd.getAbsStringRep());
					hmFrequency.put(pd.getAbsStringRep(), count+1);
					
				}
				else{
					hmFrequency.put(pd.getAbsStringRep(),1);
						
						if(pd.getParameterContent() instanceof MethodInvocationContent){
							MethodInvocationContent mic = (MethodInvocationContent)pd.getParameterContent();
							
							if(mic.getReceiver()!=null && mic.getReceiver() instanceof SimpleNameContent){
								SimpleNameContent snc = (SimpleNameContent)mic.getReceiver();
								if(snc.getTypeQualifiedName()!=null && snc.getBindingKind()!=-1 && snc.getBindingKind()==IBinding.VARIABLE){
									SimpleNameCollector simpleNameCollector = new SimpleNameCollector("",snc.getTypeQualifiedName(),queryMi, queryICompilationUnit,queryMd, rtb);
									if(simpleNameCollector.getUniqueVariableList().size()>0){
										for(int i=0;i<simpleNameCollector.getUniqueVariableList().size();i++){
											
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
							else if(mic.getReceiver()!=null && mic.getReceiver() instanceof MethodInvocationContent)
							{
									System.out.println("I am on the other part");
									MethodInvocationContent mic2 = (MethodInvocationContent)mic.getReceiver();
										if(mic2.getReceiver()!=null && mic2.getReceiver()instanceof SimpleNameContent){
											SimpleNameContent snc2 = (SimpleNameContent)mic2.getReceiver();
											if(snc2.getTypeQualifiedName()!=null && snc2.getBindingKind()!=-1 && snc2.getBindingKind()==IBinding.VARIABLE){
												SimpleNameCollector snc = new SimpleNameCollector("",snc2.getTypeQualifiedName(),queryMi,queryICompilationUnit,queryMd,rtb);
												System.out.println("snc size: "+snc.getUniqueVariableList().size());
												if(snc.getUniqueVariableList().size()>0){
													for(int i=0;i<snc.getUniqueVariableList().size();i++){
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
							if(pd.getParameterContent() instanceof SimpleNameContent){}	
							else candidateList.add(pd);
						}
					}
			}
			
			//System.out.println("UniqueVariableList");
			//for(ParamVariable b:csnc.getUniqueVariableList()){
			//	System.out.println("Name: "+b.getName());
			//}
			
			/*for(int j=simplenameCounter;j<=2&&j<csnc.getUniqueVariableList().size();j++){
				ParameterDescriptor tempPd = new ParameterDescriptor(query.getPd().getStartPosition(),query.getPd().getParameterPosition(),
						query.getPd().getMethodInvocation(),query.getPd().getMethodDeclaration());
				tempPd.setForcefulExpressioType(ParameterDescriptor.SimpleNameType);
				tempPd.setObject(csnc.getUniqueVariableList().get(simplenameCounter).getName());
				candidateList.add(tempPd);
				System.out.println("J = "+j+"  "+paramVariableList.get(j).getName());
			}*/
			
			
			if(candidateList.size()>0) {
				this.updateRecommendationMadeTestMapDataByCategory(query);
				this.updateTotalTestMapDataByCategory(query);
				this.testCases++;
				//this.updateTotalRank(rank);
			}
			else {
				System.out.println("Cannot proceed.Candidate list is empty");
				System.out.println("Method invocation for this expression: "+queryMi.toString()+  "Position: "+query.getPd().getParameterPosition()+"Type: "+query.getPd().getReceiverType() );
				return;
			}
			
			long endTime = System.currentTimeMillis();
			time=time+(endTime-startTime);
			
			rank=-1;
			found =false;
			boolean partialFound = false;
			int partialRank=-1;
			ArrayList<String> candidateStringList = new ArrayList();
			
			System.out.println("Creating Candidate STring list: ");
			for(LWParameterDescriptor pd: candidateList){
				
				/*if(pd.getForcefulExpressioType()!= LWParameterDescriptor.SimpleNameType&& pd.getParameterContent() instanceof MethodInvocationContent){
					if(pd.getForcefulExpressioType()==LWParameterDescriptor.MethodInvocation){
						System.out.println("Query: "+query.getPd().getParameterContent().getStringParamNode());
						
						System.out.println("Original: "+pd.getParameterContent().getStringParamNode());
						System.out.println("Converted: "+pd.getMethodInvocationComparisonStringRep(pd.getParameterContent()));
						
					}
					
					{
						System.out.println("Query: "+query.getPd().getParameterContent().getStringParamNode());
						
						System.out.println("Original: "+pd.getParameterContent().getStringParamNode());
						System.out.println("Converted: "+pd.getMethodInvocationComparisonStringRep(pd.getParameterContent()));
						
					}
					candidateStringList.add(""+pd.getMethodInvocationComparisonStringRep(pd.getParameterContent())+":"+pd.getObject());
				}*/
				
				if(pd.getParameterContent()!=null && !(pd.getParameterContent() instanceof SimpleNameContent)){
					//candidateStringList.add(pd.getParameterContent().getStringParamNode()+":"+pd.getObject());//hmFrequency.get(pd.getStringRep(pd.getExpression())
					candidateStringList.add(pd.getMethodInvocationComparisonStringRep(pd.getParameterContent())+":"+pd.getObject());//hmFrequency.get(pd.getStringRep(pd.getExpression())

					//System.out.println("Creating Candidate String: ");
				}
				else{
					candidateStringList.add(""+pd.getSecondaryObject());//hmFrequency.get(pd.getStringRep(pd.getExpression())		
				}
			}
			System.out.println("End of Creating Candidate STring list: ");

			for(LWParameterDescriptor lpd:candidateList){
				rank++;
				//this.printDebug(query.getPd(), lpd);
				if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof QualifiedNameContent && query.getPd().getParameterContent() instanceof QualifiedNameContent){
					//QualifiedName qnm = (QualifiedName)lpd.getExpression();
					///QualifiedName qqnm = (QualifiedName)query.getPd().getExpression();
					System.out.println("Query: "+query.getPd().getParameterContent().getStringParamNode());
					System.out.println("Candidate: "+lpd.getParameterContent().getStringParamNode());
					
					if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof QualifiedNameContent && lpd.getParameterContent().getStringParamNode().equals(query.getPd().getParameterContent().getStringParamNode())){
						System.out.println("Match Found: ");
						found=true;
						break;
					}
				
				/*else if(lpd.getExpression() instanceof QualifiedName && qqnm.getQualifier()!=null &&qnm.getQualifier()!=null && qnm.getQualifier().getFullyQualifiedName().equals(qqnm.getQualifier().getFullyQualifiedName()))
				{
					found=true;
					break;
					
				}*/
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent()instanceof ClassInstanceCreationContent && query.getPd().getParameterContent() instanceof ClassInstanceCreationContent){
					ClassInstanceCreationContent cic  = (ClassInstanceCreationContent)lpd.getParameterContent(); 
					ClassInstanceCreationContent qcic = (ClassInstanceCreationContent)query.getPd().getParameterContent();
					if(cic.getAbsStringRep().equals(qcic.getAbsStringRep())){
						found =true;
						System.out.println("Found Expected: "+"Query: "+ query.getPd().getParameterContent().getStringParamNode()  +"    Candidate: " + cic);
						break;
					}
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof StringLiteralContent && query.getPd().getParameterContent() instanceof StringLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof NumberLiteralContent && query.getPd().getParameterContent() instanceof NumberLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof ThisExpressionContent && query.getPd().getParameterContent() instanceof ThisExpressionContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof BooleanLiteralContent && query.getPd().getParameterContent() instanceof BooleanLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().toString().trim())){
					found=true;
					break;
				}
		
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof NullLiteralContent && query.getPd().getParameterContent() instanceof NullLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					found=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType && query.getPd().getParameterContent() instanceof SimpleNameContent ){
					System.out.println("Query: "+((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode());
					System.out.println("Candidate: "+lpd.getObject().toString());
					
					if(lpd.getSecondaryObject().toString().equals(((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode()))
						
					{System.out.println("Match found");
					found=true;
					break;
					}
				}
				
				if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && 
						lpd.getParameterContent() instanceof MethodInvocationContent && query.getPd().getParameterContent() instanceof MethodInvocationContent){
					
					/*MethodInvocation mi = (MethodInvocation)lpd.getExpression();
					MethodInvocation qmi = (MethodInvocation)query.getPd().getExpression();
					System.out.println("I am in method comparison");
					System.out.println("Candidate: "+lpd.getExpression());
					*/
					/*if(lpd.getStringRep(lpd.getExpression()).equals(query.getPd().getStringRep(query.getPd().getExpression())))
					{
						found=true;
						break;
					}*/
					System.out.println("Query: "+query.getPd().getAbsStringRep()+":   Main Query: "+query.getPd().getParameterContent().getStringParamNode());
					if(lpd.getForcefulExpressioType()==ParameterDescriptor.MethodInvocation)
					System.out.println("MI Candidate: "+lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()));
					System.out.println("Candidate:"+candidateList.size()+"     Candidate STring List size: "+candidateStringList.size() );
					if(lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()).equals(query.getPd().getMethodInvocationComparisonStringRep(query.getPd().getParameterContent())))
					{
						found = true;
						break;
					}
				}
			}
			
			//+++++++++++++++++++++++++++++++++++++++++++++Partial Result Collection++++++++++++++++++++++++++++++

			for(LWParameterDescriptor lpd:candidateList){
				partialRank++;
				if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof QualifiedNameContent && query.getPd().getParameterContent() instanceof QualifiedNameContent){

					System.out.println("Query: "+query.getPd().getParameterContent().getStringParamNode());
					System.out.println("Candidate: "+lpd.getParameterContent().getStringParamNode());
					
					System.out.println("Query: "+((QualifiedNameContent)(query.getPd().getParameterContent())).getQualifier());
					System.out.println("Candidate: "+((QualifiedNameContent)(lpd.getParameterContent())).getQualifier());
					
					if(((QualifiedNameContent)(lpd.getParameterContent())).getQualifier().equals(((QualifiedNameContent)(query.getPd().getParameterContent())).getQualifier())){
						partialFound=true;
						break;
					}
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent()instanceof ClassInstanceCreationContent && query.getPd().getParameterContent() instanceof ClassInstanceCreationContent){
					ClassInstanceCreationContent cic  = (ClassInstanceCreationContent)lpd.getParameterContent(); 
					ClassInstanceCreationContent qcic = (ClassInstanceCreationContent)query.getPd().getParameterContent();
					if(cic.getAbsStringRep().equals(qcic.getAbsStringRep())){
						partialFound =true;
						break;
					}
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof StringLiteralContent && query.getPd().getParameterContent() instanceof StringLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof NumberLiteralContent && query.getPd().getParameterContent() instanceof NumberLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof ThisExpressionContent && query.getPd().getParameterContent() instanceof ThisExpressionContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof BooleanLiteralContent && query.getPd().getParameterContent() instanceof BooleanLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().toString().trim())){
					partialFound=true;
					break;
				}
		
				else if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && lpd.getParameterContent() instanceof NullLiteralContent && query.getPd().getParameterContent() instanceof NullLiteralContent && lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())){
					partialFound=true;
					break;
				}
				else if(lpd.getForcefulExpressioType()==ParameterDescriptor.SimpleNameType && query.getPd().getParameterContent() instanceof SimpleNameContent ){
					System.out.println("Query: "+((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode());
					System.out.println("Candidate: "+lpd.getObject().toString());
					
					if(lpd.getSecondaryObject().toString().equals(((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode()))
						
					{System.out.println("Match found");
					partialFound=true;
					break;
					}
				}
				
				if(lpd.getForcefulExpressioType()!= ParameterDescriptor.SimpleNameType && 
						lpd.getParameterContent() instanceof MethodInvocationContent && query.getPd().getParameterContent() instanceof MethodInvocationContent){
					
					System.out.println("Query: "+query.getPd().getAbsStringRep()+":   Main Query: "+query.getPd().getParameterContent().getStringParamNode());
					if(lpd.getForcefulExpressioType()==ParameterDescriptor.MethodInvocation)
					System.out.println("MI Candidate: "+lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()));
					System.out.println("Candidate:"+candidateList.size()+"     Candidate STring List size: "+candidateStringList.size() );
					if( ((MethodInvocationContent)lpd.getParameterContent()).getMethodName().equals(((MethodInvocationContent)query.getPd().getParameterContent()).getMethodName()))
					{
						partialFound = true;
						break;
					}
					//check the base variable
					else{
					Object qreceiver = query.getPd().getParameterContent().getReceiver();
					while(qreceiver!=null && !(qreceiver instanceof SimpleNameContent)){
						if(qreceiver instanceof MethodInvocationContent){
							qreceiver = ((MethodInvocationContent)qreceiver).getReceiver();
						}
						else if(qreceiver instanceof SimpleNameContent){
							qreceiver = ((SimpleNameContent)qreceiver).getReceiver();
						}
						else if(qreceiver instanceof QualifiedNameContent){
							qreceiver = ((QualifiedNameContent)qreceiver).getReceiver();
						}	
					}
					if(lpd.getSecondaryObject()!=null && qreceiver!=null && qreceiver instanceof SimpleNameContent && ((SimpleNameContent)qreceiver).getIdentifier().equals((String)lpd.getSecondaryObject())){
						partialFound=true;
						break;
					}
					
					else if(lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()).equals(query.getPd().getMethodInvocationComparisonStringRep(query.getPd().getParameterContent())))
					{
						partialFound = true;
						break;
					}
					}
	
				}
			}
			
			System.out.println("Partial Found = " + partialFound + "  Partial Rank = "+partialRank);
			
			if(found==true){
				System.out.println("Success");
				System.out.println("Recommendation Rank = " + rank + "Total Rank = " + totalRank);
			}
			else{ System.out.println("Failed");}

			if(found==true && rank>=0 && rank<=ACCEPTED_RANGE){
				totalCorrectResult++;
				this.updateTotalRank(rank);
				this.updateCorrectTestMapDataByCategory(query,rank);
			}
			
			if(partialFound==true && partialRank>=0 && partialRank<=ACCEPTED_RANGE){
				updatePartialTestMapDataByCategory(query, partialRank);				
			}
			
			System.out.println("Required Time: "+(endTime-startTime));
			System.out.println("Candidate List Size: "+candidateList.size());
			System.out.println("In: "+query.getPd().getCompilationUnitName());
			System.out.println("MethodInvocatio: "+query.getPd().getMethodInvocation());
			System.out.println("Total correct result: "+totalCorrectResult+"   Test cases: "+testCases+"  Expected: "+query.getPd().getParameterContent().getStringParamNode()+"   suggestion: "+candidateStringList);
			
		}//end of choice loop
			
	}
	

	public void printEvaluationResultByCategory(){
		int totalCorrect=0;
		int totalInstances=0;
		int totalRecommended=0;
		
		int totalCorrectTop1  = 0;
		int totalCorrectTop3  = 0;
		int totalCorrectTop5  = 0;
		int totalCorrectTop10 = 0;
	
		int totalPartialCorrectTop1  = 0;
		int totalPartialCorrectTop3  = 0;
		int totalPartialCorrectTop5  = 0;
		int totalPartialCorrectTop10 = 0;
	
		Iterator it = this.hmDataTestMapByExpCategory.keySet().iterator();
		while(it.hasNext()){
			String key  = (String)it.next();
			int total = (Integer)this.hmDataTestMapByExpCategory.get(key);
			
			int correct = 0;	
			int partial = 0;
			int recommendationMade = 0;
			int whole = 0;
			
			if(this.hmCorrectTestMapByExpCategory.containsKey(key)){
				correct = (Integer)this.hmCorrectTestMapByExpCategory.get(key);
				totalCorrect = totalCorrect+correct;
			}
			if(this.hmCorrectTestMapByExpCategoryTop1.containsKey(key)){
				correct = (Integer)this.hmCorrectTestMapByExpCategoryTop1.get(key);
				totalCorrectTop1 = totalCorrectTop1+correct;
			}if(this.hmCorrectTestMapByExpCategoryTop3.containsKey(key)){
				correct = (Integer)this.hmCorrectTestMapByExpCategoryTop3.get(key);
				totalCorrectTop3 = totalCorrectTop3+correct;
			}if(this.hmCorrectTestMapByExpCategoryTop5.containsKey(key)){
				correct = (Integer)this.hmCorrectTestMapByExpCategoryTop5.get(key);
				totalCorrectTop5 = totalCorrectTop5+correct;
			}if(this.hmCorrectTestMapByExpCategoryTop10.containsKey(key)){
				correct = (Integer)this.hmCorrectTestMapByExpCategoryTop10.get(key);
				totalCorrectTop10 = totalCorrectTop10+correct;
			}
						
			if(this.hmPartialTestMapByExpCategoryTop1.containsKey(key)){
				partial = (Integer)this.hmPartialTestMapByExpCategoryTop1.get(key);
				totalPartialCorrectTop1 = totalPartialCorrectTop1+partial;
			}if(this.hmPartialTestMapByExpCategoryTop3.containsKey(key)){
				partial = (Integer)this.hmPartialTestMapByExpCategoryTop3.get(key);
				totalPartialCorrectTop3 = totalPartialCorrectTop3+partial;
			}if(this.hmPartialTestMapByExpCategoryTop5.containsKey(key)){
				partial = (Integer)this.hmPartialTestMapByExpCategoryTop5.get(key);
				totalPartialCorrectTop5 = totalPartialCorrectTop5+partial;
			}if(this.hmPartialTestMapByExpCategoryTop10.containsKey(key)){
				partial = (Integer)this.hmPartialTestMapByExpCategoryTop10.get(key);
				totalPartialCorrectTop10 = totalPartialCorrectTop10+partial;
			}
			
			if(this.hmWholeTestMapByExpCategory.containsKey(key)){
				whole = (Integer)this.hmWholeTestMapByExpCategory.get(key);
				totalInstances = totalInstances+whole;
			}
			if(this.hmRecommendationMadeTestMapByExpCategory.containsKey(key)){
				recommendationMade = (Integer)this.hmRecommendationMadeTestMapByExpCategory.get(key);
				totalRecommended=totalRecommended+recommendationMade;
			}
			System.out.println("Expressition Type: "+key+"  Whole= "+whole+"  Total: "+total+"  Correct: "+correct+"  Partial: "+partial+"   RM: "+recommendationMade);
		} 
		
		System.out.println("For Top-1: TotalInstances: "+totalInstances+"  TotalRecommended: "+totalRecommended+"   TotalCorrectTop1 = "+totalCorrectTop1);
		System.out.println("For Top-1: Precision = "+(totalCorrectTop1/(totalRecommended*1.0f))+"   Recall= "+(totalCorrectTop1/(totalInstances*1.0f)));
		System.out.println("For Top-1: Partial Precision = "+(totalPartialCorrectTop1/(totalRecommended*1.0f))+"  Partial Recall= "+(totalPartialCorrectTop1/(totalInstances*1.0f)));

		System.out.println("For Top-3: TotalInstances: "+totalInstances+"  TotalRecommended: "+totalRecommended+"   TotalCorrectTop3 = "+totalCorrectTop3);		
		System.out.println("For Top-3: Precision = "+(totalCorrectTop3/(totalRecommended*1.0f))+"   Recall= "+(totalCorrectTop3/(totalInstances*1.0f)));
		System.out.println("For Top-3: Partial Precision = "+(totalPartialCorrectTop3/(totalRecommended*1.0f))+"  Partial Recall= "+(totalPartialCorrectTop3/(totalInstances*1.0f)));

		System.out.println("For Top-5: TotalInstances: "+totalInstances+"  TotalRecommended: "+totalRecommended+"   TotalCorrect = "+totalCorrectTop5);		
		System.out.println("For Top-5: Precision = "+(totalCorrectTop5/(totalRecommended*1.0f))+"   Recall= "+(totalCorrectTop5/(totalInstances*1.0f)));
		System.out.println("For Top-5: Partial Precision = "+(totalPartialCorrectTop5/(totalRecommended*1.0f))+" Partial  Recall= "+(totalPartialCorrectTop5/(totalInstances*1.0f)));

		System.out.println("For Top-10:TotalInstances: "+totalInstances+"  TotalRecommended: "+totalRecommended+"   TotalCorrect = "+totalCorrectTop10);		
		System.out.println("For Top-10:Precision = "+(totalCorrectTop10/(totalRecommended*1.0f))+"   Recall= "+(totalCorrectTop10/(totalInstances*1.0f)));
		System.out.println("For Top-10:Partial Precision = "+(totalPartialCorrectTop10/(totalRecommended*1.0f))+"  Partial Recall= "+(totalPartialCorrectTop10/(totalInstances*1.0f)));
	}
	
	/* To ensure that method call is not part of a static block*/
	public boolean isInMethod(MethodInvocation node) {
		try {
			IType allTypes[] = iCompilationUnit.getAllTypes();
			for (IType itype : allTypes) {
				IMethod methods[] = itype.getMethods();
				for (IMethod method : methods) {
					if (node.getStartPosition() >= method.getSourceRange()
							.getOffset()
							&& node.getStartPosition() <= method
									.getSourceRange().getOffset()
									+ method.getSourceRange().getLength()) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		td = node;
		// DHou this.cuAnalyzer.analyze(node);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		methodDeclaration = node;
		// DHou this.cuAnalyzer.analyze(node);
		return true;
	}

	/*public String filterFilePath(String path){
		for(String prefix:this.prefixToFilter){
			if(path.startsWith(prefix)){
				return path.substring(prefix.length());
			}
		}
		//throw new RuntimeException("Can not find any prefix match: " + path+"   Filters: "+this.prefixToFilter);
		return path;
	}*/
	public void print() {
		System.out.println("Total Method Calls: " + totalMethodCalls);
		System.out.println("Interesting Method Calls: "
				+ interestingMethodCalls);
		System.out.println("Unbound Method Calls: " + unboundMethodCalls);
		System.out.println("Method Calls With Parameter: " + this.methodCallWithParameter);
		
		 Set<MultiKey> s =  this.mkMethodExample.keySet();
		 System.out.println("Size = "+s.size());
		 Iterator<MultiKey> it = s.iterator();
		 while(it.hasNext()){
			 
			 MultiKey key = it.next();
			 System.out.println("key0"+key.getKey(0));
			 System.out.println("key0"+key.getKey(1));
			 System.out.println("key0"+key.getKey(2));
			 
			 ArrayList<ParameterDescriptor> parameterDescriptorList = (ArrayList<ParameterDescriptor>) this.mkMethodExample.get(key.getKey(0),key.getKey(1),key.getKey(2));
		 
		 }
	}

	public static CompilationUnit getCompilationUnit(ICompilationUnit unit) {
		@SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
	
	String getVariableCategory(SimpleName sn){
		
		HashMap bindingKind = new HashMap();
		bindingKind = new HashMap();
		bindingKind.put(1, "PACKAGE");
		bindingKind.put(2, "TYPE");
		bindingKind.put(3, "VARIABLE");
		bindingKind.put(4, "METHOD");
		bindingKind.put(5, "ANNOTATION");
		bindingKind.put(6, "MEMBER_VALUE_PAIR");
	
		
		if(bindingKind.get(sn.resolveBinding().getKind()).equals("VARIABLE")){
			if(sn.resolveBinding()instanceof IVariableBinding){
				IVariableBinding ivaraiableBinding = (IVariableBinding)sn.resolveBinding();
				if(ivaraiableBinding.isField()){
					return "FIELD";
				}
				else if(ivaraiableBinding.isParameter()){
					return "VARIABLE_PARAMETER";
				}
				else if(ivaraiableBinding.isEnumConstant()){
					return "VARIABLE_ENUM_CONSTANT";
				}
				else{
						return "VARIABLE_ENUM_CONSTANT";			
				}
			}
		}
		return null;
	}
	public MethodDeclaration getEnclosingMethodName(MethodInvocation mi){
		ASTNode node = mi;
		while(node!=null && !(node instanceof MethodDeclaration)){
			node = node.getParent();
		}
		return (MethodDeclaration)node;
	}
	public static void main(String args[]){
		String qn = "I am here.Bangladesh";
		String typeName = qn.substring(0, qn.indexOf("."));//qn.getQualifier().resolveTypeBinding().getQualifiedName();
		String simpleName =  qn.substring(qn.indexOf(".")+1,qn.length());//qn.getQualifier().resolveTypeBinding().getQualifiedName();
		System.out.println("Simple = "+simpleName);
		System.out.println("Qualified = "+typeName);
		
	}

}
