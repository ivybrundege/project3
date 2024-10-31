
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
                // @TOOD: handle if there's not a full block
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
        inBuffer.read();

        while (!inBuffer.isEmpty() || heap.heapSize() != 0) // while there's
                                                            // still file left
                                                            // to read.
        {
            Run curr = replacementSort(start);
            runs.append(curr); // add next run
            start = curr.getStart() + curr.getNumRecords(); // inc start.
        }
        replacementSort(start); // one final one to empty heap. also idk if this
                                // one is necessary but that's none of my
                                // business.

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

        while (!inBuffer.isEmpty() && heap.heapSize() != 0) {
            // 1. Write minimum to output file
            Record toWrite = heap.removeMin();
            recordsCounted++;
            curr.addRec(); // increment this run
            outBuffer.enqueue(toWrite);
            if (outBuffer.isFull()) {
                outBuffer.write(recordsCounted);
            }

            // 2. Add next input into heap
            Record toHeap = inBuffer.dequeue();
            if (toHeap.compareTo(toWrite) < 0) // toHeap < toWrite
            {
                heap.insert(toHeap);
                heap.removeMin(); // store as hidden.
            }
            else // valid to add to heap and continue this run
            {
                heap.insert(toHeap); // just add to the heap.
            }
            if (inBuffer.isEmpty()) {
                // System.out.println("reading in");
                inBuffer.read();
            }
        }

        // first- keep track of current heap size. this will be used to
        // determine hidden values that need to be added to next run.
        int hidden = heap.heapSize();

        // now, empty the heap into output
        while (heap.heapSize() != 0) {
            Record toWrite = heap.removeMin();
            outBuffer.enqueue(toWrite);
            if (outBuffer.isFull()) {
                recordsCounted++;
                outBuffer.write(recordsCounted);
            }
        }

        // carry any hidden values into the next run.
        for (int i = hidden; i < heap.getCapacity(); i++) {
            heap.insert(heap.getPos(i)); // heap.removeMin()?
        }

        // one final write: for whatever's left?? or would i not do that till
        // the next time idk
        // outBuffer.write(recordsCounted);

        // return this run
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

    /*
     * {
     * while (heap.heapSize() != 0) {
     * 
     * // first, add minimum heap value to output buffer
     * Record toOutput = heap.removeMin();
     * outBuffer.enqueue(toOutput);
     * curr.addRec(); // increment run
     * if (outBuffer.isFull()) {
     * recordsCounted++;
     * outBuffer.write(recordsCounted);
     * }
     * 
     * // then, check to see if we can add something from input
     * if (inBuffer.isEmpty()) {
     * inBuffer.read();
     * }
     * 
     * Record toHeap = inBuffer.dequeue();
     * if (toHeap != null) {
     * if (toHeap.compareTo(toOutput) < 0) {
     * heap.insert(toHeap);
     * heap.removeMin();
     * }
     * else // add input to heap
     * {
     * heap.insert(toHeap);
     * }
     * }
     * }
     * if (!outBuffer.isEmpty()) {
     * outBuffer.write(recordsCounted); // write all remaining values
     * }
     * }
     */
}
