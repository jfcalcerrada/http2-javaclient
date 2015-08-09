/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.ui;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.*;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import org.json.*;

/**
 *
 * @author José Fernando
 */
public class Container extends javax.swing.JFrame {

    private static final String ENDPOINT = "https://ametrics.it.uc3m.es/start/win";

    private OkHttpClient httpClient;
    private OkHttpClient http2Client;

    private URL startEndpoint;

    /**
     * Creates new form Container
     */
    public Container() {
        initComponents();
        urlInput.setText(ENDPOINT);

        DefaultCaret caret = (DefaultCaret) log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        initClients();
    }

    private void initClients() {
        http2Client = new OkHttpClient();
        httpClient = http2Client.clone();

        httpClient.setProtocols(Arrays.asList(Protocol.HTTP_1_1));
        http2Client.setProtocols(Arrays.asList(Protocol.HTTP_2));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlLabel = new javax.swing.JLabel();
        urlInput = new javax.swing.JTextField();
        progress = new javax.swing.JProgressBar();
        scroll = new javax.swing.JScrollPane();
        log = new javax.swing.JTextPane();
        button = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(600, 400));
        setMinimumSize(new java.awt.Dimension(600, 400));
        setPreferredSize(new java.awt.Dimension(600, 400));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        urlLabel.setText("URL endpoint:");
        getContentPane().add(urlLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 80, 20));

        urlInput.setPreferredSize(new java.awt.Dimension(200, 20));
        urlInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlInputActionPerformed(evt);
            }
        });
        getContentPane().add(urlInput, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 10, 490, 20));
        getContentPane().add(progress, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 570, 20));

        scroll.setViewportView(log);

        getContentPane().add(scroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, 570, 250));

        button.setText("Start");
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                buttonMouseReleased(evt);
            }
        });
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });
        getContentPane().add(button, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 330, 70, 30));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void urlInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_urlInputActionPerformed

    private void buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonActionPerformed

    private void buttonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseReleased
        urlInput.setEnabled(false);
        progress.setValue(0);
        progress.setIndeterminate(true);
        button.setEnabled(false);

        try {
            test(urlInput.getText());

            /**/
        } catch (Exception e) {
            urlInput.setEnabled(true);
            progress.setIndeterminate(false);
            progress.setValue(0);

            JOptionPane.showMessageDialog(
                    this, "Invalid url: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE
            );
        }
    }//GEN-LAST:event_buttonMouseReleased

    public void test(String url) throws IOException, MalformedURLException {

        Thread worker = new Thread() {
            public void run() {
                String error = null;

                try {
                    int http = 0;
                    int h2 = 0;

                    URL entry = new URL(urlInput.getText());
                    Request request = new Request.Builder().url(entry.toString()).build();

                    Response response = httpClient.newCall(request).execute();
                    log.setText("Getting urls to test from: " + entry.toString());

                    JSONObject json = new JSONObject(response.body().string());

                    JSONArray urls = json.getJSONArray("urls");
                    progress.setMaximum(urls.length());
                    progress.setIndeterminate(false);

                    SocketFactory factory = SSLSocketFactory.getDefault();
                    JSONArray certs = new JSONArray();

                    for (int i = 0; i < urls.length(); ++i) {
                        URL url = new URL(urls.getString(i));

                        if ("https".equals(url.getProtocol())) {
                            certs.put(getCert(factory, url));
                        }

                        response = request(httpClient, url);
                        if (response != null) {
                            http++;
                        }

                        response = request(http2Client, url);
                        if (response != null) {
                            h2++;
                        }
                        progress.setValue(progress.getValue() + 1);
                    }

                    URL finish = new URL(
                            entry.getProtocol(),
                            entry.getHost(),
                            entry.getPort(),
                            json.getString("finish")
                    );
                    
                    json = new JSONObject();
                    json.put("certs", certs);
                    
                    request(httpClient, finish, json.toString());

                    JOptionPane.showMessageDialog(
                            Container.this,
                            "Successful requests: "
                            + "\nHTTP/1.1: " + Integer.toString(http) + "/" + Integer.toString(urls.length())
                            + "\nHTTP/2:    " + Integer.toString(h2) + "/" + Integer.toString(urls.length()),
                            "Completed!",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception e) {
                    error = e.getMessage();
                }

                if (error != null) {
                    System.out.println(error);
                    JOptionPane.showMessageDialog(
                            Container.this, "Error: " + error, "Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    progress.setIndeterminate(false);
                    progress.setValue(0);
                }

                urlInput.setEnabled(true);
                button.setEnabled(true);
            }
        };

        worker.start();
    }
    
    private Response request(OkHttpClient client, URL url) {
        return request(client, url, "");
    }

    private Response request(OkHttpClient client, URL url, String json) {
        Response response = null;

        try {
            log("Requesting: " + client.getProtocols().get(0) + " => " + url.toString());

            Builder builder = new Request.Builder().url(url.toString());
            if (!"".equals(json)) {
                builder.post(RequestBody.create(MediaType.parse("application/json"), json));
            }
            Request request = builder.build();
            
            response = client.newCall(request).execute();

            log("Completed: " + response.code());

        } catch (ConnectException e) {
            log("\n" + "Failed: " + e.getMessage());
        } catch (IOException e) {
            log("Failed: " + e.getMessage());
        }

        log.setCaretPosition(log.getText().length());

        return response;
    }

    private JSONObject getCert(SocketFactory factory, URL url) {
        
        JSONObject json = new JSONObject();
        json.put("host", url.getHost());
        json.put("port", url.getPort());
            
        try {
            log.setText(log.getText() + "\n" + "Get Certs: " + url.getHost() + ":" + url.getPort());
            
            SSLSocket socket = (SSLSocket) factory.createSocket(url.getHost(), url.getPort());
            socket.startHandshake();

            Certificate[] certs = socket.getSession().getPeerCertificates();
            String result = "";

            for (Certificate cert : certs) {
                
                if (cert instanceof X509Certificate) {
                    try {
                        ((X509Certificate) cert).checkValidity();
                        result += "OK ";

                    } catch (CertificateExpiredException cee) {
                        result += "Expired ";
                    } catch (CertificateNotYetValidException ex) {
                        result += "NotYetValid ";
                    }
                }
            }
            
            log.setText(log.getText() + "\n" + "Result: " + result.trim());
            json.put("result", result.trim());

        } catch (SSLException se) {
            log.setText(log.getText() + "\n" + "Error: SSLException (" + se.getMessage() + ")");
            json.put("result", "SSLException: " + se.getMessage());
        } catch (ConnectException ce) {
            log.setText(log.getText() + "\n" + "Error: ConnectException (" + ce.getMessage() + ")");
            json.put("result", "ConnectException: " + ce.getMessage());
        } catch (IOException ioe) {
            log.setText(log.getText() + "\n" + "Error: IOException (" + ioe.getMessage() + ")");
            json.put("result", "IOException: " + ioe.getMessage());
        }
        
        log.setCaretPosition(log.getText().length());

        return json;
    }

    
    private void log(String log) {
        this.log.setText(this.log.getText() + "\n" + log);
    }
    
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
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Container.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Container.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Container.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Container.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Container().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button;
    private javax.swing.JTextPane log;
    private javax.swing.JProgressBar progress;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTextField urlInput;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables
}