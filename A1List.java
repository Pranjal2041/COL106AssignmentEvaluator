// Implements Dictionary using Doubly Linked List (DLL)
// Implement the following functions using the specifications provided in the class List

public class A1List extends List {

    private A1List  next; // Next Node
    private A1List prev;  // Previous Node 

    public A1List(int address, int size, int key) { 
        super(address, size, key);
    }
    
    public A1List(){
        super(-1,-1,-1);
        // This acts as a head Sentinel

        A1List tailSentinel = new A1List(-1,-1,-1); // Intiate the tail sentinel
        
        this.next = tailSentinel;
        tailSentinel.prev = this;
    }

    public A1List Insert(int address, int size, int key)
    {
        return null;
    }

    public boolean Delete(Dictionary d) 
    {
        return false;
    }

    public A1List Find(int k, boolean exact)
    { 
        return null;
    }

    public A1List getFirst()
    {
        return null;
    }
    
    public A1List getNext() 
    {
        return null;
    }

    public boolean sanity()
    {
        return true;
    }

}


