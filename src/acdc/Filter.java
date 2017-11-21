package acdc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {


	private ArrayList<String> acceptedExtensions;
	private ArrayList<String> refusedExtensions;
	private ArrayList<String> refusedFiles;
	private String name;
	private String lastModifiedTime;
	private Long weight;
	private boolean GtWeight;
	private boolean LwWeight;

	private String pattern;

	public Filter() {
		super();
		this.acceptedExtensions = new ArrayList<>();
		this.refusedExtensions = new ArrayList<>();
		this.refusedFiles = new ArrayList<>();
		this.name = null;
		this.lastModifiedTime = null;
		this.weight = null;
		this.pattern = null;
	}

	public ArrayList<String> getAcceptedExtensions() {
		return acceptedExtensions;
	}

	public void setAcceptedExtensions(ArrayList<String> acceptedExtensions) {
		this.acceptedExtensions = acceptedExtensions;
	}

	public void addExtension(String extension) {
		this.acceptedExtensions.add(extension);
	}

	public ArrayList<String> getRefusedExtensions() {
		return refusedExtensions;
	}

	public void setRefusedExtensions(ArrayList<String> refusedExtensions) {
		this.refusedExtensions = refusedExtensions;
	}

	public void addRefusedExtension(String extension) {
		this.refusedExtensions.add(extension);
	}

	public ArrayList<String> getRefusedFiles() {
		return refusedFiles;
	}

	public void setRefusedFiles(ArrayList<String> refusedFiles) {
		this.refusedFiles = refusedFiles;
	}

	public void addRefusedFiles(String refusedFile) {
		this.refusedFiles.add(refusedFile);
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

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public boolean accept(Path entry) throws IOException {
		boolean accept = true;

		String currentFileName = entry.getFileName().toString();
        BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);

		// ACCEPTED EXTENSIONS
		if(!acceptedExtensions.isEmpty()) {
			String extension = "";

			int i = currentFileName.lastIndexOf('.');
			if (i > 0) {
				extension = currentFileName.substring(i+1).toLowerCase();
			}

			accept = acceptedExtensions.contains(extension);
		}

		//REFUSED EXTENSIONS
		if(!refusedExtensions.isEmpty()) {
			String extension = "";

			int i = currentFileName.lastIndexOf('.');
			if (i > 0) {
				extension = currentFileName.substring(i+1).toLowerCase();
			}

			accept = accept && !refusedExtensions.contains(extension);
		}

		//REFUSED NAMES
		if(!refusedFiles.isEmpty()) {
			System.out.println(currentFileName);
			accept = (accept && !refusedFiles.contains(currentFileName));
		}

		//NAME
		if(this.name != null) {
			accept = (accept && currentFileName.contains(this.name));
		}

		//LAST MODIFIED DATE
		if(this.lastModifiedTime != null) {

			FileTime currentLastModifiedTime = attr.lastModifiedTime();
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			String currentLastModifiedDate = df.format(currentLastModifiedTime.toMillis());

			accept = (accept && currentLastModifiedDate.equals(this.lastModifiedTime));
		}

		//WEIGHT
		if(this.weight != null){
			if(this.weight > 0){
				long currentWeight = attr.size();

				if(this.GtWeight){
					accept =  accept && (currentWeight > this.weight);
				} else if(this.LwWeight){
					accept =  accept && (currentWeight < this.weight);
				} else {
					accept = accept && (currentWeight == this.weight);
				}
			}
		}

		//REGEX PATTERN
		if(this.pattern != null){
			Pattern pattern = Pattern.compile(getPattern());
			Matcher matcher = pattern.matcher(currentFileName);
			accept = accept && matcher.find();
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

        return this.getName() == null &&
				this.getLastModifiedTime() == null &&
				this.getWeight() == null &&
				this.getAcceptedExtensions().isEmpty() &&
				this.getRefusedExtensions().isEmpty() &&
				this.getRefusedFiles().isEmpty() &&
				this.getPattern() == null;
	}
}
