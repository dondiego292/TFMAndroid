package net.geomovil.gestor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.geomovil.gestor.R;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private final Logger log = Logger.getLogger(DatabaseHelper.class.getSimpleName());
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "gestor.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Survey.class);
            TableUtils.createTable(connectionSource, SurveyCatalog.class);
            TableUtils.createTable(connectionSource, SurveyCatalogPadre.class);
            TableUtils.createTable(connectionSource, SurveyCatalogHijo.class);
            TableUtils.createTable(connectionSource, SurveyQuestion.class);
            TableUtils.createTable(connectionSource, SurveyData.class);
            TableUtils.createTable(connectionSource, QuestionRule.class);
            TableUtils.createTable(connectionSource, QuestionType.class);
            TableUtils.createTable(connectionSource, ClientData.class);
            TableUtils.createTable(connectionSource, LogEvent.class);
            TableUtils.createTable(connectionSource, LocationData.class);

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        /*try {

        } catch (SQLException e) {
            Log.e(TAG, "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
        }*/
    }

    private Dao<LocationData, Integer> locationDao;

    public Dao<LocationData, Integer> getLocationDao() throws SQLException {
        if (locationDao == null) {
            locationDao = getDao(LocationData.class);
        }
        return locationDao;
    }

    /**
     * Retorna las ultimas {quantity} localizaciones por enviar
     *
     * @param quantity
     * @return
     */
    public List<LocationData> getLocationToSend(int quantity) {
        List<LocationData> result = new LinkedList<>();
        try {
            QueryBuilder<LocationData, Integer> queryBuilder = getLocationDao().queryBuilder();
            queryBuilder.where()
                    .eq(LocationData.STATUS, 0)
                    .or()
                    .eq(LocationData.STATUS, 1);
            queryBuilder.limit((long) quantity);
            queryBuilder.orderBy(LocationData.ID, true);
            result = queryBuilder.query();
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }


    private Dao<LogEvent, Integer> logEventDao;

    public Dao<LogEvent, Integer> getLogEventDao() throws SQLException {
        if (logEventDao == null) {
            logEventDao = getDao(LogEvent.class);
        }
        return logEventDao;
    }

    public List<LogEvent> getLogsToSend(int quantity) {
        List<LogEvent> result = new LinkedList<>();
        try {
            QueryBuilder<LogEvent, Integer> queryBuilder = getLogEventDao().queryBuilder();
            queryBuilder.where()
                    .eq(LogEvent.ENVIADO, false);
            queryBuilder.limit((long) quantity);
            queryBuilder.orderBy("id", true);
            result = queryBuilder.query();
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    /**
     * Busca la ultima localizacion aun en proceso
     *
     * @return
     */
    public LocationData getLastLocation() {
        try {
            QueryBuilder<LocationData, Integer> queryBuilder = getLocationDao().queryBuilder();
            queryBuilder.where().eq(LocationData.STATUS, 0);
            queryBuilder.orderBy(LocationData.ID, false);
            return queryBuilder.queryForFirst();

        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * agrega la nueva localizacion del GPS al sistema
     *
     * @param location
     */
    public void addNewLocation(Location location) {
        try {
            getLocationDao().create(new LocationData(location));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * cierra la ultima localizacion aun abierta
     */
    public void closeLastLocation() {
        try {
            LocationData last = getLastLocation();
            if (last != null) {
                last.setEndTime(System.currentTimeMillis());
                last.setStatus(1);
                getLocationDao().update(last);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }



    private Dao<SurveyData, Integer> surveyDataDao;

    public Dao<SurveyData, Integer> getSurveyDataDao() throws SQLException {
        if (surveyDataDao == null) {
            surveyDataDao = getDao(SurveyData.class);
        }
        return surveyDataDao;
    }

    private Dao<ClientData, Integer> clientDao;

    public Dao<ClientData, Integer> getClientDao() throws SQLException {
        if (clientDao == null) {
            clientDao = getDao(ClientData.class);
        }
        return clientDao;
    }

    private Dao<QuestionType, Integer> questionTypeDao;

    public Dao<QuestionType, Integer> getQuestionTypeDao() throws SQLException {
        if (questionTypeDao == null) {
            questionTypeDao = getDao(QuestionType.class);
        }
        return questionTypeDao;
    }

    private Dao<QuestionRule, Integer> questionRuleDao;

    public Dao<QuestionRule, Integer> getQuestionRuleDao() throws SQLException {
        if (questionRuleDao == null) {
            questionRuleDao = getDao(QuestionRule.class);
        }
        return questionRuleDao;
    }

    private Dao<Survey, Integer> surveyDao;

    public Dao<Survey, Integer> getSurveyDao() throws SQLException {
        if (surveyDao == null) {
            surveyDao = getDao(Survey.class);
        }
        return surveyDao;
    }

    private Dao<SurveyQuestion, Integer> surveyQuestionDao;

    public Dao<SurveyQuestion, Integer> getSurveyQuestionDao() throws SQLException {
        if (surveyQuestionDao == null) {
            surveyQuestionDao = getDao(SurveyQuestion.class);
        }
        return surveyQuestionDao;
    }

    private Dao<SurveyCatalogPadre, Integer> surveyCatalogPadreDao;

    public Dao<SurveyCatalogPadre, Integer> getSurveyCatalogPadreDao() throws SQLException {
        if (surveyCatalogPadreDao == null) {
            surveyCatalogPadreDao = getDao(SurveyCatalogPadre.class);
        }
        return surveyCatalogPadreDao;
    }

    private Dao<SurveyCatalogHijo, Integer> surveyCatalogHijoDao;

    public Dao<SurveyCatalogHijo, Integer> getSurveyCatalogHijoDao() throws SQLException {
        if (surveyCatalogHijoDao == null) {
            surveyCatalogHijoDao = getDao(SurveyCatalogHijo.class);
        }
        return surveyCatalogHijoDao;
    }

    private Dao<SurveyCatalog, Integer> surveyCatalogDao;

    public Dao<SurveyCatalog, Integer> getSurveyCatalogDao() throws SQLException {
        if (surveyCatalogDao == null) {
            surveyCatalogDao = getDao(SurveyCatalog.class);
        }
        return surveyCatalogDao;
    }


    private Dao<User, Integer> userDao;

    public Dao<User, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    /**
     * Retorna los datos del usuario autenticado
     *
     * @return
     */
    public User getUser() {
        try {
            List<User> users = getUserDao().queryForAll();
            if (users.size() > 0)
                return users.get(0);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }
}
