package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "user")
public class User implements Serializable{
    private static final long serialVersionUID = -6570375943817420090L;

    private static final String LOGIN = "login";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "token";
    private static final String NOMBRES = "nombres";
    private static final String APELLIDOS = "apellidos";
    private static final String NICKNAME = "nickname";
    private static final String GESTORID = "gestorId";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = LOGIN)
    private String login;

    @DatabaseField(columnName = PASSWORD)
    private String password;

    @DatabaseField(columnName = TOKEN)
    private String token;

    @DatabaseField(columnName = NOMBRES)
    private String nombres;

    @DatabaseField(columnName = APELLIDOS)
    private String apellidos;

    @DatabaseField(columnName = NICKNAME)
    private String nickname;

    @DatabaseField(columnName = GESTORID)
    private String gestorID;

    public User() {
    }

    public User(String login, String password, String token, String nombres, String apellidos, String nickname, String gestorID) {
        this.login = login;
        this.password = password;
        this.token = token;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.nickname = nickname;
        this.gestorID = gestorID;
    }

    public String getLogin() {
        return login;
    }

    public String getToken() {
        return token;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNickname() {
        return nickname;
    }

    public String getGestorID() {
        return gestorID;
    }
}
