
/**
 * 
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;

/**
 * Handles the sorting logic and file I/O
 * 
 * @author ivyb
 * @author shabanii
 * @version 10.27.24
 */
public class Controller {
    public static final int NUM_HEAP_BLOCKS = 8; //number of blocks in the heap
    public static final int BLOCK_SIZE = 512; //number of records in a block
    private RandomAccessFile input; //input file
    private File runfile; //created run file
    private RandomAccessFile run; //random access run file
    private MinHeap<Record> heap; // 8 block heap
    private BufferPool inBuffer;
    private BufferPool outBuffer;
     

    /**
     * Constructor - initializes files for r+w
     * 
     * @param inputname
     *            The input
     * @throws IOException 
     * @TODO can i do filenotfound exceptions
     * @TODO do i need to specify run file size
     */
    public Controller(String inputname) throws IOException 
    {
        init(inputname);
    }
    
    private void init(String inputname) throws IOException
    {
        runfile = new File("run.txt");
        input = new RandomAccessFile(inputname, "rw");
        run = new RandomAccessFile("run.txt", "rw");
        heap = buildHeap();
        inBuffer = new BufferPool(input);
        inBuffer.populate();
        outBuffer = new BufferPool(run);
    }
    
    
    /**
     * Reads the first 8 blocks into the heap. 
     * @throws IOException
     */
    private MinHeap<Record> buildHeap() throws IOException
    {
        Record[] records = new Record[NUM_HEAP_BLOCKS * BLOCK_SIZE];
        for (int i = 0; i < records.length; i++) //while we're not at capacity
        {
            long ID = input.readLong();
            double key = input.readDouble();
            records[i] = new Record(ID, key);
        }
        return(new MinHeap<Record>(records, records.length, records.length));
    }
    
    /**
     * The overarching sort method
     * @throws IOException 
     */
    public void sort() throws IOException
    {
        //step one: replacement sort
        replacementSort();
    }
    
    public void replacementSort() throws IOException
    {
        while (heap.heapSize() > 0)
        {
            Record next = heap.removeMin();
            if (!outBuffer.insert(next)) //if outbuffer is full
            {
                outBuffer.write();
                outBuffer.insert(next);
            }
            
            Record toInsert = inBuffer.nextRecord();
            if (toInsert != null)
            {
                heap.insert(inBuffer.nextRecord());
                inBuffer.read(); //add next value if it exists.
            }
        }
    }
}
