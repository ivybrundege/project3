
/**
 * 
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.io.File;

/**
 * Handles the sorting logic and file I/O
 * 
 * @author ivyb
 * @author shabanii
 * @version 10.27.24
 */
public class Controller {
    /**
     * Number of blocks we can read at a time
     */
    public static final int NUM_HEAP_BLOCKS = 8; // number of blocks in the heap
    // public static final int BLOCK_SIZE = 512; //number of records in a block
    private RandomAccessFile input; // input file
    private File runfile; // created run file
    private RandomAccessFile run; // random access run file
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
    public Controller(String inputname) throws IOException {
        init(inputname);
    }


    private void init(String inputname) throws IOException {
        /*
         * runfile = new File("run.txt");
         * input = new RandomAccessFile(inputname, "rw");
         * run = new RandomAccessFile("run.txt", "rw");
         * heap = buildHeap();
         * inBuffer = new BufferPool(input);
         * inBuffer.populate();
         * outBuffer = new BufferPool(run);
         */

        byte[] basicBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        ByteBuffer bb = ByteBuffer.wrap(basicBuffer);

        File inputfile = new File(inputname);
        input = new RandomAccessFile(inputfile, "r");
        input.seek(0);

        Record[] heapArray = new Record[NUM_HEAP_BLOCKS
            * ByteFile.RECORDS_PER_BLOCK];
        for (int block = 0; block < NUM_HEAP_BLOCKS; block++) {
            input.read(basicBuffer); // read in the next block
            bb.position(0); // goes to byte position zero in ByteBuffer
            for (int i = 0; i < ByteFile.RECORDS_PER_BLOCK; i++) { // iterate
                                                                   // through
                                                                   // records
                long recID = bb.getLong();
                double recKey = bb.getDouble();
                Record rec = new Record(recID, recKey);
                heapArray[i] = rec;
                // @TOOD: handle if there's not a full block
            }
        }
        heap = new MinHeap<Record>(heapArray, heapArray.length,
            heapArray.length);

    }


    /**
     * The overarching sort method
     * 
     * @throws IOException
     */
    public void sort() throws IOException {
        // step one: replacement sort
        replacementSort();
        input.close();
    }


    // ----------------------------------------------------------
    /**
     * replacement sort algorithm
     * @throws IOException
     */
    public void replacementSort() throws IOException {
        while (heap.heapSize() > 0) {
            Record next = heap.removeMin();
            if (!outBuffer.insert(next)) // if outbuffer is full
            {
                outBuffer.write();
                outBuffer.insert(next);
            }

            Record toInsert = inBuffer.nextRecord();
            if (toInsert != null) {
                heap.insert(inBuffer.nextRecord());
                inBuffer.read(); // add next value if it exists.
            }
        }
    }
}
