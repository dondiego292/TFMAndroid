package net.geomovil.gestor;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.geomovil.gestor.database.User;
import net.geomovil.gestor.util.DeviceTool;
import net.geomovil.gestor.util.FolderInspector;
import net.geomovil.gestor.util.NetApi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LoginActivity extends BaseActivity {

    private final Logger log = Logger.getLogger(LoginActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        configureLog();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((Button) findViewById(R.id.btn_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView) findViewById(R.id.txt_app_name)).setText(getString(R.string.app_name) + " " + pInfo.versionName);
            checkUserLogued();
        } catch (Exception e) {
            log.error("", e);
        }
        setTitle(getString(R.string.app_name));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void checkUserLogued() {
        User user = getDBHelper().getUser();
        if (user != null) {
            startClientListActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    protected void loginUser() {
        String user = ((EditText) findViewById(R.id.edt_user_name)).getText().toString();
        String pass = ((EditText) findViewById(R.id.edt_user_pass)).getText().toString();
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
            showMessage(SweetAlertDialog.ERROR_TYPE, getString(R.string.auth_error), getString(R.string.user_and_pass_required));
        } else {
            showProgressDialog(String.format(getString(R.string.starting_session)));
            HashMap<String, String> params = new HashMap<>();
            params.put("username", user);
            params.put("password", pass);
            params.put("imei", DeviceTool.getDeviceUniqueID(this));
            String url = NetApi.URL + "movil/login";
            log.info("url: " + url);
            log.info("params: " + params.toString());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    log.info(String.format("Respuesta del servidor %s", response.toString()));
                    try {
                        hideProgressDialog();
                        if (response.has("ok") && !response.getBoolean("ok")) {
                            log.error("Server request error " + response.toString());
                            showMessage(SweetAlertDialog.ERROR_TYPE,
                                    getString(R.string.auth_error),
                                    response.getString("mensaje"));
                        } else {
                            //log.info("respuesta " + response.toString());
                            createUser(response);
                        }
                    } catch (Exception e) {
                        log.error("", e);
                        hideProgressDialog();
                        showMessage(SweetAlertDialog.ERROR_TYPE,
                                getString(R.string.auth_error),
                                e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error(error.fillInStackTrace().toString());
                    hideProgressDialog();
                    showMessage(SweetAlertDialog.ERROR_TYPE,
                            getString(R.string.server_error_title),
                            error.fillInStackTrace().toString());
                }
            })/*{
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");

                    return params;
                }
            }*/;
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    NetApi.TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            jsObjRequest.setShouldCache(false);
            NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
        }
    }

    private void createUser(JSONObject response) {
        try {
            String user = ((EditText) findViewById(R.id.edt_user_name)).getText().toString();
            String pass = ((EditText) findViewById(R.id.edt_user_pass)).getText().toString();
            String token = response.getString("token");
            String nombres = response.has("nombres") ? response.getString("nombres") : "";
            String apellidos = response.has("apellidos") ? response.getString("apellidos") : "";
            String nickname = response.has("nickname") ? response.getString("nickname") : "";
            String gestorID = response.has("gestorID") ? response.getString("gestorID") : "";
            User u = getDBHelper().getUser();
            if (u != null)
                getDBHelper().getUserDao().delete(u);
            //String login, String password, String token, String nombres, String apellidos, String nickname, String gestorID
            getDBHelper().getUserDao().create(new User(user, pass, token, nombres, apellidos, nickname, gestorID));
            startClientListActivity();
        } catch (Exception e) {
            log.error("", e);
            showMessage(SweetAlertDialog.ERROR_TYPE,
                    getString(R.string.server_error_title),
                    e.fillInStackTrace().toString());
        }
    }

    private void startClientListActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(mainIntent, 1);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        finish();
    }


    /**
     * Configuracion inicial del log del sistema
     */
    public void configureLog() {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(FolderInspector.getProjectFolder(this) + File.separator + "mobile.log");
        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.configure();
    }
}
