
/**
 * 
 */

import student.TestCase;

/**
 * 
 */
public class BufferPoolTest extends TestCase {
    private BufferPool buff;
    private Record r1;
    private Record r2;

    /**
     * setup
     */
    public void setUp() {
        long id = 10;
        double key = 1.4;
        buff = new BufferPool(null);
        r1 = new Record(id, key);
        r2 = new Record(id, key);

    }


    /**
     * tests enqueue and dequeue
     */
    public void testBuffer() {
        assertTrue(buff.isEmpty());
        for (int i = 0; i < ByteFile.RECORDS_PER_BLOCK - 1; i++) {
            buff.enqueue(r1);
            assertFalse(buff.isEmpty());
            assertFalse(buff.isFull());
            assertEquals(buff.length(), i + 1);
        }
        buff.enqueue(r1);
        assertFalse(buff.isEmpty());
        assertTrue(buff.isFull());
        assertEquals(buff.length(), 512);

        for (int i = ByteFile.RECORDS_PER_BLOCK - 1; i > 0; i--) {
            Record r = buff.dequeue();
            assertFalse(buff.isEmpty());
            assertFalse(buff.isFull());
            assertEquals(buff.length(), i);
            assertEquals(r, r1);
        }
        buff.dequeue();
        System.out.println(buff.length());

        assertTrue(buff.isEmpty());
    }
}
