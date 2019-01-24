package net.geomovil.encuesta.form;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.dao.Dao;

import net.geomovil.gestor.database.QuestionRule;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class FormWidget implements FormDependentListener {
    private final Logger log = Logger.getLogger(FormWidget.class.getSimpleName());
    public static final String MOSTRAR = "mostrar";
    public static final String OCULTAR = "ocultar";
    public static final String IGUAL = "igual";
    public static final String DISTINTO = "distinto";

    protected ViewGroup _viewgroup;
    protected Context _context;
    protected String label;
    protected String text;
    protected String expresion;
    protected boolean required;
    protected boolean visible;
    protected int id;
    protected List<VisibilityRule> reglas;
    protected List<FormDependentListener> dependents;
    protected HashMap<Integer, Boolean> dependents_visibility;

    public FormWidget(Context _context, JSONObject data) throws Exception {
        this._context = _context;
        this.expresion = data.has("expresion") ? data.getString("expresion") : "";
        this.label = data.has("label") ? data.getString("label") : "label";
        this.text = data.has("name") ? data.getString("name") : "name";
        this.id = data.has("id") ? data.getInt("id") : 0;
        this.required = data.has("required") ? data.getBoolean("required") : false;
        this.visible = true;
        this.reglas = new ArrayList<VisibilityRule>();
        this.dependents = new ArrayList<FormDependentListener>();
    }

    public abstract void setValue(String data);

    public abstract String getValue();

    public abstract JSONObject getError();

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            _viewgroup.setVisibility(View.VISIBLE);
        } else {
            _viewgroup.setVisibility(View.GONE);
        }
        this.visible = visible;
    }

    public void loadRules(Dao<QuestionRule, Integer> questionRuleDao) {
        try {
            List<QuestionRule> rls = questionRuleDao.queryForEq(QuestionRule.DEPENDE, id);
            for (QuestionRule r : rls)
                reglas.add(new VisibilityRule(r));
        } catch (Exception e) {
            log.error("", e);
        }
        onChangeValueEvent();
    }

    /**
     * Metodo que se utiliza en las reglas de visibilidad. Tiene como
     * objetivo determinar si el componente tiene el valor pasado por
     * parametro
     *
     * @param value
     * @return
     */
    public abstract boolean haveValue(String value);

    protected void sendVisibilityNotification(int component, boolean visibility) {
        ((VisibilityRuleListener) _context).changeVisbility(component, visibility);
    }

    protected void onChangeValueEvent() {
        dependents_visibility = new HashMap<>();
        for (VisibilityRule rule : reglas) {
            if (haveValue(rule.getValor())) {
                setVisibilityToDependent(rule.dependiente, MOSTRAR.equals(rule.getActividad()));
				/*
                if(DISTINTO.equals(rule.getOperador())){
					sendVisibilityNotification(rule.getDependiente(), (MOSTRAR.equals(rule.getActividad())));
				} else if(IGUAL.equals(rule.getOperador())){
					sendVisibilityNotification(rule.getDependiente(), (!MOSTRAR.equals(rule.getActividad())));
				} else {
					sendVisibilityNotification(rule.getDependiente(), true);
				}
				*/
            } else {
                setVisibilityToDependent(rule.dependiente, false);
                /*
				if(IGUAL.equals(rule.getOperador())){
					sendVisibilityNotification(rule.getDependiente(), (MOSTRAR.equals(rule.getActividad())));
				} else if(DISTINTO.equals(rule.getOperador())) {
					sendVisibilityNotification(rule.getDependiente(), (!MOSTRAR.equals(rule.getActividad())));
				} else {
					sendVisibilityNotification(rule.getDependiente(), true);
				}
                */
            }
        }
        applyVisbilityRules();
    }

    /**
     * Segun las reglas ejecutadas en el metodo onChangeValueEvent
     * aplica las reglas de visibilidad establecida
     */
    private void applyVisbilityRules() {
        for (Integer key : dependents_visibility.keySet()) {
            sendVisibilityNotification(key, dependents_visibility.get(key));
        }
    }

    /**
     * Asigna regla de visibilidad.
     *
     * @param dependiente
     * @param b
     */
    private void setVisibilityToDependent(int dependiente, boolean b) {
        if (dependents_visibility.containsKey(dependiente)) {
            if (!dependents_visibility.get(dependiente))
                dependents_visibility.put(dependiente, b);
        } else {
            dependents_visibility.put(dependiente, b);
        }
    }

    @Override
    public void parentValueChange(String value) {

    }

    public void addDependent(FormDependentListener d) {
        this.dependents.add(d);
    }

    public abstract String getFinalValue();
}
