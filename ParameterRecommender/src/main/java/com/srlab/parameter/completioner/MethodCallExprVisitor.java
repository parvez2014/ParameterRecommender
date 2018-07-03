package com.srlab.parameter.completioner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.sail.evaluatingevaluator.completioner.ModelEntry;
import com.sail.evaluatingevaluator.completioner.Token;
import com.srlab.parameter.binding.JSSConfigurator;
import com.srlab.parameter.config.Config;


public class MethodCallExprVisitor extends VoidVisitorAdapter<Void>{

	private List<ParameterDescriptor> parameterDescriptorList;
	private String filePath;
	private CompilationUnit cu;
	private HashMap<String,String> hmKeyword;
	private HashMap<String, String> hmLineKeyword;
	public static int NL=4;
	
	public MethodCallExprVisitor(CompilationUnit _cu, String _path) {
		// TODO Auto-generated constructor stub
		this.cu = _cu;
		this.parameterDescriptorList = new LinkedList();
		this.filePath = _path;
		this.hmKeyword = new HashMap();
		this.hmLineKeyword = new HashMap();
		this.initKeywords();
	}
	
	private void initKeywords(){
		
		String keywords[]={"String","abstract",	"continue",	"goto",	"package",	"switch",
		"assert",	"default",	"if",	"this",
		"boolean",	"do","implements"	,"throw",
		"break",	"double",	"import",	"throws",
		"byte",	"else",	"instanceof",	"return",	"transient",
		"case",	"extends",	"int",	"short",	"try",
		"catch",	"final",	"interface",	"static",	"void",
		"char",	"finally",	"long",	"strictfp",	"volatile",
		"class",	"native",	"super",	"while",
		"const",	"for",	"new",	"synchronized"};
		
		String lineKeywords[]={"String","abstract",	"continue",	"goto",	"package",	"switch",
				"assert",	"default",	"if",	"this",
				"boolean",	"do","implements"	,"throw",
				"break",	"double",	"import",	"throws",
				"byte",	"else",	"instanceof",	"return",	"transient",
				"case",	"extends",	"int",	"short",	"try",
				"catch",	"final",	"interface",	"static",	"void",
				"char",	"finally",	"long",	"strictfp",	"volatile",
				"class",	"native",	"super",	"while",
				"const",	"for",	"new",	"synchronized","[","]","="};	
	
		for(String k:keywords){
			hmKeyword.put(k, k);
		}
		for(String k:lineKeywords){
			hmLineKeyword.put(k, k);
		}
	}
	
