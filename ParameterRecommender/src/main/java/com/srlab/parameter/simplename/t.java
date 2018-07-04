package com.codecompletion.parameter.category;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import plubibtest3.handlers.ConfigManager;

public class SimpleNameCollector3 extends ASTVisitor{

	/**
	 * @param args
	 */
	private MethodInvocation methodInvocationNode;
	private MethodDeclaration methodDeclaration;
	
	//private ICompilationUnit icu;
	private HashMap variableTable;
	ArrayList<ParamVariable> variableList;
	ArrayList<ParamVariable> uniqueVariableList;
	//ArrayList<VariableDeclarationFragment> vdclNodeList;
	ArrayList<Object> vdclNodeList;
	private static int count=0;
	private String expectedType;
	private String parameterName;
	private int startPosition;
	private ArrayList<SimpleName> usedVariable;
	public ArrayList<Object> getVdclNodeList() {
		return vdclNodeList;
	}

	public void updateParamVariableUsage(){
		for(ParamVariable v:this.variableList){
			//check whether it is used or not
			for(SimpleName sn:this.usedVariable){
				//System.out.println("vType:"+v.qualifiedTypeName+"   otherType:"+sn.resolveTypeBinding().getQualifiedName());
				if( sn.resolveTypeBinding()!=null && v.qualifiedTypeName.equals(sn.resolveTypeBinding().getQualifiedName())&& v.name.equals(sn.getIdentifier())){
					v.alreadyMatched=true;
					break;
				}
			}
		}
	}
	public void setVdclNodeList(ArrayList<Object> vdclNodeList) {
		this.vdclNodeList = vdclNodeList;
	}

	public int isTypeHierarchyMatches(String expectedType, ITypeBinding tb,int count){
		//our goal is to find whether the current class is in type hierarchy of the expected type
		//if(tb!=null && expectedType.equals(tb.getQualifiedName())) return true;
		
		//System.out.println("Is in type hierarchy")
		//otherwise check in backword direction
		
		while( tb!=null){
			count++;
			if(tb.getQualifiedName().equals(expectedType)){
				return count;
			}
			
			ITypeBinding bindingInterfaces[] = tb.getInterfaces();
			for(ITypeBinding bindingInterface:bindingInterfaces){
				int tempCount = this.isTypeHierarchyMatches(expectedType, bindingInterface,count);
				if(tempCount>0)
				{
						
				return tempCount;
				}
				/*if(bindingInterface.getQualifiedName().equals(expectedType)){
					return true;
				}
				while(bindingInterface.getSuperclass()!=null){
					bindingInterface = bindingInterface.getSuperclass();
					if(bindingInterface.getQualifiedName().equals(expectedType)){
						return true;
					}
				}*/
			}
			
			tb = tb.getSuperclass();	
		}
		return -1;
	}
	public boolean isTypeHierarchyMatches(String expectedType, ITypeBinding tb){
		//our goal is to find whether the current class is in type hierarchy of the expected type
		//if(tb!=null && expectedType.equals(tb.getQualifiedName())) return true;
		
		//System.out.println("Is in type hierarchy")
		//otherwise check in backword direction
		int count =0;
		while( tb!=null){
			count++;
			if(tb.getQualifiedName().equals(expectedType)){
				return true;
			}
			
			ITypeBinding bindingInterfaces[] = tb.getInterfaces();
			for(ITypeBinding bindingInterface:bindingInterfaces){
				if(this.isTypeHierarchyMatches(expectedType, bindingInterface)==true){
				
						
				return true;
				}
				/*if(bindingInterface.getQualifiedName().equals(expectedType)){
					return true;
				}
				while(bindingInterface.getSuperclass()!=null){
					bindingInterface = bindingInterface.getSuperclass();
					if(bindingInterface.getQualifiedName().equals(expectedType)){
						return true;
					}
				}*/
			}
			
			tb = tb.getSuperclass();	
		}
		return false;
	}
	
	public ArrayList<ParamVariable> getVariableList() {
		return variableList;
	}

	public void setVariableList(ArrayList<ParamVariable> variableList) {
		this.variableList = variableList;
	}

