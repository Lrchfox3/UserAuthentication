/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.lrchfox3.login.model;

import com.gmail.lrchfox3.basedatos.Base;
import com.gmail.lrchfox3.basedatos.Campo;
import com.gmail.lrchfox3.basedatos.SqlTipos;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lchinchilla
 */
public class User extends Base {

    public Campo sequence = null;
    public Campo usercode = null;
    public Campo username = null;
    public Campo password = null;
    public Campo email = null;
    public Campo creationDate = null;
    public Campo image = null;
    public Campo state = null;

    private final java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/gmail/lrchfox3/login/resources/application"); // NOI18N

    public User() {
        sequence = new Campo(bundle.getString("USER_SEQUENCE"), bundle.getString("SECUENCIA"), SqlTipos.INTEGER, true, false);
        usercode = new Campo(bundle.getString("USER_USER_CODE"), bundle.getString("CODIGO_USUARIO"), SqlTipos.VARCHAR, 7, false);
        username = new Campo(bundle.getString("USER_USER_NAME"), bundle.getString("USER_NAME"), SqlTipos.VARCHAR, 32, false);
        password = new Campo(bundle.getString("USER_PASSWORD"), bundle.getString("PASSWORD"), SqlTipos.VARCHAR, 16, false);

        email = new Campo(bundle.getString("USER_EMAIL"), bundle.getString("EMAIL"), SqlTipos.VARCHAR, 16, true);
        creationDate = new Campo(bundle.getString("USER_CREATION_DATE"), bundle.getString("USER_CREATION_DATE"), false, SqlTipos.DATE_TIME);
        image = new Campo(bundle.getString("USER_IMAGE"), bundle.getString("USER_IMAGE"), SqlTipos.VARCHAR, 512, true);
        state = new Campo(bundle.getString("USER_STATE"), bundle.getString("USER_STATE"), SqlTipos.VARCHAR, 1, true);

        setTabla("USER");
        setTitulo("Usuarios");
    }

    public Campo Sequence() {
        return sequence;
    }

    public Campo Usercode() {
        return usercode;
    }

    public Campo Username() {
        return username;
    }

    public Campo Password() {
        return password;
    }

    public int getSequence() {
        return sequence.getIntValue();
    }

    public void setSequence(int codigo) {
        this.sequence.setValue(codigo);
    }

    public String getUsercode() {
        return usercode.getStringValue();
    }

    public void setUsercode(String value) {
        this.usercode.setValue(value);
    }

    public String getUsername() {
        return username.getStringValue();
    }

    public void setUsername(String value) {
        this.username.setValue(value);
    }

    public String getPassword() {
        return password.getStringValue();
    }

    public void setPassword(String value) {
        this.password.setValue(value);
    }

    public String getEmail() {
        return email.getStringValue();
    }

    public void setEmail(String value) {
        this.email.setValue(value);
    }

    public String getCreationDate() {
        return creationDate.getStringValue();
    }

    public void setCreationDate(java.sql.Date value) {
        this.creationDate.setValue(value);
    }

    public String getImagen() {
        return image.getStringValue();
    }

    public void setImage(String value) {
        this.image.setValue(value);
    }

    public String getState() {
        return state.getStringValue();
    }

    public void setState(String value) {
        this.state.setValue(value);
    }

    public String getNombreTabla() {
        return this.getTabla();
    }

    public List<Campo> getCampos() throws Exception {
        List<Campo> lst = new ArrayList<>();
        lst.add(sequence);
        lst.add(usercode);
        lst.add(username);
        lst.add(password);

        lst.add(email);
        lst.add(creationDate);
        lst.add(image);
        lst.add(state);

        return lst;
    }

    @Override
    public String toString() {
        return this.getUsername();
    }
}