	public CompilationUnit getCu() {
		return cu;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public MethodDeclaration getMethodDeclarationContainer(Node node) {
		Optional<Node> parent = node.getParentNode();
		while(parent.isPresent() && ((parent.get() instanceof MethodDeclaration))==false){
			parent = parent.get().getParentNode();
		}
		if(parent.isPresent() && ((parent.get())instanceof MethodDeclaration)) {
			return (MethodDeclaration)parent.get();
		}
		else return null;
	}
	
	 public String getLineContent(List<Token> stList, int index){
			
		  List<String> list = new ArrayList();
		  int newLineNumber=stList.get(index).getLine();
		  //System.out.println("LN="+lineNumber+"  Method LineNumber:="+entryParam.getMethodStartLine()+"  Index "+index);
		  int oldLineNumber = stList.get(index).getLine();
		  for(int i=0,j=index,k=0,l=0;j>=0 && stList.get(j).getLine()>=1 && k<1;j--)
		  {
			  Pattern patt  = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)\\.([a-zA-Z0-9\\_]+)");
			  Pattern patt2 = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)");
			  
				//System.out.println(":"+stList.get(j).getToken());
				Matcher m = patt.matcher(stList.get(j).getToken());
				Matcher m2 = patt2.matcher(stList.get(j).getToken());
				
				//System.out.println("Matcher m= "+ m);
				
				if( m.matches()){
					if((j+1)<stList.size() && stList.get(j+1).getToken().startsWith("(")){
						//System.out.println("I am in if part");
							
					String s=m.group(4);//constants are usually written in the upper case letter 
					
					//if(mkMapToEntry.containsKey(file.getAbsolutePath(),stList.get(j).getLine()))
					{
						//Entry entry= (Entry)mkMapToEntry.get(file.getAbsolutePath(),stList.get(j).getLine());
						//if(entry.getBinding().startsWith("javax.swing")||entry.getBinding().startsWith("java.awt"))
						{
							//list.add(entry.getBinding());
							//list.add(entry.getShortName());
							
							if(m.group(4).toUpperCase().equals(s)){
								//list.add(m.group(3));
								//System.out.println("I am in ifif part");
							}
							else{				
								//System.out.println("I am in if-else part");
								//System.out.println("Adiing: "+m.group(4));
								
								//added code
											if(Character.isUpperCase( m.group(4).charAt(0)))
											list.add(m.group(4));
											else{
												//if(this.findSupport(m.group(4), calls)>=1)
												list.add(m.group(4));
												
											}
							}
						}
						
						}
					
					
					}
						
					
				}
				else if (m2.matches() &&(j+1)<stList.size() && stList.get(j+1).getToken().startsWith("(")){
					//System.out.println("I am in else if part");
					if((j+1)<stList.size() && stList.get(j+1).getToken().startsWith("(")){
						
						String s=m2.group(3);//constants are usually written in the upper case letter 
						
						//if(mkMapToEntry.containsKey(file.getAbsolutePath(),stList.get(j).getLine()))
						{
							//Entry entry= (Entry)mkMapToEntry.get(file.getAbsolutePath(),stList.get(j).getLine());
							//if(entry.getBinding().startsWith("javax.swing")||entry.getBinding().startsWith("java.awt"))
							{
								//list.add(entry.getBinding());
								//list.add(entry.getShortName());
								
								if(m2.group(3).toUpperCase().equals(s)){
									//list.add(m.group(3));
								}
								else{			
									if(Character.isUpperCase( m2.group(3).charAt(0)))
										list.add(m2.group(3));
									else{
										//if(this.findSupport( m2.group(3), calls)>=1)
										list.add(m2.group(3));
												
									}
										
								}
							}
						}
					}
				}
				else{
					//System.out.println("I am in else part");
					//System.out.println("Token = "+stList.get(j).getToken());
					if(this.hmLineKeyword.containsKey(stList.get(j).getToken())){
						 // System.out.println(""+stList.get(j).getToken());
						    list.add(stList.get(j).getToken());
					}
					
					else if(stList.get(j).getToken().equals("(")||stList.get(j).getToken().equals(")")||stList.get(j).getToken().equals(",")||stList.get(j).getToken().equals("{")||stList.get(j).getToken().equals("(")||stList.get(j).getToken().equals(")")||stList.get(j).getToken().equals("}")||stList.get(j).getToken().equals(")")||stList.get(j).getToken().equals(",")==true||stList.get(j).getToken().equals(";")==true ||Character.isUpperCase(stList.get(j).getToken().charAt(0))==false ){} 
					else list.add(stList.get(j).getToken());
				}	
			    newLineNumber= stList.get(j).getLine();
			    if(oldLineNumber!=newLineNumber ) {
				  k++;
			    //System.out.println("Value of k="+k);
			  	oldLineNumber=newLineNumber;
			  }
				 
		  }
		  
