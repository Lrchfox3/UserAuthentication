/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.lrchfox3.login.swing;

import com.gmail.lrchfox3.basedatos.BD;
import com.gmail.lrchfox3.javamail.EviarCorreo;

import com.gmail.lrchfox3.login.model.User;
import com.gmail.lrchfox3.utilitarios.Mensajes;
import com.gmail.lrchfox3.utilitarios.encriptar.Encriptar;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author lchinchilla
 */
public class Login extends javax.swing.JDialog {

    /**
     * Creates new form Login
     */
    public Login() {
        try {
            Inicializar();
            centerScreen();
            obtenerLogin();

            /*EviarCorreo mail = new EviarCorreo();
             mail.setFrom("webnotificacion@gmail.com");
             mail.setPassword("Infinit03".toCharArray());
             mail.setTo("luis.chinchilla@outlook.com");
             mail.setSubject("Recuperar contraseña");
             mail.setMessage("Tu contraseña es: 123456");
             mail.SEND();*/
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void Inicializar() throws Exception {
        com.gmail.lrchfox3.utilitarios.Utileria.lookAndFeelSystem();
        this.setModal(true);
        initComponents();
        mnuLogin.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        this.lblMandatorios.off();
        con.iniciarConexion("USER_AUTHENTICATION", BD.DBMSMSMYSQL, cnnConfig.getString("NombreServidor"),
                cnnConfig.getString("NombreBaseDatos"), cnnConfig.getString("Usuario"),
                cnnConfig.getString("Contrasenya"), null, Integer.valueOf(cnnConfig.getString("Puerto")));

    }

    private void recordarLogin() throws IOException, Exception {
        OutputStream file = new FileOutputStream("cookie.bin");
        OutputStream buffer = new BufferedOutputStream(file);
        try (ObjectOutput output = new ObjectOutputStream(buffer)) {
            List info = new ArrayList();
            info.add(new Encriptar().encrypt(this.txtUserName.getText()));
            info.add(new Encriptar().encrypt(new String(this.txtPassword.getPassword())));
            info.add(this.chkRecordar.isSelected());
            info.add(this.chkAutoLogin.isSelected());

            output.writeObject(info);
            output.flush();
        }

    }

    /**
     * Lee la parametrización para inicar sesión. Se obtiene recordar usuario de
     * sesion y el de poder iniciar automaticamente
     */
    private void obtenerLogin() throws IOException, Exception {

        File f = new File("cookie.bin");

        if (!f.exists()) {
            f.createNewFile();
        }

        InputStream file = new FileInputStream("cookie.bin");
        InputStream buffer = new BufferedInputStream(file);
        if (buffer != null) {
            try {
                ObjectInput input = new ObjectInputStream(buffer);
                if (input != null) {
                    List info = new ArrayList();
                    info = (List) input.readObject();
                    if (info != null) {
                        userInfo.username.setValue(info.get(0));
                        userInfo.password.setValue(info.get(1));
                        boolean recordar = (boolean) info.get(2);
                        boolean autologin = (boolean) info.get(3);

                        if (recordar) {
                            this.txtUserName.setText(new Encriptar().decrypt(userInfo.username.getValue().toString()));

                            this.txtPassword.setText(new Encriptar().decrypt(userInfo.password.getValue().toString()));

                            this.chkRecordar.setSelected(recordar);

                            if (autologin) {
                                this.chkAutoLogin.setSelected(autologin);
                                try {
                                    inicarSesion();
                                } catch (IOException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (SQLException ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (Exception ex) {
                                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Este método permite poder obtener la información del usuario que se
     * autentico a través de la ventana de inicio de sesion.
     *
     * @return retorna el usuario autenticado
     */
    public User getUsuario() {
        return userAuthenticated;
    }

    /**
     * verifica que las credenciales ingresadas se encuentren en la base de
     * datos y permite la autenticación del usuario.
     */
    private void inicarSesion() throws SQLException, Exception {
        boolean error = true;
        int codeErr = 101;

        String query = con.selectSQL(userInfo.getNombreTabla(), userInfo.getNombreCampos(), userInfo.Username().getNombre() + " = '"
                + this.txtUserName.getText() + "'");
        String[] value = new String[]{null, null};
        try (ResultSet rs = con.ejecutarSentencia(query)) {
            while (rs.next()) {
                value[0] = rs.getString(userInfo.Username().getNombre());
                value[1] = rs.getString(userInfo.Password().getNombre());
            }
        }
        lblMandatorios.off();
        if (this.txtUserName.isMandatory() || this.txtPassword.isMandatory()) {
            lblMandatorios.on();
            return;
        };

        if (value[0] != null) {
            if (value[0].compareTo(this.txtUserName.getText()) == 0) {
                value[1] = new Encriptar().decrypt(value[1]);
                if (value[1].compareTo(new String(this.txtPassword.getPassword())) == 0) {
                    userAuthenticated = userInfo;
                    System.out.println("encontro");
                    recordarLogin();
                    con.cerrarConexion();
                    error = false;
                    con.stmtActual.close();
                    setVisible(false);
                }
            }
        }
        if (error) {
            Mensajes msg = new Mensajes();
            if (msg.mensajes(this, con, codeErr, "", userInfo.getEtiquetasCampos(3, 4)) != javax.swing.JOptionPane.YES_OPTION) {
                recordarLogin();
                return;
            }
        }

    }

    /**
     * Centrar la ventana de dialog en el centro de la pantalla
     */
    private void centerScreen() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2);
        requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelBase1 = new com.gmail.lrchfox3.controles.paneles.JPanelBase();
        JPanelConTitulo1 = new com.gmail.lrchfox3.controles.paneles.JPanelConTitulo();
        lblUserName = new com.gmail.lrchfox3.controles.textos.JEtiquetaBase();
        txtUserName = new com.gmail.lrchfox3.controles.textos.JTextoBase();
        lblPassword = new com.gmail.lrchfox3.controles.textos.JEtiquetaBase();
        txtPassword = new com.gmail.lrchfox3.controles.textos.JTextoContrasenya();
        chkRecordar = new javax.swing.JCheckBox();
        chkAutoLogin = new javax.swing.JCheckBox();
        lblMandatorios = new com.gmail.lrchfox3.controles.textos.JEtiquetaObligatorios();
        btnAceptar = new com.gmail.lrchfox3.controles.botones.JBotonBase();
        mnuLogin = new javax.swing.JMenuBar();
        jMenu2 = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/gmail/lrchfox3/login/resources/application"); // NOI18N
        setTitle(bundle.getString("INICIO_SESION")); // NOI18N
        setAlwaysOnTop(true);
        setIconImage(new ImageIcon(getClass().getResource("/com/gmail/lrchfox3/login/resources/imagenes/login16x16.png")).getImage());
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jPanelBase1.setLayout(new java.awt.BorderLayout());

        JPanelConTitulo1.setTitulo(bundle.getString("INICIAR_SESION")); // NOI18N

        lblUserName.setText("Usuario");
        lblUserName.setBindingBean(userInfo);
        lblUserName.setIndexBindingBean(3);

        txtUserName.setBindingBean(userInfo);
        txtUserName.setIndexBindingBean(3);
        txtUserName.setNextFocusableComponent(txtPassword);
        txtUserName.setRoundedCornerBorder(true);
        txtUserName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtUserNameKeyReleased(evt);
            }
        });

        lblPassword.setText("Contraseña");
        lblPassword.setBindingBean(userInfo);
        lblPassword.setIndexBindingBean(4);

        txtPassword.setBindingBean(userInfo);
        txtPassword.setIndexBindingBean(4);
        txtPassword.setMinimumSize(new java.awt.Dimension(16, 24));
        txtPassword.setRoundedCornerBorder(true);
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPasswordKeyReleased(evt);
            }
        });

        chkRecordar.setText(bundle.getString("REMEMBER")); // NOI18N
        chkRecordar.setNextFocusableComponent(chkAutoLogin);

        chkAutoLogin.setText(bundle.getString("AUTO_LOGIN")); // NOI18N
        chkAutoLogin.setNextFocusableComponent(txtUserName);

        lblMandatorios.setText(bundle.getString("LBL_OBLIGATORIOS")); // NOI18N
        lblMandatorios.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N

        btnAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gmail/lrchfox3/login/resources/imagenes/login16x16.png"))); // NOI18N
        btnAceptar.setText(bundle.getString("BTN_INICIAR")); // NOI18N
        btnAceptar.setToolTipText(bundle.getString("TOOLTIP_BTN_INICIAR")); // NOI18N
        btnAceptar.setFlatButton(true);
        btnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAceptarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout JPanelConTitulo1Layout = new javax.swing.GroupLayout(JPanelConTitulo1);
        JPanelConTitulo1.setLayout(JPanelConTitulo1Layout);
        JPanelConTitulo1Layout.setHorizontalGroup(
            JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPanelConTitulo1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMandatorios, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelConTitulo1Layout.createSequentialGroup()
                            .addComponent(chkRecordar)
                            .addGap(18, 18, 18)
                            .addComponent(chkAutoLogin)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JPanelConTitulo1Layout.createSequentialGroup()
                            .addGroup(JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(JPanelConTitulo1Layout.createSequentialGroup()
                                    .addComponent(lblUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(126, 126, 126))
                                .addGroup(JPanelConTitulo1Layout.createSequentialGroup()
                                    .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(88, 88, 88))
        );
        JPanelConTitulo1Layout.setVerticalGroup(
            JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JPanelConTitulo1Layout.createSequentialGroup()
                .addGroup(JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(JPanelConTitulo1Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(lblPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(JPanelConTitulo1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(lblUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(JPanelConTitulo1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkRecordar)
                    .addComponent(chkAutoLogin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMandatorios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );

        jPanelBase1.add(JPanelConTitulo1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelBase1);

        mnuLogin.setBorderPainted(false);

        jMenu2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gmail/lrchfox3/login/resources/imagenes/adduser.png"))); // NOI18N
        jMenu2.setText(bundle.getString("CREAR_USUARIO")); // NOI18N
        jMenu2.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        mnuLogin.add(jMenu2);

        jMenu1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/gmail/lrchfox3/login/resources/imagenes/question.png"))); // NOI18N
        jMenu1.setText(bundle.getString("RECORDAR_PASSWORD")); // NOI18N
        jMenu1.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        mnuLogin.add(jMenu1);

        setJMenuBar(mnuLogin);

        setBounds(0, 0, 366, 170);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAceptarActionPerformed
        try {
            inicarSesion();
        } catch (IOException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnAceptarActionPerformed

    private void txtPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPasswordKeyReleased
        if (evt.getKeyCode() == 10) {
        try {
            inicarSesion();
        } catch (IOException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }//GEN-LAST:event_txtPasswordKeyReleased

    private void txtUserNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUserNameKeyReleased
if (evt.getKeyCode() == 10) {
        try {
            inicarSesion();
        } catch (IOException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
        }
        }        // TODO add your handling code here:
    }//GEN-LAST:event_txtUserNameKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                new Login().setVisible(true);
            }
        });
    }

    /*@Override
     public Image getIconImage() {
     return new javax.swing.ImageIcon(getClass().getResource("/com/gmail/lrchfox3/login/resources/imagenes/login16x16.png")).getImage();
     }*/
    private BD con = new BD();
    private User userInfo = new User();
    private User userAuthenticated;
    private final java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/gmail/lrchfox3/login/resources/application"); // NOI18N
    private final ResourceBundle cnnConfig = ResourceBundle.getBundle("com/gmail/lrchfox3/login/resources/config");
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.gmail.lrchfox3.controles.paneles.JPanelConTitulo JPanelConTitulo1;
    private com.gmail.lrchfox3.controles.botones.JBotonBase btnAceptar;
    private javax.swing.JCheckBox chkAutoLogin;
    private javax.swing.JCheckBox chkRecordar;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private com.gmail.lrchfox3.controles.paneles.JPanelBase jPanelBase1;
    private com.gmail.lrchfox3.controles.textos.JEtiquetaObligatorios lblMandatorios;
    private com.gmail.lrchfox3.controles.textos.JEtiquetaBase lblPassword;
    private com.gmail.lrchfox3.controles.textos.JEtiquetaBase lblUserName;
    private javax.swing.JMenuBar mnuLogin;
    private com.gmail.lrchfox3.controles.textos.JTextoContrasenya txtPassword;
    private com.gmail.lrchfox3.controles.textos.JTextoBase txtUserName;
    // End of variables declaration//GEN-END:variables
}
