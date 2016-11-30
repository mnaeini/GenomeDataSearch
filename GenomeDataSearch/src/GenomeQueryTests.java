import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Please run this class in order to test the algorithm. 
 * First you need to move the probes.txt file in the ./data/folder (My git account doesn't allow me to do that due to not having enough space)
 * It populates the index and RA (RandomAccess) file on disk, then executes queries against it and writes the output 
 * to a text file in the ./data/results folder of the project. 
 *
 * You can test more queries by following the template below to call the query method in the querying service. 
 * 
 * Please see examples below in the way that queries are parsed and passed is:
 * 
 * chr18:0-60000000  start = 5000, end = 9000, chrs = {"chr18"}
 * chr3:5000-chr5:8000  start = 5000, end = 8000, chrs = {"chr3", "chr4", "chr5"}
 * 
 * PS. Ideally there should be a UI and Query parser layer on top too
 * 
 * @author maryammoslemi
 */
public class GenomeQueryTests {

	public static void main(String args[]) {
		
		// Create the index file first
		IndexingService indexer = new IndexingService();
		indexer.buildIndex("./data/probes.txt");
		
		QueryingService queryService = new QueryingService();

		// EG. query : chr18:0-60000000 
		save(new File("./results/output-chr18:0-60000000.txt"), queryService.query(0, 60000000, new String[] {"chr18" }));
				
		// EG. query : chr3:5000-chr5:8000 
		save(new File("./results/output-chr3:3000-chr5:8000.txt"), queryService.query(3000, 8000, new String[] {"chr3", "chr4", "chr5" }));
		
		// EG. query : chr1:50000-150000 
		save(new File("./results/output-chr1:50000-150000.txt"), queryService.query(50000, 150000, new String[] {"chr1" }));
		
		// EG. query : chr19:100000-chrY200000 
		save(new File("./results/output-chr21:100000-chrY2959129.txt"), queryService.query(100000, 2959129, new String[] {"chr21", "chr22", "chrX", "chrY" }));
		
		System.out.println("Finished! Check the results stored in the results folder of the project");
	}
	
	private static void save(File file, String textToSave) {
	    try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(file));
	        out.write(textToSave);
	        out.close();
	    } catch (IOException e) {
			e.printStackTrace();
	    }
	}

}
