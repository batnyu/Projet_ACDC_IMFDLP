package acdc;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.chrono.ThaiBuddhistEra;
import java.util.ArrayList;

public class Filter {
	
	private ArrayList<String> extensions;
	private String name;
	private String lastModifiedTime;
	private Long weight;
	private boolean GtWeight;
	private boolean LwWeight;
	
	public Filter() {
		super();
		this.extensions = new ArrayList<String>();
		this.name = null;
		this.lastModifiedTime = null;
		this.weight = null;
	}
	
	public ArrayList<String> getExtensions() {
		return extensions;
	}

	public void addExtension(String extention) {
		this.extensions.add(extention);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(String lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}
	
	public boolean accept(Path entry) throws IOException {
		boolean accept = true;
		
		String currentFileName = entry.getFileName().toString();
        BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);

		// EXTENSION
		if(!extensions.isEmpty()) {
			String extension = "";
			
			int i = currentFileName.lastIndexOf('.');
			if (i > 0) {
				extension = currentFileName.substring(i+1);
			}
			
			accept = extensions.contains(extension);			
		}
				
		//NAME
		if(this.name != null) {
			accept = currentFileName.contains(this.name);
		}
		
		//LAST MODIFIED DATE
		if(this.lastModifiedTime != null) {
			
			FileTime currentLastModifiedTime = attr.lastModifiedTime();
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String currentLastModifiedDate = df.format(currentLastModifiedTime.toMillis());
			
			accept = currentLastModifiedDate.equals(this.lastModifiedTime);
		}
		
		//WEIGHT
		if(this.weight != null){
			if(this.weight > 0){
				long currentWeight = attr.size();
				
				if(this.GtWeight){
					accept = (currentWeight > this.weight);
				} else if(this.LwWeight){
					accept = (currentWeight < this.weight);
				} else {
					accept = (currentWeight == this.weight);
				}
			}			
		}
		
		return accept;
	}

    public void equalsWeight(long weight){
        this.weight = weight;
        this.GtWeight = false;
        this.LwWeight = false;
    }
    
    public void GtWeight(long weight){
        this.weight = weight;
        this.GtWeight = true;
        this.LwWeight = false;
    }
    
    public void LwWeight(long weight){
        this.weight = weight;
        this.GtWeight = false;
        this.LwWeight = true;
    }

	public boolean isEmpty() {
		
		if(this.getName() == null && this.getLastModifiedTime() == null && this.getWeight() == null && this.getExtensions().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
