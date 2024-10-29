
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
    private RandomAccessFile run; // random access run file
    private MinHeap<Record> heap; // 8 block heap
    private BufferPool inBuffer;
    private BufferPool outBuffer;
    private int runs = 1; //ok. ultimately this will be a list but that's a lotta work for milestone 1

    /**
     * Constructor - initializes files for r+w
     * 
     * @param inputname
     *            The input
     * @throws IOException
     * @TODO do i need to specify run file size
     */
    public Controller(String inputname) throws IOException {
        init(inputname);
    }


    private void init(String inputname) throws IOException { 
        //set up input
        byte[] basicBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        ByteBuffer bb = ByteBuffer.wrap(basicBuffer);
        File inputfile = new File(inputname);
        input = new RandomAccessFile(inputfile, "rw");
        input.seek(0);

        //create heap
        Record[] heapArray = new Record[NUM_HEAP_BLOCKS
            * ByteFile.RECORDS_PER_BLOCK];
        for (int block = 0; block < NUM_HEAP_BLOCKS; block++) {
            input.read(basicBuffer); // read in the next block
            bb.position(0); // goes to byte position zero in ByteBuffer
            for (int i = 0; i < ByteFile.RECORDS_PER_BLOCK; i++) { // iterate
                long recID = bb.getLong();
                double recKey = bb.getDouble();
                Record rec = new Record(recID, recKey);
                heapArray[i + block * ByteFile.RECORDS_PER_BLOCK] = rec;
                // @TOOD: handle if there's not a full block
            }
        }
        heap = new MinHeap<Record>(heapArray, heapArray.length,
            heapArray.length);
        
        //set up run file
        File runfile = new File("run.txt");
        run = new RandomAccessFile(runfile, "rw");
        run.seek(0);
        
        //set up buffers
        inBuffer = new BufferPool(input);
        outBuffer = new BufferPool(run);
        
    }


    /**
     * The overarching sort method
     * 
     * @throws IOException
     */
    public void sort() throws IOException {
        // step one: replacement sort
        replacementSort();
        //@TODO: multiple runs. ignoring that for first milestone
        //mergeSort();
        input.close();
        run.close();
    }


    // ----------------------------------------------------------
    /**
     * replacement sort algorithm
     * 
     * @throws IOException
     * @TODO multiple runs?? potentially keep a list of removed values
     */
    public void replacementSort() throws IOException {
        int recordsCounted = 0;
        while (heap.heapSize() != 0) {
            
            // first, add minimum heap value to output buffer
            Record toOutput = heap.removeMin();
            outBuffer.enqueue(toOutput);
            if (outBuffer.isFull()) {
                recordsCounted++;
                outBuffer.write(recordsCounted);
            }

            // then, check to see if we can add something from input
            if (inBuffer.isEmpty())
            {
                inBuffer.read();
            }
            
            Record toHeap = inBuffer.dequeue();
            if (toHeap != null) {
                if (toHeap.compareTo(toOutput) < 0) 
                {
                    heap.insert(toHeap);
                    heap.removeMin(); // umm need to keep track of these somehow
                }
                else // add input to heap
                {
                    heap.insert(toHeap);
                }
            }
        }
        if (!outBuffer.isEmpty()) {
            outBuffer.write(recordsCounted); // write all remaining values
        }
    }
    
    public void mergeSort() throws IOException
    {
        //first-- swap input and output files
        RandomAccessFile temp = input;
        input = run;
        run = temp;
        run.seek(0); //set to beginning- we can now overwrite what's there i think
        input.seek(0); //set to beginning
        
        inBuffer = new BufferPool(input);
        outBuffer = new BufferPool(run);
        
        //create working area-- for now we only have 1 run so im gonna ignore the rest
        Record[] sortSpace = new Record[runs * ByteFile.RECORDS_PER_BLOCK]; 
        
    }
}
