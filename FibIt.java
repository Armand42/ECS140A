public class FibIt implements SeqIt {
    private int first1, first2, last;
    public FibIt(Fib fib){
        first1 = fib.first1;
        first2 = fib.first2;
        last = fib.last;
        
    }
    public boolean hasNext(){
        if (first1 <= last) {
            return true;
        }
        else {
            return false;
        }

    }
    public int next() throws UsingIteratorPastEndException { 
        if (! hasNext()) {
            throw new UsingIteratorPastEndException();
        }
        int i = first1;
        int sum = first1 + first2;
        first1 = first2;
        first2 = sum;
        return i;
    }
}