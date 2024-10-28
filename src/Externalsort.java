/**
 * Project 3 - buffers
 */

/**
 * The class containing the main method.
 *
 * @author Ibrahim Shabani
 * @author Ivy Brungege
 * @version 10/28/2024
 */

// On my honor:
//
// - I have not used source code obtained from another student,
// or any other unauthorized source, either modified or
// unmodified.
//
// - All source code and documentation used in my program is
// either my original work, or was derived by me from the
// source code published in the textbook for this course.
//
// - I have not discussed coding details about this project with
// anyone other than my partner (in the case of a joint
// submission), instructor, ACM/UPE tutors or the TAs assigned
// to this course. I understand that I may discuss the concepts
// of this program with other students, and that another student
// may help me debug my program so long as neither of us writes
// anything during the discussion or modifies any computer file
// during the discussion. I have violated neither the spirit nor
// letter of this restriction.

import java.io.IOException;

// -------------------------------------------------------------------------
/**
 *  Runs the project
 * 
 *  @author Ibrahim Shabani
 *  @author Ivy Brundege
 *  @version Oct 28, 2024
 */
public class Externalsort {

    /**
     * @param args
     *     Command line parameters
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller(args[0]);
        controller.sort();
    }

}
