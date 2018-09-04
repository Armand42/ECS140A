public class Integer implements Element{
    private int myInt;
    public Integer(int myInt){
        this.myInt = myInt;
    }
    public AList myExpand(AList list){
        list.add(this);
        return list;
    }
    public AList myFlatten(AList list){
        list.add(this);
        return list;
    }
    public String toString(){
        return "" + myInt;
    }
}