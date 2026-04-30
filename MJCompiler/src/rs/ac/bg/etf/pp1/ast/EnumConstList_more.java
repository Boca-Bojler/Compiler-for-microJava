// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class EnumConstList_more extends EnumConstList {

    private EnumConst EnumConst;
    private EnumConstList EnumConstList;

    public EnumConstList_more (EnumConst EnumConst, EnumConstList EnumConstList) {
        this.EnumConst=EnumConst;
        if(EnumConst!=null) EnumConst.setParent(this);
        this.EnumConstList=EnumConstList;
        if(EnumConstList!=null) EnumConstList.setParent(this);
    }

    public EnumConst getEnumConst() {
        return EnumConst;
    }

    public void setEnumConst(EnumConst EnumConst) {
        this.EnumConst=EnumConst;
    }

    public EnumConstList getEnumConstList() {
        return EnumConstList;
    }

    public void setEnumConstList(EnumConstList EnumConstList) {
        this.EnumConstList=EnumConstList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(EnumConst!=null) EnumConst.accept(visitor);
        if(EnumConstList!=null) EnumConstList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(EnumConst!=null) EnumConst.traverseTopDown(visitor);
        if(EnumConstList!=null) EnumConstList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(EnumConst!=null) EnumConst.traverseBottomUp(visitor);
        if(EnumConstList!=null) EnumConstList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("EnumConstList_more(\n");

        if(EnumConst!=null)
            buffer.append(EnumConst.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(EnumConstList!=null)
            buffer.append(EnumConstList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [EnumConstList_more]");
        return buffer.toString();
    }
}
