package acdc;

import javax.swing.tree.TreeModel;
import java.util.List;
import java.util.Map;

public interface IMFDLP {
	TreeModel tree(String path, Filter filter);
	TreeModel tree(String path, Filter filter, int depth);
	Map<String,List<String>> doublons();
	String filename();
	String hash();
	long weight();
	String absolutePath();
	void filter(Filter[] filters);
}