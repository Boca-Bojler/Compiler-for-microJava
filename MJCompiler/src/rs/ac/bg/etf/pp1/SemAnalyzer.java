package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import java.util.HashSet;

public class SemAnalyzer extends VisitorAdaptor {

	private java.util.ArrayList<Struct> actArgs = new java.util.ArrayList<>();

	// ===== ENUM state =====
	private Obj currentEnumObj = null; // Obj.Type za enum
	private Struct currentEnumType = null; // Struct(Enum)
	private int nextEnumVal = 0;
	private java.util.HashSet<Integer> usedEnumVals = null;

	// ===== DESIGNATOR state =====
	private java.util.ArrayDeque<Obj> designatorBaseStack = new java.util.ArrayDeque<>();

	private boolean errorDetected = false;
	Logger log = Logger.getLogger(getClass());
	private Obj currentProgam;
	private Struct currentType;
	private int constant;
	private Struct constantType;
	private Struct boolType = Tab.find("bool").getType();
	private Obj mainMethod = null;
	public int nVars = 0;
	private Obj currentMethod;

	private boolean isIntLike(Struct s) {
		return s != null && (s.equals(Tab.intType) || s.getKind() == Struct.Enum);
	}

	private boolean hasLength(DesignatorSuffixList lst) {
		if (lst == null)
			return false;

		if (lst instanceof DesignatorSuffixList_more) {
			DesignatorSuffixList_more m = (DesignatorSuffixList_more) lst;
			if (m.getDesignatorSuffix() instanceof DesignatorSuffix_length)
				return true;
			return hasLength(m.getDesignatorSuffixList());
		}

		// DesignatorSuffixList_e
		return false;
	}

	/* LOG MESSAGES */
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	public boolean passed() {
		return !errorDetected;
	}

	/* SEMANTIC PASS CODE */

	/* ===== PROGRAM ===== */
	@Override
	public void visit(ProgramName programName) {
		currentProgam = Tab.insert(Obj.Prog, programName.getI1(), Tab.noType);
		Tab.openScope();
	}

	@Override
	public void visit(Program program) {
		Tab.chainLocalSymbols(currentProgam);
		Tab.closeScope();
		currentProgam = null;

		if (mainMethod == null) {
			report_error("Program nema main metodu", program);
		} else {
			// mora biti void i bez argumenata (u tvojoj gramatici je svakako bez
			// argumenata
			if (mainMethod.getType() != Tab.noType) {
				report_error("main mora biti void", program);
			}
			if (mainMethod.getLevel() != 0) {
				report_error("main ne sme imati argumente", program);
			}
		}
	}

	/* ===== ENUM DECLARATIONS ===== */

	@Override
	public void visit(EnumName enumName) {
		String enumId = enumName.getI1();

		// duplo ime enuma u ISTOM opsegu
		if (Tab.currentScope().findSymbol(enumId) != null) {
			report_error("Dvostruka definicija enuma: " + enumId, enumName);

			currentEnumObj = Tab.noObj;
			currentEnumType = Tab.noType;
			Tab.openScope(); // dummy
			return;
		}

		currentEnumType = new Struct(Struct.Enum);
		currentEnumObj = Tab.insert(Obj.Type, enumId, currentEnumType);

		Tab.openScope(); // scope za enum konstante
		nextEnumVal = 0;
		usedEnumVals = new HashSet<>();
	}

	@Override
	public void visit(EnumConst_noassign n) {
		if (currentEnumType == null || currentEnumType == Tab.noType)
			return;

		String name = n.getI1();

		if (Tab.currentScope().findSymbol(name) != null) {
			report_error("Dvostruka enum konstanta: " + name, n);
			return;
		}

		int val = nextEnumVal;

		if (usedEnumVals.contains(val)) {
			report_error("Enum vrednost vec postoji: " + val, n);
			nextEnumVal++;
			return;
		}

		// ENUM KONSTANTE SU TIPA INT
		Obj c = Tab.insert(Obj.Con, name, Tab.intType);
		c.setAdr(val);

		usedEnumVals.add(val);
		nextEnumVal++;
	}

