package com.srlab.parameter.logic;

import com.srlab.parameter.category.ParamVariable;
import com.srlab.parameter.category.SimpleNameCollector;
import com.srlab.parameter.node.BooleanLiteralContent;
import com.srlab.parameter.node.ClassInstanceCreationContent;
import com.srlab.parameter.node.MethodInvocationContent;
import com.srlab.parameter.node.NullLiteralContent;
import com.srlab.parameter.node.NumberLiteralContent;
import com.srlab.parameter.node.ParameterContent;
import com.srlab.parameter.node.QualifiedNameContent;
import com.srlab.parameter.node.SimpleNameContent;
import com.srlab.parameter.node.StringLiteralContent;
import com.srlab.parameter.node.ThisExpressionContent;
import com.srlab.parameter.utility.ConfigManager;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import simmetrics.similaritymetrics.CosineSimilarity;

public class LWParameterEvaluation
  extends ASTVisitor
{
  private int ACCEPTED_RANGE = 9;
  private int failedPhaseOne;
  private int totalMethodCalls;
  private int interestingMethodCalls;
  private int unboundMethodCalls;
  private int methodCallWithParameter;
  private int totalRank;
  private LWParameterCollection parameterCollection;
  private MethodDeclaration methodDeclaration;
  private TypeDeclaration td;
  private String fileName;
  private CompilationUnit compilationUnit;
  private ICompilationUnit iCompilationUnit;
  private MultiKeyMap mkMethodExample;
  private MultiKeyMap mkMethodExampleCtaegorizerCount;
  private MultiKeyMap mkTestData;
  private MultiKeyMap mkAllData;
  private int totalCorrectResult = 0;
  private int totalCorrectResultPhaseOne = 0;
  private long time;
  private int testCases = 0;
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
  private MultiKeyMap parameterNameMap;
  private boolean testPhase;
  private MultiKeyMap mkGpdByMethod;
  private ArrayList<LWParameterModelEntry> parameterModelEntryList;
  private HashMap hmCompilationUnits;
  
  public ArrayList<LWParameterModelEntry> getParameterModelEntryList()
  {
    return this.parameterModelEntryList;
  }
  
  public long getTime()
  {
    return this.time;
  }
  
  public boolean isTestPhase()
  {
    return this.testPhase;
  }
  
  public void setTestPhase(boolean testPhase)
  {
    this.testPhase = testPhase;
  }
  
  public int getInterestingMethodCalls()
  {
    return this.interestingMethodCalls;
  }
  
  public void setInterestingMethodCalls(int interestingMethodCalls)
  {
    this.interestingMethodCalls = interestingMethodCalls;
  }
  
  public LWParameterEvaluation(MultiKeyMap mkTestData, MultiKeyMap mkAllData, MultiKeyMap _parameterNameMap, HashMap hmCompilationUnits)
  {
    this.time = 0L;
    this.hmCorrectTestMapByExpCategory = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop1 = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop3 = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop5 = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop10 = new HashMap();
    
    this.hmDataTestMapByExpCategory = new HashMap();
    this.hmPartialTestMapByExpCategoryTop1 = new HashMap();
    this.hmPartialTestMapByExpCategoryTop3 = new HashMap();
    this.hmPartialTestMapByExpCategoryTop5 = new HashMap();
    this.hmPartialTestMapByExpCategoryTop10 = new HashMap();
    
    this.hmRecommendationMadeTestMapByExpCategory = new HashMap();
    this.hmWholeTestMapByExpCategory = new HashMap();
    this.hmCompilationUnits = hmCompilationUnits;
    this.parameterNameMap = _parameterNameMap;
    
    this.totalMethodCalls = 0;
    this.interestingMethodCalls = 0;
    this.unboundMethodCalls = 0;
    
    this.totalCorrectResultPhaseOne = 0;
    this.failedPhaseOne = 0;
    this.parameterCollection = new LWParameterCollection();
    this.mkMethodExample = new MultiKeyMap();
    this.mkMethodExampleCtaegorizerCount = new MultiKeyMap();
    this.mkTestData = mkTestData;
    this.mkAllData = mkAllData;
    this.testPhase = false;
    this.totalCorrectResult = 0;
    this.testCases = 0;
    this.parameterModelEntryList = new ArrayList();
    this.totalRank = 0;
    this.mkGpdByMethod = new MultiKeyMap();
  }
  
  public LWParameterEvaluation()
  {
    this.time = 0L;
    this.hmCorrectTestMapByExpCategory = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop1 = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop3 = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop5 = new HashMap();
    this.hmCorrectTestMapByExpCategoryTop10 = new HashMap();
    
    this.hmDataTestMapByExpCategory = new HashMap();
    this.hmPartialTestMapByExpCategoryTop1 = new HashMap();
    this.hmPartialTestMapByExpCategoryTop3 = new HashMap();
    this.hmPartialTestMapByExpCategoryTop5 = new HashMap();
    this.hmPartialTestMapByExpCategoryTop10 = new HashMap();
    
    this.hmRecommendationMadeTestMapByExpCategory = new HashMap();
    this.hmWholeTestMapByExpCategory = new HashMap();
    this.hmCompilationUnits = this.hmCompilationUnits;
    this.parameterNameMap = new MultiKeyMap();
    
    this.totalMethodCalls = 0;
    this.interestingMethodCalls = 0;
    this.unboundMethodCalls = 0;
    
    this.totalCorrectResultPhaseOne = 0;
    this.failedPhaseOne = 0;
    this.parameterCollection = new LWParameterCollection();
    this.mkMethodExample = new MultiKeyMap();
    this.mkMethodExampleCtaegorizerCount = new MultiKeyMap();
    this.testPhase = false;
    this.totalCorrectResult = 0;
    this.testCases = 0;
    this.parameterModelEntryList = new ArrayList();
    this.totalRank = 0;
    this.mkGpdByMethod = new MultiKeyMap();
  }
  
  public int getTotalMethodCalls()
  {
    return this.totalMethodCalls;
  }
  
  public void setMethodCalls(int methodCalls)
  {
    this.totalMethodCalls = methodCalls;
  }
  
  public void updateTotalRank(int rank)
  {
    this.totalRank += rank;
  }
  
  public int getTotalRank()
  {
    return this.totalRank;
  }
  
  public void setTotalRank(int totalRank)
  {
    this.totalRank = totalRank;
  }
  
  public void addExternalJars(IProject project)
    throws JavaModelException
  {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClassPath = javaProject.getRawClasspath();
    List list = new LinkedList(Arrays.asList(rawClassPath));
    ConfigManager.getInstance();
    String[] arrayOfString;
    int j = (arrayOfString = ConfigManager.getJarPathList()).length;
    for (int i = 0; i < j; i++)
    {
      String path = arrayOfString[i];
      String jarPath = path.toString();
      boolean isAlreadyAdded = false;
      IClasspathEntry[] arrayOfIClasspathEntry1;
      int m = (arrayOfIClasspathEntry1 = rawClassPath).length;
      for (int k = 0; k < m; k++)
      {
        IClasspathEntry cpe = arrayOfIClasspathEntry1[k];
        isAlreadyAdded = cpe.getPath().toOSString().equals(jarPath);
        if (isAlreadyAdded) {
          break;
        }
      }
      if (!isAlreadyAdded)
      {
        IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(jarPath), null, null);
        list.add(jarEntry);
      }
    }
    IClasspathEntry[] newClassPath = (IClasspathEntry[])list.toArray(new IClasspathEntry[0]);
    javaProject.setRawClasspath(newClassPath, null);
  }
  
  public void run()
  {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    
    IProject[] projects = root.getProjects();
    IProject[] arrayOfIProject1;
    int j = (arrayOfIProject1 = projects).length;
    for (int i = 0; i < j; i++)
    {
      IProject project = arrayOfIProject1[i];
      try
      {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature"))
        {
          addExternalJars(project);
          IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
          IPackageFragment[] arrayOfIPackageFragment1;
          int m = (arrayOfIPackageFragment1 = packages).length;
          for (int k = 0; k < m; k++)
          {
            IPackageFragment mypackage = arrayOfIPackageFragment1[k];
            if (mypackage.getKind() == 1)
            {
              ICompilationUnit[] arrayOfICompilationUnit;
              int i1 = (arrayOfICompilationUnit = mypackage.getCompilationUnits()).length;
              for (int n = 0; n < i1; n++)
              {
                ICompilationUnit unit = arrayOfICompilationUnit[n];
                
                CompilationUnit cu = getCompilationUnit(unit);
                
                this.fileName = unit.getPath().toFile().getAbsolutePath();
                if (cu.getPackage() != null) {
                  this.fileName = 
                  
                    (cu.getPackage().getName().getFullyQualifiedName() + "." + cu.getJavaElement()
                    .getElementName()
                    .substring(
                    0, 
                    cu.getJavaElement().getElementName().length() - 
                    ".java".length()));
                } else {
                  this.fileName = cu
                    .getJavaElement()
                    .getElementName()
                    .substring(0, 
                    cu.getJavaElement().getElementName().length() - ".java".length());
                }
                this.iCompilationUnit = unit;
                this.compilationUnit = cu;
                cu.accept(this);
              }
            }
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public boolean visit(MethodInvocation node)
  {
    this.totalMethodCalls += 1;
    IMethodBinding mb = node.resolveMethodBinding();
    if ((mb == null) || (mb.getDeclaringClass() == null) || (mb.getMethodDeclaration() == null)) {
      return true;
    }
    Expression exp = node.getExpression();
    ITypeBinding tb = null;
    if (exp != null)
    {
      tb = exp.resolveTypeBinding();
      if (tb == null) {
        if ((exp instanceof FieldAccess))
        {
          FieldAccess fa = (FieldAccess)exp;
          IVariableBinding vb = fa.resolveFieldBinding();
          if (vb != null) {
            tb = vb.getType();
          } else {
            tb = fa.resolveTypeBinding();
          }
        }
        else if ((exp instanceof SimpleName))
        {
          SimpleName sn = (SimpleName)exp;
          tb = sn.resolveTypeBinding();
        }
        else if ((exp instanceof MethodInvocation))
        {
          MethodInvocation mi = (MethodInvocation)exp;
          tb = mi.resolveTypeBinding();
        }
        else if ((exp instanceof QualifiedName))
        {
          QualifiedName qn = (QualifiedName)exp;
          tb = qn.resolveTypeBinding();
        }
        else if ((exp instanceof TypeLiteral))
        {
          TypeLiteral tl = (TypeLiteral)exp;
          tb = tl.resolveTypeBinding();
        }
        else if ((exp instanceof ArrayAccess))
        {
          ArrayAccess aa = (ArrayAccess)exp;
          tb = aa.resolveTypeBinding();
        }
        else if ((exp instanceof ClassInstanceCreation))
        {
          ClassInstanceCreation cic = (ClassInstanceCreation)exp;
          tb = cic.resolveTypeBinding();
        }
        else if ((exp instanceof ParenthesizedExpression))
        {
          ParenthesizedExpression pe = (ParenthesizedExpression)exp;
          tb = pe.resolveTypeBinding();
        }
        else if ((exp instanceof ThisExpression))
        {
          ThisExpression te = (ThisExpression)exp;
          tb = te.resolveTypeBinding();
        }
        else if ((exp instanceof StringLiteral))
        {
          StringLiteral sl = (StringLiteral)exp;
          tb = sl.resolveTypeBinding();
        }
        else if ((exp instanceof ArrayCreation))
        {
          ArrayCreation ac = (ArrayCreation)exp;
          tb = ac.resolveTypeBinding();
        }
        else
        {
          throw new IllegalStateException("Need to handle Expressions of type: " + exp.getClass().getName());
        }
      }
    }
    else
    {
      tb = this.td.resolveBinding();
    }
    if ((node.resolveMethodBinding() == null) || (node.resolveMethodBinding().getDeclaringClass() == null)) {
      return true;
    }
    if (!isInMethod(node))
    {
      this.unboundMethodCalls += 1;
    }
    else if ((isInMethod(node)) && (
      (ConfigManager.getInstance().isInteresting(tb)) || (ConfigManager.getInstance().isInteresting(tb))))
    {
      this.interestingMethodCalls += 1;
      
      List<Expression> argumentsExpression = node.arguments();
      if (argumentsExpression.size() > 0) {
        this.methodCallWithParameter += 1;
      }
      ITypeBinding[] ptb = node.resolveMethodBinding().getParameterTypes();
      String[] parameters = new String[ptb.length];
      for (int i = 0; i < ptb.length; i++) {
        parameters[i] = ptb[i].getQualifiedName();
      }
      if ((node.getExpression() instanceof SimpleName))
      {
        SimpleName sn = (SimpleName)node.getExpression();
        getVariableCategory(sn);
      }
      for (int position = 0; position < argumentsExpression.size(); position++)
      {
        Expression paramExp = (Expression)argumentsExpression.get(position);
        if ((isTestPhase()) && (this.mkTestData.containsKey(Integer.valueOf(paramExp.getStartPosition()), this.fileName)))
        {
          LWParameterDescriptor pd = (LWParameterDescriptor)this.mkTestData.get(Integer.valueOf(paramExp.getStartPosition()), 
            this.fileName);
          LWParameterModelEntry pme = new LWParameterModelEntry(pd);
          if (((pd.getParameterContent() instanceof QualifiedNameContent)) || 
            ((pd.getParameterContent() instanceof MethodInvocationContent)) || 
            ((pd.getParameterContent() instanceof NumberLiteralContent)) || 
            ((pd.getParameterContent() instanceof ThisExpressionContent)) || 
            ((pd.getParameterContent() instanceof StringLiteralContent)) || 
            ((pd.getParameterContent() instanceof NullLiteralContent)) || 
            ((pd.getParameterContent() instanceof BooleanLiteralContent)) || 
            ((pd.getParameterContent() instanceof SimpleNameContent)) || 
            ((pd.getParameterContent() instanceof ClassInstanceCreationContent)))
          {
            System.out.println("++++++++++++++++++++++++++++Evaluate a test case...   Test case number: " + 
              this.testCases + " :" + "  Test Data Size: " + this.mkTestData.keySet().size());
            System.out.println("Enclosing Method Expression:" + node.toString());
            System.out.println("Test Parameter Expression Detail: " + paramExp.toString() + 
              "Test Parameter Expression Type: " + 
              LWParameterCategorizer.getExpressionType(paramExp));
            
            evaluateNov16(paramExp, pme, node, getEnclosingMethodName(node), this.iCompilationUnit, 
              parameters[position], node.resolveMethodBinding().getDeclaringClass()
              .getQualifiedName());
            System.out.println("Available Memory: " + Runtime.getRuntime().freeMemory());
            System.out
              .println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
          }
        }
        else if ((!isTestPhase()) && 
          (!this.mkTestData.containsKey(Integer.valueOf(paramExp.getStartPosition()), this.fileName)))
        {
          LWParameterDescriptor pd = (LWParameterDescriptor)this.mkAllData.get(Integer.valueOf(paramExp.getStartPosition()), 
            this.fileName);
          System.out.println("PD = " + pd + "  File: " + this.fileName + " Start Position: " + 
            paramExp.getStartPosition());
          System.out.println("Training Data Size " + this.mkAllData.keySet().size() + "  Memory: " + 
            Runtime.getRuntime().freeMemory() + "  ParameterModelEntry: " + 
            this.parameterModelEntryList.size());
          if (pd != null)
          {
            if (this.mkGpdByMethod.containsKey(pd.getMethodName(), pd.getReceiverType()))
            {
              ArrayList<LWParameterDescriptor> list = (ArrayList)this.mkGpdByMethod.get(
                pd.getMethodName(), pd.getReceiverType());
              list.add(pd);
            }
            else
            {
              ArrayList<LWParameterDescriptor> list = new ArrayList();
              list.add(pd);
              this.mkGpdByMethod.put(pd.getMethodName(), pd.getReceiverType(), list);
            }
            LWParameterModelEntry pme = new LWParameterModelEntry(pd);
            this.parameterModelEntryList.add(pme);
          }
        }
      }
    }
    return true;
  }
  
  public void load(ArrayList<LWParameterDescriptor> pdList)
  {
    this.parameterModelEntryList = new ArrayList();
    for (LWParameterDescriptor pd : pdList) {
      this.parameterModelEntryList.add(new LWParameterModelEntry(pd));
    }
    for (LWParameterModelEntry pme : this.parameterModelEntryList) {
      if (this.mkGpdByMethod.containsKey(pme.getPd().getMethodName(), pme.getPd().getReceiverType()))
      {
        ArrayList<LWParameterDescriptor> list = (ArrayList)this.mkGpdByMethod.get(pme.getPd().getMethodName(), pme
          .getPd().getReceiverType());
        list.add(pme.getPd());
      }
      else
      {
        ArrayList<LWParameterDescriptor> list = new ArrayList();
        list.add(pme.getPd());
        this.mkGpdByMethod.put(pme.getPd().getMethodName(), pme.getPd().getReceiverType(), list);
      }
    }
  }
  
  public void updateWholeTestMapDataByCategory(LWParameterModelEntry query)
  {
    if (this.hmWholeTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType()))
    {
      int count = ((Integer)this.hmWholeTestMapByExpCategory.get(query.getPd().getParameterExpressionType())).intValue();
      this.hmWholeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
    }
    else
    {
      this.hmWholeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
    }
  }
  
  public void updateRecommendationMadeTestMapDataByCategory(LWParameterModelEntry query)
  {
    if (this.hmRecommendationMadeTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType()))
    {
      int count = ((Integer)this.hmRecommendationMadeTestMapByExpCategory.get(query.getPd()
        .getParameterExpressionType())).intValue();
      this.hmRecommendationMadeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
    }
    else
    {
      this.hmRecommendationMadeTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
    }
  }
  
  public void updatePartialTestMapDataByCategory(LWParameterModelEntry query, int rank)
  {
    System.out.println("Query Expression type:" + query.getPd().getParameterExpressionType());
    System.out.println("Rank = " + rank);
    if ((rank >= 0) && (rank <= 0)) {
      if (this.hmPartialTestMapByExpCategoryTop1.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = ((Integer)this.hmPartialTestMapByExpCategoryTop1.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmPartialTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmPartialTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
    if ((rank >= 0) && (rank <= 2)) {
      if (this.hmPartialTestMapByExpCategoryTop3.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = ((Integer)this.hmPartialTestMapByExpCategoryTop3.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmPartialTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmPartialTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
    if ((rank >= 0) && (rank <= 4)) {
      if (this.hmPartialTestMapByExpCategoryTop5.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = ((Integer)this.hmPartialTestMapByExpCategoryTop5.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmPartialTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmPartialTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
    if ((rank >= 0) && (rank <= 9)) {
      if (this.hmPartialTestMapByExpCategoryTop10.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = 
          ((Integer)this.hmPartialTestMapByExpCategoryTop10.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmPartialTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmPartialTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
  }
  
  public void updateTotalTestMapDataByCategory(LWParameterModelEntry query)
  {
    if (this.hmDataTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType()))
    {
      int count = ((Integer)this.hmDataTestMapByExpCategory.get(query.getPd().getParameterExpressionType())).intValue();
      this.hmDataTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
    }
    else
    {
      this.hmDataTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
    }
  }
  
  public void updateCorrectTestMapDataByCategory(LWParameterModelEntry query, int rank)
  {
    if (this.hmCorrectTestMapByExpCategory.containsKey(query.getPd().getParameterExpressionType()))
    {
      int count = ((Integer)this.hmCorrectTestMapByExpCategory.get(query.getPd().getParameterExpressionType())).intValue();
      this.hmCorrectTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
    }
    else
    {
      this.hmCorrectTestMapByExpCategory.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
    }
    if ((rank >= 0) && (rank <= 0)) {
      if (this.hmCorrectTestMapByExpCategoryTop1.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = ((Integer)this.hmCorrectTestMapByExpCategoryTop1.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmCorrectTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmCorrectTestMapByExpCategoryTop1.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
    if ((rank >= 0) && (rank <= 2)) {
      if (this.hmCorrectTestMapByExpCategoryTop3.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = ((Integer)this.hmCorrectTestMapByExpCategoryTop3.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmCorrectTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmCorrectTestMapByExpCategoryTop3.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
    if ((rank >= 0) && (rank <= 4)) {
      if (this.hmCorrectTestMapByExpCategoryTop5.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = ((Integer)this.hmCorrectTestMapByExpCategoryTop5.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmCorrectTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmCorrectTestMapByExpCategoryTop5.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
    if ((rank >= 0) && (rank <= 9)) {
      if (this.hmCorrectTestMapByExpCategoryTop10.containsKey(query.getPd().getParameterExpressionType()))
      {
        int count = 
          ((Integer)this.hmCorrectTestMapByExpCategoryTop10.get(query.getPd().getParameterExpressionType())).intValue();
        this.hmCorrectTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(), Integer.valueOf(count + 1));
      }
      else
      {
        this.hmCorrectTestMapByExpCategoryTop10.put(query.getPd().getParameterExpressionType(), Integer.valueOf(1));
      }
    }
  }
  
  public void evaluateNov16(Expression queryExpression, LWParameterModelEntry query, MethodInvocation queryMi, MethodDeclaration queryMd, ICompilationUnit queryICompilationUnit, String queryExpectedType, String rtb)
  {
    ArrayList<LWParameterDescriptor> paramerList = new ArrayList();
    HashMap<String, Integer> fqMap = new HashMap();
    for (LWParameterModelEntry pme : this.parameterModelEntryList) {
      if ((pme.getPd().getParameterPosition() == query.getPd().getParameterPosition()) && 
        (pme.getPd().getMethodName().equals(query.getPd().getMethodName())))
      {
        LWParameterDescriptor pd = pme.getPd();
        
        float similarity = new CosineSimilarity().getSimilarity(pd.getNeighborList(), query.getPd()
          .getNeighborList());
        pd.setObject(Float.valueOf(similarity));
        paramerList.add(pd);
        if (fqMap.containsKey(pd.getParameterExpressionType()))
        {
          int count = ((Integer)fqMap.get(pd.getParameterExpressionType())).intValue();
          fqMap.put(pd.getParameterExpressionType(), Integer.valueOf(count + 1));
        }
        else
        {
          fqMap.put(pd.getParameterExpressionType(), Integer.valueOf(1));
        }
      }
    }
    int rank = -1;
    boolean found = false;
    updateWholeTestMapDataByCategory(query);
    if ((query.getPd().getParameterExpressionType().equals("QualifiedName")) || 
      (query.getPd().getParameterExpressionType().equals("StringLiteral")) || 
      (query.getPd().getParameterExpressionType().equals("ThisExpression")) || 
      (query.getPd().getParameterExpressionType().equals("NumberLiteral")) || 
      (query.getPd().getParameterExpressionType().equals("BooleanLiteral")) || 
      (query.getPd().getParameterExpressionType().equals("NullLiteral")) || 
      (query.getPd().getParameterExpressionType().equals("SimpleName")) || 
      (query.getPd().getParameterExpressionType().equals("MethodInvocation")) || 
      (query.getPd().getParameterExpressionType().equals("ClassInstanceCreation")))
    {
      if ((query.getPd().getParameterContent() instanceof MethodInvocationContent)) {
        query.getPd().setForcefulExpressioType(LWParameterDescriptor.QueryMethodInvocation);
      }
      query.getPd();System.out.println("Main Method Invocation: " + LWParameterDescriptor.getMethodInvocation());
      System.out.println("String Method Invocation: " + query.getPd().getParameterContent().getStringParamNode());
      
      System.out.println("**I am on qualified Name Query part: ");
      long startTime = System.currentTimeMillis();
      
      final HashMap<String, Integer> hmFrequency = new HashMap();
      
      ArrayList<LWParameterDescriptor> candidateList = new ArrayList();
      ArrayList<LWParameterDescriptor> possibleCandidateList = new ArrayList();
      ArrayList<LWParameterDescriptor> possibleCandidates = (ArrayList)this.mkGpdByMethod.get(query.getPd()
        .getMethodName(), query.getPd().getReceiverType());
      
      String parameterName = (String)this.parameterNameMap.get(rtb, queryMi.getName().getFullyQualifiedName(), 
        Integer.valueOf(queryMi.arguments().size()), Integer.valueOf(query.getPd().getParameterPosition()));
      SimpleNameCollector csnc;
      SimpleNameCollector csnc;
      if (parameterName == null) {
        csnc = new SimpleNameCollector("", queryExpectedType, queryMi.getName().getStartPosition(), 
          queryICompilationUnit, queryMd, rtb);
      } else {
        csnc = new SimpleNameCollector(parameterName, queryExpectedType, queryMi.getName().getStartPosition(), 
          queryICompilationUnit, queryMd, rtb);
      }
      csnc.getUniqueVariableList();
      
      System.out.println("ParameterModelEntryListSize = " + this.parameterModelEntryList.size());
      new HashMap();
      if (possibleCandidates == null) {
        possibleCandidates = new ArrayList();
      }
      int count;
      for (LWParameterDescriptor pd : possibleCandidates) {
        if (pd.getMethodName().equals(query.getPd().getMethodName())) {
          if (pd.getParameterPosition() == query.getPd().getParameterPosition()) {
            if ((pd.getReceiverType().equals(query.getPd().getReceiverType())) && 
              (pd.getMethodArguments() == query.getPd().getMethodArguments())) {
              if ((pd.getParameterExpressionType().equals("QualifiedName")) || 
                (pd.getParameterExpressionType().equals("StringLiteral")) || 
                (pd.getParameterExpressionType().equals("NumberLiteral")) || 
                (pd.getParameterExpressionType().equals("ThisExpression")) || 
                (pd.getParameterExpressionType().equals("MethodInvocation")) || 
                (pd.getParameterExpressionType().equals("NullLiteral")) || 
                (pd.getParameterExpressionType().equals("SimpleName")) || 
                (pd.getParameterExpressionType().equals("BooleanLiteral")) || 
                (pd.getParameterExpressionType().equals("ClassInstanceCreation")))
              {
                possibleCandidateList.add(pd);
                if (hmFrequency.containsKey(pd.getAbsStringRep()))
                {
                  count = ((Integer)hmFrequency.get(pd.getAbsStringRep())).intValue();
                  hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(count + 1));
                }
                else
                {
                  hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(1));
                }
              }
            }
          }
        }
      }
      Collections.sort(possibleCandidateList, new Comparator()
      {
        public int compare(LWParameterDescriptor fruit1, LWParameterDescriptor fruit2)
        {
          float f1 = ((Float)fruit1.getObject()).floatValue();
          float f2 = ((Float)fruit2.getObject()).floatValue();
          if (f1 > f2) {
            return -1;
          }
          if (f1 < f2) {
            return 1;
          }
          if (((Integer)hmFrequency.get(fruit1.getAbsStringRep())).intValue() > ((Integer)hmFrequency.get(fruit2.getAbsStringRep())).intValue()) {
            return -1;
          }
          if (((Integer)hmFrequency.get(fruit1.getAbsStringRep())).intValue() < ((Integer)hmFrequency.get(fruit2.getAbsStringRep())).intValue()) {
            return 1;
          }
          return 0;
        }
      });
      Collections.sort(possibleCandidateList, new Comparator()
      {
        public int compare(LWParameterDescriptor fruit1, LWParameterDescriptor fruit2)
        {
          return 0;
        }
      });
      hmFrequency.clear();
      
      int simplenameCounter = 0;
      int i;
      for (LWParameterDescriptor pd : possibleCandidateList) {
        if ((pd.getParameterContent() instanceof SimpleNameContent))
        {
          System.out.println("CSNC is empty:" + csnc.getUniqueVariableList().size());
          if ((simplenameCounter <= 2) && (csnc.getUniqueVariableList().size() > simplenameCounter))
          {
            pd.setForcefulExpressioType(LWParameterDescriptor.SimpleNameType);
            pd.setSecondaryObject(((ParamVariable)csnc.getUniqueVariableList().get(simplenameCounter)).getName());
            candidateList.add(pd);
            simplenameCounter++;
          }
        }
        else if (hmFrequency.containsKey(pd.getAbsStringRep()))
        {
          int count = ((Integer)hmFrequency.get(pd.getAbsStringRep())).intValue();
          hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(count + 1));
        }
        else
        {
          hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(1));
          if ((pd.getParameterContent() instanceof MethodInvocationContent))
          {
            MethodInvocationContent mic = (MethodInvocationContent)pd.getParameterContent();
            if ((mic.getReceiver() != null) && ((mic.getReceiver() instanceof SimpleNameContent)))
            {
              SimpleNameContent snc = (SimpleNameContent)mic.getReceiver();
              if ((snc.getTypeQualifiedName() != null) && (snc.getBindingKind() != -1) && 
                (snc.getBindingKind() == 3))
              {
                SimpleNameCollector simpleNameCollector = new SimpleNameCollector("", 
                  snc.getTypeQualifiedName(), queryMi.getName().getStartPosition(), 
                  queryICompilationUnit, queryMd, rtb);
                if (simpleNameCollector.getUniqueVariableList().size() > 0) {
                  for (int i = 0; i < simpleNameCollector.getUniqueVariableList().size(); i++)
                  {
                    LWParameterDescriptor newPd = pd.createClone();
                    newPd.setObject(pd.getObject());
                    newPd.setForcefulExpressioType(LWParameterDescriptor.MethodInvocation);
                    newPd.setSecondaryObject(((ParamVariable)simpleNameCollector.getUniqueVariableList().get(i))
                      .getName());
                    candidateList.add(newPd);
                  }
                }
              }
              else
              {
                candidateList.add(pd);
              }
            }
            else if ((mic.getReceiver() != null) && ((mic.getReceiver() instanceof MethodInvocationContent)))
            {
              System.out.println("I am on the other part");
              MethodInvocationContent mic2 = (MethodInvocationContent)mic.getReceiver();
              if ((mic2.getReceiver() != null) && ((mic2.getReceiver() instanceof SimpleNameContent)))
              {
                SimpleNameContent snc2 = (SimpleNameContent)mic2.getReceiver();
                if ((snc2.getTypeQualifiedName() != null) && (snc2.getBindingKind() != -1) && 
                  (snc2.getBindingKind() == 3))
                {
                  SimpleNameCollector snc = new SimpleNameCollector("", snc2.getTypeQualifiedName(), 
                    queryMi.getName().getStartPosition(), queryICompilationUnit, queryMd, rtb);
                  System.out.println("snc size: " + snc.getUniqueVariableList().size());
                  if (snc.getUniqueVariableList().size() > 0) {
                    for (i = 0; i < snc.getUniqueVariableList().size(); i++)
                    {
                      LWParameterDescriptor newPd = pd.createClone();
                      newPd.setObject(pd.getObject());
                      newPd.setForcefulExpressioType(LWParameterDescriptor.MethodInvocation);
                      newPd.setSecondaryObject(((ParamVariable)snc.getUniqueVariableList().get(i)).getName());
                      candidateList.add(newPd);
                    }
                  }
                }
              }
              else
              {
                candidateList.add(pd);
              }
            }
            candidateList.add(pd);
          }
          else if (!(pd.getParameterContent() instanceof SimpleNameContent))
          {
            candidateList.add(pd);
          }
        }
      }
      if (candidateList.size() > 0)
      {
        updateRecommendationMadeTestMapDataByCategory(query);
        updateTotalTestMapDataByCategory(query);
        this.testCases += 1;
      }
      else
      {
        System.out.println("Cannot proceed.Candidate list is empty");
        System.out.println("Method invocation for this expression: " + queryMi.toString() + "Position: " + 
          query.getPd().getParameterPosition() + "Type: " + query.getPd().getReceiverType());
        return;
      }
      long endTime = System.currentTimeMillis();
      this.time = (this.time + endTime - startTime);
      
      rank = -1;
      found = false;
      boolean partialFound = false;
      int partialRank = -1;
      ArrayList<String> candidateStringList = new ArrayList();
      
      System.out.println("Creating Candidate STring list: ");
      for (LWParameterDescriptor pd : candidateList) {
        if ((pd.getParameterContent() != null) && (!(pd.getParameterContent() instanceof SimpleNameContent))) {
          candidateStringList.add(pd.getMethodInvocationComparisonStringRep(pd.getParameterContent()) + ":" + 
            pd.getObject());
        } else {
          candidateStringList.add(pd.getSecondaryObject());
        }
      }
      System.out.println("End of Creating Candidate STring list: ");
      for (LWParameterDescriptor lpd : candidateList)
      {
        rank++;
        if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
          ((lpd.getParameterContent() instanceof QualifiedNameContent)) && 
          ((query.getPd().getParameterContent() instanceof QualifiedNameContent)))
        {
          System.out.println("Query: " + query.getPd().getParameterContent().getStringParamNode());
          System.out.println("Candidate: " + lpd.getParameterContent().getStringParamNode());
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof QualifiedNameContent))) {
            if (lpd.getParameterContent().getStringParamNode().equals(query.getPd().getParameterContent().getStringParamNode()))
            {
              System.out.println("Match Found: ");
              found = true;
              break;
            }
          }
        }
        else if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
          ((lpd.getParameterContent() instanceof ClassInstanceCreationContent)) && 
          ((query.getPd().getParameterContent() instanceof ClassInstanceCreationContent)))
        {
          ClassInstanceCreationContent cic = (ClassInstanceCreationContent)lpd.getParameterContent();
          ClassInstanceCreationContent qcic = (ClassInstanceCreationContent)query.getPd()
            .getParameterContent();
          if (cic.getAbsStringRep().equals(qcic.getAbsStringRep()))
          {
            found = true;
            System.out.println("Found Expected: Query: " + 
              query.getPd().getParameterContent().getStringParamNode() + "    Candidate: " + cic);
            break;
          }
        }
        else
        {
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof StringLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof StringLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            found = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof NumberLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof NumberLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            found = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof ThisExpressionContent)) && 
            ((query.getPd().getParameterContent() instanceof ThisExpressionContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            found = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof BooleanLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof BooleanLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().toString().trim())))
          {
            found = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof NullLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof NullLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            found = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() == LWParameterDescriptor.SimpleNameType) && 
            ((query.getPd().getParameterContent() instanceof SimpleNameContent)))
          {
            System.out.println("Query: " + 
              ((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode());
            System.out.println("Candidate: " + lpd.getObject().toString());
            if (lpd.getSecondaryObject().toString().equals(((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode()))
            {
              System.out.println("Match found");
              found = true;
              break;
            }
          }
        }
        if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
          ((lpd.getParameterContent() instanceof MethodInvocationContent)) && 
          ((query.getPd().getParameterContent() instanceof MethodInvocationContent)))
        {
          System.out.println("Query: " + query.getPd().getAbsStringRep() + ":   Main Query: " + 
            query.getPd().getParameterContent().getStringParamNode());
          if (lpd.getForcefulExpressioType() == LWParameterDescriptor.MethodInvocation) {
            System.out.println("MI Candidate: " + 
              lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()));
          }
          System.out.println("Candidate:" + candidateList.size() + "     Candidate STring List size: " + 
            candidateStringList.size());
          if (lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()).equals(query.getPd().getMethodInvocationComparisonStringRep(query.getPd().getParameterContent())))
          {
            found = true;
            break;
          }
        }
      }
      for (LWParameterDescriptor lpd : candidateList)
      {
        partialRank++;
        if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
          ((lpd.getParameterContent() instanceof QualifiedNameContent)) && 
          ((query.getPd().getParameterContent() instanceof QualifiedNameContent)))
        {
          System.out.println("Query: " + query.getPd().getParameterContent().getStringParamNode());
          System.out.println("Candidate: " + lpd.getParameterContent().getStringParamNode());
          
          System.out.println("Query: " + 
            ((QualifiedNameContent)query.getPd().getParameterContent()).getQualifier());
          System.out.println("Candidate: " + 
            ((QualifiedNameContent)lpd.getParameterContent()).getQualifier());
          if (((QualifiedNameContent)lpd.getParameterContent()).getQualifier().equals(((QualifiedNameContent)query.getPd().getParameterContent()).getQualifier()))
          {
            partialFound = true;
            break;
          }
        }
        else if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
          ((lpd.getParameterContent() instanceof ClassInstanceCreationContent)) && 
          ((query.getPd().getParameterContent() instanceof ClassInstanceCreationContent)))
        {
          ClassInstanceCreationContent cic = (ClassInstanceCreationContent)lpd.getParameterContent();
          ClassInstanceCreationContent qcic = (ClassInstanceCreationContent)query.getPd()
            .getParameterContent();
          if (cic.getAbsStringRep().equals(qcic.getAbsStringRep()))
          {
            partialFound = true;
            break;
          }
        }
        else
        {
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof StringLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof StringLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            partialFound = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof NumberLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof NumberLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            partialFound = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof ThisExpressionContent)) && 
            ((query.getPd().getParameterContent() instanceof ThisExpressionContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            partialFound = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof BooleanLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof BooleanLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().toString().trim())))
          {
            partialFound = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
            ((lpd.getParameterContent() instanceof NullLiteralContent)) && 
            ((query.getPd().getParameterContent() instanceof NullLiteralContent)) && 
            (lpd.getAbsStringRep().equals(query.getPd().getAbsStringRep().trim())))
          {
            partialFound = true;
            break;
          }
          if ((lpd.getForcefulExpressioType() == LWParameterDescriptor.SimpleNameType) && 
            ((query.getPd().getParameterContent() instanceof SimpleNameContent)))
          {
            System.out.println("Query: " + 
              ((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode());
            System.out.println("Candidate: " + lpd.getObject().toString());
            if (lpd.getSecondaryObject().toString().equals(((SimpleNameContent)query.getPd().getParameterContent()).getStringParamNode()))
            {
              System.out.println("Match found");
              partialFound = true;
              break;
            }
          }
        }
        if ((lpd.getForcefulExpressioType() != LWParameterDescriptor.SimpleNameType) && 
          ((lpd.getParameterContent() instanceof MethodInvocationContent)) && 
          ((query.getPd().getParameterContent() instanceof MethodInvocationContent)))
        {
          System.out.println("Query: " + query.getPd().getAbsStringRep() + ":   Main Query: " + 
            query.getPd().getParameterContent().getStringParamNode());
          if (lpd.getForcefulExpressioType() == LWParameterDescriptor.MethodInvocation) {
            System.out.println("MI Candidate: " + 
              lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()));
          }
          System.out.println("Candidate:" + candidateList.size() + "     Candidate STring List size: " + 
            candidateStringList.size());
          if (((MethodInvocationContent)lpd.getParameterContent()).getMethodName().equals(((MethodInvocationContent)query.getPd().getParameterContent()).getMethodName()))
          {
            partialFound = true;
            break;
          }
          Object qreceiver = query.getPd().getParameterContent().getReceiver();
          while ((qreceiver != null) && (!(qreceiver instanceof SimpleNameContent))) {
            if ((qreceiver instanceof MethodInvocationContent)) {
              qreceiver = ((MethodInvocationContent)qreceiver).getReceiver();
            } else if ((qreceiver instanceof SimpleNameContent)) {
              qreceiver = ((SimpleNameContent)qreceiver).getReceiver();
            } else if ((qreceiver instanceof QualifiedNameContent)) {
              qreceiver = ((QualifiedNameContent)qreceiver).getReceiver();
            }
          }
          if ((lpd.getSecondaryObject() != null) && (qreceiver != null) && 
            ((qreceiver instanceof SimpleNameContent)) && 
            (((SimpleNameContent)qreceiver).getIdentifier().equals(lpd.getSecondaryObject())))
          {
            partialFound = true;
            break;
          }
          if (lpd.getMethodInvocationComparisonStringRep(lpd.getParameterContent()).equals(query.getPd().getMethodInvocationComparisonStringRep(query.getPd().getParameterContent())))
          {
            partialFound = true;
            break;
          }
        }
      }
      System.out.println("Partial Found = " + partialFound + "  Partial Rank = " + partialRank);
      if (found)
      {
        System.out.println("Success");
        System.out.println("Recommendation Rank = " + rank + "Total Rank = " + this.totalRank);
      }
      else
      {
        System.out.println("Failed");
      }
      if ((found) && (rank >= 0) && (rank <= this.ACCEPTED_RANGE))
      {
        this.totalCorrectResult += 1;
        updateTotalRank(rank);
        updateCorrectTestMapDataByCategory(query, rank);
      }
      if ((partialFound) && (partialRank >= 0) && (partialRank <= this.ACCEPTED_RANGE)) {
        updatePartialTestMapDataByCategory(query, partialRank);
      }
      System.out.println("Required Time: " + (endTime - startTime));
      System.out.println("Candidate List Size: " + candidateList.size());
      System.out.println("In: " + query.getPd().getCompilationUnitName());
      query.getPd();System.out.println("MethodInvocatio: " + LWParameterDescriptor.getMethodInvocation());
      System.out.println("Total correct result: " + this.totalCorrectResult + "   Test cases: " + this.testCases + 
        "  Expected: " + query.getPd().getParameterContent().getStringParamNode() + "   suggestion: " + 
        candidateStringList);
    }
  }
  
  public void printEvaluationResultByCategory()
  {
    int totalCorrect = 0;
    int totalInstances = 0;
    int totalRecommended = 0;
    
    int totalCorrectTop1 = 0;
    int totalCorrectTop3 = 0;
    int totalCorrectTop5 = 0;
    int totalCorrectTop10 = 0;
    
    int totalPartialCorrectTop1 = 0;
    int totalPartialCorrectTop3 = 0;
    int totalPartialCorrectTop5 = 0;
    int totalPartialCorrectTop10 = 0;
    
    Iterator it = this.hmDataTestMapByExpCategory.keySet().iterator();
    while (it.hasNext())
    {
      String key = (String)it.next();
      int total = ((Integer)this.hmDataTestMapByExpCategory.get(key)).intValue();
      
      int correct = 0;
      int partial = 0;
      int recommendationMade = 0;
      int whole = 0;
      if (this.hmCorrectTestMapByExpCategory.containsKey(key))
      {
        correct = ((Integer)this.hmCorrectTestMapByExpCategory.get(key)).intValue();
        totalCorrect += correct;
      }
      if (this.hmCorrectTestMapByExpCategoryTop1.containsKey(key))
      {
        correct = ((Integer)this.hmCorrectTestMapByExpCategoryTop1.get(key)).intValue();
        totalCorrectTop1 += correct;
      }
      if (this.hmCorrectTestMapByExpCategoryTop3.containsKey(key))
      {
        correct = ((Integer)this.hmCorrectTestMapByExpCategoryTop3.get(key)).intValue();
        totalCorrectTop3 += correct;
      }
      if (this.hmCorrectTestMapByExpCategoryTop5.containsKey(key))
      {
        correct = ((Integer)this.hmCorrectTestMapByExpCategoryTop5.get(key)).intValue();
        totalCorrectTop5 += correct;
      }
      if (this.hmCorrectTestMapByExpCategoryTop10.containsKey(key))
      {
        correct = ((Integer)this.hmCorrectTestMapByExpCategoryTop10.get(key)).intValue();
        totalCorrectTop10 += correct;
      }
      if (this.hmPartialTestMapByExpCategoryTop1.containsKey(key))
      {
        partial = ((Integer)this.hmPartialTestMapByExpCategoryTop1.get(key)).intValue();
        totalPartialCorrectTop1 += partial;
      }
      if (this.hmPartialTestMapByExpCategoryTop3.containsKey(key))
      {
        partial = ((Integer)this.hmPartialTestMapByExpCategoryTop3.get(key)).intValue();
        totalPartialCorrectTop3 += partial;
      }
      if (this.hmPartialTestMapByExpCategoryTop5.containsKey(key))
      {
        partial = ((Integer)this.hmPartialTestMapByExpCategoryTop5.get(key)).intValue();
        totalPartialCorrectTop5 += partial;
      }
      if (this.hmPartialTestMapByExpCategoryTop10.containsKey(key))
      {
        partial = ((Integer)this.hmPartialTestMapByExpCategoryTop10.get(key)).intValue();
        totalPartialCorrectTop10 += partial;
      }
      if (this.hmWholeTestMapByExpCategory.containsKey(key))
      {
        whole = ((Integer)this.hmWholeTestMapByExpCategory.get(key)).intValue();
        totalInstances += whole;
      }
      if (this.hmRecommendationMadeTestMapByExpCategory.containsKey(key))
      {
        recommendationMade = ((Integer)this.hmRecommendationMadeTestMapByExpCategory.get(key)).intValue();
        totalRecommended += recommendationMade;
      }
      System.out.println("Expressition Type: " + key + "  Whole= " + whole + "  Total: " + total + "  Correct: " + 
        correct + "  Partial: " + partial + "   RM: " + recommendationMade);
    }
    System.out.println("For Top-1: TotalInstances: " + totalInstances + "  TotalRecommended: " + totalRecommended + 
      "   TotalCorrectTop1 = " + totalCorrectTop1);
    System.out.println("For Top-1: Precision = " + totalCorrectTop1 / (totalRecommended * 1.0F) + "   Recall= " + 
      totalCorrectTop1 / (totalInstances * 1.0F));
    System.out.println("For Top-1: Partial Precision = " + totalPartialCorrectTop1 / (totalRecommended * 1.0F) + 
      "  Partial Recall= " + totalPartialCorrectTop1 / (totalInstances * 1.0F));
    
    System.out.println("For Top-3: TotalInstances: " + totalInstances + "  TotalRecommended: " + totalRecommended + 
      "   TotalCorrectTop3 = " + totalCorrectTop3);
    System.out.println("For Top-3: Precision = " + totalCorrectTop3 / (totalRecommended * 1.0F) + "   Recall= " + 
      totalCorrectTop3 / (totalInstances * 1.0F));
    System.out.println("For Top-3: Partial Precision = " + totalPartialCorrectTop3 / (totalRecommended * 1.0F) + 
      "  Partial Recall= " + totalPartialCorrectTop3 / (totalInstances * 1.0F));
    
    System.out.println("For Top-5: TotalInstances: " + totalInstances + "  TotalRecommended: " + totalRecommended + 
      "   TotalCorrect = " + totalCorrectTop5);
    System.out.println("For Top-5: Precision = " + totalCorrectTop5 / (totalRecommended * 1.0F) + "   Recall= " + 
      totalCorrectTop5 / (totalInstances * 1.0F));
    System.out.println("For Top-5: Partial Precision = " + totalPartialCorrectTop5 / (totalRecommended * 1.0F) + 
      " Partial  Recall= " + totalPartialCorrectTop5 / (totalInstances * 1.0F));
    
    System.out.println("For Top-10:TotalInstances: " + totalInstances + "  TotalRecommended: " + totalRecommended + 
      "   TotalCorrect = " + totalCorrectTop10);
    System.out.println("For Top-10:Precision = " + totalCorrectTop10 / (totalRecommended * 1.0F) + "   Recall= " + 
      totalCorrectTop10 / (totalInstances * 1.0F));
    System.out.println("For Top-10:Partial Precision = " + totalPartialCorrectTop10 / (totalRecommended * 1.0F) + 
      "  Partial Recall= " + totalPartialCorrectTop10 / (totalInstances * 1.0F));
  }
  
  public boolean isInMethod(MethodInvocation node)
  {
    try
    {
      IType[] allTypes = this.iCompilationUnit.getAllTypes();
      IType[] arrayOfIType1;
      int j = (arrayOfIType1 = allTypes).length;
      for (int i = 0; i < j; i++)
      {
        IType itype = arrayOfIType1[i];
        IMethod[] methods = itype.getMethods();
        IMethod[] arrayOfIMethod1;
        int m = (arrayOfIMethod1 = methods).length;
        for (int k = 0; k < m; k++)
        {
          IMethod method = arrayOfIMethod1[k];
          if (node.getStartPosition() >= method.getSourceRange().getOffset()) {
            if (node.getStartPosition() <= method.getSourceRange().getOffset() + method.getSourceRange().getLength()) {
              return true;
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public boolean visit(TypeDeclaration node)
  {
    this.td = node;
    
    return true;
  }
  
  public boolean visit(MethodDeclaration node)
  {
    this.methodDeclaration = node;
    
    return true;
  }
  
  public void print()
  {
    System.out.println("Total Method Calls: " + this.totalMethodCalls);
    System.out.println("Interesting Method Calls: " + this.interestingMethodCalls);
    System.out.println("Unbound Method Calls: " + this.unboundMethodCalls);
    System.out.println("Method Calls With Parameter: " + this.methodCallWithParameter);
    
    Set<MultiKey> s = this.mkMethodExample.keySet();
    System.out.println("Size = " + s.size());
    Iterator<MultiKey> it = s.iterator();
    while (it.hasNext())
    {
      MultiKey key = (MultiKey)it.next();
      System.out.println("key0" + key.getKey(0));
      System.out.println("key0" + key.getKey(1));
      System.out.println("key0" + key.getKey(2));
      
      ((ArrayList)this.mkMethodExample
        .get(key.getKey(0), key.getKey(1), key.getKey(2)));
    }
  }
  
  public static CompilationUnit getCompilationUnit(ICompilationUnit unit)
  {
    ASTParser parser = ASTParser.newParser(3);
    parser.setKind(8);
    parser.setSource(unit);
    parser.setResolveBindings(true);
    return (CompilationUnit)parser.createAST(null);
  }
  
  String getVariableCategory(SimpleName sn)
  {
    HashMap bindingKind = new HashMap();
    bindingKind = new HashMap();
    bindingKind.put(Integer.valueOf(1), "PACKAGE");
    bindingKind.put(Integer.valueOf(2), "TYPE");
    bindingKind.put(Integer.valueOf(3), "VARIABLE");
    bindingKind.put(Integer.valueOf(4), "METHOD");
    bindingKind.put(Integer.valueOf(5), "ANNOTATION");
    bindingKind.put(Integer.valueOf(6), "MEMBER_VALUE_PAIR");
    if ((bindingKind.get(Integer.valueOf(sn.resolveBinding().getKind())).equals("VARIABLE")) && 
      ((sn.resolveBinding() instanceof IVariableBinding)))
    {
      IVariableBinding ivaraiableBinding = (IVariableBinding)sn.resolveBinding();
      if (ivaraiableBinding.isField()) {
        return "FIELD";
      }
      if (ivaraiableBinding.isParameter()) {
        return "VARIABLE_PARAMETER";
      }
      if (ivaraiableBinding.isEnumConstant()) {
        return "VARIABLE_ENUM_CONSTANT";
      }
      return "VARIABLE_ENUM_CONSTANT";
    }
    return null;
  }
  
  public MethodDeclaration getEnclosingMethodName(MethodInvocation mi)
  {
    ASTNode node = mi;
    while ((node != null) && (!(node instanceof MethodDeclaration))) {
      node = node.getParent();
    }
    return (MethodDeclaration)node;
  }
  
  public static void main(String[] args)
  {
    String qn = "I am here.Bangladesh";
    String typeName = qn.substring(0, qn.indexOf("."));
    String simpleName = qn.substring(qn.indexOf(".") + 1, qn.length());
    System.out.println("Simple = " + simpleName);
    System.out.println("Qualified = " + typeName);
  }
  
  public ArrayList<LWParameterDescriptor> recommend(int queryParameterPosition, String queryMethodName, int queryMethodNameStartPosition, int queryMethodArguments, String queryReceiverType, String queryNeighborList, MethodDeclaration queryMd, ICompilationUnit queryICompilationUnit, String queryExpectedType)
  {
    ArrayList<LWParameterDescriptor> paramerList = new ArrayList();
    HashMap<String, Integer> fqMap = new HashMap();
    System.out.println("Quesry Method Name: " + queryMethodName);
    System.out.println("Quesry ReceiverType: " + queryReceiverType);
    System.out.println("Query ExpectedType: " + queryExpectedType);
    System.out.println("Query parameter Position: " + queryMethodArguments);
    System.out.println("Query arguments: " + queryParameterPosition);
    
    System.out.println("PME list: " + this.parameterModelEntryList.size());
    for (LWParameterModelEntry pme : this.parameterModelEntryList) {
      if ((pme.getPd().getParameterPosition() == queryParameterPosition) && 
        (pme.getPd().getMethodName().equals(queryMethodName)))
      {
        LWParameterDescriptor pd = pme.getPd();
        
        float similarity = new CosineSimilarity().getSimilarity(pd.getNeighborList(), queryNeighborList);
        pd.setObject(Float.valueOf(similarity));
        paramerList.add(pd);
        if (fqMap.containsKey(pd.getParameterExpressionType()))
        {
          int count = ((Integer)fqMap.get(pd.getParameterExpressionType())).intValue();
          fqMap.put(pd.getParameterExpressionType(), Integer.valueOf(count + 1));
        }
        else
        {
          fqMap.put(pd.getParameterExpressionType(), Integer.valueOf(1));
        }
      }
    }
    System.out.println("Intial Match: " + paramerList.size());
    System.out.println("   >>>" + this.mkGpdByMethod.keySet().size());
    
    System.out.println("**I am on qualified Name Query part2: ");
    long startTime = System.currentTimeMillis();
    
    final HashMap<String, Integer> hmFrequency = new HashMap();
    
    ArrayList<LWParameterDescriptor> candidateList = new ArrayList();
    ArrayList<LWParameterDescriptor> possibleCandidateList = new ArrayList();
    ArrayList<LWParameterDescriptor> possibleCandidates = (ArrayList)this.mkGpdByMethod.get(queryMethodName, 
      queryReceiverType);
    System.out.println("QueryMenod anme: " + queryMethodName);
    System.out.println("Query Receiver: " + queryReceiverType);
    
    System.out.println("Possible Candiates: " + possibleCandidates);
    
    SimpleNameCollector csnc = new SimpleNameCollector("", queryExpectedType, queryMethodNameStartPosition, queryICompilationUnit, 
      queryMd, queryReceiverType);
    
    csnc.getUniqueVariableList();
    
    new HashMap();
    if (possibleCandidates == null) {
      possibleCandidates = new ArrayList();
    }
    int count;
    for (LWParameterDescriptor pd : possibleCandidates) {
      if (pd.getMethodName().equals(queryMethodName)) {
        if (pd.getParameterPosition() == queryParameterPosition) {
          if ((pd.getReceiverType().equals(queryReceiverType)) && 
            (pd.getMethodArguments() == queryMethodArguments) && 
            (pd.getParameterExpectedType().equals(queryExpectedType))) {
            if ((pd.getParameterExpressionType().equals("QualifiedName")) || 
              (pd.getParameterExpressionType().equals("StringLiteral")) || 
              (pd.getParameterExpressionType().equals("NumberLiteral")) || 
              (pd.getParameterExpressionType().equals("ThisExpression")) || 
              (pd.getParameterExpressionType().equals("MethodInvocation")) || 
              (pd.getParameterExpressionType().equals("NullLiteral")) || 
              (pd.getParameterExpressionType().equals("SimpleName")) || 
              (pd.getParameterExpressionType().equals("BooleanLiteral")) || 
              (pd.getParameterExpressionType().equals("ClassInstanceCreation")))
            {
              possibleCandidateList.add(pd);
              if (hmFrequency.containsKey(pd.getAbsStringRep()))
              {
                count = ((Integer)hmFrequency.get(pd.getAbsStringRep())).intValue();
                hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(count + 1));
              }
              else
              {
                hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(1));
              }
            }
          }
        }
      }
    }
    System.out.println("Possible Candidate List:" + possibleCandidateList.size());
    
    Collections.sort(possibleCandidateList, new Comparator()
    {
      public int compare(LWParameterDescriptor fruit1, LWParameterDescriptor fruit2)
      {
        float f1 = ((Float)fruit1.getObject()).floatValue();
        float f2 = ((Float)fruit2.getObject()).floatValue();
        if (f1 > f2) {
          return -1;
        }
        if (f1 < f2) {
          return 1;
        }
        if (((Integer)hmFrequency.get(fruit1.getAbsStringRep())).intValue() > ((Integer)hmFrequency.get(fruit2.getAbsStringRep())).intValue()) {
          return -1;
        }
        if (((Integer)hmFrequency.get(fruit1.getAbsStringRep())).intValue() < ((Integer)hmFrequency.get(fruit2.getAbsStringRep())).intValue()) {
          return 1;
        }
        return 0;
      }
    });
    Collections.sort(possibleCandidateList, new Comparator()
    {
      public int compare(LWParameterDescriptor fruit1, LWParameterDescriptor fruit2)
      {
        return 0;
      }
    });
    System.out.println("Finished Sorting...");
    hmFrequency.clear();
    
    int simplenameCounter = 0;
    SimpleNameContent snc2;
    for (LWParameterDescriptor pd : possibleCandidateList) {
      if ((pd.getParameterContent() instanceof SimpleNameContent))
      {
        System.out.println("CSNC is empty:" + csnc.getUniqueVariableList().size());
        if ((simplenameCounter <= 2) && (csnc.getUniqueVariableList().size() > simplenameCounter))
        {
          pd.setForcefulExpressioType(LWParameterDescriptor.SimpleNameType);
          pd.setSecondaryObject(((ParamVariable)csnc.getUniqueVariableList().get(simplenameCounter)).getName());
          candidateList.add(pd);
          simplenameCounter++;
        }
      }
      else if (hmFrequency.containsKey(pd.getAbsStringRep()))
      {
        int count = ((Integer)hmFrequency.get(pd.getAbsStringRep())).intValue();
        hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(count + 1));
      }
      else
      {
        hmFrequency.put(pd.getAbsStringRep(), Integer.valueOf(1));
        if ((pd.getParameterContent() instanceof MethodInvocationContent))
        {
          MethodInvocationContent mic = (MethodInvocationContent)pd.getParameterContent();
          if ((mic.getReceiver() != null) && ((mic.getReceiver() instanceof SimpleNameContent)))
          {
            SimpleNameContent snc = (SimpleNameContent)mic.getReceiver();
            if ((snc.getTypeQualifiedName() != null) && (snc.getBindingKind() != -1) && 
              (snc.getBindingKind() == 3))
            {
              SimpleNameCollector simpleNameCollector = new SimpleNameCollector("", 
                snc.getTypeQualifiedName(), queryMethodNameStartPosition, 
                queryICompilationUnit, queryMd, queryReceiverType);
              if (simpleNameCollector.getUniqueVariableList().size() > 0) {
                for (int i = 0; i < simpleNameCollector.getUniqueVariableList().size(); i++)
                {
                  LWParameterDescriptor newPd = pd.createClone();
                  newPd.setObject(pd.getObject());
                  newPd.setForcefulExpressioType(LWParameterDescriptor.MethodInvocation);
                  newPd.setSecondaryObject(((ParamVariable)simpleNameCollector.getUniqueVariableList().get(i))
                    .getName());
                  candidateList.add(newPd);
                }
              }
            }
            else
            {
              candidateList.add(pd);
            }
          }
          else if ((mic.getReceiver() != null) && ((mic.getReceiver() instanceof MethodInvocationContent)))
          {
            System.out.println("I am on the other part");
            MethodInvocationContent mic2 = (MethodInvocationContent)mic.getReceiver();
            if ((mic2.getReceiver() != null) && ((mic2.getReceiver() instanceof SimpleNameContent)))
            {
              snc2 = (SimpleNameContent)mic2.getReceiver();
              if ((snc2.getTypeQualifiedName() != null) && (snc2.getBindingKind() != -1) && 
                (snc2.getBindingKind() == 3))
              {
                SimpleNameCollector snc = new SimpleNameCollector("", snc2.getTypeQualifiedName(), 
                  queryMethodNameStartPosition, queryICompilationUnit, queryMd, 
                  queryReceiverType);
                System.out.println("snc size: " + snc.getUniqueVariableList().size());
                if (snc.getUniqueVariableList().size() > 0) {
                  for (int i = 0; i < snc.getUniqueVariableList().size(); i++)
                  {
                    LWParameterDescriptor newPd = pd.createClone();
                    newPd.setObject(pd.getObject());
                    newPd.setForcefulExpressioType(LWParameterDescriptor.MethodInvocation);
                    newPd.setSecondaryObject(((ParamVariable)snc.getUniqueVariableList().get(i)).getName());
                    candidateList.add(newPd);
                  }
                }
              }
            }
            else
            {
              candidateList.add(pd);
            }
          }
          candidateList.add(pd);
        }
        else if (!(pd.getParameterContent() instanceof SimpleNameContent))
        {
          candidateList.add(pd);
        }
      }
    }
    long endTime = System.currentTimeMillis();
    this.time = (this.time + endTime - startTime);
    
    ArrayList<String> candidateStringList = new ArrayList();
    
    System.out.println("Creating Candidate STring list: ");
    for (LWParameterDescriptor pd : candidateList) {
      if ((pd.getParameterContent() != null) && (!(pd.getParameterContent() instanceof SimpleNameContent))) {
        candidateStringList.add(pd.getMethodInvocationComparisonStringRep(pd.getParameterContent()) + ":" + 
          pd.getObject());
      } else {
        candidateStringList.add(pd.getSecondaryObject());
      }
    }
    System.out.println("End of Creating Candidate STring list: ");
    
    return candidateList;
  }
}
