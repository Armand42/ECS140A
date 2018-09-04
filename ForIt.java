public class ForIt implements SeqIt {
    private int first, last, step;
    public ForIt(For f){
        first = f.first;
        last = f.last;
        step = f.step;
    }
    public boolean hasNext(){
        if (step > 0) {
            return first <= last;
        }
        else if (step < 0) {
            return first >= last;
        }
        else {
            return true;
        }
    }
    public int next() throws UsingIteratorPastEndException{
        if (! hasNext()) {
            throw new UsingIteratorPastEndException();
        }
        int i = first;
        first += step;
        return i;
    }
}