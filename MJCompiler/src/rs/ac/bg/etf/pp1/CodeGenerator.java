package rs.ac.bg.etf.pp1;

import java.util.HashMap;


import java.util.Map;
import java.util.Stack;

import rs.ac.bg.etf.pp1.ast.AddTermList_list;
import rs.ac.bg.etf.pp1.ast.Addop_plus;
import rs.ac.bg.etf.pp1.ast.Colon;
import rs.ac.bg.etf.pp1.ast.CondExpr;
import rs.ac.bg.etf.pp1.ast.CondFact_rel;
import rs.ac.bg.etf.pp1.ast.CondFact_single;
import rs.ac.bg.etf.pp1.ast.DesArrName;
import rs.ac.bg.etf.pp1.ast.Designator;
import rs.ac.bg.etf.pp1.ast.DesignatorStatement_actpar;
import rs.ac.bg.etf.pp1.ast.DesignatorStatement_dec;
import rs.ac.bg.etf.pp1.ast.DesignatorStatement_expr;
import rs.ac.bg.etf.pp1.ast.DesignatorStatement_inc;
import rs.ac.bg.etf.pp1.ast.Designator_arr;
//import rs.ac.bg.etf.pp1.ast.DesignatorStatement_swap;
import rs.ac.bg.etf.pp1.ast.Designator_length;
import rs.ac.bg.etf.pp1.ast.Designator_std;
//import rs.ac.bg.etf.pp1.ast.Designator_monkey;
import rs.ac.bg.etf.pp1.ast.FactorContext_arr;
import rs.ac.bg.etf.pp1.ast.FactorContext_bool;
import rs.ac.bg.etf.pp1.ast.FactorContext_char;
import rs.ac.bg.etf.pp1.ast.FactorContext_dsg;
import rs.ac.bg.etf.pp1.ast.FactorContext_meth;
import rs.ac.bg.etf.pp1.ast.FactorContext_num;
//import rs.ac.bg.etf.pp1.ast.Left;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodName;
//import rs.ac.bg.etf.pp1.ast.Monkey;
import rs.ac.bg.etf.pp1.ast.MulFactorList_list;
import rs.ac.bg.etf.pp1.ast.Mulop_div;
import rs.ac.bg.etf.pp1.ast.Mulop_mul;
import rs.ac.bg.etf.pp1.ast.Print;
import rs.ac.bg.etf.pp1.ast.Relop;
import rs.ac.bg.etf.pp1.ast.Relop_equal;
import rs.ac.bg.etf.pp1.ast.Relop_greater;
import rs.ac.bg.etf.pp1.ast.Relop_greaterequal;
import rs.ac.bg.etf.pp1.ast.Relop_lower;
import rs.ac.bg.etf.pp1.ast.Relop_notequal;
//import rs.ac.bg.etf.pp1.ast.Right;
import rs.ac.bg.etf.pp1.ast.Statement_print;
import rs.ac.bg.etf.pp1.ast.Statement_printMore;
import rs.ac.bg.etf.pp1.ast.Statement_read;
import rs.ac.bg.etf.pp1.ast.Statement_ret;
import rs.ac.bg.etf.pp1.ast.Unary_minus;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	Map<String, Obj> arrayMap = new HashMap<>();
	private int mainPc;
	private Stack<Integer> ternaryFalse = new Stack<>();
	private Stack<Integer> ternaryTrue = new Stack<>();
	private int boolFalseJmp;
	private int boolTrueJmp;
	private boolean charArray;
	private boolean finalFlag;
	private int arrayFinalJump;
	private boolean printStatement;
	
	public int getMainPc() {
		return this.mainPc;
	}
	
	private void printChar(char c) {
		Code.loadConst(c);
		Code.loadConst(0);
		Code.put(Code.bprint);
	}
	
	private void printBool() {
		Code.loadConst(0);
		Code.putFalseJump(Code.ne, 0);
		boolFalseJmp = Code.pc - 2;
		
		printChar('t');
		printChar('r');
		printChar('u');
		printChar('e');
		
		Code.putJump(boolTrueJmp);
		boolTrueJmp = Code.pc - 2;
		Code.fixup(boolFalseJmp);
		
		printChar('f');
		printChar('a');
		printChar('l');
		printChar('s');
		printChar('e');
		
		Code.fixup(boolTrueJmp);
	}
	
	CodeGenerator() {
		initializeMethods();
	}
	
	private void initializeMethods() {
		initChrOrd();
		initLen();
		
	}

	private void initChrOrd() {
		Obj chrObj = Tab.find("chr");
		chrObj.setAdr(Code.pc);
		Obj ordObj = Tab.find("ord");
		ordObj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		Code.put(Code.load_n);
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	private void initLen() {
		Obj lenObj = Tab.find("len");
		lenObj.setAdr(Code.pc);
		Code.put(Code.enter);
		Code.put(1);
		Code.put(1);
		Code.put(Code.load_n);
		Code.put(Code.arraylength);
		Code.put(Code.exit);
		Code.put(Code.return_);	
	}
	
	private int getRelop(Relop relop) {
		if(relop instanceof Relop_equal) {
			return Code.eq;
		}
		else if(relop instanceof Relop_notequal) {
			return Code.ne;
		}
		else if(relop instanceof Relop_greater) {
			return Code.gt;
		}
		else if(relop instanceof Relop_greaterequal) {
			return Code.ge;
		}
		else if(relop instanceof Relop_lower) {
			return Code.lt;
		}
		else {
			return Code.le;
		}
	}

	@Override
	public void visit(MethodName methodName) {
		methodName.obj.setAdr(Code.pc);
		if(methodName.getI1().equalsIgnoreCase("main")) {
			mainPc = Code.pc;
		}
		Code.put(Code.enter);
		Code.put(methodName.obj.getLevel());
		Code.put(methodName.obj.getLocalSymbols().size());
	}
 
	@Override
	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	@Override
	public void visit(DesArrName desArrName) {
		if(desArrName.obj.getType().getKind() == Struct.Array) {
			Code.load(desArrName.obj);
		}
		if(desArrName.obj.getType().getElemType() == Tab.charType) {
			charArray = true;
		}
		else {
			charArray = false;
		}
	}
	
	@Override
	public void visit(Print print) {
		printStatement = true;
	}
	
	@Override
	public void visit(Designator_std designator_std) {
		if(designator_std.obj.getFpPos() == 2) {
			finalFlag = true;
		}
	}
	
	@Override
	public void visit(Designator_arr designator_arr) {
		if(designator_arr.getDesArrName().obj.getFpPos() == 2 && !printStatement) {
			Code.put(Code.dup2);
			Code.put(Code.dup2);
			Code.put(Code.pop);
			Code.put(Code.arraylength);
			Code.loadConst(2);
			Code.put(Code.div);
			Code.put(Code.add);
			Code.put(Code.dup2);
			if(designator_arr.getDesArrName().obj.getType().getElemType() == Tab.charType) {
				Code.put(Code.baload);
			}
			else {
				Code.put(Code.aload);
			}
			Code.loadConst(0);
			Code.putFalseJump(Code.ne, 0);
			arrayFinalJump = Code.pc - 2;
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.put(Code.pop);
			Code.put(Code.trap);
			Code.put(2);
			Code.fixup(arrayFinalJump);
			Code.loadConst(50);
			if(designator_arr.getDesArrName().obj.getType().getElemType() == Tab.charType) {
				Code.put(Code.bastore);
			}
			else {
				Code.put(Code.astore);
			}
		}
	}
	
	@Override
	public void visit(Designator_length designator_length) {
		Code.put(Code.arraylength);
	}
	
	/*
	@Override
	public void visit(Monkey monkey) {
		Code.loadConst(monkey.getN1());
	}
	
	@Override
	public void visit(Designator_monkey designator_monkey) {
		Code.put(Code.dup2);
		Code.put(Code.aload);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.dup_x1);
		Code.put(Code.arraylength);
		Code.put(Code.sub);
		Code.put(Code.neg);
		Code.put(Code.aload);
		Code.put(Code.add);
	}
	*/
	@Override
	public void visit(FactorContext_num factorContext_num) {
		Code.loadConst(factorContext_num.getN1());
	}
	
	@Override
	public void visit(FactorContext_char factorContext_char) {
		Code.loadConst(factorContext_char.getC1());
	}
	
	@Override
	public void visit(FactorContext_bool factorContext_bool) {
		Code.loadConst(factorContext_bool.getB1());
	}
	
	@Override
	public void visit(FactorContext_dsg factorContext_dsg) {
		Designator des = factorContext_dsg.getDesignator();
		if(!(des instanceof Designator_length /* || des instanceof Designator_monkey */)) {
			Code.load(factorContext_dsg.getDesignator().obj);
		}
	}
	
	@Override
	public void visit(FactorContext_meth factorContext_meth) {
		int offset = factorContext_meth.getDesignator().obj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
	}
	
	@Override
	public void visit(FactorContext_arr factorContext_arr) {
		if(finalFlag) {
			Code.loadConst(2);
			Code.put(Code.mul);
		}
		Code.put(Code.newarray);
		if(factorContext_arr.getType().struct.equals(Tab.intType)) {
			Code.put(1);
		}
		else {
			Code.put(0);
		}
	}
	
	@Override
	public void visit(Unary_minus unary_minus) {
		Code.put(Code.neg);
	}
	
	@Override
	public void visit(MulFactorList_list mulFactorList_list) {
		if(mulFactorList_list.getMulop() instanceof Mulop_mul) {
			Code.put(Code.mul);
		}
		else if(mulFactorList_list.getMulop() instanceof Mulop_div) {
			Code.put(Code.div);
		}
		else {
			Code.put(Code.rem);
		}
	}
		
	@Override
	public void visit(AddTermList_list addTermList_list) {
		if(addTermList_list.getAddop() instanceof Addop_plus) {
			Code.put(Code.add);
		}
		else {
			Code.put(Code.sub);
		}
	}
	
	@Override
	public void visit(CondFact_single condFact_single) {
		Code.loadConst(0);
		Code.putFalseJump(Code.ne, 0);
		ternaryFalse.push(Code.pc - 2);
	}
	
	@Override
	public void visit(CondFact_rel condFact_rel) {
		Code.putFalseJump(getRelop(condFact_rel.getRelop()), 0);
		ternaryFalse.push(Code.pc - 2);
	}
	
	@Override
	public void visit(Colon colon) {
		Code.putJump(0);
		ternaryTrue.push(Code.pc - 2);
		Code.fixup(ternaryFalse.pop());
	}
	
	@Override
	public void visit(CondExpr condExpr) {
		Code.fixup(ternaryTrue.pop());
	}
	
	/*
	@Override
	public void visit(Left left) {
		Code.put(Code.dup2);
		if(charArray) {
			Code.put(Code.baload);
		}
		else {
			Code.put(Code.aload);
		}	
		//adr, index1, val1
		Code.put(Code.dup_x2);
		Code.put(Code.pop);      //val1, adr, index1
		Code.put(Code.dup_x1);
		Code.put(Code.pop);		 //val1, index1, adr
		Code.put(Code.dup_x1);   //val1, adr, index1, adr
	}
	
	@Override
	public void visit(Right right) {
		//val1, adr, index1, adr, index2
		Code.put(Code.dup_x2);  //val1, adr, index2, index1, adr, index2
		Code.put(Code.dup2);
		if(charArray) {
			Code.put(Code.baload);
		}
		else {
			Code.put(Code.aload);
		}	   
		//val1, adr, index2, index1, adr, index2, val2
		Code.put(Code.dup_x2);  //val1, adr, index2, index1, val2, adr, index2, val2
		Code.put(Code.pop);
		Code.put(Code.pop);     //val1, adr, index2, index1, val2, adr,
		Code.put(Code.dup_x2);
		Code.put(Code.pop);     //val1, adr, index2, adr, index1, val2
	}
	
	@Override
	public void visit(DesignatorStatement_swap designatorStatement_swap) {
		if(charArray) {
			Code.put(Code.bastore);
		}
		else {
			Code.put(Code.astore);
		}
		//val1, adr, index2
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		//index2, val1, adr
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		//adr index2, val1
		if(charArray) {
			Code.put(Code.bastore);
		}
		else {
			Code.put(Code.astore);
		}	
	}
	
	*/
	@Override
	public void visit(DesignatorStatement_expr designatorStatement_expr) {
		Code.store(designatorStatement_expr.getDesignator().obj);
		finalFlag = false;
	}
	
	@Override
	public void visit(DesignatorStatement_actpar designatorStatement_actpar) {
		int offset = designatorStatement_actpar.getDesignator().obj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if(designatorStatement_actpar.getDesignator().obj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}
	
	
	@Override
	public void visit(DesignatorStatement_inc designatorStatement_inc) {
		if(designatorStatement_inc.getDesignator().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		Code.load(designatorStatement_inc.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(designatorStatement_inc.getDesignator().obj);
	}
	
	@Override
	public void visit(DesignatorStatement_dec designatorStatement_dec) {
		if(designatorStatement_dec.getDesignator().obj.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
		}
		Code.load(designatorStatement_dec.getDesignator().obj);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(designatorStatement_dec.getDesignator().obj);
	}
	
	@Override
	public void visit(Statement_ret statement_ret) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	@Override
	public void visit(Statement_read statement_read) {
		if(statement_read.getDesignator().obj.getType().equals(Tab.intType)) {
			Code.put(Code.read);
		}
		else {
			Code.put(Code.bread);
		}
		Code.store(statement_read.getDesignator().obj);
	}
	
	@Override
	public void visit(Statement_print statement_print) {
		if(statement_print.getExpr().struct.equals(Tab.intType)) {
			Code.loadConst(0);
			Code.put(Code.print);
		}
		else if(statement_print.getExpr().struct.equals(Tab.charType)){
			Code.loadConst(0);
			Code.put(Code.bprint);
		}
		else {
			printBool();
		}
		printStatement = false;
	}
	
	@Override
	public void visit(Statement_printMore statement_printMore) {
		int space = statement_printMore.getN3();
		System.out.println(space);
		if(statement_printMore.getExpr().struct.equals(Tab.intType)) {
			Code.loadConst(space);
			Code.put(Code.print);
		}
		else if(statement_printMore.getExpr().struct.equals(Tab.charType)) {
			Code.loadConst(space);
			Code.put(Code.bprint);
		}
		else {
			Code.loadConst('\0');
			Code.loadConst(space);
			Code.put(Code.bprint);
			printBool();
		}
		printStatement = false;
	}
	
}
