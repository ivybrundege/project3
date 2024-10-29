// -------------------------------------------------------------------------
/**
 * This is a doubly-linked implementation of a list using doubly-linked nodes.
 * Specifically holds run objects.
 * 
 * @author Ibrahim Shabani
 * @author Ivy Brundege
 * @version 10.30.24
 * 
 *            Type to go into list (nodes will hold this type as well)
 */
public class DoubleLL {
    // ~ Private Node Class ....................................................
    private static class DLLNode{
        // fields
        private Run data;
        private DLLNode prev;
        private DLLNode next;

        // consturctors
        public DLLNode(DLLNode prevNode, Run item, DLLNode nextNode) {
            prev = prevNode;
            data = item;
            next = nextNode;
        }


        public DLLNode(Run item) {
            this(null, item, null);
        }


        // methods
        public Run getData() {
            return data;
        }


        public void setPrev(DLLNode prevNode) {
            prev = prevNode;
        }


        public DLLNode getPrev() {
            return prev;
        }


        public void setNext(DLLNode nextNode) {
            next = nextNode;
        }


        public DLLNode getNext() {
            return next;
        }

    }

    // ~ Fields ................................................................
    private int size;
    private DLLNode head;
    private DLLNode tail;
    private DLLNode curr;

    // ----------------------------------------------------------
    /**
     * Create a new DoubleLL object.
     */
    // ~ Constructors ..........................................................
    public DoubleLL() {
        head = new DLLNode(null);
        tail = new DLLNode(null);
        curr = head;
        head.setNext(tail);
        tail.setPrev(head);
        size = 0;
    }

    // ~ Public Methods ........................................................


    // ----------------------------------------------------------
    /**
     * Appends an item to list
     * 
     * @param item
     *            item to add to list
     * @return true if added to list
     */
    public boolean append(Run item) {
        tail.setPrev(new DoubleLL.DLLNode(tail.getPrev(), item, tail));
        tail.getPrev().getPrev().setNext(tail.getPrev());
        size++;
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Removes an item from the list
     * 
     * @param item
     *            item to remove
     * 
     * @return true if item is removed
     */
    public boolean remove(Run item) {
        DoubleLL.DLLNode current = head.getNext();
        while (!current.equals(tail)) {
            // System.out.println("repeating");
            if (current.getData().equals(item)) {
                current.getPrev().setNext(current.getNext());
                current.getNext().setPrev(current.getPrev());
                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Gets the next item in list
     * 
     * @return next node in list
     */
    public Run next() {
        if (curr.getNext() != tail) {
            return curr.getNext().getData();
        }
        return curr.getData();

    }


    // ----------------------------------------------------------
    /**
     * Determine if list is empty
     * 
     * @return true if list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }


    // ----------------------------------------------------------
    /**
     * Return the amount of objects in list
     * 
     * @return size amount of objects in list
     */
    public int getSize() {
        return size;
    }


    // ----------------------------------------------------------
    /**
     * CLears the list
     */
    public void clear() {
        head = new DLLNode(null);
        tail = new DLLNode(null);
        head.setNext(tail);
        tail.setPrev(head);
        curr = head;
        size = 0;
    }


    // ----------------------------------------------------------
    /**
     * returns the first node's data
     * 
     * @return T the first node in list
     */
    public Run getFirst() {
        
        return head.getNext().getData();
    }


    // ----------------------------------------------------------
    /**
     * sets the current node as the head to begin traversal
     */
    public void resetCurr() {
        curr = head;
    }


    // ----------------------------------------------------------
    /**
     * gets the next data point in the list.
     * 
     * @return T the next node in list
     */
    public Run getNextNode() {
        if (curr.getNext().equals(tail)) {
            return null;
        }
        curr = curr.getNext();
        return curr.getData();
    }

}