	public void collectInheritedVariables(ITypeBinding tb, Code expectedTypeCode,ITypeBinding rtb){
		//System.out.println("Collect Inherited Field for:"+tb.getQualifiedName());
		//System.out.println("Expected Type: :"+expectedType);
		
		//System.out.println("Collect Inherited Field for:"+tb+"   SuperClass= "+tb.getSuperclass());
		
		while(tb!=null && tb.getSuperclass()!=null){
				 
			IVariableBinding variables[] = tb.getSuperclass().getDeclaredFields();
			ArrayList<IVariableBinding> ivbList = new ArrayList(Arrays.asList(variables));	
			ArrayList<ParamVariable> tempVariableList = new ArrayList();	
			Collections.reverse(ivbList);
			for(IVariableBinding v:ivbList){
				 Code candidateCode= this.getPrimitiveTypeCode(v.getType().getQualifiedName());
					
				if(Flags.isPublic(v.getModifiers())|| Flags.isProtected(v.getModifiers()) ){
					 if((candidateCode==null && expectedTypeCode==null &&this.isTypeHierarchyMatches(expectedType, v.getType()))
								||(candidateCode==expectedTypeCode && candidateCode!=null)||
								(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
								(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
										)
					
					{ 
					
				    ParamVariable paramVariable = new ParamVariable(
							 v.getType().getQualifiedName(),
							 v.getName(),
							 ParamVariable.INHERITED_FIELD,
							 false,
							 count++,
							 this.methodInvocationNode.getName().getFullyQualifiedName(),
							 rtb.getQualifiedName()
						 );
					 variableList.add(paramVariable);
					 }
				}
			}
			
			tb = tb.getSuperclass();
		}
	}
	
	public ArrayList<SimpleName> getUsedVariable() {
		return usedVariable;
	}

	public void setUsedVariable(ArrayList<SimpleName> usedVariable) {
		this.usedVariable = usedVariable;
	}

	/*public SimpleNameCollector(String _expectedType, MethodInvocation _methodInvocation, MethodDeclaration _methodDeclaration, ITypeBinding rtb){
		this.methodDeclaration = _methodDeclaration;
		this.methodInvocationNode = _methodInvocation;
		this.expectedType = _expectedType;
		this.variableTable = new HashMap();
		this.variableList  = new ArrayList();
		this.uniqueVariableList = new ArrayList();
		this.vdclNodeList = new ArrayList();
		this.usedVariable = new ArrayList();
		Code expectedTypeCode = getPrimitiveTypeCode(expectedType);
		this.startPosition =    _methodInvocation.getName().getStartPosition();
		methodDeclaration.accept(this);
		this.addConstrainLocals(expectedTypeCode,rtb);
		if(methodDeclaration.parameters().size()>0){
			List<SingleVariableDeclaration> parameterList = methodDeclaration.parameters();
			
			for(SingleVariableDeclaration svdc:parameterList){
				if(svdc.resolveBinding()!=null && ConfigManager.getInstance().constrainTypeHierarchyMatches(rtb,svdc.resolveBinding().getType()))
				variableList.add(new ParamVariable(svdc.resolveBinding().getType().getName(),svdc.getName().getIdentifier(),ParamVariable.LOCAL,false, count++,
						this.methodInvocationNode.getName().getFullyQualifiedName(),rtb.getQualifiedName()));
			}
		}
		this.collectConstraintFieldVariables(expectedTypeCode,expectedType,rtb);
		
		HashMap hm = new HashMap();
		for(ParamVariable p:variableList){
			if(!hm.containsKey(p.name))
			{
				uniqueVariableList.add(p);
				hm.put(p.name, p);
			}
		}
	}*/

	public SimpleNameCollector(String _parameterName,String _expectedType, MethodInvocation _methodInvocation,ICompilationUnit _icu, MethodDeclaration _methodDeclaration, ITypeBinding rtb){
		//this.icu = _icu;
		this.parameterName = _parameterName;
		this.expectedType = _expectedType;
		this.methodDeclaration = _methodDeclaration;
		this.methodInvocationNode = _methodInvocation;
		
		this.variableTable = new HashMap();
		this.variableList  = new ArrayList();
		this.uniqueVariableList = new ArrayList();
		this.vdclNodeList = new ArrayList();
		this.usedVariable = new ArrayList();
		Code expectedTypeCode = getPrimitiveTypeCode(expectedType);
		this.startPosition =    _methodInvocation.getName().getStartPosition();
		methodDeclaration.accept(this);
		this.addLocals(expectedTypeCode,rtb);
		this.updateParamVariableUsage();
		
		//System.out.println("Collecting variables for recommendation. Method Name: "+this.methodDeclaration.getName().getFullyQualifiedName());
		//System.out.println("Collecting variables for recommendation. Method Invocation: "+this.methodInvocationNode.getName().getFullyQualifiedName());
			
		//System.out.println("After updating parameter usage: used variable size: "+this.usedVariable.size());
		//this.print();
		/*Collections.sort(variableList,
				new Comparator<ParamVariable>(){

					@Override
					public int compare(ParamVariable p1, ParamVariable p2) {
						// TODO Auto-generated method stub
						if(p1.getLocation()>p2.getLocation())
							return -1;
							else if(p1.getLocation()<p2.getLocation()){
								return 1;
							}
							else return 0;
					}
			
				
				}
				
				
		);*/
		//System.out.println("After Location Sort : stratPosition: "+startPosition);
		
	//	this.print();
		
		/*Collections.sort(variableList,
				new Comparator<ParamVariable>(){

					@Override
					public int compare(ParamVariable p1, ParamVariable p2) {
						// TODO Auto-generated method stub
						if(p1.getInheritanceDepth()>p2.getInheritanceDepth())
							return -1;
							else if(p1.getInheritanceDepth()<p2.getInheritanceDepth()){
								return 1;
							}
							else return 0;
					}
			
				
				}
				
				
		);*/
		//System.out.println("After Type Sort : stratPosition: "+startPosition);
        //this.print();
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		//add method parameters
		if(methodDeclaration.parameters().size()>0){
			List<SingleVariableDeclaration> parameterList = methodDeclaration.parameters();
			
			for(SingleVariableDeclaration svdc:parameterList){
				if(svdc.resolveBinding()!=null && isTypeHierarchyMatches(_expectedType,svdc.resolveBinding().getType()))
				variableList.add(new ParamVariable(svdc.resolveBinding().getType().getName(),svdc.getName().getIdentifier(),
                                                   ParamVariable.LOCAL,false, count++,
						this.methodInvocationNode.getName().getFullyQualifiedName(),rtb.getQualifiedName()));
			}
		}
		
		
		
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// add 'this'
		/*String fullyQualifiedName= td.getName().getFullyQualifiedName();
		if (methodDeclaration.resolveBinding()!=null && !Flags.isStatic(methodDeclaration.resolveBinding().getModifiers())) {
		      if (td!=null && this.isTypeHierarchyMatches(_expectedType, td.resolveBinding())) {
		        variableList.add(new ParamVariable(fullyQualifiedName, "this", ParamVariable.LITERALS, false, count++));  //$NON-NLS-1$
		      }
		    }
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			 if (expectedTypeCode == null) {
		      // add 'null'
		      variableList.add(new ParamVariable(expectedType, "null", ParamVariable.LITERALS, false,count++));  //$NON-NLS-1$
		    } else {
		      String typeName= expectedTypeCode.toString();
		      boolean isAutoboxing = !typeName.equals(expectedType);
		      if (expectedTypeCode == PrimitiveType.BOOLEAN) {
		        // add 'true', 'false'
		    	 variableList.add(new ParamVariable(typeName, "true", ParamVariable.LITERALS, false,count++));  //$NON-NLS-1$
		    	 variableList.add(new ParamVariable(typeName, "false", ParamVariable.LITERALS, false,count++));  //$NON-NLS-1$
		      } else {
		        // add 0
		    	  variableList.add(new ParamVariable(typeName, "0", ParamVariable.LITERALS,false, count++));   //$NON-NLS-1$
		      }
		    }*/
		//System.out.println("Fully Qualified Name:"+fullyQualifiedName+ "  ExpectedType: "+expectedType);
		//System.out.println("After performing usage update: ");
		//this.print();
		this.collectFieldVariables(expectedTypeCode,rtb);
		
		//sorting for our technique
		Collections.sort(variableList,new MyMatchComparator(parameterName));
		
		//this.collectInheritedVariables(td.resolveBinding(), expectedTypeCode);
		//System.out.println("Collection Finished");
		
	    //this.print();
		
		//System.out.println("After Sorting:");
		
		//System.out.println("ParameterName: "+parameterName);
		//this.orderMatches(variableList, parameterName); //this is for the jdt
		
		/*  remove duplicates*/
		
		HashMap hm = new HashMap();
		for(ParamVariable p:variableList){
			if(!hm.containsKey(p.name))
			{
				uniqueVariableList.add(p);
				hm.put(p.name, p);
			}
		}
		//System.out.println("Ordering Finished");
		//this.print();
	}
	public ArrayList<ParamVariable> getUniqueVariableList() {
		return uniqueVariableList;
	}

	public void setUniqueVariableList(ArrayList<ParamVariable> uniqueVariableList) {
		this.uniqueVariableList = uniqueVariableList;
	}

	private boolean isPrimitiveType(String type) {
	    return PrimitiveType.toCode(type) != null;
	  }
	 private PrimitiveType.Code getPrimitiveTypeCode(String type) {
	        PrimitiveType.Code code= PrimitiveType.toCode(type);
	        if (code != null) {
	          return code;
	        }
	        //if (fEnclosingElement != null && JavaModelUtil.is50OrHigher(fEnclosingElement.getJavaProject())) 
	        {
	          if (code == PrimitiveType.SHORT) {
	            if ("java.lang.Short".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          } else if (code == PrimitiveType.INT) {
	            if ("java.lang.Integer".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          } else if (code == PrimitiveType.LONG) {
	            if ("java.lang.Long".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          } else if (code == PrimitiveType.FLOAT) {
	            if ("java.lang.Float".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          } else if (code == PrimitiveType.DOUBLE) {
	            if ("java.lang.Double".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          } else if (code == PrimitiveType.CHAR) {
	            if ("java.lang.Character".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          } else if (code == PrimitiveType.BYTE) {
	            if ("java.lang.Byte".equals(type)) { //$NON-NLS-1$
	              return code;
	            }
	          }
	        }
	        return null;
	      }


	public HashMap getVariableTable() {
		return variableTable;
	}

	public void setVariableTable(HashMap variableTable) {
		this.variableTable = variableTable;
	}

	public void collectFieldVariables(Code expectedTypeCode, ITypeBinding rtb){

		//System.out.println("Collect Field Variables: ");
		//System.out.println("Method Declaration: "+this.methodDeclaration.resolveBinding());
		
		if(this.methodDeclaration.resolveBinding()!=null){
		 
			 IVariableBinding ivbs[] = this.methodDeclaration.resolveBinding().getDeclaringClass().getDeclaredFields();
			 ArrayList<IVariableBinding> ivbsList= new ArrayList(Arrays.asList(ivbs));
			 Collections.reverse(ivbsList);
			 ArrayList<ParamVariable> tempVariableList= new ArrayList();
			 for(IVariableBinding ivb: ivbsList){
				 //System.out.println("Field variable "+ivb.getName());
				 //System.out.println("Type "+ivb.getType().getQualifiedName());
				 //System.out.println(" Expected Type " + expectedType);
				 
				 //System.out.println("Is type hierarchy Matches "+this.isTypeHierarchyMatches(expectedType, ivb.getType()));
				 
				 
				 
				 Code candidateCode= this.getPrimitiveTypeCode(ivb.getType().getQualifiedName());
				 
				 if((candidateCode==null && expectedTypeCode==null &&ivb.getType()!=null && this.isTypeHierarchyMatches(expectedType, ivb.getType()))
							||(candidateCode==expectedTypeCode && candidateCode!=null)||
							(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
							(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
									){
				 ParamVariable paramVariable = new ParamVariable(
						 ivb.getType().getQualifiedName(),
						 ivb.getName(),
						 ParamVariable.FIELD,
						 false,
						 count++,
						 this.methodInvocationNode.getName().getFullyQualifiedName(),
						 rtb.getQualifiedName()
	
					 );
				 variableList.add(paramVariable);
				 }
			 }
		 }
		else{
			//find the type declaration
			ASTNode node = methodDeclaration.getParent();
			while(node!=null &&  !(node instanceof TypeDeclaration)){
				node = node.getParent();
			}
			if(node instanceof TypeDeclaration){
				TypeDeclaration td = (TypeDeclaration)node;
				if(td.resolveBinding()!=null){
					 IVariableBinding ivbs[] = td.resolveBinding().getDeclaredFields();
					 ArrayList<IVariableBinding> ivbsList= new ArrayList(Arrays.asList(ivbs));
					 Collections.reverse(ivbsList);
					 ArrayList<ParamVariable> tempVariableList= new ArrayList();
					 for(IVariableBinding ivb: ivbsList){
						 //System.out.println("Field variable "+ivb.getName());
						 //System.out.println("Type "+ivb.getType().getQualifiedName());
						 //System.out.println(" Expected Type " + expectedType);
						 
						 //System.out.println("Is type hierarchy Matches "+this.isTypeHierarchyMatches(expectedType, ivb.getType()));
						 
						 
						 
						 Code candidateCode= this.getPrimitiveTypeCode(ivb.getType().getQualifiedName());
						 
						 if((candidateCode==null && expectedTypeCode==null &&ivb.getType()!=null && this.isTypeHierarchyMatches(expectedType, ivb.getType()))
									||(candidateCode==expectedTypeCode && candidateCode!=null)||
									(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
									(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
											){
						 ParamVariable paramVariable = new ParamVariable(
								 ivb.getType().getQualifiedName(),
								 ivb.getName(),
								 ParamVariable.FIELD,
								 false,
								 count++,
								 this.methodInvocationNode.getName().getFullyQualifiedName(),
								 rtb.getQualifiedName()
			
							 );
						 variableList.add(paramVariable);
						 }
					 }

				}
			}
		}
		
		//check to see whether the method declaration and the corresponding type declaration indose another type declaration
		ASTNode node  = this.methodDeclaration.getParent();
		TypeDeclaration td = null;
		while(node !=null && !(node instanceof TypeDeclaration)){
			node = node.getParent();
		}
		if(node instanceof TypeDeclaration){
			System.out.println("T Name: "+((TypeDeclaration)node).getName().getFullyQualifiedName());
			td = (TypeDeclaration)node;
		
		node = td.getParent();
		while(node !=null && !(node instanceof TypeDeclaration)){
			node = node.getParent();
		}
		if(node instanceof TypeDeclaration){
		//System.out.println("This node contain a class inside another class");
		
		//System.out.println("Type Declaration resolved to: "+ ((TypeDeclaration)node).getName().getFullyQualifiedName());
		}
		if(node instanceof TypeDeclaration && ((TypeDeclaration)node).resolveBinding()!=null){

			 IVariableBinding ivbs[] = ((TypeDeclaration)node).resolveBinding().getDeclaredFields();
			 ArrayList<IVariableBinding> ivbsList= new ArrayList(Arrays.asList(ivbs));
			 Collections.reverse(ivbsList);
			 ArrayList<ParamVariable> tempVariableList= new ArrayList();
			 for(IVariableBinding ivb: ivbsList){
				 Code candidateCode= this.getPrimitiveTypeCode(ivb.getType().getQualifiedName());
				 
				 if((candidateCode==null && expectedTypeCode==null &&ivb.getType()!=null && this.isTypeHierarchyMatches(expectedType, ivb.getType()))
							||(candidateCode==expectedTypeCode && candidateCode!=null)||
							(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
							(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
									){
				 ParamVariable paramVariable = new ParamVariable(
						 ivb.getType().getQualifiedName(),
						 ivb.getName(),
						 ParamVariable.FIELD,
						 false,
						 count++,
						 this.methodInvocationNode.getName().getFullyQualifiedName(),
						 rtb.getQualifiedName()
	
					 );
				 variableList.add(paramVariable);
				 }
			 }

		}}
	}
	

	public void collectConstraintFieldVariables(Code expectedTypeCode, String expectedQualifiedType,ITypeBinding rtb){

		//System.out.println("Collect Field Variables: ");
		//System.out.println("Method Declaration: "+this.methodDeclaration.resolveBinding());
		
		if(this.methodDeclaration.resolveBinding()!=null){
		 
			 IVariableBinding ivbs[] = this.methodDeclaration.resolveBinding().getDeclaringClass().getDeclaredFields();
			 ArrayList<IVariableBinding> ivbsList= new ArrayList(Arrays.asList(ivbs));
			 Collections.reverse(ivbsList);
			 ArrayList<ParamVariable> tempVariableList= new ArrayList();
			 for(IVariableBinding ivb: ivbsList){
				 //System.out.println("Field variable "+ivb.getName());
				 //System.out.println("Type "+ivb.getType().getQualifiedName());
				 //System.out.println(" Expected Type " + expectedType);
				 
				 //System.out.println("Is type hierarchy Matches "+this.isTypeHierarchyMatches(expectedType, ivb.getType()));
				 
				 
				 
				 Code candidateCode= this.getPrimitiveTypeCode(ivb.getType().getQualifiedName());
				 
				 if((candidateCode==null && expectedTypeCode==null &&ivb.getType()!=null && ConfigManager.getInstance().constrainTypeHierarchyMatches(expectedQualifiedType, ivb.getType()))
							||(candidateCode==expectedTypeCode && candidateCode!=null)||
							(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
							(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
									){
				 ParamVariable paramVariable = new ParamVariable(
						 ivb.getType().getQualifiedName(),
						 ivb.getName(),
						 ParamVariable.FIELD,
						 false,
						 count++,
						 this.methodInvocationNode.getName().getFullyQualifiedName(),
						 rtb.getQualifiedName()
					 );
				 variableList.add(paramVariable);
				 }
			 }
		 }
		else{
			//find the type declaration
			ASTNode node = methodDeclaration.getParent();
			while(node!=null &&  !(node instanceof TypeDeclaration)){
				node = node.getParent();
			}
			if(node instanceof TypeDeclaration){
				TypeDeclaration td = (TypeDeclaration)node;
				if(td.resolveBinding()!=null){
					 IVariableBinding ivbs[] = td.resolveBinding().getDeclaredFields();
					 ArrayList<IVariableBinding> ivbsList= new ArrayList(Arrays.asList(ivbs));
					 Collections.reverse(ivbsList);
					 ArrayList<ParamVariable> tempVariableList= new ArrayList();
					 for(IVariableBinding ivb: ivbsList){
						 //System.out.println("Field variable "+ivb.getName());
						 //System.out.println("Type "+ivb.getType().getQualifiedName());
						 //System.out.println(" Expected Type " + expectedType);
						 
						 //System.out.println("Is type hierarchy Matches "+this.isTypeHierarchyMatches(expectedType, ivb.getType()));
						 
						 
						 
						 Code candidateCode= this.getPrimitiveTypeCode(ivb.getType().getQualifiedName());
						 
						 if((candidateCode==null && expectedTypeCode==null &&ivb.getType()!=null && ConfigManager.getInstance().constrainTypeHierarchyMatches(rtb, ivb.getType()))
									||(candidateCode==expectedTypeCode && candidateCode!=null)||
									(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
									(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)){
						 ParamVariable paramVariable = new ParamVariable(
								 ivb.getType().getQualifiedName(),
								 ivb.getName(),
								 ParamVariable.FIELD,
								 false,
								 count++,
								 this.methodInvocationNode.getName().getFullyQualifiedName(),
								 rtb.getQualifiedName()
			
							 );
						 variableList.add(paramVariable);
						 }
					 }
				}
			}
		}
		
		//check to see whether the method declaration and the corresponding type declaration indose another type declaration
		ASTNode node  = this.methodDeclaration.getParent();
		TypeDeclaration td = null;
		while(node !=null && !(node instanceof TypeDeclaration)){
			node = node.getParent();
		}
		if(node instanceof TypeDeclaration){
			System.out.println("T Name: "+((TypeDeclaration)node).getName().getFullyQualifiedName());
			td = (TypeDeclaration)node;
		
		node = td.getParent();
		while(node !=null && !(node instanceof TypeDeclaration)){
			node = node.getParent();
		}
		if(node instanceof TypeDeclaration){
		//System.out.println("This node contain a class inside another class");
		
		//System.out.println("Type Declaration resolved to: "+ ((TypeDeclaration)node).getName().getFullyQualifiedName());
		}
		if(node instanceof TypeDeclaration && ((TypeDeclaration)node).resolveBinding()!=null){

			 IVariableBinding ivbs[] = ((TypeDeclaration)node).resolveBinding().getDeclaredFields();
			 ArrayList<IVariableBinding> ivbsList= new ArrayList(Arrays.asList(ivbs));
			 Collections.reverse(ivbsList);
			 ArrayList<ParamVariable> tempVariableList= new ArrayList();
			 for(IVariableBinding ivb: ivbsList){
				 Code candidateCode= this.getPrimitiveTypeCode(ivb.getType().getQualifiedName());
				 
				 if((candidateCode==null && expectedTypeCode==null &&ivb.getType()!=null && this.isTypeHierarchyMatches(expectedType, ivb.getType()))
							||(candidateCode==expectedTypeCode && candidateCode!=null)||
							(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
							(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
									){
				 ParamVariable paramVariable = new ParamVariable(
						 ivb.getType().getQualifiedName(),
						 ivb.getName(),
						 ParamVariable.FIELD,
						 false,
						 count++,
						 this.methodInvocationNode.getName().getFullyQualifiedName(),
						 rtb.getQualifiedName()
	
					 );
				 variableList.add(paramVariable);
				 }
			 }

		}}
	}
public void addConstrainLocals(Code expectedTypeCode, ITypeBinding rtb){
		
		Collections.reverse(this.vdclNodeList);
		for(Object n:this.vdclNodeList){
			if(n instanceof VariableDeclaration){
				VariableDeclaration node = (VariableDeclaration)n;
				if(node.resolveBinding()==null) continue;
			Code candidateCode= this.getPrimitiveTypeCode(node.resolveBinding().getType().getQualifiedName());
			
			/*System.out.println("Local:+++++++++++"+node.toString()+  "  count = "+count);
			System.out.println("Expected Type: :"+expectedType);
			
			System.out.println("Expected:"+expectedTypeCode);
			System.out.println("Candidaete:"+candidateCode);
			System.out.println("resolve binding:"+node.resolveBinding());
			System.out.println("resolve binding:"+node.resolveBinding().getDeclaringClass());
			
			System.out.println("Is type hierarchy Matches: "+ this.isTypeHierarchyMatches(expectedType, node.resolveBinding().getType()));
			*/
			if((candidateCode==null && expectedTypeCode==null && node.resolveBinding()!=null && ConfigManager.getInstance().constrainTypeHierarchyMatches(rtb, node.resolveBinding().getType()))
			||(candidateCode==expectedTypeCode &&candidateCode!=null )||
			
			(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
			(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
					){
				if(this.isInScope(node) && node.getStartPosition()<this.methodInvocationNode.getName().getStartPosition()){
					IVariableBinding ivb=node.resolveBinding(); 
					
					ParamVariable paramVariable = new ParamVariable(
							 ivb.getType().getQualifiedName(),
							 ivb.getName(),
							 ParamVariable.LOCAL,
							 false,
							 count++,
							 this.methodInvocationNode.getName().getFullyQualifiedName(),
							 rtb.getQualifiedName()
						 );
					paramVariable.setLocation(node.getStartPosition());
					if(expectedTypeCode==null && node.resolveBinding()!=null){
						paramVariable.setInheritanceDepth(this.isTypeHierarchyMatches(expectedType, node.resolveBinding().getType(),0));
					}
					this.variableList.add(paramVariable);
				}
				
			}
			
			
		
			}
		else if(n instanceof Assignment){
			
			Assignment a = (Assignment)n;
			//System.out.println("I am in assignment variabel binding");
			//System.out.println("Start postions: "+a.getStartPosition());
			
			SimpleName sn = (SimpleName)a.getLeftHandSide();
			if(sn.resolveBinding()instanceof IVariableBinding){
				IVariableBinding ivb = (IVariableBinding)sn.resolveBinding();
				Code candidateCode= this.getPrimitiveTypeCode(sn.resolveTypeBinding().getQualifiedName());
				if((expectedTypeCode==null && ConfigManager.getInstance().constrainTypeHierarchyMatches(rtb,ivb.getType())
						)||
						(candidateCode==expectedTypeCode &&candidateCode!=null )||
						(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
								(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE))
								
								
						
						{
					
				
				
				//if(ivb.isField())
				{
					//System.out.println("I am in assignment variabel binding Field: ");
					
					ParamVariable paramVariable = new ParamVariable(
							 ivb.getType().getQualifiedName(),
							 ivb.getName(),
							 ParamVariable.LOCAL,
							 false,
							 count++,
							 this.methodInvocationNode.getName().getFullyQualifiedName(),
							 rtb.getQualifiedName()
		
						 );
					paramVariable.setLocation(sn.getStartPosition());
					
					this.variableList.add(paramVariable);
				/*}
				else if(ivb.isParameter()){
					System.out.println("I am in assignment variabel binding Parameter");
					
					ParamVariable paramVariable = new ParamVariable(
							 ivb.getType().getQualifiedName(),
							 ivb.getName(),
							 ParamVariable.LOCAL,
							 false,
							 count++
						 );
					paramVariable.setLocation(a.getStartPosition());
						
					this.variableList.add(paramVariable);
				}
				else if(ivb.isEnumConstant()){
					
				}
				else{*/
					
				}
				
			}}
		}
		}
		
	}
	
	public void addLocals(Code expectedTypeCode, ITypeBinding rtb){
		
		Collections.reverse(this.vdclNodeList);
		for(Object n:this.vdclNodeList){
			if(n instanceof VariableDeclaration){
				VariableDeclaration node = (VariableDeclaration)n;
				if(node.resolveBinding()==null) continue;
			Code candidateCode= this.getPrimitiveTypeCode(node.resolveBinding().getType().getQualifiedName());
			
			/*System.out.println("Local:+++++++++++"+node.toString()+  "  count = "+count);
			System.out.println("Expected Type: :"+expectedType);
			
			System.out.println("Expected:"+expectedTypeCode);
			System.out.println("Candidaete:"+candidateCode);
			System.out.println("resolve binding:"+node.resolveBinding());
			System.out.println("resolve binding:"+node.resolveBinding().getDeclaringClass());
			
			System.out.println("Is type hierarchy Matches: "+ this.isTypeHierarchyMatches(expectedType, node.resolveBinding().getType()));
			*/
			if((candidateCode==null && expectedTypeCode==null && node.resolveBinding()!=null && this.isTypeHierarchyMatches(expectedType, node.resolveBinding().getType()))
			||(candidateCode==expectedTypeCode &&candidateCode!=null )||
			
			(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
			(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE)
					){
				if(this.isInScope(node) && node.getStartPosition()<this.methodInvocationNode.getName().getStartPosition()){
					IVariableBinding ivb=node.resolveBinding(); 
					
					ParamVariable paramVariable = new ParamVariable(
							 ivb.getType().getQualifiedName(),
							 ivb.getName(),
							 ParamVariable.LOCAL,
							 false,
							 count++,
							 this.methodInvocationNode.getName().getFullyQualifiedName(),
							 rtb.getQualifiedName()
						 );
					paramVariable.setLocation(node.getStartPosition());
					if(expectedTypeCode==null && node.resolveBinding()!=null){
						paramVariable.setInheritanceDepth(this.isTypeHierarchyMatches(expectedType, node.resolveBinding().getType(),0));
					}
					this.variableList.add(paramVariable);
				}
				
			}
			
			
		
			}
		else if(n instanceof Assignment){
			
			Assignment a = (Assignment)n;
			//System.out.println("I am in assignment variabel binding");
			//System.out.println("Start postions: "+a.getStartPosition());
			
			SimpleName sn = (SimpleName)a.getLeftHandSide();
			if(sn.resolveBinding()instanceof IVariableBinding){
				IVariableBinding ivb = (IVariableBinding)sn.resolveBinding();
				Code candidateCode= this.getPrimitiveTypeCode(sn.resolveTypeBinding().getQualifiedName());
				if((expectedTypeCode==null && this.isTypeHierarchyMatches(expectedType,ivb.getType())
						)||
						(candidateCode==expectedTypeCode &&candidateCode!=null )||
						(candidateCode==PrimitiveType.LONG && expectedTypeCode==PrimitiveType.INT||expectedTypeCode==PrimitiveType.LONG)||
								(candidateCode==PrimitiveType.DOUBLE && expectedTypeCode==PrimitiveType.FLOAT||expectedTypeCode==PrimitiveType.DOUBLE))
								
								
						
						{
					
				
				
				//if(ivb.isField())
				{
					//System.out.println("I am in assignment variabel binding Field: ");
					
					ParamVariable paramVariable = new ParamVariable(
							 ivb.getType().getQualifiedName(),
							 ivb.getName(),
							 ParamVariable.LOCAL,
							 false,
							 count++,
							 this.methodInvocationNode.getName().getFullyQualifiedName(),
							 rtb.getQualifiedName()
		
						 );
					paramVariable.setLocation(sn.getStartPosition());
					
					this.variableList.add(paramVariable);
				/*}
				else if(ivb.isParameter()){
					System.out.println("I am in assignment variabel binding Parameter");
					
					ParamVariable paramVariable = new ParamVariable(
							 ivb.getType().getQualifiedName(),
							 ivb.getName(),
							 ParamVariable.LOCAL,
							 false,
							 count++
						 );
					paramVariable.setLocation(a.getStartPosition());
						
					this.variableList.add(paramVariable);
				}
				else if(ivb.isEnumConstant()){
					
				}
				else{*/
					
				}
				
			}}
		}
		}
		
	}
	@SuppressWarnings("unused")
	public boolean visit(VariableDeclarationFragment node) {
		//System.out.println("VariableDeclarationFragment Local Node: "+node.getStartPosition()+ "   ST: "+startPosition +"  Node: "+node.toString());
		  //IVariableBinding ivb[] = this.methodDeclaration.resolveBinding().getDeclaringClass().getDeclaredFields();
		if(node.getStartPosition()<startPosition)  
		this.vdclNodeList.add(node);
			return true; // do not continue 
			
	}
	
	public boolean visit(SingleVariableDeclaration node) {
		//check whether the variable is declared in an enhanced for statement
		
		ASTNode parent = node.getParent();
		while(parent!=null && !(parent instanceof EnhancedForStatement)){
			parent = parent.getParent();
		}
		if(parent instanceof EnhancedForStatement){
			if(node.getStartPosition()<startPosition){  
				this.vdclNodeList.add(node);
				return true; // do not continue 
			}
		}
		return true;
	}
	
	public boolean visit(SimpleName node) {
		//check to see whther the parent is method invocation
		if(node.getStartPosition()<this.startPosition){
		ASTNode parent = node.getParent();
		while(parent!=null && !(parent instanceof MethodInvocation) && !(parent instanceof MethodDeclaration)){
			parent = parent.getParent();
		}
		if(parent!=null && parent instanceof MethodInvocation ){
			this.usedVariable.add(node);
		}
		}
		return true;
		
	}

	
	public boolean visit(Assignment node) {
	    //IVariableBinding ivb[] = this.methodDeclaration.resolveBinding().getDeclaringClass().getDeclaredFields();  
		//check whether left node is a variable or not.
		//System.out.println("I am in assignment: "+node.toString());
		//System.out.println("I am in assignment: "+node.getLeftHandSide().toString());
		//System.out.println("I am in assignment: "+(node.getLeftHandSide() instanceof SimpleName));
		//System.out.println("Node st: "+(node.getLeftHandSide()).getStartPosition()+"   st: "+this.startPosition);
		Expression leftHand = node.getLeftHandSide();
		if(node.getStartPosition()<startPosition && leftHand instanceof SimpleName){
			SimpleName sn = (SimpleName)leftHand;
			this.vdclNodeList.add(node);
			return true; // do not continue
		}
		return  true;
	}
	
	public boolean isInScope(VariableDeclaration node) {
	    
		ASTNode currentNode1 = node;
		while((currentNode1 instanceof Statement) ==false || (
				(currentNode1 instanceof VariableDeclarationStatement)==true ||
				(currentNode1 instanceof ExpressionStatement)==true||
				(currentNode1 instanceof BreakStatement )==true ||
				(currentNode1 instanceof ContinueStatement )==true ||
				(currentNode1 instanceof ReturnStatement )==true ||
				(currentNode1 instanceof LabeledStatement )==true
			)  
		)
		
		{
			if(currentNode1.getParent()!=null)
			currentNode1 = currentNode1.getParent();
			else break;
		}
		
		
		/*added code to determine is in scope
		
		//ASTNode parent = node.getParent();
		while(parent!=null && !(parent instanceof Statement) &&  !(parent instanceof MethodDeclaration)  )
				{
			parent = parent.getParent();
			if(parent!=null){
				currentNode1 = parent;
			}
			else break;
		}
		*/
		
		
		/*ASTNode currentNode2 = this.methodInvocationNode;
		while((currentNode2 instanceof Statement)==false || 
				(currentNode2 instanceof VariableDeclarationStatement)==true || 
				(currentNode2 instanceof ExpressionStatement)==true
				){
			if(currentNode2.getParent()!=null)
			currentNode2 = currentNode2.getParent();
			else break;
		}*/
		
		//System.out.println("CurrentNode1: "+node+"@@@@@@@@@@"+currentNode1.toString());
		//System.out.println("CurrentNode2: "+currentNode2.toString());
		
		if(currentNode1.getStartPosition()<=this.methodInvocationNode.getStartPosition() && (currentNode1.getStartPosition()+currentNode1.getLength())>=this.methodInvocationNode.getStartPosition()){
			//System.out.println("They declared on the same block: "+node.toString());
			return true;
		}
		else{
			//System.out.println("They are not declared on the same block"+node.toString());
			
		}
        return false;
	}

	public void print(){
		/*Iterator it = this.variableTable.keySet().iterator();
		while(it.hasNext()){
			Object key = it.next();
			VariableInfo variableInfo = (VariableInfo) this.variableTable.get(key);
			variableInfo.print();
		}*/
		//System.out.println("Printing all variables....");
		//System.out.println("ExpectedType:"+this.expectedType);
		//System.out.println("Parametername:"+this.parameterName);
		
		//System.out.println("IsPrimitive:"+this.isPrimitiveType(expectedType));
		
		//System.out.println("MehodInvocation: "+this.methodInvocationNode.toString());
		for(ParamVariable paramVariable:this.variableList){
			//System.out.println(paramVariable);
			System.out.println("Variable Name: "+paramVariable.name+"  Type: "+paramVariable.qualifiedTypeName);
		}
		
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private static class MyMatchComparator implements Comparator {

	    private String fParamName;

	    MyMatchComparator(String paramName) {
	      fParamName= paramName;
	    }
	    public int compare(Object o1, Object o2) {
	      ParamVariable one= (ParamVariable)o1;
	      ParamVariable two= (ParamVariable)o2;

	      return score(two) - score(one);
	    }

	    /**
	     * The four order criteria as described below - put already used into bit 10, all others into
	     * bits 0-9, 11-20, 21-30; 31 is sign - always 0
	     * @param v
	     * @return the score for <code>v</code>
	     */
	    private int score(ParamVariable v) {
	      int subStringScore= getLongestCommonSubstring(v.name, fParamName).length();
	      // substring scores under 60% are not considered
	      // this prevents marginal matches like a - ba and false - isBool that will
	      // destroy the sort order
	      int shorter= Math.min(v.name.length(), fParamName.length());
	      if (subStringScore < 0.6 * shorter)
	        subStringScore= 0;

	      int score= subStringScore;
	      return score;
	    }

	  }
	private static class MatchComparator implements Comparator {

	    private String fParamName;

	    MatchComparator(String paramName) {
	      fParamName= paramName;
	    }
	    public int compare(Object o1, Object o2) {
	      ParamVariable one= (ParamVariable)o1;
	      ParamVariable two= (ParamVariable)o2;

	      return score(two) - score(one);
	    }

	    /**
	     * The four order criteria as described below - put already used into bit 10, all others into
	     * bits 0-9, 11-20, 21-30; 31 is sign - always 0
	     * @param v
	     * @return the score for <code>v</code>
	     */
	    private int score(ParamVariable v) {
	      int variableScore= 100 - v.variableType; // since these are increasing with distance
	      int subStringScore= getLongestCommonSubstring(v.name, fParamName).length();
	      // substring scores under 60% are not considered
	      // this prevents marginal matches like a - ba and false - isBool that will
	      // destroy the sort order
	      int shorter= Math.min(v.name.length(), fParamName.length());
	      if (subStringScore < 0.6 * shorter)
	        subStringScore= 0;

	      int positionScore= v.positionScore; // since ???
	      int matchedScore= v.alreadyMatched ? 0 : 1;
	      int autoboxingScore= v.isAutoboxingMatch ? 0 : 1;

	      int score= autoboxingScore << 30 | variableScore << 21 | subStringScore << 11 | matchedScore << 10 | positionScore;
	      return score;
	    }

	  }

	  /**
	   * Determine the best match of all possible type matches.  The input into this method is all
	   * possible completions that match the type of the argument. The purpose of this method is to
	   * choose among them based on the following simple rules:
	   *
	   *   1) Local Variables > Instance/Class Variables > Inherited Instance/Class Variables
	   *
	   *   2) A longer case insensitive substring match will prevail
	   *
	   *  3) Variables that have not been used already during this completion will prevail over
	   *     those that have already been used (this avoids the same String/int/char from being passed
	   *     in for multiple arguments)
	   *
	   *   4) A better source position score will prevail (the declaration point of the variable, or
	   *     "how close to the point of completion?"
	   *
	   * @param typeMatches the list of type matches
	   * @param paramName the parameter name
	   */
	  private static void orderMatches(List typeMatches, String paramName) {
	    if (typeMatches != null) Collections.sort(typeMatches, new MatchComparator(paramName));
	  }

	  /**
	   * Returns the longest common substring of two strings.
	   *
	   * @param first the first string
	   * @param second the second string
	   * @return the longest common substring
	   */
	  private static String getLongestCommonSubstring(String first, String second) {

	    String shorter= (first.length() <= second.length()) ? first : second;
	    String longer= shorter == first ? second : first;

	    int minLength= shorter.length();

	    StringBuffer pattern= new StringBuffer(shorter.length() + 2);
	    String longestCommonSubstring= ""; //$NON-NLS-1$

	    for (int i= 0; i < minLength; i++) {
	      for (int j= i + 1; j <= minLength; j++) {
	        if (j - i < longestCommonSubstring.length())
	          continue;

	        String substring= shorter.substring(i, j);
	        pattern.setLength(0);
	        pattern.append('*');
	        pattern.append(substring);
	        pattern.append('*');

	        StringMatcher matcher= new StringMatcher(pattern.toString(), true, false);
	        if (matcher.match(longer))
	          longestCommonSubstring= substring;
	      }
	    }

	    return longestCommonSubstring;
	  }

	  private Image getImage(ImageDescriptor descriptor) {
	    return (descriptor == null) ? null : JavaPlugin.getImageDescriptorRegistry().get(descriptor);
	  }
}
