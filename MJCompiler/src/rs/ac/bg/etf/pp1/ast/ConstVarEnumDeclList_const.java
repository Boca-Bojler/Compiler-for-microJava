// generated with ast extension for cup
// version 0.8
// 30/3/2026 18:34:41


package rs.ac.bg.etf.pp1.ast;

public class ConstVarEnumDeclList_const extends ConstVarEnumDeclList {

    private ConstVarEnumDeclList ConstVarEnumDeclList;
    private ConstDeclList ConstDeclList;

    public ConstVarEnumDeclList_const (ConstVarEnumDeclList ConstVarEnumDeclList, ConstDeclList ConstDeclList) {
        this.ConstVarEnumDeclList=ConstVarEnumDeclList;
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.setParent(this);
        this.ConstDeclList=ConstDeclList;
        if(ConstDeclList!=null) ConstDeclList.setParent(this);
    }

    public ConstVarEnumDeclList getConstVarEnumDeclList() {
        return ConstVarEnumDeclList;
    }

    public void setConstVarEnumDeclList(ConstVarEnumDeclList ConstVarEnumDeclList) {
        this.ConstVarEnumDeclList=ConstVarEnumDeclList;
    }

    public ConstDeclList getConstDeclList() {
        return ConstDeclList;
    }

    public void setConstDeclList(ConstDeclList ConstDeclList) {
        this.ConstDeclList=ConstDeclList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.accept(visitor);
        if(ConstDeclList!=null) ConstDeclList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.traverseTopDown(visitor);
        if(ConstDeclList!=null) ConstDeclList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.traverseBottomUp(visitor);
        if(ConstDeclList!=null) ConstDeclList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConstVarEnumDeclList_const(\n");

        if(ConstVarEnumDeclList!=null)
            buffer.append(ConstVarEnumDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ConstDeclList!=null)
            buffer.append(ConstDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConstVarEnumDeclList_const]");
        return buffer.toString();
    }
}
