
public class ForUser {

    public static int sum1(For r){
        ForIt it = new ForIt(r);
        int sum = 0;
        while (it.hasNext()) {
            try {
                sum+= it.next();   
            }
            catch (Exception e){
            }
        }
        return sum;

    }

    public static int sum2(For r){
        ForIt it = new ForIt(r);
        int sum = 0;
        while (true){
            try {
                sum += it.next();
            }
            catch (UsingIteratorPastEndException u) {
                //System.err.println("ForIt called past end");
                return sum;
            }
        }
        
    }
}