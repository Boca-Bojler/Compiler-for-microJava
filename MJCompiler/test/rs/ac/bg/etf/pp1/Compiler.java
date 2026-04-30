package rs.ac.bg.etf.pp1;

import java.io.BufferedReader; 

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.*;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(Compiler.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/test301.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //formiranje AST
	        
	        Program prog = (Program)(s.value);
	        
			// ispis AST
			log.info(prog.toString(""));
			log.info("=====================================================================");

			
			//Inicijalizacja tabele simbola
			TabExtended.init();
			
			//Semanticka analiza
			
			SemanticAnalyzer semAnalyzer = new SemanticAnalyzer();
			prog.traverseBottomUp(semAnalyzer);
			
			
			//Ispis tabele simbola
			log.info("=====================================================================");
			TabExtended.dump(new DumpSymbolTableVisitorExtended());
			
			
			if(!p.errorDetected && semAnalyzer.testPassed()){
				
				//Generisanje koda
				File objFile = new File("test/program.obj");
				if(objFile.exists()) objFile.delete();
				
				CodeGenerator codeGen = new CodeGenerator();
				prog.traverseBottomUp(codeGen);
				Code.dataSize = semAnalyzer.getVarCnt();
				Code.mainPc = codeGen.getMainPc();
				Code.write(new FileOutputStream(objFile));
				
				log.info("Parsiranje uspesno zavrseno!");
			}else{
				log.error("Parsiranje NIJE uspesno zavrseno!");
			}
			
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
}