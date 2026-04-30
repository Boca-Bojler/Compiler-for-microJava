// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class ConVarEnumList_var extends ConVarEnumList {

    private ConVarEnumList ConVarEnumList;
    private VarDeclList VarDeclList;

    public ConVarEnumList_var (ConVarEnumList ConVarEnumList, VarDeclList VarDeclList) {
        this.ConVarEnumList=ConVarEnumList;
        if(ConVarEnumList!=null) ConVarEnumList.setParent(this);
        this.VarDeclList=VarDeclList;
        if(VarDeclList!=null) VarDeclList.setParent(this);
    }

    public ConVarEnumList getConVarEnumList() {
        return ConVarEnumList;
    }

    public void setConVarEnumList(ConVarEnumList ConVarEnumList) {
        this.ConVarEnumList=ConVarEnumList;
    }

    public VarDeclList getVarDeclList() {
        return VarDeclList;
    }

    public void setVarDeclList(VarDeclList VarDeclList) {
        this.VarDeclList=VarDeclList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ConVarEnumList!=null) ConVarEnumList.accept(visitor);
        if(VarDeclList!=null) VarDeclList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ConVarEnumList!=null) ConVarEnumList.traverseTopDown(visitor);
        if(VarDeclList!=null) VarDeclList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ConVarEnumList!=null) ConVarEnumList.traverseBottomUp(visitor);
        if(VarDeclList!=null) VarDeclList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConVarEnumList_var(\n");

        if(ConVarEnumList!=null)
            buffer.append(ConVarEnumList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarDeclList!=null)
            buffer.append(VarDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConVarEnumList_var]");
        return buffer.toString();
    }
}
