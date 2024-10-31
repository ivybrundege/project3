
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
    private BufferPool inBuffer; // input buffer
    private BufferPool outBuffer; // output buffer
    private DoubleLL runs; // holds the runs for a given sorting round
    private File runf;

    /**
     * Constructor - initializes files for r+w
     * 
     * @param inputname
     *            The input
     * @throws IOException
     * 
     */
    public Controller(String inputname) throws IOException {
        byte[] basicBuffer = new byte[ByteFile.BYTES_PER_BLOCK];
        ByteBuffer bb = ByteBuffer.wrap(basicBuffer);
        File inputfile = new File(inputname);
        input = new RandomAccessFile(inputfile, "rw");
        input.seek(0);
        init(basicBuffer, bb);
    }


    private void init(byte[] basicBuffer, ByteBuffer bb) throws IOException {
        // create heap
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
            }
        }
        heap = new MinHeap<Record>(heapArray, heapArray.length,
            heapArray.length);

        // set up run file
        runf = new File("run.txt");
        runFile = new RandomAccessFile(runf, "rw");
        runFile.seek(0);

        // set up buffers
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
        int start = 0;
        inBuffer.read(); //get input started
        while (!inBuffer.isEmpty())
        {
            Run curr = replacementSort(start);
            runs.append(curr);
            start = curr.getNumRecords() + curr.getStart();
        }
        //one final replacementSort to empty heap.
        runs.append(replacementSort(start));
        
        //one final write just to b sure -- @TODO replace 0 w something meaningful idk.
        outBuffer.write(0);
        
        
        // next step: merge sort :D
        // mergeSort();
        input.close();
        runFile.close();
        runf.delete();
    }


    // ----------------------------------------------------------
    /**
     * replacement sort algorithm
     * 
     * @throws IOException
     */
    public Run replacementSort(int start) throws IOException {
        int recordsCounted = 0; // prints 5 records/line
        Run curr = new Run(start);
        int hidden = 0;
        System.out.println("next run");
        int j = 0;
        while (heap.heapSize() != 0)
        {
            
            //1. refill buffer if needed
            if (inBuffer.isEmpty())
            {
                inBuffer.read();
            }
            
            //2. output next value
            Record toWrite = heap.removeMin();
            outBuffer.enqueue(toWrite);
            curr.addRec(); //increment run
            if (outBuffer.isFull()) 
            {
                recordsCounted++;
                outBuffer.write(recordsCounted);
            }
            
            //3. Add next from input 
            if (!inBuffer.isEmpty()) // could still be empty if we're at end of file
            {
                Record toHeap = inBuffer.dequeue();
                if (toHeap == null)
                {
                    System.out.println("catch");
                }
                if (toHeap.compareTo(toWrite) < 0)
                {
                    heap.insert(toHeap);
                    heap.removeMin(); //hide for next round
                    hidden++;
                }
                else
                {
                    heap.insert(toHeap);
                }
            }
            //continue until heap runs out
        }
        
        System.out.println("\n HIDDEN: " + hidden + "\n");
        System.out.println("heap size: " + heap.heapSize());
        //once heap is empty: @TODO check cause wtf is this.
        int ogSize = heap.heapSize();
        for (int i = 0; i < hidden - 2; i++)
        {
            heap.insert(heap.getPos(ogSize + i));
        }
        System.out.println("finished repopulating");
        return curr;
    }


    public void mergeSort() throws IOException {
        // first-- swap input and output files
        RandomAccessFile temp = input;
        input = runFile;
        runFile = temp;
        runFile.seek(0); // set to beginning- we can now overwrite what's there
                         // i think
        input.seek(0); // set to beginning

        inBuffer = new BufferPool(input);
        outBuffer = new BufferPool(runFile);

    }

    
}
