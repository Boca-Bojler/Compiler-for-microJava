// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class ConVarEnumList_con extends ConVarEnumList {

    private ConVarEnumList ConVarEnumList;
    private ConDecList ConDecList;

    public ConVarEnumList_con (ConVarEnumList ConVarEnumList, ConDecList ConDecList) {
        this.ConVarEnumList=ConVarEnumList;
        if(ConVarEnumList!=null) ConVarEnumList.setParent(this);
        this.ConDecList=ConDecList;
        if(ConDecList!=null) ConDecList.setParent(this);
    }

    public ConVarEnumList getConVarEnumList() {
        return ConVarEnumList;
    }

    public void setConVarEnumList(ConVarEnumList ConVarEnumList) {
        this.ConVarEnumList=ConVarEnumList;
    }

    public ConDecList getConDecList() {
        return ConDecList;
    }

    public void setConDecList(ConDecList ConDecList) {
        this.ConDecList=ConDecList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ConVarEnumList!=null) ConVarEnumList.accept(visitor);
        if(ConDecList!=null) ConDecList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ConVarEnumList!=null) ConVarEnumList.traverseTopDown(visitor);
        if(ConDecList!=null) ConDecList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ConVarEnumList!=null) ConVarEnumList.traverseBottomUp(visitor);
        if(ConDecList!=null) ConDecList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConVarEnumList_con(\n");

        if(ConVarEnumList!=null)
            buffer.append(ConVarEnumList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ConDecList!=null)
            buffer.append(ConDecList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConVarEnumList_con]");
        return buffer.toString();
    }
}
