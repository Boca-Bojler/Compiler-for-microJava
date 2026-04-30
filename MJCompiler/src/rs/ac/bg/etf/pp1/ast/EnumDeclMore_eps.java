// generated with ast extension for cup
// version 0.8
// 30/3/2026 18:34:41


package rs.ac.bg.etf.pp1.ast;

public class EnumDeclMore_eps extends EnumDeclMore {

    public EnumDeclMore_eps () {
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("EnumDeclMore_eps(\n");

        buffer.append(tab);
        buffer.append(") [EnumDeclMore_eps]");
        return buffer.toString();
    }
}
