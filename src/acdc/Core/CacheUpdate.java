package acdc.Core;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <b>CacheUpdate is my take to read, write and update the json cache of the whole tree.</b>
 *
 * <p>
 * I manage to get the reading working a little bit but not the writing.
 *
 * @author Baptiste
 * @version 1.0
 */
public class CacheUpdate {

    private String[] truePath;
    private boolean found;
    private long currentLastModifiedTime;


    public CacheUpdate(String[] truePath, long currentLastModifiedTime) {
        this.truePath = truePath;
        this.currentLastModifiedTime = currentLastModifiedTime;
    }

    //ACCESSORS
    public String[] getTruePath() {
        return truePath;
    }

    public void setTruePath(String[] truePath) {
        this.truePath = truePath;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public long getCurrentLastModifiedTime() {
        return currentLastModifiedTime;
    }

    public void setCurrentLastModifiedTime(long currentLastModifiedTime) {
        this.currentLastModifiedTime = currentLastModifiedTime;
    }

    /**
     * Opens the streams to read and write.
     *
     * @throws IOException file exception
     */
    public void readJsonStream() throws IOException {
        JsonReader reader = new JsonReader(new FileReader("cacheTree.json"));
        //JsonWriter writer = new JsonWriter(new FileWriter("cacheTree.json"));
        try {
            readFile1(reader, null, 0);
        } finally {
            reader.close();
            //writer.close();
        }
    }

    /**
     * Reads the children array.
     *
     * @param reader the stream reader
     * @param writer the stream writer
     * @param index  to navigate in the tree
     * @throws IOException file exception
     */
    private void readChildrenArray(JsonReader reader, JsonWriter writer, int index) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            if (this.isFound()) {
                return;
            }
            readFile1(reader, writer, index);
        }
        reader.endArray();
    }

    /**
     * Read the object lastModifiedTime.
     *
     * @param reader the stream reader
     * @param writer the stream writer
     * @throws IOException file exception
     */
    private void readLastModifiedTime(JsonReader reader, JsonWriter writer) throws IOException {
        reader.beginObject();
        //writer.beginObject();
        while (reader.hasNext()) {
/*            if (this.isFound()) {
                return;
            }*/
            String name = reader.nextName();
            if (name.equals("value")) {
                long lastModifiedTime = reader.nextLong();
                //TEST
/*                if (this.isFound()) {
                    if(this.getCurrentLastModifiedTime() != lastModifiedTime){
                        writer.value(lastModifiedTime);
                    }
                    return;
                }*/
                this.setCurrentLastModifiedTime(lastModifiedTime);
                System.out.println(lastModifiedTime);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        //writer.endObject();
    }

    /**
     * Reads the File1 object
     *
     * @param reader the stream reader
     * @param writer the stream writer
     * @param index  to navigate in the tree
     * @throws IOException file exception
     */
    private void readFile1(JsonReader reader, JsonWriter writer, int index) throws IOException {
        reader.beginObject();
        //writer.beginObject();
        String currentValue = null;
        while (reader.hasNext()) {
            if (this.isFound()) {
                return;
            }
            String name = reader.nextName();
            if (name.equals("filename")) {
                currentValue = reader.nextString();
                System.out.println(currentValue);
                if (currentValue.equals(truePath[truePath.length - 1])) {
                    System.out.println("ALLOOOOO");
                    this.setFound(true);
                    return;
                }
                //System.out.println(reader.nextString());
            } else if (name.equals("lastModifiedTime")) {
                readLastModifiedTime(reader, writer);
            } else if (name.equals("children") && currentValue.equals(truePath[index]) && reader.peek() != JsonToken.NULL) {
                readChildrenArray(reader, writer, index + 1);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        //writer.endObject();
    }
}

