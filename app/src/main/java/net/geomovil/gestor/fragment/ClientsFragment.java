package net.geomovil.gestor.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.geomovil.gestor.BaseActivity;
import net.geomovil.gestor.MainActivity;
import net.geomovil.gestor.R;
import net.geomovil.gestor.calendar.CalendarDayHelper;
import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.DatabaseHelper;
import net.geomovil.gestor.helper.NetClientHelper;
import net.geomovil.gestor.interfaces.FragmentChangeInterface;
import net.geomovil.gestor.interfaces.ProcessClientListener;
import net.geomovil.gestor.interfaces.SearchClientListener;
import net.geomovil.gestor.util.TextHelper;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientsFragment extends Fragment implements SearchClientListener {

    private final Logger log = Logger.getLogger(ClientsFragment.class.getSimpleName());

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DatabaseHelper db;
    private Context parent;

    public static ClientsFragment newInstance() {
        ClientsFragment fragment = new ClientsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public ClientsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clients, container, false);
        try {

            /*ClientDataInit data = new ClientDataInit(getDBHelper());
            data.initData();*/

            mRecyclerView = v.findViewById(R.id.my_recycler_view_client_list);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new ClientListAdapter(getClients());
            mRecyclerView.setAdapter(mAdapter);
            updateVisitStatus(v);
        } catch (Exception e) {
            log.error("", e);
        }
        return v;
    }


    private void updateVisitStatus(View v) throws Exception {
        Dao<ClientData, Integer> clientDao = getDBHelper().getClientDao();
        QueryBuilder<ClientData, Integer> queryBuilder = clientDao.queryBuilder();
        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
        String fecha = sharedPref.getString("fecha_inspeccion", null);
        if (fecha == null) {
            fecha = CalendarDayHelper.get(CalendarDay.today());
        }
        Where<ClientData, Integer> where = queryBuilder.where();
        where.and(
                where.or(
                        where.eq(ClientData.MOVILSTATUS, 0),
                        where.eq(ClientData.MOVILSTATUS, 1),
                        where.eq(ClientData.MOVILSTATUS, 2),
                        where.eq(ClientData.MOVILSTATUS, 3)
                ),
                where.eq(ClientData.FECHAINSPECCION, CalendarDayHelper.getDate(fecha))
        );

        ((TextView) v.findViewById(R.id.txt_clientes_quantity)).setText(queryBuilder.query().size() + "");

        where = queryBuilder.where();
        where.and(
                where.or(
                        where.eq(ClientData.MOVILSTATUS, 2),
                        where.eq(ClientData.MOVILSTATUS, 2)
                ),
                where.eq(ClientData.FECHAINSPECCION, CalendarDayHelper.getDate(fecha))
        );
        int visitados = queryBuilder.query().size();
        where = queryBuilder.where();
        List<ClientData> pendientes = where.and(
                where.or(
                        where.eq(ClientData.MOVILSTATUS, 0),
                        where.eq(ClientData.MOVILSTATUS, 0)
                ),
                where.eq(ClientData.FECHAINSPECCION, CalendarDayHelper.getDate(fecha))
        ).query();
        int total = visitados;
        ((TextView) v.findViewById(R.id.txt_clientes_visitados_quantity)).setText(total + "");
        ((TextView) v.findViewById(R.id.label_fecha_inspeccion)).setText(CalendarDayHelper.revertFormat(fecha, true));
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentChangeInterface) {
            ((FragmentChangeInterface) activity).changeFragment(this);
            ((MainActivity) activity).changeKingOfMenu(MainActivity.MENU_CLIENTS);
        }
        parent = activity;
    }

    protected DatabaseHelper getDBHelper() {
        if (db == null) {
            db = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return db;
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ((ClientListAdapter) mAdapter).refreshData(getClients());
                updateVisitStatus(getView());
            } catch (Exception e) {
                log.error("", e);
            }
        }
    };

    private List<ClientData> getClients() {
        List<ClientData> clients = new LinkedList<>();
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
            String fecha = sharedPref.getString("fecha_inspeccion", null);
            if (fecha == null) {
                fecha = CalendarDayHelper.get(CalendarDay.today());
            }
            log.info(String.format("Buscando los clientes de la fecha: %s", fecha));
            QueryBuilder<ClientData, Integer> queryBuilder = getDBHelper().getClientDao().queryBuilder();
            queryBuilder.where().eq(ClientData.FECHAINSPECCION, CalendarDayHelper.getDate(fecha));
            queryBuilder.orderBy(ClientData.NOMBRE, true);
            clients = queryBuilder.query();
        } catch (Exception e) {
            log.error("", e);
        }
        return clients;
    }

    @Override
    public void onDetach() {
        parent = null;
        getActivity().unregisterReceiver(myReceiver);
        //removeLocationListener();
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(myReceiver, new IntentFilter(NetClientHelper.CLIENTS_UPDATED));
    }


    /**
     * funcion que se ejecuta al dar clic sobre la lista de clientes
     *
     * @param client cliente seleccionado
     */
    private void processClient(ClientData client) {
        try {
            if (!client.isSaved() && !client.isSend() && !client.isInactive() && !client.isError() && parent != null &&
                    parent instanceof ProcessClientListener)
                ((ProcessClientListener) parent).processClient(client.getId());
            else if (client.isError() && parent instanceof BaseActivity) {
                ((BaseActivity) parent).showWarningMessage(SweetAlertDialog.WARNING_TYPE, "Gestión Fallida", "Error de Envio de Datos");
            } else if (client.isSaved() && parent instanceof BaseActivity) {
                ((BaseActivity) parent).showMessage(SweetAlertDialog.NORMAL_TYPE, "Estado de la Gestión", "Información pendiente por enviar");
            } else if (client.isInactive() && parent instanceof BaseActivity) {
                ((BaseActivity) parent).showMessage(SweetAlertDialog.NORMAL_TYPE, "Estado de la Gestión", "Reagendada en fecha posterior");
            } else if (client.isSend() && parent instanceof BaseActivity) {
                ((BaseActivity) parent).showMessage(SweetAlertDialog.NORMAL_TYPE, "Estado de la Gestión", "Información enviada al servidor");
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void filterData(String criteria) {
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
            String fecha = sharedPref.getString("fecha_inspeccion", null);
            if (fecha == null) {
                fecha = CalendarDayHelper.get(CalendarDay.today());
            }
            Dao<ClientData, Integer> clientDao = getDBHelper().getClientDao();
            QueryBuilder<ClientData, Integer> queryBuilder = clientDao.queryBuilder();
            Where<ClientData, Integer> where = queryBuilder.where();

            if (TextUtils.isEmpty(criteria)) {
                where.eq(ClientData.FECHAINSPECCION, CalendarDayHelper.getDate(fecha));
                queryBuilder.orderBy(ClientData.NOMBRE, true);
                ((ClientListAdapter) mAdapter).refreshData(queryBuilder.query());
            } else {
                if (criteria.matches("\\s*\\d+\\s*")) {
                    where.or(
                            where.like(ClientData.IDENTIFICACION, "%" + criteria + "%"),
                            where.like(ClientData.DIRECCION, "%" + criteria + "%")
                    );
                } else {
                    where.or(
                            where.like(ClientData.NOMBRE, "%" + criteria + "%"),
                            where.like(ClientData.DIRECCION, "%" + criteria + "%")
                    );
                }
                ((ClientListAdapter) mAdapter).refreshData(queryBuilder.query());
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    /**
     * Adaptador para mostrar la lista de los clientes
     */
    public class ClientListAdapter extends RecyclerView.Adapter<ClientListAdapter.ViewHolder> {
        private List<ClientData> cls;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            public TextView txt_gestion;
            public TextView txt_name;
            public TextView txt_cedula;
            public TextView txt_direction;
            //public TextView txt_barrio;
            public ImageView status_img;
            public ImageView location_img;
            public int position;
            public CardView _view;

            public ViewHolder(View v) {
                super(v);
                txt_gestion = v.findViewById(R.id.txt_gestion);
                txt_name = v.findViewById(R.id.txt_name);
                txt_cedula = v.findViewById(R.id.txt_cedula);
                txt_direction = v.findViewById(R.id.txt_direction);
                //txt_barrio = (TextView) v.findViewById(R.id.txt_barrio);
                status_img = v.findViewById(R.id.status_img);
                location_img = v.findViewById(R.id.location_img);
                v.findViewById(R.id.layout_container).setOnClickListener(this);
                v.findViewById(R.id.layout_container).setOnLongClickListener(this);
                _view = (CardView) v;
                v.setOnCreateContextMenuListener(ClientsFragment.this);
            }

            @Override
            public void onClick(View v) {
                processClient(cls.get(position));
            }

            @Override
            public boolean onLongClick(View v) {
                //showClientInfo(cls.get(position), v);
                return true;
            }
        }

        public ClientListAdapter(List<ClientData> cls) {
            this.cls = cls;
        }


        public void refreshData(List<ClientData> cls) {
            this.cls = cls;
            notifyDataSetChanged();
        }

        @Override
        public ClientListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.client_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ClientData c = cls.get(position);
            holder.txt_gestion.setText(c.getGestion());
            holder.txt_name.setText(c.getNombre() + " " + c.getApellido());
            String id = c.getIdentificacion();
            holder.txt_cedula.setText(id);

            if (!TextHelper.isEmptyData(c.getDireccion()) && c.getDireccion().trim().length() > 1) {
                holder.txt_direction.setText(c.getDireccion());
                holder.txt_direction.setVisibility(View.VISIBLE);
            } else {
                holder.txt_direction.setVisibility(View.GONE);
            }

            holder.position = position;

            if (!TextHelper.isEmptyData(c.getLatitudCapturada()) && !TextHelper.isEmptyData(c.getLongitudCapturada())) {
                holder.location_img.setImageResource(R.drawable.user_location);
                holder.location_img.setVisibility(View.VISIBLE);
            } else {
                holder.location_img.setVisibility(View.GONE);
            }

            if (c.isSaved()) {
                holder.status_img.setImageResource(R.drawable.ic_action_save);
                holder.status_img.setVisibility(View.VISIBLE);
            } else if (c.isError()) {
                holder.status_img.setImageResource(R.drawable.ic_action_error);
                holder.status_img.setVisibility(View.VISIBLE);
            } else if (c.isSend()) {
                holder.status_img.setImageResource(R.drawable.ic_action_upload);
                holder.status_img.setVisibility(View.VISIBLE);
            } else if (c.isInactive()) {
                holder.status_img.setImageResource(R.drawable.inactivo);
                holder.status_img.setVisibility(View.VISIBLE);
            } else {
                holder.status_img.setVisibility(View.GONE);
            }
            holder._view.setCardBackgroundColor(Color.parseColor((position % 2) == 0 ? "#f2f2f2" : "#ffffff"));
        }

        @Override
        public int getItemCount() {
            return cls.size();
        }
    }

}
