package acdc;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;

public class Test {
	public static StringBuffer compositeBuilder = new StringBuffer();
	
	public static Node tree;

	public static void main(String[] args) throws IOException {
				
		//Path startingDir = Paths.get("C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Desktop" + File.separator + "test");
		//Path startingDir = Paths.get("D:" + File.separator);
		
		//TreeCreator pf = new TreeCreator();
		//Files.walkFileTree(startingDir, pf);
		//Files.walkFileTree(startingDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 3, pf);
		
		//tree.ls();
		
		// Files.walk
		
//		long startTime2 = System.nanoTime();
//		
//		Files.walk(startingDir)
//        .forEach(System.out::println);
//		
//		long endTime2 = System.nanoTime();
//		System.out.println("Took "+(endTime2 - startTime2) + " ns"); 
		
		
		//String path = "D:" + File.separator;
		//String path = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Desktop" + File.separator + "test";
		String path = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "pictures";
		
		tree = tree(path);
		tree.ls();
		
		
	}
	
	public static class TreeCreator extends SimpleFileVisitor<Path> {

		Folder newFolder;
		Folder currentDir;
		
		Deque<Long> dirSizeStack = new ArrayDeque<>();
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			//System.out.format("Directory: %s%n", dir);
			
			dirSizeStack.push(new Long(0));
			
			String simpleDir = null;
			
			//to counter bug when getFileName() is called on root
			if(dir.getNameCount() == 0){
				simpleDir = dir.getRoot().toString();
			} else {
				simpleDir = dir.getFileName().toString();
			}
								
			if(tree == null){
				tree = new Folder(simpleDir, 0, "truc", dir.toString());
				currentDir = (Folder) tree;
			} else {
				newFolder = new Folder(simpleDir, 0, "truc", dir.toString());
				
				currentDir.add(newFolder);
				currentDir = newFolder;
			} 
			
			//System.out.println("currentDir : " + currentDir.filename);

			
			return FileVisitResult.CONTINUE;
		}
				
	    // Print information about
	    // each type of file.
	    @Override
	    public FileVisitResult visitFile(Path file,BasicFileAttributes attr) throws IOException {
	    	
	    	
	    	
	        if (attr.isSymbolicLink()) {
	            //System.out.format("Symbolic link: %s ", file);
	        } else if (attr.isRegularFile()) {
	            //System.out.format("Regular file: %s ", file);
	        	
	        	//Testing sort by extension
	            if(file.toString().endsWith(".JPG")){
	            	dirSizeStack.push(dirSizeStack.pop() + attr.size());
	            	currentDir.add(new File1(file.getFileName().toString(),attr.size(),"hash",file.toString()));
	            }
	            
	            
	        } else {
	            //System.out.format("Other: %s ", file);
	        }
	        //System.out.println("(" + attr.size() + "bytes)");
	        return FileVisitResult.CONTINUE;
	    }
	    
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			long size = dirSizeStack.pop();
			
			currentDir.setWeight(size);
			
	        if (!dirSizeStack.isEmpty()) // add this dir size to parent's size
	            dirSizeStack.push(dirSizeStack.pop() + size);
			
	        //climb up after visiting the folder
			currentDir = (Folder) currentDir.getParent();
			
			//System.out.println("currentDir : " + currentDir.filename);
			
			return FileVisitResult.CONTINUE;
		}
		
	    // If there is some error accessing
	    // the file, let the user know.
	    // If you don't override this method
	    // and an error occurs, an IOException 
	    // is thrown.
	    @Override
	    public FileVisitResult visitFileFailed(Path file,
	                                       IOException exc) {
	        System.err.println(exc);
	        return FileVisitResult.CONTINUE;
	    }
	    
//		public String buildHash(Path file) {
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			try (InputStream is = Files.newInputStream(file);
//			     DigestInputStream dis = new DigestInputStream(is, md)) 
//			{
//			  /* Read decorated stream (dis) to EOF as normal... */
//			}
//			byte[] digest = md.digest();
//			
//			return digest.toString();
//		}
	}

	public static Node tree(String path) throws IOException {
		tree = null; //reset tree for re-calculating
		
		Path startingDir = Paths.get(path);
		
		TreeCreator pf = new TreeCreator();
		Files.walkFileTree(startingDir, pf);
		
		return tree;
	}
	
	public static Node tree(String path, int depth) throws IOException {
		tree = null;
		
		Path startingDir = Paths.get(path);
		
		TreeCreator pf = new TreeCreator();
		Files.walkFileTree(startingDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth, pf);
		return tree;
	}
	
}
