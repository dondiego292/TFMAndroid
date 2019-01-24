package net.geomovil.gestor;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;

import net.geomovil.encuesta.form.FormCheckBoxs;
import net.geomovil.encuesta.form.FormEditDate;
import net.geomovil.encuesta.form.FormEditDecimal;
import net.geomovil.encuesta.form.FormEditNumber;
import net.geomovil.encuesta.form.FormEditText;
import net.geomovil.encuesta.form.FormEditTime;
import net.geomovil.encuesta.form.FormList;
import net.geomovil.encuesta.form.FormLongText;
import net.geomovil.encuesta.form.FormPhoto;
import net.geomovil.encuesta.form.FormWidget;
import net.geomovil.encuesta.form.VisibilityRuleListener;
import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.QuestionType;
import net.geomovil.gestor.database.Survey;
import net.geomovil.gestor.database.SurveyData;
import net.geomovil.gestor.database.SurveyQuestion;
import net.geomovil.gestor.helper.QuestionHelper;
import net.geomovil.gestor.util.ClientDataInitializer;
import net.geomovil.gestor.util.DeviceTool;
import net.geomovil.gestor.util.FolderInspector;
import net.geomovil.gestor.util.TextHelper;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SurveyActivity extends BaseActivity implements FormPhoto.photoData, FormPhoto.takePhoto,
        VisibilityRuleListener, FormList.FormListChangeListener, FormEditDate.FormEditDateChangeListener {

    private final Logger log = Logger.getLogger(SurveyActivity.class.getSimpleName());

    public static final int CAMERA_REQUEST = 999;
    public static final int SURVEY_REQUEST = 909;

    public static final int FREE_SURVEY_REQUEST = 939;

    private static final boolean GPS_REQUIRED = true;
    // cliente al que se le esta realizando la gestion
    private ClientData client;
    private Survey survey;

    protected List<FormWidget> components;
    protected ViewGroup container;
    // indicador para saber si la encuesta mientras se mostraba dio error
    protected boolean encuesta_error = false;
    // indicador para saber si la encuesta elaborada es libre o es relacionada
    protected boolean encuesta_libre = false;

    protected String starttime;

    protected File current_photo;
    private MenuItem gps_menuitem;
    private Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;
    protected List<String> phones;
    private static final int REQUEST_CALL_PHONE = 150;


    private Thread.UncaughtExceptionHandler androidDefaultUEH;
    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            log.error("", ex);
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        container = (LinearLayout) findViewById(R.id.container);
        loadClient();
        loadSurvey();
        loadDataFromPreferences();
        initStartTime();
        (findViewById(R.id.btn_save_data)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSurveyData();
            }
        });
        requestGPSLocation();

        starttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPhotoTaked();
    }

    private void requestGPSLocation() {
        try {
            Toast.makeText(this, "buscando GPS", Toast.LENGTH_SHORT).show();
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
//                    Toast.makeText(SurveyActivity.this, "GPS Encontrado", Toast.LENGTH_SHORT).show();
                    updateLocation(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3, locationListener);
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
                } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3, locationListener);
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
                } else {
                    log.error("No hay permiso para acceder al servicio de GPS");
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 3, locationListener);
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 20, locationListener);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void updateLocation(Location location) {
        this.location = location;
        if (gps_menuitem != null) {
            if (location == null) {
                gps_menuitem.setIcon(R.drawable.ic_action_location_off);
            } else {
                gps_menuitem.setIcon(R.drawable.ic_action_location_found);
            }
        }
    }

    public ClientData getClient() {
        return client;
    }

    public Survey getSurvey() {
        return survey;
    }


    /**
     * Leyendo los datos del cliente desde la base de datos
     */
    private void loadClient() {
        try {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey("ID")) {
                    client = getDBHelper().getClientDao().queryForId(getIntent().getIntExtra("ID", 0));
                    //fieldsDataNameConfigurator = new FieldsDataNameConfigurator(client,this);
                    ((TextView) findViewById(R.id.txt_name)).setText(client.getNombre());
                    ((TextView) findViewById(R.id.txt_cedula)).setText(client.getNombre());
                    ((TextView) findViewById(R.id.txt_direction)).setText(client.getNombre());
                } else {
                    ((TextView) findViewById(R.id.txt_cedula)).setText("");
                    ((TextView) findViewById(R.id.txt_name)).setText("");
                    ((TextView) findViewById(R.id.txt_direction)).setText("");
                }
            }
        } catch (Exception e) {
            client = new ClientData();
            log.error("", e);
        }
    }

    /**
     * Lee los datos de la encuesta relacionada con el cliente
     * y carga la estructura de la encuesta
     */
    private void loadSurvey() {
        try {
            List<Survey> svs = null;
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String id = getIntent().getStringExtra("WEBID");
                List<Survey> ss = getDBHelper().getSurveyDao().queryForAll();
                if (extras.containsKey("ID")) {
                    svs = getDBHelper().getSurveyDao().queryForEq(Survey.ETIQUETA, client.getGestion());
                } else {
                    svs = getDBHelper().getSurveyDao().queryForEq(Survey.WEBID, getIntent().getStringExtra("WEBID"));
                }
            }
            //List<Survey> svs = getDBHelper().getSurveyDao().queryForEq(Survey.ETIQUETA, client.getGestion());
            if (svs.size() > 0) {
                survey = svs.get(0);
                setTitle(survey.getNombre());
                loadSurveyStructure(survey.getPreguntas());
                encuesta_libre = survey.isLibre();
            } else {
                encuesta_error = true;
                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", "Encuesta no instalada en el sistema");
            }
        } catch (Exception e) {
            log.error("", e);
            encuesta_error = true;
        }
    }

    /**
     * Carga la estructura de la encuesta en el formulario
     *
     * @param preguntas Listado de preguntas de la encuesta
     */
    private void loadSurveyStructure(ForeignCollection<SurveyQuestion> preguntas) {
        if (preguntas.size() == 0) {
            showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", "Encuesta no instalada en el sistema");
        }
        components = new LinkedList<>();
        for (SurveyQuestion p : loadQuestionsFromDb()) {
            load(QuestionHelper.translate(p));
        }
        for (FormWidget component : this.components) {
            if (component instanceof FormList) {
                int parent = ((FormList) component).getParentDependent();
                if (parent != 0) {
                    FormWidget parent_component = getComponentById(parent);
                    parent_component.addDependent(component);
                }
            }
        }
    }

    /**
     * Lee de la BD del movil el listado de pregunta de la encuesta
     *
     * @return
     */
    private List<SurveyQuestion> loadQuestionsFromDb() {
        try {
            Dao<SurveyQuestion, Integer> questionDao = getDBHelper().getSurveyQuestionDao();
            QueryBuilder<SurveyQuestion, Integer> queryBuilder = questionDao.queryBuilder();
            queryBuilder.where()
                    .eq("encuesta_id", survey.getId());
            queryBuilder.orderBy(SurveyQuestion.POCISION, true);
            return queryBuilder.query();
        } catch (Exception e) {
            log.error("", e);
        }
        return new LinkedList<>();
    }

    /**
     * Retorna de los componentes cargados el que tiene como id el pasado
     * por parametro
     *
     * @param id
     * @return
     */
    public FormWidget getComponentById(int id) {
        for (FormWidget component : this.components) {
            if (component.getId() == id)
                return component;
        }
        return null;
    }

    private void initStartTime() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        try {
            JSONObject data = new JSONObject(sharedPref.getString(getDataSaveId(), "{}"));
            if (data.has("uuid")) {
                String starttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.getDefault())
                        .format(new Date());
                data.put("hora_inicio", starttime);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getDataSaveId(), data.toString());
                editor.commit();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Retorna el ID con el cual se debe guardar los datos de la encuestas
     * para su posterior procesamiento
     *
     * @return
     */
    private String getDataSaveId() {
        if (!survey.isLibre() && client != null)
            return client.getWebID() + "";
        return survey.getEtiqueta();
    }

    /**
     * Lee la pregunta represntada a traves del JSONObject component y la muestra en el formulario
     *
     * @param component
     */
    private void load(JSONObject component) {
        try {
            String tipoWebId = component.getString("tipo");
            Dao<QuestionType, Integer> surveyDataDao = getDBHelper().getQuestionTypeDao();
            QueryBuilder<QuestionType, Integer> builder = surveyDataDao.queryBuilder();
            builder.where().eq(QuestionType.WEBID, tipoWebId);
            List<QuestionType> typeQuestions = builder.query();
            if (typeQuestions.size() > 0) {
                String tipo = typeQuestions.get(0).getTipo();
                if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_NUMERO)) {
                    components.add(new FormEditNumber(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_DECIMAL)) {
                    components.add(new FormEditDecimal(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_TEXTO)) {
                    components.add(new FormEditText(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_TEXTO_LARGO)) {
                    components.add(new FormLongText(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_FOTO)) {
                    components.add(new FormPhoto(this, component, getLayoutInflater(), container, component.getString("label")));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_FECHA)) {
                    components.add(new FormEditDate(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_HORA)) {
                    components.add(new FormEditTime(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_LISTA_SIMPLE)) {
                    components.add(new FormList(this, component, getLayoutInflater(), container));
                } else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_LISTA_MULTIPLE)) {
                    components.add(new FormCheckBoxs(this, component, getLayoutInflater(), container));
                } /*else if (tipo.equalsIgnoreCase(SurveyQuestion.TIPO_AUTO_COMPLETE)) {
                components.add(new FormAutoComplete(this, component, getLayoutInflater(), container));
            }*/
            }

        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (encuesta_error == false && encuesta_libre == false) {
            getMenuInflater().inflate(R.menu.menu_survey, menu);
            MenuItem call_item = menu.findItem(R.id.menu_call);
            call_item.setVisible(false);
            /*if (client == null || TextHelper.isEmptyData(client.getLatitud()) || TextHelper.isEmptyData(client.getLongitud())) {
                MenuItem map_item = menu.findItem(R.id.menu_map);
                map_item.setVisible(false);
            }*/
            phones = getPhones();
            for (int i = 0; i < phones.size(); i++) {
                menu.add(0, i, 10, "Tlf: " + phones.get(i));
            }
            gps_menuitem = menu.findItem(R.id.menu_gps);
        }
        if (encuesta_error == false && survey.isLibre()) {
            getMenuInflater().inflate(R.menu.menu_survey, menu);
            MenuItem gps_item = menu.findItem(R.id.menu_gps);
            gps_item.setVisible(true);

            MenuItem call_item = menu.findItem(R.id.menu_call);
            call_item.setVisible(false);

            /*MenuItem map_item = menu.findItem(R.id.menu_map);
            map_item.setVisible(false);*/

            /*MenuItem data_item = menu.findItem(R.id.menu_client_data);
            data_item.setVisible(false);*/
            gps_menuitem = menu.findItem(R.id.menu_gps);
        }
        /*MenuItem online_payment_item = menu.findItem(R.id.menu_online_payment);
        if (survey.getEtiqueta().equalsIgnoreCase("MORA"))
            online_payment_item.setVisible(true);
        else
            online_payment_item.setVisible(false);*/

        MenuItem map_item = menu.findItem(R.id.menu_map);
        map_item.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.menu_online_payment:
                sendOnlinePayment(client.getOperacion());
                return true;*/
            case android.R.id.home:
                saveDataToPreferences();
                finish();
                return true;
            case R.id.menu_map:
                /*if (client != null) {
                    MapLocationDialog dialog = MapLocationDialog.newInstace(client.getId());
                    dialog.show(getSupportFragmentManager(), "map_location_dialog");
                    log.info(String.format("Mostrando mapa para el cliente %d - %s ", client.getId(), client.getNombre()));
                }*/
                return true;
            /*case R.id.menu_client_data:
                if (client != null) {
                    log.info(String.format("Mostrando datos para el cliente %d - %s ", client.getId(), client.getNombre()));
                    Intent i = new Intent(this, DataActivity.class);
                    i.putExtra("ID", client.getId());
                    startActivity(i);
                }
                return true;*/
            case R.id.menu_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + client.getTelefono()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                REQUEST_CALL_PHONE);
                    } else {
                        startActivity(callIntent);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        startActivity(callIntent);
                    } else {
                        log.error("El usuario no dio permiso para realizar la llamada");
                    }
                } else {
                    startActivity(callIntent);
                }
        }
        //if (item.getItemId() >= 0 && item.getItemId() < phones.size()) {
        if (item.getItemId() >= 0 && !survey.isLibre() && phones != null && item.getItemId() < phones.size()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phones.get(item.getItemId())));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PHONE);
                } else {
                    startActivity(callIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    log.error("El usuario no dio permiso para realizar la llamada");
                }
            } else {
                startActivity(callIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Retorna el listado de los telefonos que
     * tiene el cliente para generar los menus
     *
     * @return
     */
    public List<String> getPhones() {
        List<String> phones = new LinkedList<>();
        try {
            Pattern p = Pattern.compile("\\d+");
            if (!TextHelper.isEmptyData(client.getTelefono())) {
                Matcher m = p.matcher(client.getTelefono());
                while (m.find()) {
                    phones.add(m.group());
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return phones;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_CALL_PHONE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //onCall();
                    log.info("Call Permission Granted");
                } else {
                    log.info("Call Permission Not Granted");
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            REQUEST_CALL_PHONE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        saveDataToPreferences();
        super.onBackPressed();
    }

    public void removeLocationListener() {
        try {
            if (locationManager != null && locationListener != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        locationManager.removeUpdates(locationListener);
                    } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.removeUpdates(locationListener);
                    } else {
                        log.info("No hay permiso para acceder al servicio de GPS");
                    }
                } else {
                    locationManager.removeUpdates(locationListener);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Almacena los datos en las preferencias del aplicativo
     */
    private void saveDataToPreferences() {
        removeLocationListener();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getDataSaveId(),
                getQuestionValues(sharedPref.getString(getDataSaveId(), "{}")).toString());
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (current_photo != null) {
            editor.putString("current_photo", current_photo.getPath());
        }
        editor.commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String path = sharedPref.getString("current_photo", null);
        if (path != null)
            current_photo = new File(path);
    }

    /**
     * Revisa el estado de las fotos de la encuesta, cuando ya fueron tomadas
     * marca el boton con un color de backgroung rojo
     */
    private void checkPhotoTaked() {
        for (FormWidget wg : components) {
            if (wg instanceof FormPhoto)
                ((FormPhoto) wg).checkTaken();
        }
    }

    /**
     * lee los datos de la encuesta que han sido almacenados en la preferencias del app
     */
    private void loadDataFromPreferences() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        try {
            JSONObject data = new JSONObject(sharedPref.getString(getDataSaveId(), "{}"));
            if (data.has("uuid") == false) {
                String starttime = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.getDefault())
                        .format(new Date());
                data.put("device_id", DeviceTool.getDeviceUniqueID(this));
                data.put("hora_inicio", starttime);
                data.put("uuid", UUID.randomUUID().toString());
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                data.put("apk_version", pInfo.versionName);
                data.put("_survey", survey.getEtiqueta());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getDataSaveId(), data.toString());
                editor.commit();
            }
            if (data.has("fecha") && data.get("_survey").equals("MORA")) {
                data.put("fecha", "");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getDataSaveId(), data.toString());
                editor.commit();
            }


            for (FormWidget fw : components) {
                if (data.has(fw.getLabel())) {
                    fw.setValue(data.getString(fw.getLabel()));
                }
                //else{
                ClientDataInitializer.initData(fw, client, survey);
                //}
            }
            for (FormWidget component : components) {
                component.loadRules(getDBHelper().getQuestionRuleDao());
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Retorna un objeto json con los valores de la encuesta para ser almacenados
     * mientras se minimiza la app
     *
     * @return Objeto JSON
     */
    private JSONObject getQuestionValues(String d) {
        JSONObject data = new JSONObject();
        try {
            data = new JSONObject(d);
            for (FormWidget fw : components)
                data.put(fw.getLabel(), fw.getFinalValue());
        } catch (Exception e) {
            log.error("", e);
        }
        return data;
    }

    public void confirmSaveData() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("UBICACIÓN DEL CLIENTE")
                .setContentText("Desea utilizar latitud y longitud previamente almacenada?")
                .setConfirmText("SI")
                .setCancelText("NO")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sendDataToSave(true);
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {

                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sendDataToSave(false);
                    }
                })
                .show();
    }

    private void saveSurveyData() {
        try {
            if (checkErrors())
                showErrorMessages();
            else {
                if (survey.isLibre()) {
                    sendDataToSave(false);
                } else {
                    if (!TextHelper.isEmptyData(client.getLatitudCapturada()) && !TextHelper.isEmptyData(client.getLongitudCapturada())) {
                        confirmSaveData();
                    } else {
                        sendDataToSave(false);
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * alamcena los datos de la encuesta en la BD del dispositivo para su posterior
     * envio al servidor de datos
     *
     * @param pos_capturada
     */
    protected void sendDataToSave(boolean pos_capturada) {
        try {
            if ((pos_capturada == false && checkGPSLocations() == true) || pos_capturada) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                JSONObject d = getQuestionValues(sharedPref.getString(getDataSaveId(), "{}"));

                //corriegiendo error cuando envia sin uuid
                if (d.has("uuid") == false) {
                    d.put("device_id", DeviceTool.getDeviceUniqueID(this));
                    d.put("hora_inicio", starttime);
                    d.put("uuid", UUID.randomUUID().toString());
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    d.put("apk_version", pInfo.versionName);
                    d.put("_survey", survey.getEtiqueta());
                }

                String endtime = new SimpleDateFormat("yyyy-MM-dd HH:mm:s", Locale.getDefault()).format(new Date());
                d.put("hora_fin", endtime);
                if (pos_capturada) {
                    d.put("latitud", client.getLatitudCapturada());
                    d.put("longitud", client.getLongitudCapturada());
                } else {
                    if (location != null) {
                        d.put("latitud", location.getLatitude());
                        d.put("longitud", location.getLongitude());
                    }
                }
                d.put("_tiene_foto", surveyHavePicture());
                editor.remove(getDataSaveId());

                if (!survey.isLibre()) {
                    SurveyData data = new SurveyData(client.getWebID(), 0, d.toString());
                    getDBHelper().getSurveyDataDao().create(data);
                    client.setMovilStatus(1);
                    getDBHelper().getClientDao().update(client);
                    /*if (client.getGestion().equals("MORA") && d.getString("NUMEROCOMPROBANTE") != null)*
                        updateRecibo(d.getString("NUMEROCOMPROBANTE"));*/
                } else {
                    SurveyData data = new SurveyData("0", 0, d.toString());
                    getDBHelper().getSurveyDataDao().create(data);
                }
                log.info(d.toString());
                setResult(Activity.RESULT_OK);
                editor.commit();
                removeLocationListener();
                finish();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Retorna verdadero en caso que la encuesta contenga fotos, falso en
     * caso contrario
     *
     * @return
     */
    private boolean surveyHavePicture() {
        for (FormWidget fw : components) {
            if (fw instanceof FormPhoto &&
                    !TextUtils.isEmpty(fw.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Chequea si la localizacion del GPS es necesaria,
     * en caso de serlo muestra un mensaje
     *
     * @return
     */
    private boolean checkGPSLocations() {
        if (GPS_REQUIRED && location == null) {
            showMessage(SweetAlertDialog.ERROR_TYPE,
                    getString(R.string.gps_location_title),
                    getString(R.string.gps_location_nedded));
            return false;
        }
        return true;
    }


    /**
     * Muestra los errores encontrados en el formulario
     */
    private void showErrorMessages() {
        try {
            String msg = "";
            for (FormWidget wg : components) {
                JSONObject jerror = wg.getError();
                if (jerror != null) {
                    msg += jerror.getString("name") + ": ";
                    msg += jerror.getString("message") + "\n ";
                }
            }
            if (TextUtils.isEmpty(msg) == false) {
                showMessage(SweetAlertDialog.ERROR_TYPE, getString(R.string.error_title), msg);
            }
        } catch (Exception e) {
            //log.error("", e);
            log.error(e);
        }
    }

    /**
     * Funcion para determinar si el formulario contiene errores
     *
     * @return
     */
    private boolean checkErrors() {
        try {
            for (FormWidget wg : components) {
                if (wg.getError() != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("", e);
            return true;
        }

        return false;
    }

    /**
     * Retorna la direccion donde debe guardarse la foto
     *
     * @param label etiqueta de la pregunta
     * @return
     */
    @Override
    public String getPath(String label) {
        File survey_file = FolderInspector.getPictureFolder(this, survey);
        return new File(survey_file, label).getAbsolutePath();
    }

    /**
     * retorna el nombre con el que debe guardarse la foto
     *
     * @param label etiqueta de la pregunta
     * @return
     */
    @Override
    public String getPhotoName(String label) {
        if (survey.isLibre()) {
            String imei = DeviceTool.getDeviceUniqueID(this);
            String fecha = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(new Date());
            return survey.getEtiqueta() + "-" + imei + "-" + label + "-" + fecha + ".jpg";
        }
        if (client != null) {
            //return survey.getEtiqueta() + "-" + client.getWebID() + "-" + label + ".jpg";
            if (survey.getEtiqueta().equalsIgnoreCase("MORA") || survey.getEtiqueta().equalsIgnoreCase("SEGUIMIENTO") || survey.getEtiqueta().equalsIgnoreCase("VERIFICACION")) {
                return survey.getEtiqueta() + "-" + client.getWebID() + "-" + label + ".jpg";
            } else {
                return survey.getEtiqueta() + "-" + client.getWebID() + "-" + label + ".jpg";
            }
        }
        return label;
    }

    public void onTakePhoto(String _path) {
        //log.info(String.format("Tomando la foto %s", _path));
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            current_photo = new File(_path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uriSavedImage = FileProvider.getUriForFile(SurveyActivity.this, BuildConfig.APPLICATION_ID + ".provider", current_photo);
                List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    this.grantUriPermission(packageName, uriSavedImage, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
            } else {
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(current_photo));
            }

            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } catch (Exception e) {

        }
    }

    @Override
    public void changeVisbility(int component_id, boolean visibility) {
        for (FormWidget component : components) {
            if (component.getId() == component_id) {
                component.setVisible(visibility);
                break;
            }
        }
    }

    @Override
    public void formEditDateChange(FormEditDate component) {
        for (FormWidget fw : components) {
            /*if (survey.getEtiqueta().equalsIgnoreCase("MORA") && component.getLabel().equalsIgnoreCase("fecha") && fw.getLabel().equalsIgnoreCase("RESPUESTA") && fw.getValue().equalsIgnoreCase("1047")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!TextUtils.isEmpty(component.getValue())) {
                        verifyAgendamiento(client.getWebID(), component.getValue());
                    }
                } else {
                    if (!TextUtils.isEmpty(component.getValue()) && test) {
                        verifyAgendamiento(client.getWebID(), component.getValue());
                        test = false;
                    } else {
                        test = true;
                    }
                }

            }*/
        }
    }


    @Override
    public void formListChange(FormList component) {
        try {
            //if (survey.getEtiqueta().equalsIgnoreCase("COBRANZAGESTOR") && component.getLabel().equalsIgnoreCase("IdDireccion")) {
            /*if (survey.getEtiqueta().equalsIgnoreCase("MORA") && component.getLabel().equalsIgnoreCase("IdDireccion")) {
                if (component.getValue().equalsIgnoreCase(client.getId_direccion() + "")) {
                    changeAdressValues(client);
                } else {
                    QueryBuilder<ClientDireccion, Integer> queryBuilder = getDBHelper().getDireccionDao().queryBuilder();
                    queryBuilder.where().eq(ClientDireccion.ID_DIRECCION, component.getValue());

                    ClientDireccion dir = null;
                    if (!TextHelper.isEmptyData(component.getValue()))
                        dir = queryBuilder.queryForFirst();
                    if (dir == null)
                        changeAdressValues("");
                    else
                        changeAdressValues(dir);
                }
            }*/
        } catch (Exception e) {
            //changeAdressValues("");
            log.error("", e);
        }
    }


}
