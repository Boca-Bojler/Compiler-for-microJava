package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;


import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	private boolean errorDetected = false;
	Logger log = Logger.getLogger(getClass());
	private Obj currentProg;
	private Struct currentType;
	private int constant;
	private Struct constantType;
	private Struct boolType = Tab.find("bool").getType();
	private boolean mainMethod = false;
	private Obj currentMethod;
	private FactorContext currentFactorContext;
	private int lastEnumValue = -1;
	private Obj currentEnum;
	private int varCnt;
	private String lastEnumName;
	private Map<String, String> enumVars = new HashMap<>();
	private String currentEnumName;
	private boolean finalFlag;
	
	
	private class ActParsCounter extends VisitorAdaptor {
		
		List<Struct> actParsListfinal;
		Stack<List<Struct>> actParsLists = new Stack<>();
		
		@Override
		public void visit(ActParsStart actParsStart) {
			actParsLists.push(new ArrayList<>());
		}
		
		@Override
		public void visit(ActPars actPars) {
			actParsLists.peek().add(actPars.getExpr().struct);
		}
		
		@Override
		public void visit(ActParsList_std actParsList_std) {
			actParsListfinal = actParsLists.pop();
		}
		
		@Override
		public void visit(ActParsList_eps actParsList_eps) {
			actParsListfinal = actParsLists.pop();
		}
	}
	
	public int getVarCnt() {
		return this.varCnt;
	}
	
	/* LOG MESSAGES */
	public void report_error(String message, SyntaxNode info) {
		errorDetected  = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	private String dumpInfo(String type, Obj object) {
		StringBuilder output = new StringBuilder();
		output.append(type + " (");
		if(currentType == Tab.intType)
			output.append("int");
		else if(currentType == Tab.charType)
			output.append("char");
		else if(currentType == boolType)
			output.append("bool");
		output.append("): " + object.getName());
		output.append(", ");
		output.append(object.getAdr());
		output.append(", ");
		output.append(object.getLevel() + " ");
		output.append(", ");
		output.append("fpPos: " + object.getFpPos());
		
		return output.toString();
	}
	
	public boolean testPassed() {
		return !errorDetected;
	}
	
	@Override
	public void visit(ProgramName programName) {
		currentProg = Tab.insert(Obj.Prog, programName.getI1(), Tab.noType);
		Tab.openScope();
	}

	@Override
	public void visit(Program program) {
		varCnt = Tab.currentScope().getnVars();
		Tab.chainLocalSymbols(currentProg);
		Tab.closeScope();
		currentProg = null;
		if(!mainMethod) {
			report_error("GRESKA! Program ne sadrzi main metodu!", program);
		}
		
	}
	
	@Override
	public void visit(Type type) {
		Obj objType = Tab.find(type.getI1());
		if(objType == Tab.noObj) {
			report_error("GRESKA! Nepostojeci tip podatka: " +type.getI1(), type);
			currentType = Tab.noType;
			type.struct = currentType;

		}
		else if(objType.getKind() != Obj.Type) {
			report_error("GRESKA! Neadekvatan tip podatka: " +type.getI1(), type);
			currentType = Tab.noType;
			type.struct = currentType;
		}
		else {
			currentType = objType.getType();
			type.struct = currentType;
			currentEnumName = type.getI1();
		}
	}
	
	@Override
	public void visit(ConstDecl constDecl) {
		Obj conObj = Tab.find(constDecl.getI1());
		if(conObj != Tab.noObj) {
			report_error("GRESKA! Dvostruka definicija konstante " + constDecl.getI1(), constDecl);
		}
		else {
			if(constantType.assignableTo(currentType)) {
				conObj = Tab.insert(Obj.Con, constDecl.getI1(), currentType);
				conObj.setAdr(constant);
				String dump = dumpInfo("Con", conObj);
				report_info(dump, constDecl);
			}
			else {
				report_error("GRESKA! Neadekvatna dodela konstante", constDecl);
			}
			
		}	
	}
	
	@Override
	public void visit(ConstantVar_num constant_num) {
		constant = constant_num.getN1();
		constantType = Tab.intType;
	}
	
	@Override
	public void visit(ConstantVar_char constant_char) {
		constant = constant_char.getC1();
		constantType = Tab.charType;
	}
	
	@Override
	public void visit(ConstantVar_bool constant_bool) {
		constant = constant_bool.getB1();
		constantType = boolType;
	}
	
	@Override
	public void visit(Final_std final_std) {
		finalFlag = true;
	}
	
	@Override
	public void visit(VarDeclList_std varDeclList_std) {
		if(mainMethod && finalFlag) {
			report_error("GRESKA! Identifikator final iskoriscen nad lokalnim promenljivama", varDeclList_std);
		}
		finalFlag = false;
	}
	
	@Override
	public void visit(VarDecl_std varDecl_std) {
		Obj varObj = null;
		if(currentMethod != null) {
			varObj = Tab.currentScope().findSymbol(varDecl_std.getI1());
		}
		else {
			varObj = Tab.find(varDecl_std.getI1());
		}
		if(varObj != null && varObj != Tab.noObj) {
			report_error("Dvostruka definicija promenljive " + varDecl_std.getI1(), varDecl_std);
		}
		else {
			if(mainMethod && currentType.getKind() == Struct.Enum) {
				
				enumVars.put(varDecl_std.getI1(), currentEnumName);
				currentType = Tab.intType;
			}
			varObj = Tab.insert(Obj.Var, varDecl_std.getI1(), currentType);	
			String dump = dumpInfo("Var", varObj);
			report_info(dump, varDecl_std);
			currentEnumName = null;
		}	
	}
	
	@Override
	public void visit(VarDecl_arr varDecl_arr) {
		Obj varObj = null;
		if(currentMethod != null) {
			varObj = Tab.currentScope().findSymbol(varDecl_arr.getI1());
		}
		else {
			varObj = Tab.find(varDecl_arr.getI1());
		}
		if(varObj != null && varObj != Tab.noObj) {
			report_error("Dvostruka definicija promenljive nizovskog tipa " + varDecl_arr.getI1(), varDecl_arr);
		}
		else {
			varObj = Tab.insert(Obj.Var, varDecl_arr.getI1(), new Struct(Struct.Array, currentType));
			if(finalFlag) {
				varObj.setFpPos(2);
			}
			String dump = dumpInfo("Array", varObj);
			report_info(dump, varDecl_arr);
		}	
	}
	
	@Override
	public void visit(EnumName enumName) {
		Obj obj = Tab.currentScope().findSymbol(enumName.getI1());
		if (obj != null) {
			report_error("GRESKA! Vec postoji ime " + enumName.getI1() + " deklarisano unutar istog opsega", enumName);
			currentEnum = Tab.noObj;
			
		} else {
			currentEnum = Tab.insert(Obj.Type, enumName.getI1(), new Struct(Struct.Enum));
			lastEnumValue = -1;
			Tab.openScope();
		}
	}
	
	private boolean enumHasVal(Integer val) {
		for (Obj valObj : Tab.currentScope().values()) {
			if (valObj.getAdr() == val) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void visit(EnumDecl_value enumDecl_value) {
		Obj obj = Tab.currentScope().findSymbol(enumDecl_value.getI1());
		if (obj != null) {
			report_error("GRESKA! Konstanta vec definisana unutar datog enuma", enumDecl_value);
		} else {
			if (enumHasVal(enumDecl_value.getN2())) {
				report_error("GRESKA! Vrednost vec dodeljena nekoj konstanti u okviru datog enuma", enumDecl_value);
			}
			Obj currentEnumConst = Tab.insert(Obj.Con, enumDecl_value.getI1(), Tab.intType);
			currentEnumConst.setAdr(enumDecl_value.getN2());
			lastEnumValue = enumDecl_value.getN2();
			
		}
	}
	
	@Override
	public void visit(EnumDecl_std enumDecl_std) {
		Obj obj = Tab.currentScope().findSymbol(enumDecl_std.getI1());
		if (obj != null) {
			report_error("GRESKA! Konstanta vec definisana unutar datog enuma", enumDecl_std);
		} else {
			if (enumHasVal(++lastEnumValue)) {
				report_error("GRESKA! Vrednost vec dodeljena nekoj konstanti u okviru datog enuma", enumDecl_std);
			}
			Obj currentEnumConst = Tab.insert(Obj.Con, enumDecl_std.getI1(), Tab.intType);
			currentEnumConst.setAdr(lastEnumValue);
		}
	}
	
	
	
	  @Override
	  public void visit(EnumDeclList enumDeclList) {
		  currentEnum.getType().setMembers(Tab.currentScope().getLocals());
			Tab.closeScope();
			currentEnum = null;
	}
	
	@Override
	public void visit(MethodName methodName) {
		if(methodName.getI1().equalsIgnoreCase("main")) {
			mainMethod = true;
		}
		currentMethod = Tab.insert(Obj.Meth, methodName.getI1(), Tab.noType);
		methodName.obj = currentMethod;
		Tab.openScope();
	}
	
	@Override
	public void visit(MethodDecl methodDecl) {
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
	}
	
	//Context conditions
	
	@Override
	public void visit(Designator_std designator_std) {
		Obj varObj = Tab.find(designator_std.getI1());
		if(varObj == Tab.noObj) {
			report_error("GRESKA! Pristup promenljivoj " + designator_std.getI1() + " koja ne postoji!", designator_std);
			designator_std.obj = Tab.noObj;
		}
		else if(varObj.getKind() != Obj.Var && varObj.getKind() != Obj.Con && varObj.getKind() != Obj.Meth) {
			report_error("Neadekvatan tip promenljive: " + designator_std.getI1(), designator_std);
			designator_std.obj = Tab.noObj;
		}
		else {
			designator_std.obj = varObj;
			designator_std.obj.setFpPos(varObj.getFpPos());
		}
	}
	
	@Override
	public void visit(DesArrName desArrName) {
		Obj arrObj = Tab.find(desArrName.getI1());
		if(arrObj == Tab.noObj) {
			report_error("GRESKA! Pristup promenljivoj " + desArrName.getI1() + " koja ne postoji!", desArrName);
			desArrName.obj = Tab.noObj;
		}
		else if((arrObj.getKind() != Obj.Var && arrObj.getKind() != Obj.Type) || (arrObj.getType().getKind() != Struct.Array && arrObj.getType().getKind() != Struct.Enum)) {
			report_error("GRESKA! Neadekvatna promenljiva niza " + desArrName.getI1(), desArrName);
			desArrName.obj = Tab.noObj;
		}
		else {
			desArrName.obj = arrObj;
			desArrName.obj.setFpPos(arrObj.getFpPos());
		}
	}
	
	@Override
	public void visit(Designator_arr designator_arr) {
		Obj arrObj = designator_arr.getDesArrName().obj;
		if(arrObj == Tab.noObj) {
			designator_arr.obj = Tab.noObj;
		}
		else if(arrObj.getType().getKind() == Struct.Enum) {
			report_error("Neadekvatna promenljiva niza" + designator_arr.getDesArrName().getI1(), designator_arr);
		} 
		else if(!designator_arr.getExpr().struct.equals(Tab.intType)) {
			report_error("GRESKA! Indeksiranje niza pomocu ne-int vrednosti", designator_arr);
			designator_arr.obj = Tab.noObj;
		}
		else {
			designator_arr.obj = new Obj(Obj.Elem, arrObj.getName() + "[?]", arrObj.getType().getElemType());
		}
	}
	
	@Override
	public void visit(Designator_length designator_length) {
		Obj arrObj = Tab.find(designator_length.getDesArrName().getI1());
		if(arrObj == Tab.noObj) {
			report_error("GRESKA! Pristup promenljivoj " + designator_length.getDesArrName().getI1() + " koja ne postoji", designator_length);
			designator_length.obj = Tab.noObj;
		}
		else if(arrObj.getKind() != Obj.Var || arrObj.getType().getKind() != Struct.Array) {
			report_error("GRESKA! Pokusaj poziva metode length nad promenljivom koja nije niz", designator_length);
			designator_length.obj = Tab.noObj;
		}
		else {
			designator_length.obj = new Obj(Obj.Var, designator_length.getDesArrName().getI1() + "Len", Tab.intType);
		}
	}
	
	@Override
	public void visit(Designator_enum designator_enum) {
		lastEnumName = designator_enum.getDesArrName().getI1();
		Obj obj = Tab.find(designator_enum.getDesArrName().getI1());
		if(obj == Tab.noObj || obj.getType().getKind() == Struct.Array) {
			report_error("GRESKA! Nepostojeci enum " + designator_enum.getDesArrName().getI1(), designator_enum);
			designator_enum.obj = Tab.noObj;
		}
		else {
			String enumName = obj.getName();
			String constName = designator_enum.getI2();
			boolean found = false;
			if (obj.getType().getKind() == Struct.Enum) {
				for (Obj sym : obj.getType().getMembers()) {
					if (sym.getName().contentEquals(constName)) {
						found = true;
						designator_enum.obj = sym;
						break;
					}
				}
				if (!found) {
					report_error("GRESKA! Nepostojeca konstanta " + constName + " unutar datog enuma", designator_enum);
					designator_enum.obj = Tab.noObj;
				}
			} 
		}	
	}
	
	@Override
	public void visit(FactorContext_char factorContext_char) {
		factorContext_char.struct = Tab.charType;
		currentFactorContext = factorContext_char;
	}
	
	@Override
	public void visit(FactorContext_num factorContext_num) {
		factorContext_num.struct = Tab.intType;
		currentFactorContext = factorContext_num;
	}
	
	@Override
	public void visit(FactorContext_bool factorContext_bool) {
		factorContext_bool.struct = boolType;
		currentFactorContext = factorContext_bool;
	}
	
	@Override
	public void visit(FactorContext_dsg factorContext_dsg) {
		factorContext_dsg.struct = factorContext_dsg.getDesignator().obj.getType();
		currentFactorContext = factorContext_dsg;
	}
	
	@Override
	public void visit(FactorContext_arr factorContext_arr) {
		if(!factorContext_arr.getExpr().struct.equals(Tab.intType)) {
			report_error("GRESKA! Velicina niza je ne-int vrednost", factorContext_arr);
			factorContext_arr.struct = Tab.noType;
		}
		else {
			factorContext_arr.struct = new Struct(Struct.Array, currentType);
		}
		currentFactorContext = factorContext_arr;
	}
	
	@Override
	public void visit(FactorContext_paren factorContext_paren) {
		lastEnumName = null;
		factorContext_paren.struct = factorContext_paren.getExpr().struct;
		currentFactorContext = factorContext_paren;
	}
	
	@Override
	public void visit(FactorContext_meth factorContext_meth) {
		lastEnumName = null;
		if(factorContext_meth.getDesignator().obj.getKind() != Obj.Meth) {
			report_error("Poziv nedefinisane metode " + factorContext_meth.getDesignator().obj.getName(), factorContext_meth);
			factorContext_meth.struct = Tab.noType;
		}
		else {
			List<Struct> formalParamsList = new ArrayList<>();
			for(Obj formal: factorContext_meth.getDesignator().obj.getLocalSymbols()) {
				if(formal.getKind() == Obj.Var && formal.getLevel() == 1) {
					formalParamsList.add(formal.getType());
				}
			}
			ActParsCounter parsCounter = new ActParsCounter();
			factorContext_meth.getActParsList().traverseBottomUp(parsCounter);
			List<Struct> actualParamsList = parsCounter.actParsListfinal;
			if(formalParamsList.size() != actualParamsList.size()) {
				report_error("GRESKA! Funkcija " + factorContext_meth.getDesignator().obj.getName() + " ne prima tacan broj argumenata", factorContext_meth);
			}
			else {
				for(int i = 0; i < formalParamsList.size(); i++) {
					Struct formPar = formalParamsList.get(i);
					Struct actPar = actualParamsList.get(i);
					if(!actPar.assignableTo(formPar)) {
						report_error("GRESKA! Nepoklapanje tipova formalnih i stvarnih parametara funkcije " + factorContext_meth.getDesignator().obj.getName(), factorContext_meth);
					}
				}
			}
			factorContext_meth.struct = factorContext_meth.getDesignator().obj.getType();
		}
		currentFactorContext = factorContext_meth;
	}
	
	@Override
	public void visit(Factor factor) {
		if(factor.getUnary() instanceof Unary_minus) {
			lastEnumName = null;
			if(!currentFactorContext.struct.equals(Tab.intType)) {
				report_error("GRESKA! Negacija ne-int tipa", factor);
				factor.struct = Tab.noType;
			}
			else {
				factor.struct = Tab.intType;
			}
		}
		else {
			factor.struct = currentFactorContext.struct;
		}
		currentFactorContext = null;
	}
	
	@Override
	public void visit(MulFactorList_factor mulFactorList_factor) {
		mulFactorList_factor.struct = mulFactorList_factor.getFactor().struct;
	}
	
	@Override
	public void visit(MulFactorList_list mulFactorList_list) {
		lastEnumName = null;
		Struct left = mulFactorList_list.getMulFactorList().struct;
		Struct right = mulFactorList_list.getFactor().struct;
		if(!left.equals(Tab.intType) || !right.equals(Tab.intType)) {
			report_error("GRESKA! Operacija mulop nad ne-int tipovima", mulFactorList_list);
			mulFactorList_list.struct = Tab.noType;
		}
		else {
			mulFactorList_list.struct = Tab.intType;
		}
	}
	
	@Override
	public void visit(Term term) {
		term.struct = term.getMulFactorList().struct;
	}
	
	@Override
	public void visit(AddTermList_term addTermList_term) {
		addTermList_term.struct = addTermList_term.getTerm().struct;
	}
	
	@Override
	public void visit(AddTermList_list addTermList_list) {
		lastEnumName = null;
		Struct left = addTermList_list.getAddTermList().struct;
		Struct right = addTermList_list.getTerm().struct;
		if(!left.equals(Tab.intType) || !right.equals(Tab.intType)) {
			report_error("GRESKA! Operacija addop nad ne-int tipovima", addTermList_list);
			addTermList_list.struct = Tab.noType;
		}
		else {
			addTermList_list.struct = Tab.intType;
		}
	}
	
	@Override
	public void visit(CondFact_single condFact_single) {
		condFact_single.struct = condFact_single.getAddTermList().struct;
	}
	
	@Override
	public void visit(CondFact_rel condFact_rel) {
		Struct left = condFact_rel.getAddTermList().struct;
		Struct right = condFact_rel.getAddTermList1().struct;
		Relop relop = condFact_rel.getRelop();
		if(!left.compatibleWith(right)) {
			report_error("GRESKA! Relop operacija nad nekompatibilnim tipovima izraza", condFact_rel);
			condFact_rel.struct = Tab.noType;
		}
		else if((left.isRefType() || right.isRefType()) && !(relop instanceof Relop_equal || relop instanceof Relop_notequal)) {
			report_error("GRESKA! Neadekvatna relop operacija za nizovski tip", relop);
			condFact_rel.struct = Tab.noType;
		}
		else {
			condFact_rel.struct = boolType;
		}
	}
	
	@Override
	public void visit(CondExpr condExpr) {
		Struct left = condExpr.getExpr().struct;
		Struct right = condExpr.getExpr1().struct;
		if(left.getKind() != right.getKind()) {
			report_error("GRESKA! Razliciti tipovi izraza", condExpr);
			condExpr.struct = Tab.noType;
		}
		else {
			condExpr.struct = left;
		}
	}
	
	@Override
	public void visit(Expr_cond expr_cond) {
		expr_cond.struct = expr_cond.getCondExpr().struct;
	}
	
	@Override
	public void visit(Expr_addterm expr_addterm) {
		expr_addterm.struct = expr_addterm.getAddTermList().struct;
	}
	
	@Override
	public void visit(DesignatorStatement_expr designatorStatement_expr) {
		int kind = designatorStatement_expr.getDesignator().obj.getKind();
		if(kind != Obj.Var && kind != Obj.Elem) {
			report_error("GRESKA! Dodela neadekvatnom tipu " + designatorStatement_expr.getDesignator().obj.getName(), designatorStatement_expr);
		}
		else if(!designatorStatement_expr.getExpr().struct.assignableTo(designatorStatement_expr.getDesignator().obj.getType())) {
			report_error("GRESKA! Neadekvatna dodela vrednosti u promenljivu " + designatorStatement_expr.getDesignator().obj.getName(), designatorStatement_expr);
		}
		else if(enumVars.containsKey(designatorStatement_expr.getDesignator().obj.getName()) 
				&& (lastEnumName == null || !lastEnumName.equals(enumVars.get(designatorStatement_expr.getDesignator().obj.getName())))) {
			report_error("GRESKA! Neadekvatna dodela u promenljivu tipa enuma " +enumVars.get(designatorStatement_expr.getDesignator().obj.getName()), designatorStatement_expr);
		}
		lastEnumName = null;
	}
	
	@Override
	public void visit(DesignatorStatement_inc designatorStatement_inc) {
		int kind = designatorStatement_inc.getDesignator().obj.getKind();
		if(kind != Obj.Var && kind != Obj.Elem) {
			report_error("GRESKA! Nije moguce izvrsiti operaciju inkrementiranja nad tipom promenljive " + designatorStatement_inc.getDesignator().obj.getName(), designatorStatement_inc);
		}
		else if(!designatorStatement_inc.getDesignator().obj.getType().equals(Tab.intType)) {
			report_error("GRESKA! Nije moguce izvrsiti operaciju inkrementiranja ne-int promenljive " + designatorStatement_inc.getDesignator().obj.getName(), designatorStatement_inc);
		}
	}
	
	@Override
	public void visit(DesignatorStatement_dec designatorStatement_dec) {
		int kind = designatorStatement_dec.getDesignator().obj.getKind();
		if(kind != Obj.Var && kind != Obj.Elem) {
			report_error("GRESKA! Nije moguce izvrsiti operaciju dekrementiranja nad tipom promenljive " + designatorStatement_dec.getDesignator().obj.getName(), designatorStatement_dec);
		}
		else if(!designatorStatement_dec.getDesignator().obj.getType().equals(Tab.intType)) {
			report_error("GRESKA! Nije moguce izvrsiti operaciju dekrementiranja ne-int promenljive " + designatorStatement_dec.getDesignator().obj.getName(), designatorStatement_dec);
		}
	}
	
	@Override
	public void visit(DesignatorStatement_actpar designatorStatement_actpar) {
		if(designatorStatement_actpar.getDesignator().obj.getKind() != Obj.Meth) {
			report_error("GRESKA! Poziv nedefinisane metode: " +designatorStatement_actpar.getDesignator().obj.getName(), designatorStatement_actpar);
		}
		else {
			List<Struct> formalParamsList = new ArrayList<>();
			for(Obj formal: designatorStatement_actpar.getDesignator().obj.getLocalSymbols()) {
				if(formal.getKind() == Obj.Var && formal.getLevel() == 1) {
					formalParamsList.add(formal.getType());
				}
			}
			ActParsCounter parsCounter = new ActParsCounter();
			designatorStatement_actpar.getActParsList().traverseBottomUp(parsCounter);
			List<Struct> actualParamsList = parsCounter.actParsListfinal;
			if(formalParamsList.size() != actualParamsList.size()) {
				report_error("GRESKA! Funkcija " + designatorStatement_actpar.getDesignator().obj.getName() + " ne prima tacan broj argumenata", designatorStatement_actpar);
			}
			else {
				for(int i = 0; i < formalParamsList.size(); i++) {
					Struct formPar = formalParamsList.get(i);
					Struct actPar = actualParamsList.get(i);
					if(!actPar.assignableTo(formPar)) {
						report_error("GRESKA! Nepoklapanje tipova formalnih i stvarnih parametara funkcije " + designatorStatement_actpar.getDesignator().obj.getName(), designatorStatement_actpar);
					}
				}
			}
		}
	}
	/*
	@Override
	public void visit(DesignatorStatement_swap designatorStatement_swap) {
		Obj arrObj = Tab.find(designatorStatement_swap.getDesArrName().getI1());
		if(arrObj == Tab.noObj) {
			report_error("GRESKA! Pristup promenljivoj " + designatorStatement_swap.getDesArrName().getI1() + " koja ne postoji", designatorStatement_swap);	
		}
		else if(arrObj.getKind() != Obj.Var || arrObj.getType().getKind() != Struct.Array) {
			report_error("GRESKA! Neadekvatan tip promenljive (!= Array)", designatorStatement_swap);
		}
	}
	*/
	@Override
	public void visit(Statement_read statement_read) {
		int kind = statement_read.getDesignator().obj.getKind();
		if(kind != Obj.Var && kind != Obj.Elem) {
			report_error("GRESKA! Neadekvatan tip argumenta operacije read!", statement_read);
		}
		Struct designatorType = statement_read.getDesignator().obj.getType();
		if(!designatorType.equals(Tab.intType) && !designatorType.equals(Tab.charType) && !designatorType.equals(boolType)) {
			report_error("GRESKA! Neadekvatan tip promenljive u argumentu operacije read (int/char/bool): " +statement_read.getDesignator().obj.getName(), statement_read);
		}
	}
	
	@Override
	public void visit(Statement_print statement_print) {
		Struct exprType = statement_print.getExpr().struct;
		if(!exprType.equals(Tab.intType) && !exprType.equals(Tab.charType) && !exprType.equals(boolType)) {
			report_error("GRESKA! Neadekvatan tip izraza u argumentu operacije print (int/char/bool)", statement_print);
		}
	}
	
	public void visit(Statement_printMore statement_printMore) {
		Struct exprType = statement_printMore.getExpr().struct;
		if(!exprType.equals(Tab.intType) && !exprType.equals(Tab.charType) && !exprType.equals(boolType)) {
			report_error("GRESKA! Neadekvatan tip izraza u argumentu operacije print (int/char/bool)", statement_printMore);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
}
