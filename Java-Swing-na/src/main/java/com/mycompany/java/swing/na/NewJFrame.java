package com.mycompany.java.swing.na;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class NewJFrame extends javax.swing.JFrame {
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Enviar");

        jScrollPane1.setViewportView(jTextPane1);

        jTextField1.setText("Escribe tu texto aqui");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(2, 2, 2)))
                .addGap(1, 1, 1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        
    }//GEN-LAST:event_jTextField1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private StringBuilder conversationLog;
    private String userName = "Usuario";
    
    public NewJFrame() {
        initComponents(); 
        conversationLog = new StringBuilder();
        
        conversationLog = new StringBuilder();
        
        String inputName = javax.swing.JOptionPane.showInputDialog(this, "¿Cuál es tu nombre?");
        
        if (inputName == null || inputName.trim().isEmpty()) {
            inputName = "Anónimo";
        }

        int randomId = (int)(Math.random() * 9000) + 1000;

        userName = inputName + "#" + randomId;
        
        this.setTitle("Chat Grupal - Soy: " + userName);
        jButton1.addActionListener(e -> sendMessage());
        jTextField1.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveLogFile();
                closeConnection();
            }
        });

        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            saveLogFile();     
            closeConnection();  
        }
    });
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
            SwingUtilities.invokeLater(() -> 
                appendToPane("Conectado al Chat Grupal.\n", Color.GRAY)
            );

            new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readUTF()) != null) {
                        String finalMsg = msg;
                        SwingUtilities.invokeLater(() -> receiveMessage(finalMsg));
                    }
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> appendToPane("Servidor desconectado.\n", Color.RED));
                }
            }).start();

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> appendToPane("Error: No se encontró el servidor.\n", Color.RED));
        }
    }

    private void sendMessage() {
        try {
            String msgText = jTextField1.getText().trim();
            
            if (msgText.isEmpty() || out == null) 
                return;
            String fullMessage = userName + ": " + msgText;
            
            out.writeUTF(fullMessage);

            jTextField1.setText(""); 
            
        } catch (IOException e) {
            appendToPane("Error enviando mensaje.\n", Color.RED);
        }
    }

    private void receiveMessage(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        
        conversationLog.append("[").append(timestamp).append("] ").append
        (message).append("\n");

        Color colorMsg = Color.GREEN; 
        if (message.contains(userName + ":")) {
            colorMsg = Color.BLUE; 
        }
        
        appendToPane("[" + timestamp + "] " + message + "\n", colorMsg);
    }

    private void appendToPane(String msg, Color c) {
        if (jTextPane1 == null) return;
        
        StyledDocument doc = jTextPane1.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, c);
        
        try {
            doc.insertString(doc.getLength(), msg, style);
            // Auto-scroll hacia abajo
            jTextPane1.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) { e.printStackTrace(); }
    }

    
    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
                    .getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log
        (java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new NewJFrame().setVisible(true);
        });
    }
    
    private void saveLogFile() {
    try {
        String fileName = "Chat_Grupal_" + System.currentTimeMillis() + ".txt";
        
        FileWriter writer = new FileWriter(fileName);
        writer.write(conversationLog.toString()); 
        writer.close();
        System.out.println("Historial guardado en: " + fileName);
    } catch (IOException e) {
        System.out.println("Error guardando historial.");
    }
}
}
