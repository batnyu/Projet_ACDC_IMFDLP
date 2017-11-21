package acdc;

import javax.swing.tree.TreeModel;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface IMFDLP {

	TreeModel tree(String path, Filter filter, int parallelism);

	TreeModel tree(String path, Filter filter, int parallelism, int depth);

	//Concurrent because writing in HashMap from multiple threads
	ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> getDoublons();

	String filename();

	//No hash for file, hash is independant of the building of the tree
	//String hash();

	long weight();
	String absolutePath();

	//Filter passed via the constructor
	//void filter(Filter[] filters);
}