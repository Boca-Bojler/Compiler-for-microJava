// generated with ast extension for cup
// version 0.8
// 30/3/2026 18:34:41


package rs.ac.bg.etf.pp1.ast;

public class Unary_std extends Unary {

    private FactorContext FactorContext;

    public Unary_std (FactorContext FactorContext) {
        this.FactorContext=FactorContext;
        if(FactorContext!=null) FactorContext.setParent(this);
    }

    public FactorContext getFactorContext() {
        return FactorContext;
    }

    public void setFactorContext(FactorContext FactorContext) {
        this.FactorContext=FactorContext;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(FactorContext!=null) FactorContext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(FactorContext!=null) FactorContext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(FactorContext!=null) FactorContext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Unary_std(\n");

        if(FactorContext!=null)
            buffer.append(FactorContext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Unary_std]");
        return buffer.toString();
    }
}
