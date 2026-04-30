// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class EnumConstList_single extends EnumConstList {

    private EnumConst EnumConst;

    public EnumConstList_single (EnumConst EnumConst) {
        this.EnumConst=EnumConst;
        if(EnumConst!=null) EnumConst.setParent(this);
    }

    public EnumConst getEnumConst() {
        return EnumConst;
    }

    public void setEnumConst(EnumConst EnumConst) {
        this.EnumConst=EnumConst;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(EnumConst!=null) EnumConst.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(EnumConst!=null) EnumConst.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(EnumConst!=null) EnumConst.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("EnumConstList_single(\n");

        if(EnumConst!=null)
            buffer.append(EnumConst.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [EnumConstList_single]");
        return buffer.toString();
    }
}
