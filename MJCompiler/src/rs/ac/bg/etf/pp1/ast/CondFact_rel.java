// generated with ast extension for cup
// version 0.8
// 30/3/2026 18:34:41


package rs.ac.bg.etf.pp1.ast;

public class CondFact_rel extends CondFact {

    private AddTermList AddTermList;
    private Relop Relop;
    private AddTermList AddTermList1;

    public CondFact_rel (AddTermList AddTermList, Relop Relop, AddTermList AddTermList1) {
        this.AddTermList=AddTermList;
        if(AddTermList!=null) AddTermList.setParent(this);
        this.Relop=Relop;
        if(Relop!=null) Relop.setParent(this);
        this.AddTermList1=AddTermList1;
        if(AddTermList1!=null) AddTermList1.setParent(this);
    }

    public AddTermList getAddTermList() {
        return AddTermList;
    }

    public void setAddTermList(AddTermList AddTermList) {
        this.AddTermList=AddTermList;
    }

    public Relop getRelop() {
        return Relop;
    }

    public void setRelop(Relop Relop) {
        this.Relop=Relop;
    }

    public AddTermList getAddTermList1() {
        return AddTermList1;
    }

    public void setAddTermList1(AddTermList AddTermList1) {
        this.AddTermList1=AddTermList1;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(AddTermList!=null) AddTermList.accept(visitor);
        if(Relop!=null) Relop.accept(visitor);
        if(AddTermList1!=null) AddTermList1.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(AddTermList!=null) AddTermList.traverseTopDown(visitor);
        if(Relop!=null) Relop.traverseTopDown(visitor);
        if(AddTermList1!=null) AddTermList1.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(AddTermList!=null) AddTermList.traverseBottomUp(visitor);
        if(Relop!=null) Relop.traverseBottomUp(visitor);
        if(AddTermList1!=null) AddTermList1.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("CondFact_rel(\n");

        if(AddTermList!=null)
            buffer.append(AddTermList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Relop!=null)
            buffer.append(Relop.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(AddTermList1!=null)
            buffer.append(AddTermList1.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [CondFact_rel]");
        return buffer.toString();
    }
}
