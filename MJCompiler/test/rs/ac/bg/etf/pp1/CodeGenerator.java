package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc = 0;
	private Obj programObj = null;
	private Obj currentMethod = null;
	private java.util.ArrayDeque<Obj> arrayBaseStack = new java.util.ArrayDeque<>();

	private boolean pendingUnaryMinus = false;

	public int getmainPc() {
		return mainPc;
	}

	private void putSwap() {
		// swap top-2 na steku:
		// [a, b] -> dup_x1 => [b, a, b] -> pop => [b, a]
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
	}

	private void popArrayBaseIfAny() {
		if (!arrayBaseStack.isEmpty())
			arrayBaseStack.pop();
	}

	public CodeGenerator() {
	}

	// =========================
	// Helpers: suffix scan
	// =========================

	private boolean hasIndex(DesignatorSuffixList lst) {
		while (lst instanceof DesignatorSuffixList_more) {
			DesignatorSuffix s = ((DesignatorSuffixList_more) lst).getDesignatorSuffix();
			if (s instanceof DesignatorSuffix_index)
				return true;
			lst = ((DesignatorSuffixList_more) lst).getDesignatorSuffixList();
		}
		return false;
	}

	private boolean hasLength(DesignatorSuffixList lst) {
		while (lst instanceof DesignatorSuffixList_more) {
			DesignatorSuffix s = ((DesignatorSuffixList_more) lst).getDesignatorSuffix();
			if (s instanceof DesignatorSuffix_length)
				return true;
			lst = ((DesignatorSuffixList_more) lst).getDesignatorSuffixList();
		}
		return false;
	}

	@Override
	public void visit(ProgramName pn) {
		programObj = Tab.find(pn.getI1()); // program je u univerzumu, Tab.find radi
	}

	// =========================
	// METHOD DECLARATIONS
	// =========================

	@Override
	public void visit(MethodName mn) {

		String name = mn.getI1();

		Obj m = null;

		// Preferirano: nađi metodu među simbolima programa
		if (programObj != null && programObj != Tab.noObj) {
			for (Obj o : programObj.getLocalSymbols()) {
				if (o.getKind() == Obj.Meth && o.getName().equals(name)) {
					m = o;
					break;
				}
			}
		}

		// Fallback
		if (m == null || m == Tab.noObj) {
			m = Tab.find(name);
		}

		// zapamti tekuću metodu (KLJUČNO za resolve lokalnih u codegen-u)
		currentMethod = m;

		if (m != null && m != Tab.noObj) {
			m.setAdr(Code.pc);
		}

		if ("main".equalsIgnoreCase(name)) {
			mainPc = Code.pc;
		}

		int nFormals = (m != null && m != Tab.noObj) ? m.getLevel() : 0;
		int nLocals = (m != null && m != Tab.noObj) ? m.getLocalSymbols().size() : 0;

		Code.put(Code.enter);
		Code.put(nFormals);
		Code.put(nLocals);
	}

	@Override
	public void visit(MethodDecl md) {
		Code.put(Code.exit);
		Code.put(Code.return_);
		currentMethod = null;
	}

	// =========================
	// STATEMENTS
	// =========================

	@Override
	public void visit(DS_assign ds) {
		Obj dst = ds.getDesignator().obj;

		if (dst == null || dst == Tab.noObj)
			return;

		if (dst.getKind() == Obj.Elem) {
			if (dst.getType().equals(Tab.charType))
				Code.put(Code.bastore);
			else
				Code.put(Code.astore);
		} else {
			Code.store(dst);
		}
	}

	@Override
	public void visit(DS_inc ds) {
		Obj o = ds.getDesignator().obj;
		if (o == null || o == Tab.noObj)
			return;

		if (o.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
			Code.put(Code.aload);
			Code.loadConst(1);
			Code.put(Code.add);
			Code.put(Code.astore);
		} else {
			Code.load(o);
			Code.loadConst(1);
			Code.put(Code.add);
			Code.store(o);
		}
	}

	@Override
	public void visit(DS_dec ds) {
		Obj o = ds.getDesignator().obj;
		if (o == null || o == Tab.noObj)
			return;

		if (o.getKind() == Obj.Elem) {
			Code.put(Code.dup2);
			Code.put(Code.aload);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.put(Code.astore);
		} else {
			Code.load(o);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(o);
		}
	}

	@Override
	public void visit(DS_call ds) {
		Obj fn = ds.getDesignator().obj;
		if (fn == null || fn == Tab.noObj) {
			return;
		}

		String name = fn.getName();

		if ("len".equals(name)) {
			Code.put(Code.arraylength);
			Code.put(Code.pop);
			return;
		}
		if ("ord".equals(name) || "chr".equals(name)) {
			Code.put(Code.pop);
			return;
		}

		int offset = fn.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);

		if (fn.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}

	@Override
	public void visit(Statement_read s) {
		Obj d = s.getDesignator().obj;
		if (d == null || d == Tab.noObj)
			return;

		if (d.getType().equals(Tab.charType))
			Code.put(Code.bread);
		else
			Code.put(Code.read);

		if (d.getKind() == Obj.Elem) {
			// pre read/bread već mora da stoji [arrayRef, index] na steku
			// posle read/bread: [arrayRef, index, value]
			if (d.getType().equals(Tab.charType))
				Code.put(Code.bastore);
			else
				Code.put(Code.astore);
		} else {
			Code.store(d);
		}
	}

	@Override
	public void visit(Statement_print1 s) {
		Code.loadConst(0);
		if (s.getExpr().struct != null && s.getExpr().struct.equals(Tab.charType)) {
			Code.put(Code.bprint);
		} else {
			Code.put(Code.print);
		}
	}

	@Override
	public void visit(Statement_print2 s) {
		Code.loadConst(s.getN2());
		if (s.getExpr().struct != null && s.getExpr().struct.equals(Tab.charType)) {
			Code.put(Code.bprint);
		} else {
			Code.put(Code.print);
		}
	}

	// =========================
	// EXPR: + - * / % unary
	// =========================

	@Override
	public void visit(AddopTermList_add a) {
		if (a.getAddop() instanceof Addop_plus) {
			Code.put(Code.add);
		} else {
			Code.put(Code.sub);
		}
	}

	@Override
	public void visit(MulopFactorList_mul m) {
		if (m.getMulop() instanceof Mulop_mul) {
			Code.put(Code.mul);
		} else if (m.getMulop() instanceof Mulop_div) {
			Code.put(Code.div);
		} else {
			Code.put(Code.rem);
		}
	}

	@Override
	public void visit(ExprNoCond e) {
		// NISTA: unary minus ćemo rešiti na prvom faktoru (tačno mesto)
	}

	@Override
	public void visit(Unary_m u) {
		pendingUnaryMinus = true;
	}

	// =========================
	// FACTORS
	// =========================

	@Override
	public void visit(Factor_n f) {
		Code.loadConst(f.getN1());

		if (pendingUnaryMinus) {
			Code.put(Code.neg);
			pendingUnaryMinus = false;
		}
	}

	@Override
	public void visit(Factor_c f) {
		Code.loadConst(f.getC1());

		if (pendingUnaryMinus) {
			Code.put(Code.neg);
			pendingUnaryMinus = false;
		}
	}

	@Override
	public void visit(Factor_b f) {
		Code.loadConst(f.getB1());

		if (pendingUnaryMinus) {
			Code.put(Code.neg);
			pendingUnaryMinus = false;
		}
	}

	@Override
	public void visit(Factor_new f) {
		// Expr (veličina) je već na steku
		Code.put(Code.newarray);

		// element tip dobijamo iz semantike: f.struct = Array(elem)
		Struct arr = f.struct;
		Struct elem = (arr != null && arr.getKind() == Struct.Array) ? arr.getElemType() : Tab.noType;

		// MJ: 0 za char, 1 za ostalo
		Code.put(elem != null && elem.equals(Tab.charType) ? 0 : 1);

		if (pendingUnaryMinus) {
			Code.put(Code.neg);
			pendingUnaryMinus = false;
		}
	}

	@Override
	public void visit(Factor_des f) {
		Designator d = f.getDesignator();
		if (d == null) {
			return;
		}

		if (hasLength(d.getDesignatorSuffixList())) {
			Obj base = d.obj;
			if (base != null && base != Tab.noObj && base.getKind() != Obj.Elem) {
				Code.load(base);
				Code.put(Code.arraylength);

				if (pendingUnaryMinus) {
					Code.put(Code.neg);
					pendingUnaryMinus = false;
				}
			}
			return;
		}

		Code.load(d.obj);

		if (pendingUnaryMinus) {
			Code.put(Code.neg);
			pendingUnaryMinus = false;
		}
	}

	@Override
	public void visit(Factor_call f) {
		Obj fn = f.getDesignator().obj;
		if (fn == null || fn == Tab.noObj) {
			return;
		}

		String name = fn.getName();

		if ("len".equals(name)) {
			Code.put(Code.arraylength);

			if (pendingUnaryMinus) {
				Code.put(Code.neg);
				pendingUnaryMinus = false;
			}
			return;
		}
		if ("ord".equals(name) || "chr".equals(name)) {
			// identity: argument ostaje na steku
			if (pendingUnaryMinus) {
				Code.put(Code.neg);
				pendingUnaryMinus = false;
			}
			return;
		}

		int offset = fn.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);

		if (pendingUnaryMinus) {
			Code.put(Code.neg);
			pendingUnaryMinus = false;
		}
	}

	@Override
	public void visit(DesignatorName dn) {
		String name = dn.getI1();
		Obj o = null;

		// 1) lokal u tekućoj metodi
		if (currentMethod != null && currentMethod != Tab.noObj) {
			for (Obj s : currentMethod.getLocalSymbols()) {
				if (name.equals(s.getName())) {
					o = s;
					break;
				}
			}
		}

		// 2) global u programu
		if ((o == null || o == Tab.noObj) && programObj != null && programObj != Tab.noObj) {
			for (Obj s : programObj.getLocalSymbols()) {
				if (name.equals(s.getName())) {
					o = s;
					break;
				}
			}
		}

		// 3) fallback
		if (o == null || o == Tab.noObj)
			o = Tab.find(name);

		// PUSH bazu (može biti niz, ili bilo šta; index suffix će je koristiti)
		arrayBaseStack.push(o);
	}

	@Override
	public void visit(DesignatorSuffix_index si) {
		// posle Expr indeksa: [..., index]
		if (arrayBaseStack.isEmpty())
			return;

		Obj base = arrayBaseStack.peek();
		if (base == null || base == Tab.noObj)
			return;

		// [..., index, arrayRef]
		Code.load(base);

		// [..., arrayRef, index] (VM očekuje ovako za aload/astore)
		putSwap();
	}

	@Override
	public void visit(Designator d) {
		popArrayBaseIfAny();
	}

	// =========================
	// CONDITIONS (for ternary)
	// =========================

	private int relopToOpcode(Relop r) {
		if (r instanceof Relop_eq)
			return Code.eq;
		if (r instanceof Relop_ne)
			return Code.ne;
		if (r instanceof Relop_lt)
			return Code.lt;
		if (r instanceof Relop_le)
			return Code.le;
		if (r instanceof Relop_gt)
			return Code.gt;
		if (r instanceof Relop_ge)
			return Code.ge;
		return Code.eq;
	}

	// CondFact_expr: ExprNoCond je bool -> ostavlja 0/1 na steku (već ga ima)
	@Override
	public void visit(CondFact_expr c) {
		// ništa: ExprNoCond već ostavlja bool (0/1)
	}

	// CondFact_rel: posle obilaska, na steku su left i right; ovde ih pretvaramo u
	// bool 0/1
	@Override
	public void visit(CondFact_rel c) {
		int op = relopToOpcode(c.getRelop());

		// if (left op right) push 1 else push 0
		Code.putFalseJump(op, 0);
		int jFalse = Code.pc - 2;

		Code.loadConst(1);
		Code.putJump(0);
		int jEnd = Code.pc - 2;

		Code.fixup(jFalse);
		Code.loadConst(0);

		Code.fixup(jEnd);
	}

	// =========================
	// TERNARY ?: (CondFact ? Expr : Expr)
	// =========================

	@Override
	public void visit(Expr_ternary e) {
		// Nakon bottom-up obilaska dece:
		// stek: [cond, trueExpr, falseExpr]

		Code.put(Code.dup_x2); // [false, cond, true, false]
		Code.put(Code.pop); // [false, cond, true]
		putSwap(); // [false, true, cond]

		// test cond != 0 ?
		Code.loadConst(0);
		Code.putFalseJump(Code.ne, 0); // ako je cond==0 -> skok na false granu
		int jFalse = Code.pc - 2;

		// TRUE grana: stek [false, true]
		putSwap(); // [true, false]
		Code.put(Code.pop); // ostaje [true]
		Code.putJump(0);
		int jEnd = Code.pc - 2;

		// FALSE grana: stek [false, true]
		Code.fixup(jFalse);
		Code.put(Code.pop); // skini true, ostaje [false]

		Code.fixup(jEnd);
	}

}
