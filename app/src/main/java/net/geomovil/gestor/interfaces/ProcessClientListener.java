package net.geomovil.gestor.interfaces;

public interface ProcessClientListener {

    /**
     * Metodo invocado para procesar un cliente a partir de su id en la base de datos movil
     * @param client_id
     */
    void processClient(int client_id);

    void showClientInfo(int client_id);

    void processEspecifyFreeSurvey(String survey_id);
}
