public class For extends Seq {
    protected int first, last, step;
    protected static int count = 0;
    public For(int first, int last, int step){
        this.first = first;
        this.last = last;
        this.step = step;
        count++;
    }

    public SeqIt createSeqIt() {
        //For f = new For(first, last, step);
        return new ForIt(this);
    }


    





    public int upperBound() {
        if (step > 0)   {   // global step
            return last;
        }               
        else {
            return first;
        }  

    }

    public static int getCount(){
        return count;
    }


    public String toString(){
        return "{ " + first + " to " + last + " by " + step + " }";
    }
}
