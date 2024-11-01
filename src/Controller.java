
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
    //private RandomAccessFile input; // input file
    //private RandomAccessFile runFile; // random access run file
    private MinHeap<Record> heap; // 8 block heap
    //private BufferPool inBuffer; // input buffer
    //private BufferPool outBuffer; // output buffer
    private DoubleLL runs; // holds the runs for a given sorting round
    //private File runf;
    private String fname;

    
    private Buffer inBuffer;
    private Buffer outBuffer;
    /**
     * Constructor - initializes files for r+w
     * 
     * @param inputname
     *            The input
     * @throws IOException
     * 
     */
    public Controller(String inputname) throws IOException {
        fname = inputname;
        inBuffer = new Buffer(inputname);
        outBuffer = new Buffer();
        buildHeap();
    }
    
    private void buildHeap() throws IOException
    {
        Record[] heapArray = new Record[NUM_HEAP_BLOCKS * ByteFile.RECORDS_PER_BLOCK];
        for (int i = 0; i < NUM_HEAP_BLOCKS; i++)
        {
            inBuffer.readBlock();
            for (int j = 0; j < ByteFile.RECORDS_PER_BLOCK; j++)
            {
                heapArray[j + 512 * i] = inBuffer.getRecord();
            }
        }
        heap = new MinHeap<Record>(heapArray, heapArray.length, heapArray.length);
    }

    /**
     * Replacement sort-- takes fully unsorted file + sorts into runs.
     * @param start
     * @return
     * @throws IOException
     */
    public Run replacementSort(int start) throws IOException
    {
        Run curr = new Run(start);
        int hidden = 0;
        while (heap.heapSize() != 0)
        {
            //1. Remove current minimum from heap
            Record toWrite = heap.removeMin();
            outBuffer.addRecord(toWrite); //also handles writing to file.
            curr.addRec(); //increment current run
            
            //2. get next input value
            Record toHeap = inBuffer.getRecord();
            if (toHeap != null)
            {
                if (toHeap.compareTo(toWrite) < 0)
                {
                    //then hide.
                    heap.insert(toHeap);
                    heap.removeMin();
                    hidden++;
                }
                else
                {
                    heap.insert(toHeap);
                }
            }
        }
        //once heap is empty:
        int ogSize = heap.heapSize();
        for (int i = 0; i < hidden; i++)
        {
            heap.insert(heap.getPos(ogSize + i));
        }
        return curr;
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
        
        while (inBuffer.readBlock() || !inBuffer.isEmpty()) //while file/buffer remains
        {
            Run curr = replacementSort(start);
            runs.append(curr);
            start = curr.getStart() + curr.getNumRecords(); //increment start
        }
        runs.append(replacementSort(start)); //one final one to empty heap.
        
        //mergesort
        
        //then switch run and input files.
        inBuffer.rename("placeholder");
        outBuffer.rename(fname);
        inBuffer.rename("./F24P3ExternalSorting/run.bin");
    }

    
}
