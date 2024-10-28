
/**
 * buffer array that we load stuff into before heap placement
 */

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Handles the reading + writing logic
 * Buffer array acts as a queue so that we can add and remove values.
 * 
 * @author ivyb
 * @author shabanii
 * @version 10.28.24
 */
public class BufferPool {
    private RandomAccessFile file;
    private Record[] buffer;
    private int maxSize; // Maximum size of queue
    private int front; // Index of front element
    private int rear;

    /**
     * constructor
     * 
     * @param source
     *            The file being accessed
     */
    public BufferPool(RandomAccessFile source) {
        file = source;
        maxSize = ByteFile.RECORDS_PER_BLOCK + 1; // 1 extra spot allocated for
                                             // circular queue
        buffer = new Record[maxSize];
        rear = 0;
        front = 1;
    }


    /**
     * Reads the next record from the file
     * @TODO: block level, not record level
     * 
     * @return whether or not the record was read.
     */
    public boolean read() {
        long id;
        double key;
        try {
            id = file.readLong();
            key = file.readDouble();
        }
        catch (IOException e) {
            // end of file
            return false;
        }

        Record r = new Record(id, key);
        return insert(r);

    }

    /**
     * Writes the current records to the given file
     * @throws IOException
     */
    public void write() throws IOException {
        Record r = nextRecord();
        System.out.println(r.getID());
        System.out.println(r.getKey());
        while (r != null) {
            file.writeLong(r.getID());
            file.writeDouble(r.getKey());
        }

    }
    
    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @throws IOException
     */
    public void writeToConsole() throws IOException {
        Record r = nextRecord();
        while (r != null) {
            System.out.println(r.getID());
            System.out.println(r.getKey());
        }
    }


    /**
     * populates the whole buffer
     */
    public void populate() {
        boolean moreValues = read();
        while (moreValues) {
            moreValues = read();
            // while we have space in the buffer and file, read in more
        }
    }


    /**
     * returns the next record and removes it from the buffer
     * analogous to dequeue operation
     * 
     * @return The removed record
     *         s
     */
    public Record nextRecord() {
        if (length() == 0)
            return null;
        Record r = buffer[front];
        front = (front + 1) % maxSize; // Circular increment
        return r;
    }


    // ----------------------------------------------------------
    /**
     * get length of buffer array
     * @return
     */
    public int length() {
        return ((rear + maxSize) - front + 1) % maxSize;
    }

    // implement read block function (randaccfile object, int position to read)
    // return byte

    /**
     * inserts the given record -- analogous to enqueue
     * 
     * @param record
     * @return true if inserted
     */
    public boolean insert(Record record) {
        if (((rear + 2) % maxSize) == front) {
            return false; // array's full-- can't add yet.
        }
        rear = (rear + 1) % maxSize; // Circular increment
        buffer[rear] = record;
        return true;
    }

}
