import java.util.ArrayList;

public class AList implements Element{
    protected ArrayList<Element> myList;   // specify type of object to put in list
    public AList(){
        myList = new ArrayList<Element>();
    }

    public void add(Seq s) {
        myList.add(s);
    }

    public void add(AList a) {
        myList.add(a);
    }
    public void add(int i){
        myList.add(new Integer(i));
    }

    public void add(Element e) {
        myList.add(e);
    }

    public ArrayList<Element> getList(){
        return myList;
    }

    public AList myExpand(AList list){
        list.add(expand());
        return list;
    }

    public AList myFlatten(AList list){
        for(Element e : flatten().getList()){
            list.add(e);
        }
        return list;
    }

    public AList expand(){
        AList newList = new AList();
        for (Element e : myList) {
            // if e is int add(int) else if e is 
            newList = e.myExpand(newList);
        }
        return newList;
    }

    public AList flatten(){
        AList newList = new AList();
        for (Element e : myList) {
            newList = e.myFlatten(newList);
        }
        return newList;
    }

    // public AList expand() {
    //     AList newList = new AList();
        
    //     for (Object o : myList) {
    //         if (o instanceof Integer) {
    //             newList.add((int)o);
    //         }
    //         else if (o instanceof Seq) {
    //             SeqIt it = ((Seq) o).createSeqIt();
    //             while(it.hasNext()) {
    //                 try {
    //                     newList.add(it.next());
    //                 }
    //                 catch (Exception e) {  
    //                 }

    //             }

    //         }

    //         else {
    //             AList expandedList = ((AList)o).expand();            // recursive step for going through lists
    //             newList.add(expandedList);
    //         }
           
    //     }

    //     return newList;
    // }
    // public AList flatten(){
    //     AList flattenList = new AList();
    //     for (Object o: myList) {
    //         if (o instanceof Integer) {
    //             flattenList.add((int)o);            // add (int)
    //         }
    //         else if (o instanceof Seq) {
    //             flattenList.add((Seq)o);
    //         }
    //         else {
    //             AList newFlattenList = ((AList)o).flatten();    // recursive step
    //             for (Object ob: newFlattenList.getList()) {
    //                 if (ob instanceof Integer) {    // built in java class 
    //                     flattenList.add((int)ob);
    //                 }
    //                 else if (ob instanceof Seq) {
    //                     flattenList.add((Seq)ob);
    //                 }
    //                 else {
    //                     flattenList.add((AList)ob);
    //                 }
    //                 // flattenList.add(o);

    //             }
    //         }
    //     }

    //     return flattenList;
    // }
    
    public String toString() {
        
        String output = "[ ";
        for (Object o : myList) {
            output += o + " ";
        } 
        if (myList.isEmpty()){
            output += " ]";    
        }
        else{
            output += "]";
        }
        return output;
    }
    
}