	@Override
	public void visit(EnumConst_assign a) {
		if (currentEnumType == null || currentEnumType == Tab.noType)
			return;

		String name = a.getI1();
		int val = a.getN2();

		if (Tab.currentScope().findSymbol(name) != null) {
			report_error("Dvostruka enum konstanta: " + name, a);
			return;
		}

		if (usedEnumVals.contains(val)) {
			report_error("Enum vrednost vec postoji: " + val, a);
			return;
		}

		// ENUM KONSTANTE SU TIPA INT
		Obj c = Tab.insert(Obj.Con, name, Tab.intType);
		c.setAdr(val);

		usedEnumVals.add(val);
		nextEnumVal = val + 1;
	}

	@Override
	public void visit(EnumDecl e) {
		// ako je bio duplikat imena enuma
		if (currentEnumObj == Tab.noObj) {
			Tab.closeScope();
			currentEnumObj = null;
			currentEnumType = null;
			usedEnumVals = null;
			nextEnumVal = 0;
			return;
		}

		// prebacimo konstante u members enuma
		Tab.chainLocalSymbols(currentEnumType);
		Tab.closeScope();

		currentEnumObj = null;
		currentEnumType = null;
		usedEnumVals = null;
		nextEnumVal = 0;
	}

	/* ===== TYPE ===== */

	@Override
	public void visit(Type type) {
		Obj typeObj = Tab.find(type.getI1());

		if (typeObj == Tab.noObj) {
			report_error("Nepostojeci tip: " + type.getI1(), type);
			currentType = Tab.noType;
			return;
		}

		if (typeObj.getKind() != Obj.Type) {
			report_error("Ident nije tip: " + type.getI1(), type);
			currentType = Tab.noType;
			return;
		}

		// enum je legitiman tip za deklaracije (Broj x;)
		currentType = typeObj.getType();
	}

	/* CONST DECLARATIONS */
	@Override
	public void visit(ConDecl conDecl) {
		Obj conObj = Tab.currentScope().findSymbol(conDecl.getI1());
		if (conObj != null) {
			report_error("Dvostruka definicija konstante: " + conDecl.getI1(), conDecl);
			return;
		}

		if (constantType.assignableTo(currentType)) {
			conObj = Tab.insert(Obj.Con, conDecl.getI1(), currentType);
			conObj.setAdr(constant);
		} else {
			report_error("Neadekvatna dodela konstanti: " + conDecl.getI1(), conDecl);
		}
	}

	@Override
	public void visit(Constant_n constant_n) {
		constant = constant_n.getN1();
		constantType = Tab.intType;
	}

	@Override
	public void visit(Constant_c constant_c) {
		constant = constant_c.getC1();
		constantType = Tab.charType;
	}

	@Override
	public void visit(Constant_b constant_b) {
		constant = constant_b.getB1();
		constantType = boolType;
	}

	/* ===== VAR DECLARATIONS ===== */

	@Override
	public void visit(VarDecl_var varDecl_var) {
		String name = varDecl_var.getI1();
		Obj varObj;

		if (currentMethod == null) {
			// globalno: proveri samo u TEKUĆEM scope-u programa (shadowing je dozvoljen)
			varObj = Tab.currentScope().findSymbol(name);
			if (varObj != null) {
				report_error("Dvostruka definicija promenljive: " + name, varDecl_var);
				return;
			}
		} else {
			varObj = Tab.currentScope().findSymbol(name);
			if (varObj != null) {
				report_error("Dvostruka definicija lokalne promenljive: " + name, varDecl_var);
				return;
			}
		}

		Tab.insert(Obj.Var, name, currentType);
		if (currentMethod == null)
			nVars++;
	}

