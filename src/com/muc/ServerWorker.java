/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author DOTT
 */

public class ServerWorker extends Thread {

    public final Socket clientSocket;  
    public final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();
    
    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }     
    
    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
    public boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }
    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }  
    private void handleClientSocket() throws IOException, InterruptedException{
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line=reader.readLine()) != null){
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0){
                String cmd = tokens[0];
                        if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)){
                            handleLogoff();
                            break;
                        } else if ("login".equalsIgnoreCase(cmd)) {
                            handleLogin(outputStream,tokens);
                        }else if ("msg".equalsIgnoreCase(cmd)) {
                            String[] tokensMsg = StringUtils.split(line, null, 3);
                            handleMessage(tokensMsg);
                        }else if ("join".equalsIgnoreCase(cmd)){
                            handleJoin(tokens);
                        }else if("leave".equalsIgnoreCase(cmd)){
                            handleLeave(tokens);
                        }
                        else{
                            String msg = "unknown " + cmd + "\r\n";
                            outputStream.write(msg.getBytes());
                        }
            }
            if ("quit".equalsIgnoreCase(line)){
                break;
            }
            //String msg = "You typed: " + line + "\n";
            //outputStream.write(msg.getBytes());
        }

        clientSocket.close();
    }
    
    private void handleMessage(String[] tokens) throws IOException{
        String sendTo = tokens[1];
        String body = tokens[2];
        
        boolean isTopic = sendTo.charAt(0) == '#';
        
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            if (isTopic){
                if (worker.isMemberOfTopic(sendTo)){
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\r\n";
                    worker.send(outMsg);
                }
            } else{
                if (sendTo.equalsIgnoreCase(worker.getLogin())){
                    String outMsg = "msg " + login + " " + body + "\r\n";
                    worker.send(outMsg);
                }
            }
        }
    }
    
    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        String onlineMsg = "offline " + login + "\r\n";
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList){
                    if (!login.equals(worker.getLogin())){
                       worker.send(onlineMsg);
                    }
        }
        clientSocket.close();
    }    
    
    public String getLogin() {
        return login;
    }
    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];
            
            if ((login.equals("ahmed") && password.equals("ahmed")) || (login.equals("mohamed") && password.equals("mohamed"))){
                String msg = "Successful Login\r\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println(login + " has successfully logged in\r\n");
                
                String onlineMsg = "online " + login + "\r\n";
                
                
                List<ServerWorker> workerList = server.getWorkerList();
                for(ServerWorker worker : workerList){
                    if (worker.getLogin() != null){
                        if (!login.equals(worker.getLogin())){
                            String msg2 = "online " + worker.getLogin() + "\r\n";
                            send(msg2);                 
                        }
                    }
                }
                for(ServerWorker worker : workerList){
                    if (!login.equals(worker.getLogin())){
                       worker.send(onlineMsg);
                    }
                }
            
            }else {
                String msg = "ERROR\r\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }
}
