// generated with ast extension for cup
// version 0.8
// 30/3/2026 18:34:41


package rs.ac.bg.etf.pp1.ast;

public class Designator_enum extends Designator {

    private DesArrName DesArrName;
    private String I2;

    public Designator_enum (DesArrName DesArrName, String I2) {
        this.DesArrName=DesArrName;
        if(DesArrName!=null) DesArrName.setParent(this);
        this.I2=I2;
    }

    public DesArrName getDesArrName() {
        return DesArrName;
    }

    public void setDesArrName(DesArrName DesArrName) {
        this.DesArrName=DesArrName;
    }

    public String getI2() {
        return I2;
    }

    public void setI2(String I2) {
        this.I2=I2;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DesArrName!=null) DesArrName.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DesArrName!=null) DesArrName.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DesArrName!=null) DesArrName.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Designator_enum(\n");

        if(DesArrName!=null)
            buffer.append(DesArrName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+I2);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Designator_enum]");
        return buffer.toString();
    }
}