	@Override
	public void visit(VarDecl_array varDecl_array) {
		String name = varDecl_array.getI1();
		Obj varObj;

		if (currentMethod == null) {
			varObj = Tab.currentScope().findSymbol(name);
			if (varObj != null) {
				report_error("Dvostruka definicija promenljive: " + name, varDecl_array);
				return;
			}
		} else {
			varObj = Tab.currentScope().findSymbol(name);
			if (varObj != null) {
				report_error("Dvostruka definicija lokalne promenljive: " + name, varDecl_array);
				return;
			}
		}

		Tab.insert(Obj.Var, name, new Struct(Struct.Array, currentType));
		if (currentMethod == null)
			nVars++;
	}

	/* METHOD DECLARATIONS */
	@Override
	public void visit(MethodName methodName) {
		String name = methodName.getI1();

		// duplikat metode u globalnom opsegu
		if (Tab.currentScope().findSymbol(name) != null) {
			report_error("Dvostruka definicija metode: " + name, methodName);
			currentMethod = Tab.noObj;
			Tab.openScope(); // da ne pukne MethodDecl
			return;
		}

		currentMethod = Tab.insert(Obj.Meth, name, Tab.noType);
		Tab.openScope();

		if (name.equalsIgnoreCase("main")) {
			if (mainMethod != null) {
				report_error("Dvostruka definicija main metode", methodName);
			} else {
				mainMethod = currentMethod;
			}
		}
	}

	@Override
	public void visit(MethodDecl methodDecl) {
		if (currentMethod == null || currentMethod == Tab.noObj) {
			Tab.closeScope();
			currentMethod = null;
			return;
		}

		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		currentMethod = null;
	}

	/* DESIGNATOR DECLARATIONS */
	@Override
	public void visit(DesignatorName dn) {
		Obj o = Tab.find(dn.getI1());

		if (o == Tab.noObj) {
			report_error("Nedefinisan identifikator: " + dn.getI1(), dn);
			designatorBaseStack.push(Tab.noObj); // bitno da bude izbalansirano pop-ovanje
			return;
		}

		designatorBaseStack.push(o);
	}

	@Override
	public void visit(DesignatorSuffix_field sf) {
		if (designatorBaseStack.isEmpty())
			return;
		Obj designatorBase = designatorBaseStack.peek();
		if (designatorBase == null || designatorBase == Tab.noObj)
			return;

		String memberName = sf.getI1();

		if (designatorBase.getKind() == Obj.Type && designatorBase.getType().getKind() == Struct.Enum) {
			Obj found = null;
			for (Obj c : designatorBase.getType().getMembers()) {
				if (c.getName().equals(memberName)) {
					found = c;
					break;
				}
			}

			if (found == null) {
				report_error("Nepostojeca enum konstanta: " + memberName, sf);
				designatorBaseStack.pop();
				designatorBaseStack.push(Tab.noObj);
			} else {
				designatorBaseStack.pop();
				designatorBaseStack.push(found);
			}
			return;
		}

		report_error("Operator '.' je dozvoljen samo za enum konstante (E.A)", sf);
		designatorBaseStack.pop();
		designatorBaseStack.push(Tab.noObj);
	}

	@Override
	public void visit(DesignatorSuffix_index si) {
		if (designatorBaseStack.isEmpty())
			return;
		Obj designatorBase = designatorBaseStack.peek();
		if (designatorBase == null || designatorBase == Tab.noObj)
			return;

		if (designatorBase.getType().getKind() != Struct.Array) {
			report_error("Indeksiranje je dozvoljeno samo nad nizom", si);
			designatorBaseStack.pop();
			designatorBaseStack.push(Tab.noObj);
			return;
		}

		if (!isIntLike(si.getExpr().struct)) {
			report_error("Indeks niza mora biti tipa int", si);
			designatorBaseStack.pop();
			designatorBaseStack.push(Tab.noObj);
			return;
		}

		Struct elemType = designatorBase.getType().getElemType();
		designatorBaseStack.pop();
		designatorBaseStack.push(new Obj(Obj.Elem, designatorBase.getName() + "[$]", elemType));
	}

