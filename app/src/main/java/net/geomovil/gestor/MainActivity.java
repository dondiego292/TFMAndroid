package net.geomovil.gestor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.LogEvent;
import net.geomovil.gestor.database.QuestionType;
import net.geomovil.gestor.database.Survey;
import net.geomovil.gestor.database.SurveyData;
import net.geomovil.gestor.database.SurveyQuestion;
import net.geomovil.gestor.database.User;
import net.geomovil.gestor.dialog.FreeSurveyDialog;
import net.geomovil.gestor.dialog.RouteConfirmDialog;
import net.geomovil.gestor.dialog.SearchClientDialog;
import net.geomovil.gestor.fragment.ClientsFragment;
import net.geomovil.gestor.fragment.MapFragment;
import net.geomovil.gestor.helper.NetClientHelper;
import net.geomovil.gestor.helper.RutaPackage;
import net.geomovil.gestor.helper.RutaPackageHelper;
import net.geomovil.gestor.interfaces.FragmentChangeInterface;
import net.geomovil.gestor.interfaces.ISenderData;
import net.geomovil.gestor.interfaces.LocationRecorderInterface;
import net.geomovil.gestor.interfaces.ProcessClientListener;
import net.geomovil.gestor.interfaces.SearchClientListener;
import net.geomovil.gestor.service.TrackingService;
import net.geomovil.gestor.util.CompressBitmap;
import net.geomovil.gestor.util.FileRequest;
import net.geomovil.gestor.util.FolderInspector;
import net.geomovil.gestor.util.NetApi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, SearchClientListener, FragmentChangeInterface, ProcessClientListener, RouteConfirmDialog.CalculateRutaListener {

    private final Logger log = Logger.getLogger(MainActivity.class.getSimpleName());
    private static final int FRAGMENT_CLIENTS = 1;
    private static final int FRAGMENT_MAP = 2;
    private DrawerLayout mDrawerLayout;
    private boolean drawerOpen = false;
    private Fragment activeFragment;
    public static final int MENU_CLIENTS = 2;
    public static final int MENU_MAP = 3;
    private int KIND_OF_MENU = MENU_CLIENTS;
    private static final int CALENDAR_REQUEST = 101;
    public static String rutaMode = "driving";
    private Stack<ISenderData> surveyToSend;
    private Stack<String> pictureToSend;


    private Thread.UncaughtExceptionHandler androidDefaultUEH;
    private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread thread, Throwable ex) {
            log.error("mainactivity", ex);
            androidDefaultUEH.uncaughtException(thread, ex);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureLog();
        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        FolderInspector.reviewFolderStatus(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {
            }

            @Override
            public void onDrawerOpened(View view) {
                drawerOpen = true;
            }

            @Override
            public void onDrawerClosed(View view) {
                drawerOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int i) {
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        if (savedInstanceState == null) {
            showFragment(FRAGMENT_CLIENTS);
        }
        View header = navigationView.getHeaderView(0);
        setNavName(header);
        try {
            getDBHelper().getLogEventDao().create(new LogEvent("INICIANDO APLICATIVO", 1));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideItem();
    }

    private void hideItem() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        //nav_Menu.findItem(R.id.nav_simulator).setVisible(false);
        if (cantidadEncuestasLibres() == 0) {
            nav_Menu.findItem(R.id.nav_encuestas).setVisible(false);
        } else {
            nav_Menu.findItem(R.id.nav_encuestas).setVisible(true);
        }
    }

    private FreeSurveyDialog dialogFreeSurvey;

    /**
     * Inicializa encuestas que sean libres
     */
    private void initEncuestasLibres() {
        if (cantidadEncuestasLibres() == 0) {
            showMessage(SweetAlertDialog.NORMAL_TYPE, getString(R.string.title_info),
                    getString(R.string.message_no_free_survey));
        } else {
            dialogFreeSurvey = FreeSurveyDialog.newInstace();
            dialogFreeSurvey.show(getSupportFragmentManager(), "free_survey_dialog");
        }
    }

    /**
     * Retorna la cantidad de encuestas libres que tiene el sistema
     *
     * @return
     */
    private int cantidadEncuestasLibres() {
        try {
            Dao<Survey, Integer> surveyDAO = getDBHelper().getSurveyDao();
            QueryBuilder<Survey, Integer> queryBuilder = surveyDAO.queryBuilder();
            queryBuilder.where()
                    .eq(Survey.INSTALADA, 1)
                    .and()
                    .eq(Survey.LIBRE, true);
            return queryBuilder.query().size();
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(MainActivity.this, TrackingService.class));
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        MainActivity.this.onOptionsItemSelected(menuItem);
                        return true;
                    }
                });
    }

    /**
     * Inicializa en el menu el nombre del gestor
     */
    private void setNavName(View header) {
        try {
            User user = getDBHelper().getUser();
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) header.findViewById(R.id.txt_nav_app_name)).setText(getString(R.string.app_name) + " " + pInfo.versionName);
            if (user != null) {
                ((TextView) header.findViewById(R.id.txt_user_name)).setText(user.getNombres() + " " + user.getApellidos());
                ((TextView) header.findViewById(R.id.txt_user_nickname)).setText(user.getNickname());
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    /**
     * Reemplaza el fragment que se esta mostrando por el fragment que se
     * haya seleccionado del menu lateral del sistema
     *
     * @param fragment
     */
    protected void showFragment(int fragment) {
        Fragment fg = null;
        switch (fragment) {
            case FRAGMENT_CLIENTS:
                fg = ClientsFragment.newInstance();
                setTitle(getString(R.string.Clients));
                break;
            case FRAGMENT_MAP:
                fg = MapFragment.newInstance();
                setTitle(getString(R.string.Map));
                break;
        }
        if (fg != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.containermain, fg)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (KIND_OF_MENU == MENU_MAP) {
            menu.findItem(R.id.menu_route).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(false);
            //if (hasRouteDefine() == false)
            menu.findItem(R.id.menu_route_map).setVisible(false);

        } else {
            menu.findItem(R.id.menu_route).setVisible(false);
            menu.findItem(R.id.menu_route_map).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.nav_clients:
                showFragment(FRAGMENT_CLIENTS);
                return true;
            case R.id.nav_map:
                showFragment(FRAGMENT_MAP);
                return true;
            case R.id.nav_system:
                Intent it = new Intent(this, SystemActivity.class);
                startActivity(it);
                return true;
            case R.id.nav_logout:
                confirmLogout();
                return true;
            case R.id.nav_calendar:
                Intent ic = new Intent(this, CalendarActivity.class);
                startActivityForResult(ic, CALENDAR_REQUEST);
                return true;
            case R.id.menu_search:
                SearchClientDialog dialog = SearchClientDialog.newInstace("");
                dialog.show(getSupportFragmentManager(), "search_dialog_listener");
                return true;
            case R.id.nav_send:
                sendDataToServer();
                return true;
            case R.id.nav_update:
                loadDataFromServer();
                return true;
            case R.id.nav_encuestas:
                initEncuestasLibres();
                return true;
            /*case R.id.nav_update_supervisor:
                loadDataSupervisorFromServer();
                return true;
            case R.id.menu_search:
                SearchClientDialog dialog = SearchClientDialog.newInstace("");
                dialog.show(getSupportFragmentManager(), "search_dialog_listener");
                return true;*/
            case R.id.menu_route:
                if (activeFragment != null && activeFragment instanceof LocationRecorderInterface) {
                    if (((LocationRecorderInterface) activeFragment).hasLocation() == false) {
                        showMessage(SweetAlertDialog.ERROR_TYPE, getString(R.string.location_required), getString(R.string.calculate_route));
                    } else {
                        RouteConfirmDialog rdialog = RouteConfirmDialog.newInstace();
                        rdialog.show(getSupportFragmentManager(), "route_dialog_confirm");
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirmLogout() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.out_of_system))
                .setContentText(getString(R.string.sure_to_out_of_system))
                .setConfirmText(getString(R.string.button_ok))
                .setCancelText(getString(R.string.button_cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        try {
                            DeleteBuilder<User, Integer> deleteBuilder = getDBHelper().getUserDao().deleteBuilder();
                            deleteBuilder.where().gt("id", 0);
                            deleteBuilder.delete();
                            getDBHelper().getLogEventDao().create(new LogEvent("CERRANDO APLICATIVO", 1));
                        } catch (Exception e) {
                            log.error("", e);
                        } finally {
                            stopService(new Intent(MainActivity.this, TrackingService.class));
                        }
                        MainActivity.this.finish();
                    }
                })
                .show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void configureLog() {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(FolderInspector.getProjectFolder(this) + File.separator + "mobile.log");
        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.configure();
    }

    /**
     * Cuando el listado de cliente o el mapa se muestran
     * estos fragmets se le pasan al contenedor MainActivity
     * a traves de este metodo.
     *
     * @param newFragment Nuevo fragment del contenedor
     */
    @Override
    public void changeFragment(Fragment newFragment) {
        activeFragment = newFragment;
    }

    /**
     * Cambia el tipo de menu del sistema. Para cuando se
     * el menu de mapa mostrar un menu distinto
     *
     * @param new_kind
     */
    public void changeKingOfMenu(int new_kind) {
        KIND_OF_MENU = new_kind;
        invalidateOptionsMenu();
    }

    @Override
    public void processClient(int client_id) {
        try {
            ClientData c = getDBHelper().getClientDao().queryForId(client_id);
            if (c != null /*&& c.isSend() && !c.isSend() */ && surveyExist(c)) {
                Intent it = new Intent(this, SurveyActivity.class);
                it.putExtra("ID", client_id);
                startActivityForResult(it, SurveyActivity.SURVEY_REQUEST);
            } else {
                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", "Encuesta no Existe");
            }
        } catch (Exception e) {
            log.error("", e);
        }

    }

    @Override
    public void showClientInfo(int client_id) {

    }

    @Override
    public void processEspecifyFreeSurvey(String survey_id) {
        try {
            List<Survey> svs = getDBHelper().getSurveyDao().queryForEq(Survey.WEBID, survey_id);
            Survey s = svs.get(0);
            if (s != null) {
                dialogFreeSurvey.dismiss();
                Intent it = new Intent(this, SurveyActivity.class);
                it.putExtra("WEBID", survey_id);
                startActivityForResult(it, SurveyActivity.FREE_SURVEY_REQUEST);
            } else {
                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", "Encuesta no Existe");
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Verifica que la encuesta del cliente existe
     *
     * @param c
     * @return
     */
    private boolean surveyExist(ClientData c) {
        try {
            return getDBHelper().getSurveyDao().queryForEq(Survey.ETIQUETA, c.getGestion()).size() > 0;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SurveyActivity.SURVEY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    showMessage(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.saved),
                            getString(R.string.success_saved));
                    sendToUpdateClientList();
                }
                break;

            /*case SurveyActivity.FREE_SURVEY_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    //showMessage(SweetAlertDialog.SUCCESS_TYPE,getString(R.string.saved),
                            //getString(R.string.success_saved));
                    sendDataToServer();
                    checkSentData = true;
                    sendToUpdateClientList();
                }
                break;*/
            case CALENDAR_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    showFragment(FRAGMENT_CLIENTS);
                    sendToUpdateClientList();
                    KIND_OF_MENU = MENU_CLIENTS;
                    invalidateOptionsMenu();
                }
                break;
        }
    }

    /**
     * Envia un mensaje de difucion indicando que los datos de la lista de usuario han cambiado
     */
    private void sendToUpdateClientList() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(NetClientHelper.CLIENTS_UPDATED);
        MainActivity.this.sendBroadcast(broadcastIntent);
    }

    private int route_iterations;
    private ArrayList<String> data;
    private Stack<RutaPackage> rutaPackageStack;

    @Override
    public void calculateRoute() {
        if (activeFragment != null && activeFragment instanceof LocationRecorderInterface) {
            try {
                final GeoPoint gestor_location = ((LocationRecorderInterface) activeFragment).getLocation();
                if (gestor_location != null) {
                    data = new ArrayList<>();
                    showProgressDialog("Preparando paquetes de rutas para enviar...");
                    new RoutePackageTask(gestor_location).execute();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            showMessage(SweetAlertDialog.ERROR_TYPE, "Localización Requerida", "Para calcular la ruta ... GPS");
        }
    }

    @Override
    public void filterData(String criteria) {
        if (activeFragment != null && activeFragment instanceof SearchClientListener) {
            ((SearchClientListener) activeFragment).filterData(criteria);
            log.info(String.format("Filtrando los datos por el criterio: %s", criteria));
            if (TextUtils.isEmpty(criteria) == false) {
                showSnackbar(criteria);
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("filter_criteria", criteria);
                editor.commit();
            }
        }
    }

    /**
     * Muestra el Snackbar luego de ser filtrada la lista de clientes
     * con el criterio de busqueda seleccionado y el boton para borrar
     * la busqueda activado
     *
     * @param criteria
     */
    public void showSnackbar(String criteria) {
        //Snackbar snackbar = Snackbar.make(findViewById(R.id.container),
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                String.format("Filtro: '%s'", criteria),
                Snackbar.LENGTH_INDEFINITE)
                .setAction("BORRAR", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filterData("");
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("filter_criteria", null);
                        editor.commit();
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.DKGRAY);
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private class RoutePackageTask extends AsyncTask<Void, Void, List<RutaPackage>> {
        protected GeoPoint gestor_location;

        public RoutePackageTask(GeoPoint gestor_location) {
            this.gestor_location = gestor_location;
        }

        @Override
        protected List<RutaPackage> doInBackground(Void... params) {
            return RutaPackageHelper.getRutaPackages(getDBHelper(), getApplicationContext(), gestor_location);
        }

        @Override
        protected void onPostExecute(List<RutaPackage> packages) {
            super.onPostExecute(packages);
            rutaPackageStack = new Stack<>();
            for (int i = packages.size() - 1; i >= 0; i--) {
                rutaPackageStack.add(packages.get(i));
            }
            hideProgressDialog();
            if (rutaPackageStack.size() == 0) {
                showMessage(SweetAlertDialog.WARNING_TYPE, "NO HAY RUTA", "No hay ruta para calcular");
            } else {
                showProgressDialog("Calculando Ruta...");
                route_iterations = 0;
                sendToCalculateRoute(null);
            }
        }
    }

    private void sendToUpdateMapRoute(ArrayList<String> json) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("numero_iter", json.size());
        editor.apply();
        Intent broadcastIntent = new Intent();
        broadcastIntent.putStringArrayListExtra("json", json);
        broadcastIntent.setAction(RutaPackageHelper.ROUTE_UPDATED);
        MainActivity.this.sendBroadcast(broadcastIntent);
    }


    private void sendToCalculateRoute(ArrayList<String> json) {
        if (rutaPackageStack.empty() && json != null) {
            hideProgressDialog();
            sendToUpdateMapRoute(json);
            invalidateOptionsMenu();
        } else {
            sendToCalculateRoute(rutaPackageStack.pop(), route_iterations++);
        }
    }

    private void sendToCalculateRoute(final RutaPackage route, final int iteration) {
        try {
            User user = checkUser();
            if (user != null) {
                String params = route.getURL() + "&mode=" + MainActivity.rutaMode;
                String url = "https://maps.googleapis.com/maps/api/directions/json?language=es&" + params + "&key=" + NetApi.KEY_GOOGLE_MAP;
                //log.info("url: "+url);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jroute) {
                                try {
                                    //log.error("este el devuelto: " + jroute.toString());
                                    if (jroute.has("error_message")) {
                                        hideProgressDialog();
                                        showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR",
                                                jroute.has("error_message") ? jroute.getString("error_message") :
                                                        "No se obtuvo ruta en el sistema!");
                                    } else {
                                        data.add(jroute.toString());
                                        sendToCalculateRoute(data);
                                    }
                                } catch (Exception e) {
                                    log.error(e);
                                    hideProgressDialog();
                                    showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                log.error(volleyError.toString());
                                hideProgressDialog();
                                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", volleyError.getMessage());
                            }
                        }
                );
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                        NetApi.TIMEOUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Revisa si hay datos por enviar al servidor e inicia la transmision
     */
    private void sendDataToServer() {
        try {
            Dao<SurveyData, Integer> surveyDataDao = getDBHelper().getSurveyDataDao();
            QueryBuilder<SurveyData, Integer> builder = surveyDataDao.queryBuilder();
            builder.where().eq(SurveyData.ESTADO, 0).or().eq(SurveyData.ESTADO, 1);
            builder.orderBy("id", true);
            List<SurveyData> data = builder.query();
            if (data.size() == 0) {
                hideProgressDialog();
                showMessage(SweetAlertDialog.NORMAL_TYPE, getString(R.string.no_data_title),
                        getString(R.string.no_data_to_send));
            } else {
                surveyToSend = new Stack<>();
                for (SurveyData d : data)
                    surveyToSend.push(d);
                showProgressDialog(getString(R.string.sending_data));
                sendData();
            }
        } catch (Exception e) {
            log.error("", e);
            showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
        }
    }

    /**
     * Extrae de la pila de encuestas por enviar e inicia su transmision
     */
    private void sendData() {
        if (surveyToSend.empty()) {
            hideProgressDialog();
            showMessage(SweetAlertDialog.SUCCESS_TYPE,
                    getString(R.string.data_sended_title),
                    getString(R.string.data_sended));
        } else {
            sendDataPlanner(surveyToSend.pop());
        }
    }

    /**
     * Dependiendo de el estado de la encuesta envia los datos o las fotos
     *
     * @param d
     */
    private void sendDataPlanner(ISenderData d) {
        if (d instanceof SurveyData) {
            SurveyData data = (SurveyData) d;
            if (data.getEstado() == 0)
                sendData(data);
            else
                sendPhoto(data);
        }

    }

    /**
     * Metono para enviar las fotos al servidor
     *
     * @param data
     */
    private void sendPhoto(SurveyData data) {
        populatePictureStack(data);
        sendPhotoToServer(data);
    }

    /**
     * Inicializa la pila de fotos a enviar por la encuesta
     *
     * @param data
     */
    private void populatePictureStack(SurveyData data) {
        try {
            pictureToSend = new Stack<>();
            JSONObject jdata = data.getJSONData();
            List<Survey> svs = getDBHelper().getSurveyDao().queryForEq(Survey.ETIQUETA, jdata.getString("_survey"));
            if (svs.size() > 0) {
                Dao<QuestionType, Integer> surveyDataDao = getDBHelper().getQuestionTypeDao();
                QueryBuilder<QuestionType, Integer> builder = surveyDataDao.queryBuilder();
                builder.where().eq(QuestionType.TIPO, "foto");
                List<QuestionType> typeQuestions = builder.query();
                if (typeQuestions.size() > 0) {
                    QuestionType questionType = typeQuestions.get(0);
                    QueryBuilder<SurveyQuestion, Integer> query = getDBHelper().getSurveyQuestionDao().queryBuilder();
                    List<SurveyQuestion> questions = query.where().eq("encuesta_id", svs.get(0).getId())
                            .and().eq(SurveyQuestion.TIPO, questionType.getWebId()).query();
                    //log.info("sql: " + query.prepareStatementString());
                    for (SurveyQuestion q : questions) {
                        if (jdata.has(q.getEtiqueta()) && !TextUtils.isEmpty(jdata.getString(q.getEtiqueta()))) {
                            File picture = null;
                            if (data.getWebId().equalsIgnoreCase("0")) {
                                picture = new File(FolderInspector.getPictureFolder(this, svs.get(0)), jdata.get(q.getEtiqueta()) + "");
                            } else {
                                picture = new File(FolderInspector.getPictureFolder(this, svs.get(0)),
                                        data.getPhotoPrefix(getDBHelper()) + q.getEtiqueta() + ".jpg");
                            }
                            if (picture.exists()) {
                                pictureToSend.push(picture.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
            hideProgressDialog();
            showMessage(SweetAlertDialog.ERROR_TYPE,
                    getString(R.string.error_title),
                    e.getMessage());
        }
    }

    /**
     * Envia la foto que esta en el tope de la pila al servidor
     *
     * @param data
     */
    private void sendPhotoToServer(final SurveyData data) {
        if (pictureToSend.empty()) {
            finishSurvey(data);
            sendData();
        } else {
            User user = checkUser();
            String image = pictureToSend.pop();
            //log.info(String.format("Enviando foto: %s", image));
            String field_image = new File(image).getName().split("\\.(?=[^\\.]+$)")[0];
            //String url = NetApi.getURL(this)+"ApiFoto/Upload?survey="+ data.getSurveyName()+"&token="+user.getToken()+"&foto="+field_image;
            String url = NetApi.URL + "movil/foto/" + data.getDataID() + "?token=" + user.getToken();
            log.info("url: " + url);
            CompressBitmap.decodeBitmapFile(image, 1200, 900, true);
            FileRequest request = new FileRequest(Request.Method.POST, url, image, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    log.info(String.format("Respuesta del servidor %s", response.toString()));
                    try {
                        JSONObject jresponse = new JSONObject(response);
                        if (jresponse.has("error")) {
                            log.error("Server request error sendPhotoToServer" + response.toString());
                            hideProgressDialog();
                            showMessage(SweetAlertDialog.ERROR_TYPE,
                                    getString(R.string.server_error_title),
                                    jresponse.getString("error"));
                        } else {
                            sendPhotoToServer(data);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                        hideProgressDialog();
                        showMessage(SweetAlertDialog.ERROR_TYPE,
                                getString(R.string.server_error_title),
                                e.fillInStackTrace().toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error("Error", error);
                    hideProgressDialog();
                    showMessage(SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.server_error_title),
                            error.fillInStackTrace().toString());
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    NetApi.TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetApi.getInstance(getApplicationContext()).addToRequestQueue(request);
        }
    }

    /**
     * Inicia la transmision de los datos pasados por parametros
     *
     * @param data
     */
    private void sendData(final SurveyData data) {
        User user = checkUser();
        if (user != null) {
            HashMap<String, String> params = getDataToSend(data);
            //params.put("token", user.getToken());
            String url = NetApi.URL + "movil/data?token=" + user.getToken();
            log.info("url: " + url);
            log.info("senddata: " + params.toString());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            log.info(String.format("Respuesta del servidor %s", response.toString()));
                            try {
                                if (response.has("error")) {
                                    log.error("Server request error1 sendData" + response.toString());
                                    hideProgressDialog();
                                    showMessage(SweetAlertDialog.ERROR_TYPE,
                                            getString(R.string.server_error_title),
                                            response.has("error") ? response.getString("error") :
                                                    response.toString());
                                } else {
                                    String dataID = response.has("id") ? response.getString("id") : "";
                                    data.setDataID(dataID);
                                    getDBHelper().getSurveyDataDao().update(data);
                                    finishSurvey(data);
                                    sendData();
                                }
                            } catch (Exception e) {
                                log.error("", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error("Server request error2 sendData", error);
                    hideProgressDialog();
                    showMessage(SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.server_error_title),
                            error.fillInStackTrace().toString());
                }
            });

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    NetApi.TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
        }
    }

    /**
     * Transforma los datos a enviar de una encusta en una hash, el
     * cual es utilizado como parametro en la peticion POST que se
     * le envia al servidor
     *
     * @param surveyData datos de la encuesta a enviar
     * @return
     */
    private HashMap<String, String> getDataToSend(SurveyData surveyData) {
        HashMap<String, String> params = new HashMap<String, String>();
        try {
            JSONObject data = new JSONObject(surveyData.getDatos());
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                params.put(key, data.getString(key));
            }
            params.put("client_web_id", surveyData.getWebId() + "");
        } catch (Exception e) {
            log.error("", e);
        }
        return params;
    }

    /**
     * Finaliza la encuesta.
     * Para el caso de las encuestas relacionada con clientes elimina el cliente de
     * la BD
     *
     * @param data
     */
    private void finishSurvey(SurveyData data) {
        try {
            data.setNextStatus();
            getDBHelper().getSurveyDataDao().update(data);
            if (data.esFinalizada() && !data.getWebId().equalsIgnoreCase("0")) {
                List<ClientData> clients = getDBHelper().getClientDao()
                        .queryForEq(ClientData.WEBID, data.getWebId());
                if (clients.size() > 0) {
                    ClientData c = clients.get(0);
                    c.setMovilStatus(2);
                    getDBHelper().getClientDao().update(c);
                    sendToUpdateClientList();
                }
            }
            //actualizando listado clientes una vez enviados
            /*if (data.esFinalizada() && data.getWebId() == 0) {
                sendToUpdateClientList();
            }*/
            if (data.getEstado() == 1) {
                surveyToSend.push(data);
            }
        } catch (Exception e) {
            log.error("", e);
            hideProgressDialog();
            showMessage(SweetAlertDialog.ERROR_TYPE,
                    getString(R.string.error_title),
                    e.getMessage());
        } finally {
            sendToUpdateClientList();
        }
    }

    /**
     * Funciona para realizar la peticion al servidor y descargar los datos  de los clientes que
     * deben ser gestionados
     */
    public void loadDataFromServer() {
        try {
            User user = checkUser();
            if (user != null) {
                String url = NetApi.URL + "movil/cliente/" + user.getGestorID() + "?token=" + user.getToken();
                log.info(url);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject client_data) {
                                try {
                                    log.info(client_data);
                                    JSONArray clientes = client_data.getJSONArray("clientes");
                                    List<String> ids = new ArrayList<>();
                                    for (int i = 0; i < clientes.length(); i++) {
                                        JSONObject c = clientes.getJSONObject(i);
                                        NetClientHelper.processClient(c, getDBHelper().getClientDao(), getDBHelper());
                                        ids.add(c.getString("_id"));
                                    }
                                    if (ids.size() > 0)
                                        deleteClient(ids);
                                    else
                                        deleteAllClient();
                                    Toast.makeText(MainActivity.this, getString(R.string.operacion_satisfactoria), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    log.error(e);
                                    showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
                                } finally {
                                    hideProgressDialog();
                                    sendToUpdateClientList();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                log.error(volleyError.toString());
                                hideProgressDialog();
                                showMessage(SweetAlertDialog.ERROR_TYPE, getString(R.string.server_error_title), volleyError.getMessage());
                            }
                        }
                );
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                        NetApi.TIMEOUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                showProgressDialog(getString(R.string.loading_client_data_from_server));
                NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void deleteAllClient() {
        try {
            Dao<ClientData, Integer> clientDao = getDBHelper().getClientDao();
            QueryBuilder<ClientData, Integer> queryBuilder = clientDao.queryBuilder();
            queryBuilder.where().gt("id", 0);
            for (ClientData c : queryBuilder.query())
                clientDao.delete(c);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Elimina todos clientes que
     * su id este en la lista de ids pasada por parametro
     *
     * @param ids
     */
    private void deleteClient(List<String> ids) {
        try {
            Dao<ClientData, Integer> clientDao = getDBHelper().getClientDao();
            QueryBuilder<ClientData, Integer> queryBuilder = clientDao.queryBuilder();
            queryBuilder.where().notIn(ClientData.WEBID, ids);
            for (ClientData c : queryBuilder.query())
                clientDao.delete(c);
        } catch (Exception e) {
            log.error("", e);
        }
    }


}
