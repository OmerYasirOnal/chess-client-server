package com.chess.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

import com.chess.common.ChessMove;
import com.chess.common.Message;
import com.google.gson.Gson;

public class ChessClient {
    private String host;
    private int port;
    private String username;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private Consumer<Message> messageListener;
    private boolean connected;
    private String currentGameId;
    private String currentGameType;
    private final Gson gson = new Gson();
    
    public ChessClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.connected = false;
    }
    
    public void connect() throws IOException {
        socket = new Socket(host, port);
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        connected = true;
        
        // Kullanıcı adını sunucuya gönder
        Message connectMessage = new Message(Message.MessageType.CONNECT, username);
        sendMessage(connectMessage);
        
        // Mesaj dinleme thread'ini başlat
        startListening();
    }
    
    public void disconnect() {
        if (connected) {
            try {
                Message disconnectMessage = new Message(Message.MessageType.DISCONNECT);
                disconnectMessage.setContent(username + " ayrıldı.");
                sendMessage(disconnectMessage);
                
                connected = false;
                
                if (listenerThread != null) {
                    listenerThread.interrupt();
                }
                
                if (writer != null) {
                    writer.close();
                }
                
                if (reader != null) {
                    reader.close();
                }
                
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Bağlantı kapatılırken hata: " + e.getMessage());
            }
        }
    }
    
    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String inputLine;
                while (connected && (inputLine = reader.readLine()) != null) {
                    try {
                        Message message = gson.fromJson(inputLine, Message.class);
                        
                        // Handle ping messages automatically
                        if (message.getType() == Message.MessageType.PING) {
                            // Respond with a pong message
                            Message pongMessage = new Message(Message.MessageType.PONG);
                            sendMessage(pongMessage);
                            continue; // Don't pass ping messages to the client's listener
                        }
                        
                        // Handle ERROR messages that should disconnect
                        if (message.getType() == Message.MessageType.ERROR) {
                            // Pass the message to the listener first
                            if (messageListener != null) {
                                messageListener.accept(message);
                            }
                            
                            // When we get an ERROR message about username conflicts, 
                            // the server will close the connection
                            if (message.getContent() != null && 
                                message.getContent().contains("Username already in use")) {
                                // We will stop the client-side connection after the error message is displayed
                                connected = false;
                                break;
                            }
                        } else if (messageListener != null) {
                            messageListener.accept(message);
                        }
                    } catch (Exception e) {
                        System.err.println("Mesaj işleme hatası: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("Sunucu bağlantısı kesildi: " + e.getMessage());
                    connected = false;
                }
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    public void sendMessage(Message message) {
        if (connected && writer != null) {
            writer.println(gson.toJson(message));
        }
    }
    
    public void sendChatMessage(String content) {
        Message chatMessage = new Message(Message.MessageType.CHAT, content);
        chatMessage.setSender(username);
        sendMessage(chatMessage);
    }
    
    public void sendMoveMessage(ChessMove move) {
        Message moveMessage = new Message(Message.MessageType.MOVE);
        moveMessage.setMove(move);
        moveMessage.setSender(username);
        sendMessage(moveMessage);
    }
    
    public void sendResignMessage() {
        Message resignMessage = new Message(Message.MessageType.GAME_END);
        resignMessage.setContent(username + " teslim oldu.");
        resignMessage.setSender(username);
        sendMessage(resignMessage);
    }
    
    public void sendDrawOfferMessage() {
        Message drawMessage = new Message(Message.MessageType.CHAT);
        drawMessage.setContent("Beraberlik teklif ediyor.");
        drawMessage.setSender(username);
        sendMessage(drawMessage);
    }
    
    public void handleOpponentDisconnection() {
        Message disconnectMessage = new Message(Message.MessageType.DISCONNECT);
        disconnectMessage.setContent("Opponent disconnected");
        disconnectMessage.setSender(username);
        sendMessage(disconnectMessage);
    }
    
    public void handleGameCancellation() {
        Message cancelMessage = new Message(Message.MessageType.GAME_END);
        cancelMessage.setContent("Game cancelled");
        cancelMessage.setSender(username);
        sendMessage(cancelMessage);
    }
    
    public void handleGameStateChange(String state) {
        Message stateMessage = new Message(Message.MessageType.GAME_STATE);
        stateMessage.setContent(state);
        stateMessage.setSender(username);
        sendMessage(stateMessage);
    }
    
    public void setMessageListener(Consumer<Message> listener) {
        this.messageListener = listener;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getCurrentGameId() {
        return currentGameId;
    }
    
    public void setCurrentGameId(String gameId) {
        this.currentGameId = gameId;
    }
    
    public String getCurrentGameType() {
        return currentGameType;
    }
    
    public void setCurrentGameType(String gameType) {
        this.currentGameType = gameType;
    }
} 