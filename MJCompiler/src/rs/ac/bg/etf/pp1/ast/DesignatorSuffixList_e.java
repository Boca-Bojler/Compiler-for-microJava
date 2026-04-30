// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class DesignatorSuffixList_e extends DesignatorSuffixList {

    public DesignatorSuffixList_e () {
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
        buffer.append("DesignatorSuffixList_e(\n");

        buffer.append(tab);
        buffer.append(") [DesignatorSuffixList_e]");
        return buffer.toString();
    }
}
