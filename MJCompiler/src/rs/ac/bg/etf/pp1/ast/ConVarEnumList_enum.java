// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class ConVarEnumList_enum extends ConVarEnumList {

    private ConVarEnumList ConVarEnumList;
    private EnumDecl EnumDecl;

    public ConVarEnumList_enum (ConVarEnumList ConVarEnumList, EnumDecl EnumDecl) {
        this.ConVarEnumList=ConVarEnumList;
        if(ConVarEnumList!=null) ConVarEnumList.setParent(this);
        this.EnumDecl=EnumDecl;
        if(EnumDecl!=null) EnumDecl.setParent(this);
    }

    public ConVarEnumList getConVarEnumList() {
        return ConVarEnumList;
    }

    public void setConVarEnumList(ConVarEnumList ConVarEnumList) {
        this.ConVarEnumList=ConVarEnumList;
    }

    public EnumDecl getEnumDecl() {
        return EnumDecl;
    }

    public void setEnumDecl(EnumDecl EnumDecl) {
        this.EnumDecl=EnumDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ConVarEnumList!=null) ConVarEnumList.accept(visitor);
        if(EnumDecl!=null) EnumDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ConVarEnumList!=null) ConVarEnumList.traverseTopDown(visitor);
        if(EnumDecl!=null) EnumDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ConVarEnumList!=null) ConVarEnumList.traverseBottomUp(visitor);
        if(EnumDecl!=null) EnumDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConVarEnumList_enum(\n");

        if(ConVarEnumList!=null)
            buffer.append(ConVarEnumList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(EnumDecl!=null)
            buffer.append(EnumDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConVarEnumList_enum]");
        return buffer.toString();
    }
}