	@Override
	public void visit(DesignatorSuffix_length sl) {
		if (designatorBaseStack.isEmpty())
			return;
		Obj designatorBase = designatorBaseStack.peek();
		if (designatorBase == null || designatorBase == Tab.noObj)
			return;

		if (designatorBase.getType().getKind() != Struct.Array) {
			report_error("length se moze koristiti samo nad nizom", sl);
			designatorBaseStack.pop();
			designatorBaseStack.push(Tab.noObj);
		}
	}

	@Override
	public void visit(Designator d) {
		if (designatorBaseStack.isEmpty()) {
			d.obj = Tab.noObj;
			return;
		}

		Obj designatorBase = designatorBaseStack.peek();
		if (designatorBase == null) {
			d.obj = Tab.noObj;
			designatorBaseStack.pop();
			return;
		}

		// BITNO: ne pravimo vestacki Obj za .length, jer nema adr/level
		// Designator uvek nosi realan Obj (niz, elem niza, var...), a length
		// obradjujemo u Factor_des
		d.obj = designatorBase;

		designatorBaseStack.pop();
	}

	/* Expr/Term/Factor/ternary/CondFact DECLARATIONS */

	@Override
	public void visit(Expr_simple e) {
		// Tip izraza je tip ExprNoCond-a
		e.struct = e.getExprNoCond().struct;
	}

	@Override
	public void visit(Expr_ternary e) {

		// uslov mora biti bool
		if (!e.getCondFact().struct.equals(boolType)) {
			report_error("Uslov u ternarnom operatoru mora biti bool", e);
			e.struct = Tab.noType;
			return;
		}

		Struct t1 = e.getExpr().struct;
		Struct t2 = e.getExpr1().struct;

		// drugi i treci moraju biti kompatibilni
		if (!t1.equals(t2)) {
			report_error("Drugi i treci izraz u ternarnom operatoru moraju biti istog tipa", e);
			e.struct = Tab.noType;
			return;
		}

		e.struct = t1;
	}

	@Override
	public void visit(CondFact_expr c) {

		if (!c.getExprNoCond().struct.equals(boolType)) {
			report_error("Uslov mora biti tipa bool", c);
			c.struct = Tab.noType;
		} else {
			c.struct = boolType;
		}
	}

	@Override
	public void visit(CondFact_rel c) {

		Struct left = c.getExprNoCond().struct;
		Struct right = c.getExprNoCond1().struct;

		// kompatibilnost
		if (!left.compatibleWith(right)) {
			report_error("Nekompatibilni tipovi u poredjenju", c);
			c.struct = Tab.noType;
			return;
		}

		// ref tipovi: samo == i !=
		if (left.isRefType() || right.isRefType()) {

			if (!(c.getRelop() instanceof Relop_eq || c.getRelop() instanceof Relop_ne)) {

				report_error("Reference se mogu porediti samo sa == ili !=", c);
				c.struct = Tab.noType;
				return;
			}
		}

		c.struct = boolType;
	}

	/* ===== EXPRNOCOND ===== */
	@Override
	public void visit(ExprNoCond e) {
		Struct res = e.getAddopTermList().struct;

		if (res == null) {
			e.struct = Tab.noType;
			return;
		}

		// unary minus: dozvoljen nad int/enum
		if (e.getUnary() instanceof Unary_m && !isIntLike(res)) {
			report_error("Unarni minus je dozvoljen samo nad int/enum", e);
			e.struct = Tab.noType;
			return;
		}

		// rezultat: bez minusa je tip izraza; sa minusom tretiraj kao int
		if (e.getUnary() instanceof Unary_m) {
			e.struct = Tab.intType;
		} else {
			e.struct = res;
		}
	}

	/* ===== FACTOR ===== */

	@Override
	public void visit(Factor_n f) {
		f.struct = Tab.intType;
	}

	@Override
	public void visit(Factor_c f) {
		f.struct = Tab.charType;
	}

	@Override
	public void visit(Factor_b f) {
		f.struct = boolType;
	}

	@Override
	public void visit(Factor_expr f) {
		// (Expr) -> tip faktora je tip izraza
		f.struct = f.getExpr().struct;
	}

