package net.geomovil.gestor.initializer;

import com.j256.ormlite.dao.Dao;

import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.DatabaseHelper;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ClientDataInit {
    private final Logger log = Logger.getLogger(ClientDataInit.class);
    private DatabaseHelper dbHelper;
    private List<ClientData> clientes;

    public ClientDataInit(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        loadSampleData();
    }

    public void loadSampleData() {
        clientes = new ArrayList<>();
        clientes.add(new ClientData("1", "1102983226", "BEATRIZ  MARIA JUMBO LLANLLAN", "BARRIO LA ISLA DE SOLANDA CALLE A S25-17  Y AMBUQUI", "-0.276834855", "-78.54972431", "test"));
        clientes.add(new ClientData("2", "1712898392", "WILLIAM LEONARDO MERINO VILLASIS", "CALLE GILBERTO DE LA CUEVA S12-41 Y  HELEODORO", "-0.252897275", "-78.53350231", "test"));
        clientes.add(new ClientData("3", "1721890166", "HOLGER ANDERSON HURTADO CRUZ", "1: ARTEFACTAAV AMAZONAS Y AZUAY  ESQUINA", "-0.175317597", "-78.46661875", "CREDITO"));
        clientes.add(new ClientData("4", "1722727094", "MIRIAM ALEXANDRA PEÑA ALOZA CABASCANGO", "HOTEL DAN CARTON  QUITOAV REPUBLICA DEL DALVADOR", "-0.175317597", "-78.46661875", "CREDITO"));
        clientes.add(new ClientData("5", "1722727094", "MIRIAM ALEXANDRA PEÑA ALOZA CABASCANGO", "CALLE CLEMENTE CONCHA Y PSJ  E CASA OE7-58", "-0.335014551", "-78.55519602", "COBRANZA"));
        clientes.add(new ClientData("6", "1702793678", "BLANCA EUFEMIA ABRIGO JARAMILLO", "GERMAN MEINEER 100 Y ANGAMARCA", "-0.252897275", "-78.53350231", "CREDITO"));
        clientes.add(new ClientData("7", "503165896", "JOSE REINALDO CHASILUISA LAGLA", "1: MEGA PROFER DISTRIBUIDOR FE MASTERIAL", "-0.147218908", "-78.4843857", "COBRANZA"));
        clientes.add(new ClientData("8", "1713218277", "VERONICA ELIZABETH ESPIN BOZANO", "1: CRONIC  VENTA DE MUEBLES METALICOS Y COLCHONESCALLE   PRINCIPAL  AV TENIENTE HOGO ORTIZ", "-0.335014551", "-78.55519602", "CREDITO"));
        clientes.add(new ClientData("9", "1713218277", "VERONICA ELIZABETH ESPIN BOZANO", "CALLE HUGO DIAZ  OE7-168  S35  Y CALLE 9", "-0.335014551", "-78.55519602", "CREDITO"));
        clientes.add(new ClientData("10", "1719278077", "OLGA JAQUELINA PASTILLO CATUCUAMBA", "CAMILO CASARES N33-41 Y MARIANO CALVACHE", "-0.175317597", "-78.46661875", "COBRANZA"));
        clientes.add(new ClientData("11", "603018599", "JORGE LUIS MORENO YEROVI", "1: HUMANA DE SEGUROSAV.10 DE AGOSTO Y ATAHUALPA", "-0.196725172", "-78.5006077", "CREDITO"));
        clientes.add(new ClientData("12", "603018500", "ADALBERTO SANTOS", "PASAJE A  S7-120 Y AV JUAN BAUTISTE AGUIRRE", "-0.252897275", "-78.53350231", "COBRANZA"));
        initData();
    }

    public void initData() {
        try {
            Dao<ClientData, Integer> clientDao = dbHelper.getClientDao();
            for (ClientData c : clientes) {
                List<ClientData> accountList = clientDao.query(clientDao.queryBuilder().where().eq(ClientData.IDENTIFICACION, c.getIdentificacion()).prepare());
                if (accountList.size() == 0) {
                    clientDao.create(c);
                }
            }


        } catch (Exception e) {
            log.error("Init Client Data", e);
        }
    }
}
