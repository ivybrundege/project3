
/**
 * 
 */

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Handles the reading + writing logic
 * Buffer array acts as a queue so that we can add and remove values.
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
        maxSize = Controller.BLOCK_SIZE + 1; // 1 extra spot allocated for
                                             // circular queue
        buffer = new Record[maxSize];
        rear = 0;
        front = 1;
    }


    /**
     * Reads the next record from the file
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


    public void write() throws IOException {
        Record r = nextRecord();
        while (r != null) {
            file.writeLong(r.getID());
            file.writeDouble(r.getKey());
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
s     */
    public Record nextRecord() {
        if (length() == 0)
            return null;
        Record r = buffer[front];
        front = (front + 1) % maxSize; // Circular increment
        return r;
    }


    public int length() {
        return ((rear + maxSize) - front + 1) % maxSize;
    }

    /**
     * inserts the given record -- analogous to enqueue
     * @param record
     * @return
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
