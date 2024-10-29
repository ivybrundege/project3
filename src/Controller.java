
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
    private RandomAccessFile input; // input file
    private RandomAccessFile runFile; // random access run file
    private MinHeap<Record> heap; // 8 block heap
    private BufferPool inBuffer; //input buffer
    private BufferPool outBuffer; //output buffer
    private DoubleLL runs; //holds the runs for a given sorting round

    /**
     * Constructor - initializes files for r+w
     * 
     * @param inputname
     *            The input
     * @throws IOException
     * 
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
        runFile = new RandomAccessFile(runfile, "rw");
        runFile.seek(0);
        
        //set up buffers
        inBuffer = new BufferPool(input);
        outBuffer = new BufferPool(runFile);
        
    }


    /**
     * The overarching sort method
     * 
     * @throws IOException
     */
    public void sort() throws IOException {
        // step one: replacement sort
        runs = new DoubleLL();
        Run curr = new Run(0);
        int hidden = replacementSort(curr);
        runs.append(curr);
        for (int i = heap.getCapacity() - 1; i >= hidden; i++) //iterate thru hidden
        {
            Record r = heap.getPos(i);
            inBuffer.enqueue(r);
        }
        while (!inBuffer.isEmpty()) //while input remains
        {
            curr = new Run(curr.getNumRecords() + curr.getStart()); //new run
            hidden = replacementSort(curr);
        }
        
        //@TODO: multiple runs. ignoring that for first milestone
        //mergeSort();
        input.close();
        runFile.close();
    }


    // ----------------------------------------------------------
    /**
     * replacement sort algorithm
     * 
     * @throws IOException
     * @TODO multiple runs?? potentially keep a list of removed values
     */
    public int replacementSort(Run curr) throws IOException {
        int recordsCounted = 0; // prints 5 records/line
        int recordsHidden = 0; //hidden records to add after
        while (heap.heapSize() != 0) {
            
            // first, add minimum heap value to output buffer
            Record toOutput = heap.removeMin();
            outBuffer.enqueue(toOutput);
            curr.addRec(); //increment run
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
                    heap.removeMin(); 
                    recordsHidden = heap.hide();
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
        return recordsHidden;
    }
    
    public void mergeSort() throws IOException
    {
        //first-- swap input and output files
        RandomAccessFile temp = input;
        input = runFile;
        runFile = temp;
        runFile.seek(0); //set to beginning- we can now overwrite what's there i think
        input.seek(0); //set to beginning
        
        inBuffer = new BufferPool(input);
        outBuffer = new BufferPool(runFile);
                
        
    }
}