	@Override
	public void visit(Factor_des f) {
		Designator d = f.getDesignator();
		Obj o = (d == null) ? Tab.noObj : d.obj;

		if (o == Tab.noObj) {
			f.struct = Tab.noType;
			return;
		}

		// ne dozvoli da se tip koristi kao vrednost (npr. samo "E" u izrazu)
		if (o.getKind() == Obj.Type) {
			report_error("Tip se ne moze koristiti kao vrednost u izrazu", f);
			f.struct = Tab.noType;
			return;
		}

		// Ako je designator oblika niz.length -> rezultat je int
		if (d != null && hasLength(d.getDesignatorSuffixList())) {
			if (o.getType().getKind() != Struct.Array) {
				report_error("Svojstvo length se moze koristiti samo nad nizovima", f);
				f.struct = Tab.noType;
			} else {
				f.struct = Tab.intType;
			}
			return;
		}

		// promenljiva/konstanta/element niza/enum konstanta
		f.struct = o.getType();
	}

	@Override
	public void visit(Factor_new f) {
		// new Type [ Expr ]
		if (f.getExpr().struct == null || !f.getExpr().struct.equals(Tab.intType)) {
			report_error("Velicina niza mora biti tipa int", f);
			f.struct = Tab.noType;
			return;
		}

		Type t = f.getType();
		Obj typeObj = Tab.find(t.getI1());

		if (typeObj == Tab.noObj || typeObj.getKind() != Obj.Type) {
			report_error("Nepostojeci tip u new: " + t.getI1(), f);
			f.struct = Tab.noType;
			return;
		}

		f.struct = new Struct(Struct.Array, typeObj.getType());
	}

	@Override
	public void visit(Factor_call f) {
		Obj fn = f.getDesignator().obj;

		if (fn == null || fn == Tab.noObj) {
			f.struct = Tab.noType;
			actArgs.clear();
			return;
		}

		if (fn.getKind() != Obj.Meth) {
			report_error("Poziv je dozvoljen samo nad funkcijom/metodom", f);
			f.struct = Tab.noType;
			actArgs.clear();
			return;
		}

		String name = fn.getName();

		// ===== Standardne funkcije (A nivo) =====
		if ("ord".equals(name)) {
			if (actArgs.size() != 1) {
				report_error("ord ocekuje tacno 1 argument", f);
				f.struct = Tab.noType;
			} else if (!actArgs.get(0).equals(Tab.charType)) {
				report_error("ord ocekuje argument tipa char", f);
				f.struct = Tab.noType;
			} else {
				f.struct = Tab.intType;
			}
			actArgs.clear();
			return;
		}

		if ("chr".equals(name)) {
			if (actArgs.size() != 1) {
				report_error("chr ocekuje tacno 1 argument", f);
				f.struct = Tab.noType;
			} else if (!actArgs.get(0).equals(Tab.intType)) {
				report_error("chr ocekuje argument tipa int", f);
				f.struct = Tab.noType;
			} else {
				f.struct = Tab.charType;
			}
			actArgs.clear();
			return;
		}

		if ("len".equals(name)) {
			if (actArgs.size() != 1) {
				report_error("len ocekuje tacno 1 argument", f);
				f.struct = Tab.noType;
			} else if (actArgs.get(0) == null || actArgs.get(0).getKind() != Struct.Array) {
				report_error("len ocekuje niz kao argument", f);
				f.struct = Tab.noType;
			} else {
				f.struct = Tab.intType;
			}
			actArgs.clear();
			return;
		}

		// ===== Ostale metode/funkcije (u tvojoj gramatici su bez argumenata) =====
		int nForm = fn.getLevel(); // main ima 0
		if (actArgs.size() != nForm) {
			report_error("Pogresan broj argumenata u pozivu " + fn.getName() + " (ocekivano " + nForm + ", dobijeno "
					+ actArgs.size() + ")", f);
			f.struct = Tab.noType;
			actArgs.clear();
			return;
		}

		// poziv u izrazu ne sme void
		if (fn.getType() == Tab.noType) {
			report_error("Void funkcija/metoda se ne moze koristiti u izrazu: " + fn.getName(), f);
			f.struct = Tab.noType;
			actArgs.clear();
			return;
		}

		f.struct = fn.getType();
		actArgs.clear();
	}

