package acdc.Services;

import java.util.ArrayList;

/**
 * <b>ErrorLogging is a singleton (Initialization-on-demand holder idiom) that register the error logs</b>
 *
 * @author Baptiste
 * @version 1.0
 */
public class ErrorLogging {

    private ErrorLogging() {
    }

    private static class Holder {
        private static final ErrorLogging INSTANCE = new ErrorLogging();
    }

    public static ErrorLogging getInstance() {
        return Holder.INSTANCE;
    }

    private ArrayList<String> logs = new ArrayList<>();

    //ACCESSORS
    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }

    public void addLog(String log) {
        this.logs.add(log);
    }
}