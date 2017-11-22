package acdc;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public class CacheUpdate {

    static String[] path = {"Pictures", "79gVnGo.jpg"};

    private String[] truePath;
    private boolean found;
    private long currentLastModifiedTime;


    public CacheUpdate(String[] truePath, long currentLastModifiedTime) {
        this.truePath = truePath;
        this.currentLastModifiedTime = currentLastModifiedTime;
    }

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


    public void readJsonStream() throws IOException {
        String[] path = {"Pictures", "19268_1333773742162_6130659_n.jpg"};
        JsonReader reader = new JsonReader(new FileReader("test2.json"));
        JsonWriter writer = new JsonWriter(new FileWriter("cache.json"));
        try {
            readFile1(reader,writer,0);
        } finally {
            reader.close();
            writer.close();
        }
    }

    public void readChildrenArray(JsonReader reader, JsonWriter writer, int index) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            if (this.isFound()) {
                return;
            }
            readFile1(reader, writer, index);
        }
        reader.endArray();
    }

    public void readLastModifiedTime(JsonReader reader, JsonWriter writer) throws IOException {
        reader.beginObject();
        writer.beginObject();
        while (reader.hasNext()) {
/*            if (this.isFound()) {
                return;
            }*/
            String name = reader.nextName();
            if (name.equals("value")) {
                long lastModifiedTime = reader.nextLong();
                //TEST
                if (this.isFound()) {
                    if(this.getCurrentLastModifiedTime() != lastModifiedTime){
                        writer.value(lastModifiedTime);
                    }
                    return;
                }
                this.setCurrentLastModifiedTime(lastModifiedTime);
                System.out.println(lastModifiedTime);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        writer.endObject();
    }

    public void readFile1(JsonReader reader, JsonWriter writer, int index) throws IOException {
        reader.beginObject();
        writer.beginObject();
        String currentValue = null;
        while (reader.hasNext()) {
            if (this.isFound()) {
                return;
            }
            String name = reader.nextName();
            if (name.equals("filename")) {
                currentValue = reader.nextString();
                System.out.println(currentValue);
                if (currentValue.equals(path[path.length - 1])) {
                    System.out.println("ALLOOOOO");
                    this.setFound(true);
                    return;
                }
                //System.out.println(reader.nextString());
            } else if (name.equals("lastModifiedTime")) {
                readLastModifiedTime(reader, writer);
            } else if (name.equals("children") && currentValue.equals(path[index]) && reader.peek() != JsonToken.NULL) {
                readChildrenArray(reader, writer, index + 1);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        writer.endObject();
    }
}