	/* ===== MULOP FACTOR LIST ===== */

	@Override
	public void visit(MulopFactorList_factor m) {
		// baza: jedan Factor
		m.struct = m.getFactor().struct;
	}

	@Override
	public void visit(MulopFactorList_mul m) {
		Struct left = m.getMulopFactorList().struct;
		Struct right = m.getFactor().struct;

		if (left == null || right == null) {
			m.struct = Tab.noType;
			return;
		}

		if (isIntLike(left) && isIntLike(right)) {
			m.struct = Tab.intType; // rezultat je int
		} else {
			report_error("Mulop (*, /, %) je dozvoljen samo nad int/enum", m);
			m.struct = Tab.noType;
		}
	}

	/* ===== TERM ===== */

	@Override
	public void visit(Term t) {
		// Term ::= MulopFactorList
		t.struct = t.getMulopFactorList().struct;
	}

	/* ===== ADDOP TERM LIST ===== */

	@Override
	public void visit(AddopTermList_term a) {
		// baza: jedan Term
		a.struct = a.getTerm().struct;
	}

	@Override
	public void visit(AddopTermList_add a) {
		Struct left = a.getAddopTermList().struct;
		Struct right = a.getTerm().struct;

		if (left == null || right == null) {
			a.struct = Tab.noType;
			return;
		}

		if (isIntLike(left) && isIntLike(right)) {
			a.struct = Tab.intType; // rezultat aritmetike je int
		} else {
			report_error("Addop (+, -) je dozvoljen samo nad int/enum", a);
			a.struct = Tab.noType;
		}
	}

	@Override
	public void visit(ActParsOpt_e e) {
		actArgs.clear();
	}

	@Override
	public void visit(ActPars_one a) {
		actArgs.clear();
		actArgs.add(a.getExpr().struct);
	}

	@Override
	public void visit(ActPars_more a) {
		// bottom-up: actArgs je već napunjen iz leve strane
		actArgs.add(a.getExpr().struct);
	}

	@Override
	public void visit(DS_call ds) {
		Obj fn = ds.getDesignator().obj;

		if (fn == null || fn == Tab.noObj) {
			actArgs.clear();
			return;
		}

		if (fn.getKind() != Obj.Meth) {
			report_error("Poziv je dozvoljen samo nad funkcijom/metodom", ds);
			actArgs.clear();
			return;
		}

		String name = fn.getName();

		// ===== Standardne funkcije =====
		if ("ord".equals(name)) {
			if (actArgs.size() != 1) {
				report_error("ord ocekuje tacno 1 argument", ds);
			} else if (!actArgs.get(0).equals(Tab.charType)) {
				report_error("ord ocekuje argument tipa char", ds);
			}
			actArgs.clear();
			return;
		}

		if ("chr".equals(name)) {
			if (actArgs.size() != 1) {
				report_error("chr ocekuje tacno 1 argument", ds);
			} else if (!actArgs.get(0).equals(Tab.intType)) {
				report_error("chr ocekuje argument tipa int", ds);
			}
			actArgs.clear();
			return;
		}

		if ("len".equals(name)) {
			if (actArgs.size() != 1) {
				report_error("len ocekuje tacno 1 argument", ds);
			} else if (actArgs.get(0) == null || actArgs.get(0).getKind() != Struct.Array) {
				report_error("len ocekuje niz kao argument", ds);
			}
			actArgs.clear();
			return;
		}

		// ===== Ostale metode (kod tebe nemaju argumente) =====
		if (actArgs.size() != 0) {
			report_error("Metoda " + fn.getName() + " ne prima argumente", ds);
		}

		actArgs.clear();
	}

