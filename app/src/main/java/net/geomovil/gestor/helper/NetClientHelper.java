package net.geomovil.gestor.helper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.DatabaseHelper;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.List;

public class NetClientHelper {

    private static final Logger log = Logger.getLogger(NetClientHelper.class.getSimpleName());

    public static final String CLIENTS_UPDATED = "net.geomovil.gestor.clientupdated";

    public static void processClient(JSONObject client, Dao<ClientData, Integer> clientDao, DatabaseHelper db) throws Exception {
        ClientData c = findClient(client, clientDao);
        if (c == null) {
            clientDao.create(new ClientData(client));
        } else {
            c.UpdateData(client);
            clientDao.update(c);
        }
    }

    /**
     * Busca en la base de datos el cliente
     *
     * @param client    Objeto JSON que representa el cliente
     * @param clientDao Acceso a datos
     * @return El cliente buscado o null en caso de no encontrarlo
     * @throws Exception
     */
    public static ClientData findClient(JSONObject client, Dao<ClientData, Integer> clientDao) throws Exception {
        QueryBuilder<ClientData, Integer> queryBuilder = clientDao.queryBuilder();
        queryBuilder.where().eq(ClientData.WEBID, client.getString("_id"));
        List<ClientData> clients = queryBuilder.query();
        return clients.size() > 0 ? clients.get(0) : null;
    }
}
