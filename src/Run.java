/**
 * 
 */

/**
 * Tracks the length of each run in the file.
 * 
 * @author ivyb
 * @author shabanii
 * @version 10.29.24
 * 
 */
public class Run {
    private int start; // starting point ?
    private int numRecords; // number of records in the given run

    /**
     * constructor for the run class
     * 
     * @param startingIndex
     *            The relative starting location of this run
     */
    public Run(int startingIndex) {
        start = startingIndex;
        numRecords = 0;
    }


    /**
     * increments the number of records field
     */
    public void addRec() {
        numRecords++;
    }


    /**
     * returns the start
     * 
     * @return The start field
     */
    public int getStart() {
        return start;
    }


    /**
     * returns the number of records
     * 
     * @return The numrecords field
     */
    public int getNumRecords() {
        return numRecords;
    }

}
