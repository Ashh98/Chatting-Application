/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author DOTT
 */
public class Server extends Thread{
    private final int serverPort;
    public ArrayList<ServerWorker> workerList = new ArrayList<>();
    
    
    
    public Server(int serverPort){
        this.serverPort = serverPort;
    }
    
    public List<ServerWorker> getWorkerList() {
        return workerList;
    }
    
    @Override
    public void run() {
        try{
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(true) {
                System.out.println("About to accept client connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from "+clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workerList.add(worker);
                worker.start();
            }
        }
        catch (IOException e){
        }
    } 
    public void removeWorker(ServerWorker serverWorker){
        workerList.remove(serverWorker);
    }
}

    

