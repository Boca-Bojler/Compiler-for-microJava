// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class CondFact_rel extends CondFact {

    private ExprNoCond ExprNoCond;
    private Relop Relop;
    private ExprNoCond ExprNoCond1;

    public CondFact_rel (ExprNoCond ExprNoCond, Relop Relop, ExprNoCond ExprNoCond1) {
        this.ExprNoCond=ExprNoCond;
        if(ExprNoCond!=null) ExprNoCond.setParent(this);
        this.Relop=Relop;
        if(Relop!=null) Relop.setParent(this);
        this.ExprNoCond1=ExprNoCond1;
        if(ExprNoCond1!=null) ExprNoCond1.setParent(this);
    }

    public ExprNoCond getExprNoCond() {
        return ExprNoCond;
    }

    public void setExprNoCond(ExprNoCond ExprNoCond) {
        this.ExprNoCond=ExprNoCond;
    }

    public Relop getRelop() {
        return Relop;
    }

    public void setRelop(Relop Relop) {
        this.Relop=Relop;
    }

    public ExprNoCond getExprNoCond1() {
        return ExprNoCond1;
    }

    public void setExprNoCond1(ExprNoCond ExprNoCond1) {
        this.ExprNoCond1=ExprNoCond1;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ExprNoCond!=null) ExprNoCond.accept(visitor);
        if(Relop!=null) Relop.accept(visitor);
        if(ExprNoCond1!=null) ExprNoCond1.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ExprNoCond!=null) ExprNoCond.traverseTopDown(visitor);
        if(Relop!=null) Relop.traverseTopDown(visitor);
        if(ExprNoCond1!=null) ExprNoCond1.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ExprNoCond!=null) ExprNoCond.traverseBottomUp(visitor);
        if(Relop!=null) Relop.traverseBottomUp(visitor);
        if(ExprNoCond1!=null) ExprNoCond1.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("CondFact_rel(\n");

        if(ExprNoCond!=null)
            buffer.append(ExprNoCond.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Relop!=null)
            buffer.append(Relop.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ExprNoCond1!=null)
            buffer.append(ExprNoCond1.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [CondFact_rel]");
        return buffer.toString();
    }
}
