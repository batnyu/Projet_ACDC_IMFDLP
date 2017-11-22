package acdc;

import java.util.ArrayList;

public class ErrorLogging {

    /** L'instance statique */
    private static ErrorLogging instance = new ErrorLogging();

    private ArrayList<String> logs = new ArrayList<>();

    /** Constructeur redéfini comme étant privé pour interdire
     * son appel et forcer à passer par la méthode <link
     */
    private ErrorLogging() {
    }

    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }

    public void addLog(String log){
        this.logs.add(log);
    }

    /** Récupère l'instance unique de la class ErrorLogging.<p>
     * Remarque : le constructeur est rendu inaccessible
     */
    public static ErrorLogging getInstance() {
        if (null == instance) { // Premier appel
            synchronized(objetSynchrone__) {
                if (null == instance) {
                    instance = new ErrorLogging();
                }
            }
        }
        return instance;
    }

    /** objet pour la synchronisation. <p>
     * j'ajoute deux "soulignés" (__) au nom de l'attribut car il n'a
     * qu'un intérêt purement technique.
     */
    private static Object objetSynchrone__;
}