	@Override
	public void visit(DS_assign ds) {
		Obj dst = ds.getDesignator().obj;

		if (dst == null || dst == Tab.noObj)
			return;

		// zabrani dodelu u a.length
		if (hasLength(ds.getDesignator().getDesignatorSuffixList())) {
			report_error("Nije dozvoljena dodela u length polje", ds);
			return;
		}

		int kind = dst.getKind();
		if (kind != Obj.Var && kind != Obj.Elem && kind != Obj.Fld) {
			report_error("Dodela u neadekvatan designator: " + dst.getName(), ds);
			return;
		}

		Struct dstType = dst.getType();
		Struct srcType = ds.getExpr().struct;

		if (dstType == null || srcType == null || srcType == Tab.noType) {
			report_error("Neadekvatna dodela vrednosti u: " + dst.getName(), ds);
			return;
		}

		// Posebno pravilo za enum: moze da primi int-like (int ili enum)
		if (dstType.getKind() == Struct.Enum) {
			if (!isIntLike(srcType)) {
				report_error("Neadekvatna dodela u enum promenljivu: " + dst.getName(), ds);
			}
			return;
		}

		// Standardno pravilo
		if (!srcType.assignableTo(dstType)) {
			report_error("Neadekvatna dodela vrednosti u: " + dst.getName(), ds);
		}
	}

	@Override
	public void visit(DS_inc ds) {
		Obj dst = ds.getDesignator().obj;
		if (dst == null || dst == Tab.noObj)
			return;

		// zabrani a.length++
		if (hasLength(ds.getDesignator().getDesignatorSuffixList())) {
			report_error("Nije dozvoljeno menjati length polje", ds);
			return;
		}

		int kind = dst.getKind();
		if (kind != Obj.Var && kind != Obj.Elem && kind != Obj.Fld) {
			report_error("Inkrement neadekvatnog designatora: " + dst.getName(), ds);
			return;
		}

		if (!dst.getType().equals(Tab.intType)) {
			report_error("Inkrement je dozvoljen samo nad int: " + dst.getName(), ds);
		}
	}

	@Override
	public void visit(DS_dec ds) {
		Obj dst = ds.getDesignator().obj;
		if (dst == null || dst == Tab.noObj)
			return;

		// zabrani a.length--
		if (hasLength(ds.getDesignator().getDesignatorSuffixList())) {
			report_error("Nije dozvoljeno menjati length polje", ds);
			return;
		}

		int kind = dst.getKind();
		if (kind != Obj.Var && kind != Obj.Elem && kind != Obj.Fld) {
			report_error("Dekrement neadekvatnog designatora: " + dst.getName(), ds);
			return;
		}

		if (!dst.getType().equals(Tab.intType)) {
			report_error("Dekrement je dozvoljen samo nad int: " + dst.getName(), ds);
		}
	}

	@Override
	public void visit(Statement_read s) {
		Obj d = s.getDesignator().obj;
		if (d == null || d == Tab.noObj)
			return;

		int kind = d.getKind();
		if (kind != Obj.Var && kind != Obj.Elem && kind != Obj.Fld) {
			report_error("read zahteva promenljivu/element niza/polje: " + d.getName(), s);
			return;
		}

		Struct t = d.getType();
		if (!(t.equals(Tab.intType) || t.equals(Tab.charType) || t.equals(boolType))) {
			report_error("read je dozvoljen samo za int/char/bool: " + d.getName(), s);
		}
	}

	@Override
	public void visit(Statement_print1 s) {
		Struct t = s.getExpr().struct;
		if (t == null || t == Tab.noType)
			return;

		if (!(t.equals(Tab.intType) || t.equals(Tab.charType) || t.equals(boolType))) {
			report_error("print je dozvoljen samo za int/char/bool", s);
		}
	}

	@Override
	public void visit(Statement_print2 s) {
		Struct t = s.getExpr().struct;
		if (t == null || t == Tab.noType)
			return;

		if (!(t.equals(Tab.intType) || t.equals(Tab.charType) || t.equals(boolType))) {
			report_error("print je dozvoljen samo za int/char/bool", s);
		}
	}

}