		  //System.out.println("Temp neighbor = "+list);
		  StringBuffer sb = new StringBuffer("");
		  if(list.size()>0){String old = list.get(0);
		  sb.append(old);sb.append(" ");
		  for(int i=1;i<list.size();i++){
			String str_new = list.get(i);
			if(old.equals(str_new)==false)
			{	sb.append(list.get(i));old = str_new;
				sb.append(" ");
			}
		  }
		  }
		  return sb.toString();
		  //return list;
	  }
	
	  public String calcNeighbor(List<Token> stList, int index) throws FileNotFoundException{
		  //this.overallContextLine=0;
		  List<String> list = new ArrayList();
		  int newLineNumber=stList.get(index).getLine();
		  //System.out.println("LN="+lineNumber+"  Method LineNumber:="+entryParam.getMethodStartLine()+"  Index "+index);
		  int oldLineNumber = stList.get(index).getLine();
		  int startLineNumber = oldLineNumber;
		  for(int i=0,j=index,k=0,l=0;j>=0 && stList.get(j).getLine()>=1 && k<MethodCallExprVisitor.NL;j--)
		  
		  {
			  ////System.out.println(""+stList.get(j).getToken()+"  Line Number: "+stList.get(j).getLine());
				
			  Pattern patt  = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)\\.([a-zA-Z0-9\\_]+)");
			  Pattern patt2 = Pattern.compile("((this|super)\\.)?([\\_a-zA-Z0-9]+)");
			  
				//System.out.println(":"+stList.get(j).getToken());
				Matcher m = patt.matcher(stList.get(j).getToken());
				Matcher m2 = patt2.matcher(stList.get(j).getToken());
				
				//System.out.println("Matcher m= "+ m);
				
				if( m.matches()){
					if((j+1)<stList.size() && stList.get(j+1).getToken().startsWith("(")){
						//System.out.println("I am in if part");
							
					String s=m.group(4);//constants are usually written in the upper case letter 
					
					//if(mkMapToEntry.containsKey(file.getAbsolutePath(),stList.get(j).getLine()))
					{
						//Entry entry= (Entry)mkMapToEntry.get(file.getAbsolutePath(),stList.get(j).getLine());
						//if(entry.getBinding().startsWith("javax.swing")||entry.getBinding().startsWith("java.awt"))
						{
							//list.add(entry.getBinding());
							//list.add(entry.getShortName());
							
							if(m.group(4).toUpperCase().equals(s)){
								//list.add(m.group(3));
								//System.out.println("I am in ifif part");
							}
							else{				
								//System.out.println("I am in if-else part");
								//System.out.println("Adiing: "+m.group(4));
								
								//added code
											if(Character.isUpperCase( m.group(4).charAt(0))){
											    list.add(m.group(4));
											}
											else{
												//if(this.findSupport(m.group(4), calls)>=1)
												list.add(m.group(4));
												
											}
							}
						}
						
						}
					
					
					}
						
					
				}
				else if (m2.matches() &&(j+1)<stList.size() && stList.get(j+1).getToken().startsWith("(")){
					//System.out.println("I am in else if part");
					if((j+1)<stList.size() && stList.get(j+1).getToken().startsWith("(")){
						
						String s=m2.group(3);//constants are usually written in the upper case letter 
						
						//if(mkMapToEntry.containsKey(file.getAbsolutePath(),stList.get(j).getLine()))
						{
							//Entry entry= (Entry)mkMapToEntry.get(file.getAbsolutePath(),stList.get(j).getLine());
							//if(entry.getBinding().startsWith("javax.swing")||entry.getBinding().startsWith("java.awt"))
							{
								//list.add(entry.getBinding());
								//list.add(entry.getShortName());
								
								if(m2.group(3).toUpperCase().equals(s)){
									//list.add(m.group(3));
								}
								else{			
									if(Character.isUpperCase( m2.group(3).charAt(0))){
										list.add(m2.group(3));
									}
									else{
										//if(this.findSupport( m2.group(3), calls)>=1)
										list.add(m2.group(3));
												
									}
										
								}
							}
						}
					}
				}
				else{
					//System.out.println("I am in else part");
					//System.out.println("Token = "+stList.get(j).getToken());
					if(this.hmKeyword.containsKey(stList.get(j).getToken())){
						 // System.out.println(""+stList.get(j).getToken());
						list.add(stList.get(j).getToken());
					}
					
					else if(stList.get(j).getToken().equals("(")||stList.get(j).getToken().equals(")")||stList.get(j).getToken().equals(",")||stList.get(j).getToken().equals("{")||stList.get(j).getToken().equals("(")||stList.get(j).getToken().equals(")")||stList.get(j).getToken().equals("}")||stList.get(j).getToken().equals(")")||stList.get(j).getToken().equals(",")==true||stList.get(j).getToken().equals(";")==true ||Character.isUpperCase(stList.get(j).getToken().charAt(0))==false ){} 
					//else list.add(stList.get(j).getToken());
				}	
			    newLineNumber= stList.get(j).getLine();
			    if(oldLineNumber!=newLineNumber)// && ((stList.get(j).getToken().equalsIgnoreCase("{")||stList.get(j).getToken().equalsIgnoreCase("}")||stList.get(j).getToken().equalsIgnoreCase("(")||stList.get(j).getToken().equalsIgnoreCase(")")||stList.get(j).getToken().equalsIgnoreCase(";")  ))) 
			    {
				  k++;
			     // System.out.println("Value of k = "+k+"  Token: "+stList.get(j).getToken()+"   NewLineNumber: "+newLineNumber);
			  	oldLineNumber=newLineNumber;
			  	//this.overallContextLine = newLineNumber;
			  }
				 
		  }
		 // this.overallContextLine = startLineNumber-this.overallContextLine;
		  //System.out.println("Temp neighbor = "+list);
		  StringBuffer sb = new StringBuffer("");
		  if(list.size()>0){String old = list.get(0);
		  sb.append(old);sb.append(" ");
		  for(int i=1;i<list.size();i++){
			String str_new = list.get(i);
			if(old.equals(str_new)==false)
			{	sb.append(list.get(i));old = str_new;
				sb.append(" ");
			}
		  }
		  }
		  //System.out.println("After compaction neighbor = "+sb);
		  return sb.toString();
		  //return list;
	  }
	
	  public List<Token> tokenize(String  input) throws IOException{
			
			//Step-1: declare return object 
			List<Token> tokenList = new ArrayList();
			
		    
			//Step-2: initialize StreamTokenizer to tokenize source code 
			InputStream is = new ByteArrayInputStream(input.getBytes());
		    StreamTokenizer streamTokenizer = new StreamTokenizer(new InputStreamReader(is));
		    streamTokenizer.parseNumbers();
		    streamTokenizer.wordChars('_', '_');
		    streamTokenizer.eolIsSignificant(false);
		    streamTokenizer.slashSlashComments(true);
		    streamTokenizer.slashStarComments(true);
		   	 
		    
		    //Step-3: collect list of tokens
		    int tokenNumber=0;
		    int token = streamTokenizer.nextToken();
		    while (token != StreamTokenizer.TT_EOF) {
		 
		    	if(streamTokenizer.sval!=null && streamTokenizer.sval.matches("\\s+")){
		    	  tokenNumber++;
		    	}
		      
		    	switch (token) {
		    	
		    		case StreamTokenizer.TT_NUMBER: double num = streamTokenizer.nval; 
		    										break;
		    		case StreamTokenizer.TT_WORD:   tokenList.add(new Token(streamTokenizer.sval,streamTokenizer.lineno()));
		    									  	break;
		    		case '"': 	String dquoteVal = streamTokenizer.sval;
		    					break;
		    		case '\'':	String squoteVal = streamTokenizer.sval;
		    					break;
		    		case StreamTokenizer.TT_EOL:	break;
		    		case StreamTokenizer.TT_EOF:    break;
		    		
		    		default: char ch = (char) streamTokenizer.ttype;
		    				 tokenList.add(new Token(""+ch,streamTokenizer.lineno())); break;
		    	}
		    	token = streamTokenizer.nextToken();
		    }
		    is.close();
		    return tokenList;
		}
		
		//input points to complete source code. Start is the start and end is of Position type
		private String collectSourceString(String input, Position start, Position end) {
			int idxStart = -1;
			int curIdx = 0;
			int curLine = 1;
			
			while(curLine<=end.line){
				if(curLine>=start.line && curLine<=end.line) {
					if(curLine==start.line && idxStart==-1){
						idxStart = curIdx+start.column-1;
					}
					if(curLine==end.line) {
						for(int column=0;column<end.column;column++){
							curIdx++;
						}
						curLine++;
					}
				}	
				if(input.charAt(curIdx)=='\n') curLine++;
				
				curIdx++;
			}
			System.out.println("Start = "+idxStart+" End: "+curIdx);
			return input.substring(idxStart,curIdx);
		}
		
		@Override
		public void visit(MethodCallExpr m, Void arg) {
			// TODO Auto-generated method stub
			super.visit(m, arg);
			if(m.getScope().isPresent()) {
				try {					
					SymbolReference<ResolvedMethodDeclaration> resolvedMethodDeclaration = JSSConfigurator.getInstance().getJpf().solve(m);
					List<String> parameterList = new ArrayList();
					//System.out.println("Number of Parameters: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getNumberOfParams());
					
					for(int i=0;i<resolvedMethodDeclaration.getCorrespondingDeclaration().getNumberOfParams();i++) {
						ResolvedType type = resolvedMethodDeclaration.getCorrespondingDeclaration().getParam(i).getType();
						if(type.isPrimitive())
							parameterList.add(type.asPrimitive().name());
						else if(type.isReferenceType())
							parameterList.add(type.asReferenceType().getQualifiedName());
						else {
							//System.out.println("Type: "+type);
							throw new RuntimeException("Error in resolving parameter type");
						}		
					}
					if(resolvedMethodDeclaration.isSolved() && resolvedMethodDeclaration.getCorrespondingDeclaration()!=null && Config.isInteresting(resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName())) {
						//System.out.println("Binding Resolved");
						if(Config.isInteresting(resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName())) {
							
							MethodDeclaration methodDeclaration = this.getMethodDeclarationContainer(m);	
							
							
							//System.out.println("Parameters: "+parameterList);
							if(methodDeclaration!=null && methodDeclaration.getBegin().isPresent() && m.getBegin().isPresent()) {
								
								/*System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++=");
								System.out.println("Expression expression: "+m);
								System.out.println("Method Name: "+m.getName().getIdentifier() +" Scope: "+m.getScope().get());
								System.out.println("QN: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getQualifiedName());
								System.out.println("Package Name: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getPackageName());
								System.out.println("Class Name: "+resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName());
								System.out.println(": "+resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName());
								
								System.out.println("MethoDeclaration: Start"+methodDeclaration.getBegin().get() +"End: "+methodDeclaration.getEnd().get()+ "MethodCallExpr: "+m.getBegin().get());
								*/
								String source = FileUtils.readFileToString(new File(this.filePath));
								String text = this.collectSourceString(source,methodDeclaration.getBegin().get(),m.getBegin().get());
								
								List<Token> tokenList = this.tokenize(text);
								//System.out.println("Prefix: \n"+text);
								//for(Token token:tokenList) {
								//	System.out.println("T: "+token.getToken());
								//}
								
								String neighborList   = this.calcNeighbor(tokenList,tokenList.size()-1);
								String lineContent    = this.getLineContent(tokenList, tokenList.size()-1);
								
								//System.out.println("Context: " + neighborList);
								//System.out.println("Content: " + lineContent);
								
								
								ParameterModelEntry modelEntry = new ParameterModelEntry(m.getBegin().get().line,
									m.getName().getIdentifier(),
									resolvedMethodDeclaration.getCorrespondingDeclaration().getPackageName()+"."+resolvedMethodDeclaration.getCorrespondingDeclaration().getClassName(),
									parameterList,
									neighborList,
									lineContent,
									this.getFilePath());
								modelEntryList.add(modelEntry);
								System.out.println("Model Entry: "+modelEntry);
							}
						}
					}
				}catch(Exception e) {
					//e.printStackTrace();
					//System.out.println("Fail to resolve type");
				}
			}
		}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	public List<ParameterDescriptor> getParameterDescriptorList() {
		return parameterDescriptorList;
	}

}
