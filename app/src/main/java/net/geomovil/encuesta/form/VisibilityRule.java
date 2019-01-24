package net.geomovil.encuesta.form;


import net.geomovil.gestor.database.QuestionRule;

public class VisibilityRule {
    protected int dependiente;
    protected String actividad;
    protected String valor;
    protected String operador;

    public VisibilityRule(int dependiente, String actividad, String valor,
                          String operador) {
        super();
        this.dependiente = dependiente;
        this.actividad = actividad;
        this.valor = valor;
        this.operador = operador;
    }

    public VisibilityRule(QuestionRule r) {
        this.dependiente = r.getPregunta();
        this.actividad = r.getAccion();
        this.valor = r.getValor();
        this.operador = "igual";
    }

    public int getDependiente() {
        return dependiente;
    }
    public String getActividad() {
        return actividad;
    }
    public String getValor() {
        return valor;
    }
    public String getOperador() {
        return operador;
    }

    @Override
    public String toString() {
        return "VisibilityRule [dependiente=" + dependiente + ", actividad="
                + actividad + ", valor=" + valor + ", operador=" + operador
                + "]";
    }
}
