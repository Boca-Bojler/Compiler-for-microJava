// generated with ast extension for cup
// version 0.8
// 30/3/2026 18:34:41


package rs.ac.bg.etf.pp1.ast;

public class ConstVarEnumDeclList_var extends ConstVarEnumDeclList {

    private ConstVarEnumDeclList ConstVarEnumDeclList;
    private VarDeclList VarDeclList;

    public ConstVarEnumDeclList_var (ConstVarEnumDeclList ConstVarEnumDeclList, VarDeclList VarDeclList) {
        this.ConstVarEnumDeclList=ConstVarEnumDeclList;
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.setParent(this);
        this.VarDeclList=VarDeclList;
        if(VarDeclList!=null) VarDeclList.setParent(this);
    }

    public ConstVarEnumDeclList getConstVarEnumDeclList() {
        return ConstVarEnumDeclList;
    }

    public void setConstVarEnumDeclList(ConstVarEnumDeclList ConstVarEnumDeclList) {
        this.ConstVarEnumDeclList=ConstVarEnumDeclList;
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
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.accept(visitor);
        if(VarDeclList!=null) VarDeclList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.traverseTopDown(visitor);
        if(VarDeclList!=null) VarDeclList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ConstVarEnumDeclList!=null) ConstVarEnumDeclList.traverseBottomUp(visitor);
        if(VarDeclList!=null) VarDeclList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConstVarEnumDeclList_var(\n");

        if(ConstVarEnumDeclList!=null)
            buffer.append(ConstVarEnumDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarDeclList!=null)
            buffer.append(VarDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConstVarEnumDeclList_var]");
        return buffer.toString();
    }
}
