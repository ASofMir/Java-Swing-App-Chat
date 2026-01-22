package com.mycompany.java.swing.na;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final int PORT = 5000;
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static StringBuilder serverLog = new StringBuilder();

    public static void main(String[] args) {
        System.out.println("--- Servidor de Chat Iniciado en Puerto " + PORT + " ---");
        
        // Guardar log al cerrar el servidor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveLogFile()));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String logMsg = "[" + timestamp + "] " + message;
        System.out.println(logMsg);
        serverLog.append(logMsg).append("\n");

        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private static void saveLogFile() {
        try {
            String fileName = "Log_Servidor_" + System.currentTimeMillis() + ".txt";
            FileWriter writer = new FileWriter(fileName);
            writer.write(serverLog.toString());
            writer.close();
            System.out.println("Log guardado en: " + fileName);
        } catch (IOException e) {
            System.out.println("Error guardando log.");
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                String message;
                while ((message = in.readUTF()) != null) {
                    broadcast(message, this);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado.");
            } finally {
                try {
                    clients.remove(this);
                    socket.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        public void sendMessage(String message) {
            try {
                out.writeUTF(message);
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
}