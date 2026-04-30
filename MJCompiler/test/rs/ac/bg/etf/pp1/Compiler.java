package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
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
			File sourceCode = new File("test/program.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());

			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);

			/* Formiranje AST */
			MJParser p = new MJParser(lexer);
			Symbol s = p.parse();

			Program prog = (Program) (s.value);

			/* Ispis AST */
			log.info(prog.toString(""));
			log.info("=====================================================================");

			/* Inicijalizacija tabele simbola */
			Tab.init();

			// ---- bool (predeklarisan tip) ----
			Struct boolType = new Struct(Struct.Bool);
			Obj boolObj = Tab.insert(Obj.Type, "bool", boolType);
			boolObj.setAdr(-1);
			boolObj.setLevel(-1);

			// ---- eol (predeklarisana konstanta) ----
			Obj eolObj = Tab.insert(Obj.Con, "eol", Tab.charType);
			eolObj.setAdr('\n');
			eolObj.setLevel(0);

			// ---- predeklarisane metode: ord/chr/len ----

			// ord(char) -> int
			Obj ordObj = Tab.insert(Obj.Meth, "ord", Tab.intType);
			Tab.openScope();
			Obj ordFp = Tab.insert(Obj.Var, "ch", Tab.charType);
			ordFp.setFpPos(1);
			Tab.chainLocalSymbols(ordObj);
			Tab.closeScope();
			ordObj.setLevel(1);

			// chr(int) -> char
			Obj chrObj = Tab.insert(Obj.Meth, "chr", Tab.charType);
			Tab.openScope();
			Obj chrFp = Tab.insert(Obj.Var, "i", Tab.intType);
			chrFp.setFpPos(1);
			Tab.chainLocalSymbols(chrObj);
			Tab.closeScope();
			chrObj.setLevel(1);

			// len(array) -> int
			Obj lenObj = Tab.insert(Obj.Meth, "len", Tab.intType);
			Tab.openScope();
			Obj lenFp = Tab.insert(Obj.Var, "arr", new Struct(Struct.Array, Tab.noType));
			lenFp.setFpPos(1);
			Tab.chainLocalSymbols(lenObj);
			Tab.closeScope();
			lenObj.setLevel(1);

			/* Semanticka analiza */
			SemAnalyzer sa = new SemAnalyzer();
			prog.traverseBottomUp(sa);

			/* Ispis tabele simbola */
			log.info("=====================================================================");
			Tab.dump();

			if (!p.errorDetected && sa.passed()) {

				/* Generisanje koda */
				File objFile = new File("test/program.obj");
				if (objFile.exists())
					objFile.delete();

				CodeGenerator cg = new CodeGenerator();
				prog.traverseBottomUp(cg);

				Code.dataSize = sa.nVars;
				Code.mainPc = cg.getmainPc();

				Code.write(new FileOutputStream(objFile));

				log.info("Generisanje uspesno zavrseno!");
			} else {
				log.error("Parsiranje NIJE uspesno zavrseno!");
			}

		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e1) {
					log.error(e1.getMessage(), e1);
				}
		}

	}

}
