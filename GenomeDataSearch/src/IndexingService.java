import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Indexing service that is responsible for:
 *  - Reading and parsing the genome input file one line at a time
 *  - Creating the indexes for search in memory
 *  - Storing the genome array in a RandomAccessFile, genome.data, which is persistent in Java with constant random access time O(1)
 * 
 * @author maryammoslemi
 * 
 */
public class IndexingService {

	// The number of bytes for each row of data that represent a point in the
	// input file, 100 chars in this example
	private static final int BLOCK_SIZE = 100;
	
	protected static final String GENOME_DATA = "./data/genome.data";
	
	// In memory index files created for each genome. The idea is that we use chromosome column as the first key then we have use a treemap one for start positions
	// and one for end positions. Since these values are sorted already, and we want to leverage that fact at retrieval we store them in a Treemap. 
	//Each key,value pair in  the Treemap represents a (start/end, block address) in the RandomAccessFile that holds the values. Basically it is a pointer to the data location on disk
	public static Map<String, TreeMap<Integer, Integer>> startIndexes = new HashMap<String, TreeMap<Integer, Integer>>();
	public static Map<String, TreeMap<Integer, Integer>> endIndexes = new HashMap<String, TreeMap<Integer, Integer>>();

	public void buildIndex(String inputFileName) {
		
		BufferedReader br;
		RandomAccessFile dataFile;
		
		// Start reading and building the index from the first chromosome
		String currentChr = "chr1";

		try {

			br = new BufferedReader(new FileReader(inputFileName));
			dataFile = new RandomAccessFile(GENOME_DATA, "rw");

			String sCurrentLine;
			// Discard the first line containing header elements
			br.readLine();

			// The row number in the input file
			int rowNum = 0;
			
			// content of each column in the input file for the genome being processed which are chromosome, start, end, and value
			String[] columns;
			int rowNumBefore = 0;

			while ((sCurrentLine = br.readLine()) != null) {
				columns = sCurrentLine.split("\t");
				
				// Print info lines displaying which chromosome we are processing for indexing
				if (!currentChr.equals(columns[0])) {
					System.out.println(String.format("%d rows indexed and stored for chromosome = %s ....", (rowNum - rowNumBefore), currentChr));
					currentChr = columns[0];
					rowNumBefore = rowNum;
				}
				// Update in memory index that we are building as we are going through the data 
				updateIndex(Integer.parseInt(columns[1]),
						   Integer.parseInt(columns[2]), columns[0],
						   getDataAddress(rowNum));

				updateRandomAccessDataFile(dataFile, getDataAddress(rowNum), sCurrentLine);
				rowNum++;
			}
			
			br.close();
			dataFile.close();

		} catch (IOException e) {
			e.printStackTrace();

		} 
	}
	
	private void updateIndex(int startPosition, int endPosition, String chr,
			int dataAddress) throws IOException {
		
		updateAddressMap(chr, startPosition, dataAddress, startIndexes);
		updateAddressMap(chr, endPosition, dataAddress, endIndexes);
	}

	/**
	 * Updates the Treemap associated with this Chromosome
	 * 
	 */
	private void updateAddressMap(String chr, int position, int dataAddress,
			Map<String, TreeMap<Integer, Integer>> mapping) {
	
		TreeMap<Integer, Integer> addresses = mapping.get(chr);
		// Create if this is the first time
		if (addresses == null) {
			mapping.put(chr, new TreeMap<Integer, Integer>());
			addresses = mapping.get(chr);
		}
		// Update
		addresses.put(position, dataAddress);
		mapping.put(chr, addresses);
	}

	
	private void updateRandomAccessDataFile(RandomAccessFile fileStore,
			long dataAddress, String record) {
		try {

			// moves file pointer to the location we want to store the data
			fileStore.seek(dataAddress);
			// Writing all the characters separate them by a line break for clarity purpose on the output
			fileStore.writeChars(record + "\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The data is sorted by start and end position so the idea is that we store each row that we read from the input in the next block in the random access file
	 * @param count
	 * @return
	 */
	private int getDataAddress(int rowNum) {
		return rowNum * BLOCK_SIZE;
	}
}
