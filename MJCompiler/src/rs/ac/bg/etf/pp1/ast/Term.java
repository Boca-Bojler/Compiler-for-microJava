// generated with ast extension for cup
// version 0.8
// 10/2/2026 15:1:16


package rs.ac.bg.etf.pp1.ast;

public class Term implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Struct struct = null;

    private MulopFactorList MulopFactorList;

    public Term (MulopFactorList MulopFactorList) {
        this.MulopFactorList=MulopFactorList;
        if(MulopFactorList!=null) MulopFactorList.setParent(this);
    }

    public MulopFactorList getMulopFactorList() {
        return MulopFactorList;
    }

    public void setMulopFactorList(MulopFactorList MulopFactorList) {
        this.MulopFactorList=MulopFactorList;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MulopFactorList!=null) MulopFactorList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MulopFactorList!=null) MulopFactorList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MulopFactorList!=null) MulopFactorList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Term(\n");

        if(MulopFactorList!=null)
            buffer.append(MulopFactorList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Term]");
        return buffer.toString();
    }
}
