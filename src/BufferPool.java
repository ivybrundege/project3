
/**
 * buffer array that we load stuff into before heap placement
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Handles the reading + writing logic
 * Buffer array acts as a queue so that we can add and remove values.
 * Queue logic from open DSA.
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
    private byte[] basicBuffer; //buffer before input/output to memory
    private ByteBuffer bb; // bytebuffer wrapper for basic buffer

    /**
     * constructor
     * 
     * @param source
     *            The file being accessed
     */
    public BufferPool(RandomAccessFile source) {
        file = source;
        maxSize = ByteFile.RECORDS_PER_BLOCK + 1; // 1 extra spot allocated for
        init(); // circular queue
        //initialize bb for write here so that we can seek 0 for bb?
    }


    /**
     * initializes/reinitializes the buffer queue
     */
    private void init() {
        buffer = new Record[maxSize];
        rear = 0;
        front = 1;
        
        basicBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        bb = ByteBuffer.wrap(basicBuffer);
        bb.position(0);
    }


    /**
     * Reads the next record from the file
     * 
     * @TODO: block level, not record level
     * 
     * @return whether or not the record was read.
     */
    public int read() {
        init(); //set up byte buffer + buffer array to empty
        int count = 0;
        try {
            file.read(basicBuffer);
        }
        catch (IOException e) {
            return -1; // end of file
        }
        while (bb.hasRemaining()) {
            long recID = bb.getLong();
            double recKey = bb.getDouble();
            Record rec = new Record(recID, recKey);
            enqueue(rec);
            count++;
        }
        return count;

    }


    // ----------------------------------------------------------
    /**
     * Writes the (output) buffer to the file
     * 
     * @throws IOException
     * @TODO: keep track of where we are in file
     */
    public void write(int counted) throws IOException {
        // set up byte buffer-- holds everything until mem write
        basicBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        bb = ByteBuffer.wrap(basicBuffer);
        bb.position(0);

        // get first record + print it to console
        Record r = dequeue();
        System.out.print(r.getID() + " ");
        System.out.print(r.getKey() + " ");
        if (counted % 5 == 0)
        {
            System.out.println();
        }
        while (r != null) {
            bb.putLong(r.getID());
            bb.putDouble(r.getKey());
            r = dequeue();
        }
        file.write(basicBuffer);
    }


    /**
     * returns the next record and removes it from the buffer
     * analogous to dequeue operation
     * 
     * @return The removed record
     * 
     */
    public Record dequeue() {
        if (length() == 0)
            return null;
        Record r = buffer[front];
        front = (front + 1) % maxSize; // Circular increment
        return r;
    }


    // ----------------------------------------------------------
    /**
     * get length of buffer array
     * 
     * @return The length of the queue/buffer
     */
    public int length() {
        return ((rear + maxSize) - front + 1) % maxSize;
    }


    /**
     * inserts the given record -- analogous to enqueue
     * 
     * @param record
     *            The record to enqueue
     * @return true if inserted
     */
    public boolean enqueue(Record record) {
        
        if (((rear + 2) % maxSize) == front) {
            return false; // array's full-- can't add yet.
        }
        rear = (rear + 1) % maxSize; // Circular increment

        buffer[rear] = record;
        return true;
    }


    /**
     * returns whether or not the buffer is full
     * 
     * @return True if buffer is full
     */
    public boolean isFull() {
        return length() == maxSize - 1;
    }


    /**
     * returns whether or not the buffer is empty
     * 
     * @return True if buffer is empty
     */
    public boolean isEmpty() {
        return length() == 0;
    }

}
