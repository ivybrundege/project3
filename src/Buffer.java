/**
 * 
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * basically just a shell for a byte buffer. 
 * @author ivyb
 * @author shabanii
 */
public class Buffer {
    private byte[] buffer;
    private ByteBuffer bb;
    private File f;
    private RandomAccessFile file;
    private int size;
    private int rowCount;
    
    
    /**
     * constructor -- for the input buffer
     * @param filename
     * @throws IOException
     */
    public Buffer(String filename) throws IOException
    {
        buffer = new byte[ByteFile.BYTES_PER_BLOCK]; //stores the entire block. 
        bb = ByteBuffer.wrap(buffer); //stores as byte buffer
        bb.position(0); //start at beggining
        f = new File(filename);
        file = new RandomAccessFile(f, "rw");
        file.seek(0); //start at beginning
        //readBlock();
        size = 0;
        //ok so now we have a fully loaded buffer. slay.
        rowCount = 0; 
    }
    
    /**
     * constructor for the write buffer.
     * @throws IOException
     */
    public Buffer() throws IOException
    {
        buffer = new byte[ByteFile.BYTES_PER_BLOCK]; //stores the entire block. 
        bb = ByteBuffer.wrap(buffer); //stores as byte buffer
        bb.position(0); //start at beggining
        f = new File("run.bin");
        f.delete(); //ensure file is empty
        file = new RandomAccessFile(f, "rw"); 
        file.seek(0); //start at beginning
        size = 0;
        rowCount = 0;
    }

    public boolean readBlock() throws IOException
    {
        rowCount++;
        if (file.getFilePointer() == file.length())
        {
            return false; //end of file
        }
        bb.clear();
        file.read(buffer);
        bb.position(0); //start at beginning of our buffer.
        size = ByteFile.RECORDS_PER_BLOCK;
        int pos = bb.position();
        long id = bb.getLong();
        double key = bb.getDouble();
        Record r = new Record(id, key);
        System.out.print(r.getID() + " " + r.getKey() + " ");
        
        if (rowCount % 5 == 0)
        {
            System.out.println();
        }
        bb.position(pos); //revert to previous position
        return true;
    }
    
    public Record getRecord() throws IOException
    {
        if (!bb.hasRemaining()) //if buffer empty
        {
            if(!readBlock()) //attempt to refill
            {
                return null; //end of file
            }
        }
        long id = bb.getLong();
        double key = bb.getDouble();
        size--;
        return (new Record(id, key));
    }
    
    public void addRecord(Record record) throws IOException
    {
        long id = record.getID();
        double key = record.getKey();
        bb.putLong(id);
        bb.putDouble(key);
        size++;
        if (size == ByteFile.RECORDS_PER_BLOCK)
        {
            //rowCount++;
            write(id, key);
        }
    }
    
    public void write(long id, double key) throws IOException
    {
        file.write(buffer);
        bb.clear(); 
        size = 0;
    }
    
    public void closeBuffer() throws IOException
    {
        file.close();
    }
    
    public boolean isEmpty()
    {
        return !bb.hasRemaining();
    }
    
    public void rename(String name)
    {
        f.renameTo(new File(name));
    }
}
