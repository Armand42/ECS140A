// the Seq base class

public abstract class Seq implements Element{
    public static int count = 0;
    public Seq(){
        count++;
    }
    public abstract String toString();
    public abstract int upperBound();
    public static int getCount(){
        return count;
    }
    public abstract SeqIt createSeqIt();

    public AList myFlatten(AList list){
        list.add(this);
        return list;
    }
    public AList myExpand(AList list){
        SeqIt it = this.createSeqIt();
        while (it.hasNext()){
            try {
                list.add(it.next());
            }
            catch (Exception e){
            } 
        }
        return list;
    }
}

