package net.geomovil.gestor.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetApi {
 //   public static final String URL = "http://10.10.1.116:8081/";

    public static final String URL = "http://10.10.65.6:3000/";

    public static final String KEY_GOOGLE_MAP = "AIzaSyASUfxSB0WgFuNmfPduB0b6v9jpBbahQe8";

    public static final int TIMEOUT = 60000;
    private static NetApi mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;



    private NetApi(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized NetApi getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetApi(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public void cancelAllRequest(String TAG) {
        getRequestQueue().cancelAll(TAG);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
