/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frequencytable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Examine autogenerated files from Mallet execution, generate frequency matrix
 *
 * @author Clarky
 */
public class LDAFrequency {

    // Number of header lines to skip when reading file
    private static final int SKIP_LINES = 3;

    // Relative path to file location 
    private static final String FILE_PATH = "../LDA/mallet/topic-state.gz";

    // Reader object
    private static BufferedReader reader;

    // Data structure to store all information
    // HashMap<UserID, HashMap<TopicID, HashMap<WordID, HashMap<WordFreq, WordDist>>>>
    private static HashMap<String, HashMap<String, HashMap<String, Integer>>> users;

    // Writer object to output resultant structure to text file
    private static PrintWriter writer;

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        // Grab and wrap .gz file
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(FILE_PATH))));

        // Instantiate HashMap structure for user/topic/word information
        users = new HashMap<>();

        // Instantiate PrintWriter with output file name and charset
        writer = new PrintWriter("output.txt", "UTF-8");

        // Skip over header lines (straight to relevant data)
        for (int i = 0; i < SKIP_LINES; i++) {
            reader.readLine();
        }

        // Insert data into structure
        populateStructure();

        // Iterate through and calculate statistics of entire structure
        analyseStructure();

    }

    /**
     * Iterate through entire structure and calculate various statistical values
     */
    private static void analyseStructure() {

        // Iterate through all users in structure
        for (Map.Entry userEntry : users.entrySet()) {
            String userID = (String) userEntry.getKey();
            HashMap<String, HashMap<String, Integer>> userVal
                    = (HashMap<String, HashMap<String, Integer>>) userEntry.getValue();

            // Iterate through all user's topics in structures
            for (Map.Entry topicEntry : userVal.entrySet()) {
                String topicID = (String) topicEntry.getKey();
                HashMap<String, Integer> topicVal
                        = (HashMap<String, Integer>) topicEntry.getValue();

                // Find the total number of words in given topic
                Integer sumAll = 0;
                for (Map.Entry<String, Integer> e : topicVal.entrySet()) {
                    sumAll += e.getValue();
                }

                // Write header information to file
                writer.println("USER: " + userID + " TOPIC: " + topicID);
                writer.println("USERID  TOPICID WORDID  WORDFRQ WORDDST");

                // Iterate through all user's topic's words in structure
                for (Map.Entry wordEntry : topicVal.entrySet()) {
                    String wordID = (String) wordEntry.getKey();
                    Integer wordFreq = (Integer) wordEntry.getValue();

                    // Calculate the distribution of each word within the topic
                    Double wordDist = (double) wordFreq / (double) sumAll;

                    // Write word information to file
                    writer.println(userID + "\t" + topicID + "\t"
                            + wordID + "\t" + wordFreq + "\t" + wordDist);
                }
                
                // Whitespace for output readability
                writer.println();
            }
        }
    }

    /**
     * Read through parameterised file and programmatically insert values into data structure
     *
     * @throws IOException
     */
    private static void populateStructure() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {

            // Split on space character
            String parts[] = line.split(" ");

            // The user's ID
            String user = parts[1].substring((parts[1].lastIndexOf('\\') + 1),
                    parts[1].lastIndexOf('.'));

            // The unique ID of the word
            String typeIndex = parts[3];

            // The word itself
            // String wordText = parts[4];

            // The unique ID of the topic the word belongs to
            String topicId = parts[5];

            // If the user does not yet exist in the structure, add them
            if (!users.containsKey(user)) {
                users.put(user, new HashMap<String, HashMap<String, Integer>>());
            }

            // If the topic does not yet exist in the structure, add it
            if (!(users.get(user).containsKey(topicId))) {
                users.get(user).put(topicId, new HashMap<String, Integer>());
            }

            // If the word does not yet exist in the structure, add it
            if (!(users.get(user).get(topicId).containsKey(typeIndex))) {
                users.get(user).get(topicId).put(typeIndex, 0);
            }

            // Increment the counter of occurences of the word
            users.get(user).get(topicId).put(typeIndex,
                    users.get(user).get(topicId).get(typeIndex) + 1);
        }
    }
}