public class Fib extends Seq {

    protected int first1, first2, last;             // define variables
    protected static int count = 0;
    public Fib(int first1, int first2, int last){   // constructor 
        this.first1 = first1;
        this.first2 = first2;
        this.last = last;
        count++;

    }

     public SeqIt createSeqIt() {
        //For f = new For(first, last, step);
        return new FibIt(this);
    }

    public int upperBound() {
       return last; 
    }

    public static int getCount(){
        return count;
    }

    // < 0, 1 to 10 >
    public String toString() {
        return "< " + first1 + ", " + first2 + " to " + last + " >";    
    }


}