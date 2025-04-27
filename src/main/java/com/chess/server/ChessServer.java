package com.chess.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chess.common.ChessBoard;
import com.chess.common.ChessMove;
import com.chess.common.ChessPiece;
import com.chess.common.Message;
import com.chess.common.PlayerInfo;
import com.google.gson.Gson;

public class ChessServer {
    private static final int PORT = 9999;
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<GameSession> gameSessions = new ArrayList<>();
    private final Gson gson = new Gson();
    
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Satranç sunucusu " + PORT + " portu üzerinde başlatıldı...");
            System.out.println("Bağlantı bekleniyor...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Yeni bir bağlantı kabul edildi: " + clientSocket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Sunucu başlatılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
    
    public void broadcast(Message message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("İstemci ayrıldı. Aktif bağlantı sayısı: " + clients.size());
        
        // İstemcinin oyun oturumunu kontrol et
        GameSession gameSession = findGameSessionByClient(client);
        if (gameSession != null) {
            Message disconnectMessage = new Message(Message.MessageType.DISCONNECT);
            disconnectMessage.setContent(client.getUsername() + " oyundan ayrıldı.");
            broadcast(disconnectMessage, null);
            
            // Eğer oyun devam ediyorsa, diğer oyuncuya kazandığını bildir
            if (!gameSession.getChessBoard().isGameOver()) {
                ClientHandler opponent = gameSession.getOpponent(client);
                if (opponent != null) {
                    gameSession.getChessBoard().setGameResult(opponent.getUsername() + " kazandı. (Rakip ayrıldı)");
                    
                    Message gameEndMessage = new Message(Message.MessageType.GAME_END);
                    gameEndMessage.setContent(gameSession.getChessBoard().getGameResult());
                    opponent.sendMessage(gameEndMessage);
                }
            }
            
            // Oturumu kaldır
            gameSessions.remove(gameSession);
        }
    }
    
    public void handleMessage(Message message, ClientHandler sender) {
        switch (message.getType()) {
            case CONNECT:
                handleConnect(message, sender);
                break;
            case READY:
                handleReady(message, sender);
                break;
            case MOVE:
                handleMove(message, sender);
                break;
            case CHAT:
                handleChat(message, sender);
                break;
            default:
                System.out.println("Bilinmeyen mesaj tipi: " + message.getType());
        }
    }
    
    private void handleConnect(Message message, ClientHandler sender) {
        String username = message.getContent();
        sender.setUsername(username);
        
        // Kullanıcı adını ayarla
        PlayerInfo playerInfo = new PlayerInfo(username);
        sender.setPlayerInfo(playerInfo);
        
        // Eğer bekleyen bir oyuncu varsa, eşleştir
        if (clients.size() >= 2) {
            boolean matched = false;
            for (ClientHandler client : clients) {
                if (client != sender && !isClientInGame(client)) {
                    createGameSession(sender, client);
                    matched = true;
                    break;
                }
            }
            
            if (!matched) {
                Message waitingMessage = new Message(Message.MessageType.CONNECT);
                waitingMessage.setContent("Eşleşme bekleniyor...");
                sender.sendMessage(waitingMessage);
            }
        } else {
            Message waitingMessage = new Message(Message.MessageType.CONNECT);
            waitingMessage.setContent("Rakip bekleniyor...");
            sender.sendMessage(waitingMessage);
        }
        
        // Diğer kullanıcılara bildir
        Message broadcastMessage = new Message(Message.MessageType.CONNECT);
        broadcastMessage.setContent(username + " bağlandı.");
        broadcast(broadcastMessage, sender);
        
        System.out.println(username + " bağlandı. Aktif bağlantı sayısı: " + clients.size());
    }
    
    private void handleReady(Message message, ClientHandler sender) {
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            sender.getPlayerInfo().setReady(true);
            
            // Diğer oyuncuya bildir
            Message readyMessage = new Message(Message.MessageType.READY);
            readyMessage.setContent(sender.getUsername() + " hazır.");
            ClientHandler opponent = gameSession.getOpponent(sender);
            if (opponent != null) {
                opponent.sendMessage(readyMessage);
            }
            
            // İki oyuncu da hazırsa oyunu başlat
            if (gameSession.isAllPlayersReady()) {
                startGame(gameSession);
            }
        }
    }
    
    private void handleMove(Message message, ClientHandler sender) {
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            ChessBoard board = gameSession.getChessBoard();
            ChessMove move = message.getMove();
            
            // Hamlenin geçerli olup olmadığını kontrol et
            if (move != null && isValidMove(move, board, sender)) {
                // Hamleyi yap
                board.makeMove(move);
                
                // Diğer oyuncuya hamleyi bildir
                Message moveMessage = new Message(Message.MessageType.MOVE);
                moveMessage.setMove(move);
                moveMessage.setSender(sender.getUsername());
                
                ClientHandler opponent = gameSession.getOpponent(sender);
                if (opponent != null) {
                    opponent.sendMessage(moveMessage);
                }
                
                // Oyun bitti mi kontrolü
                checkGameEnd(gameSession);
            } else {
                // Geçersiz hamle bildirimi
                Message invalidMoveMessage = new Message(Message.MessageType.MOVE);
                invalidMoveMessage.setContent("Geçersiz hamle!");
                sender.sendMessage(invalidMoveMessage);
            }
        }
    }
    
    private void handleChat(Message message, ClientHandler sender) {
        // Sadece aynı oyun oturumundaki oyunculara ilet
        GameSession gameSession = findGameSessionByClient(sender);
        if (gameSession != null) {
            ClientHandler opponent = gameSession.getOpponent(sender);
            if (opponent != null) {
                message.setSender(sender.getUsername());
                opponent.sendMessage(message);
            }
        }
    }
    
    private void createGameSession(ClientHandler player1, ClientHandler player2) {
        // Yeni bir oyun oturumu oluştur
        GameSession gameSession = new GameSession(player1, player2);
        gameSessions.add(gameSession);
        
        // Oyunculara renk ata
        player1.setPlayerInfo(new PlayerInfo(player1.getUsername(), ChessPiece.PieceColor.WHITE));
        player2.setPlayerInfo(new PlayerInfo(player2.getUsername(), ChessPiece.PieceColor.BLACK));
        
        // Oyunculara eşleşmeyi bildir
        Message matchMessage1 = new Message(Message.MessageType.GAME_START);
        matchMessage1.setContent("Rakibiniz: " + player2.getUsername() + ". Renginiz: Beyaz");
        matchMessage1.setPlayerInfo(player1.getPlayerInfo());
        player1.sendMessage(matchMessage1);
        
        Message matchMessage2 = new Message(Message.MessageType.GAME_START);
        matchMessage2.setContent("Rakibiniz: " + player1.getUsername() + ". Renginiz: Siyah");
        matchMessage2.setPlayerInfo(player2.getPlayerInfo());
        player2.sendMessage(matchMessage2);
        
        System.out.println("Yeni bir oyun başlatıldı: " + player1.getUsername() + " vs " + player2.getUsername());
    }
    
    private void startGame(GameSession gameSession) {
        // Oyun tahtasını başlat
        ChessBoard board = gameSession.getChessBoard();
        
        // İlk hamle sırasını beyaz taşlara ver
        board.setCurrentTurn(ChessPiece.PieceColor.WHITE);
        
        // Oyunculara oyunun başladığını bildir
        Message gameStartMessage = new Message(Message.MessageType.GAME_START);
        gameStartMessage.setContent("Oyun başladı! Hamle sırası: Beyaz");
        
        gameSession.getPlayer1().sendMessage(gameStartMessage);
        gameSession.getPlayer2().sendMessage(gameStartMessage);
        
        System.out.println("Oyun başladı: " + gameSession.getPlayer1().getUsername() + " vs " + gameSession.getPlayer2().getUsername());
    }
    
    private boolean isValidMove(ChessMove move, ChessBoard board, ClientHandler player) {
        // Geçerli sıra kontrolü
        if (board.getCurrentTurn() != player.getPlayerInfo().getColor()) {
            return false;
        }
        
        // Hamle yapılan konumda oyuncunun kendi taşı var mı?
        ChessPiece piece = board.getPiece(move.getStartRow(), move.getStartCol());
        if (piece == null || piece.getColor() != player.getPlayerInfo().getColor()) {
            return false;
        }
        
        // Hedef konumda kendi taşı var mı?
        ChessPiece targetPiece = board.getPiece(move.getEndRow(), move.getEndCol());
        if (targetPiece != null && targetPiece.getColor() == player.getPlayerInfo().getColor()) {
            return false;
        }
        
        // NOT: Bu basit bir kontrol, gerçek satranç mantığı daha karmaşıktır
        // İlerleyen aşamalarda taşların hareket kurallarını ekleyebilirsiniz
        
        return true;
    }
    
    private void checkGameEnd(GameSession gameSession) {
        // Şah mat veya beraberlik kontrolü
        // Şimdilik basit bir uygulama için es geçiyoruz
        // İlerleyen aşamalarda ekleyebilirsiniz
    }
    
    private GameSession findGameSessionByClient(ClientHandler client) {
        for (GameSession session : gameSessions) {
            if (session.hasPlayer(client)) {
                return session;
            }
        }
        return null;
    }
    
    private boolean isClientInGame(ClientHandler client) {
        return findGameSessionByClient(client) != null;
    }
    
    public static void main(String[] args) {
        new ChessServer().start();
    }
    
    // İç sınıf: İstemci Yöneticisi
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ChessServer server;
        private PrintWriter writer;
        private BufferedReader reader;
        private String username;
        private PlayerInfo playerInfo;
        private final Gson gson = new Gson();
        
        public ClientHandler(Socket socket, ChessServer server) {
            this.clientSocket = socket;
            this.server = server;
        }
        
        @Override
        public void run() {
            try {
                writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    try {
                        Message message = gson.fromJson(inputLine, Message.class);
                        server.handleMessage(message, this);
                    } catch (Exception e) {
                        System.err.println("Mesaj işlenirken hata oluştu: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("İstemci bağlantısında hata: " + e.getMessage());
            } finally {
                server.removeClient(this);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Soket kapatılırken hata: " + e.getMessage());
                }
            }
        }
        
        public void sendMessage(Message message) {
            writer.println(gson.toJson(message));
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public PlayerInfo getPlayerInfo() {
            return playerInfo;
        }
        
        public void setPlayerInfo(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
        }
    }
    
    // İç sınıf: Oyun Oturumu
    private static class GameSession {
        private final ClientHandler player1;
        private final ClientHandler player2;
        private final ChessBoard chessBoard;
        
        public GameSession(ClientHandler player1, ClientHandler player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.chessBoard = new ChessBoard();
        }
        
        public ClientHandler getPlayer1() {
            return player1;
        }
        
        public ClientHandler getPlayer2() {
            return player2;
        }
        
        public ChessBoard getChessBoard() {
            return chessBoard;
        }
        
        public boolean hasPlayer(ClientHandler player) {
            return player1 == player || player2 == player;
        }
        
        public ClientHandler getOpponent(ClientHandler player) {
            if (player1 == player) {
                return player2;
            } else if (player2 == player) {
                return player1;
            }
            return null;
        }
        
        public boolean isAllPlayersReady() {
            return player1.getPlayerInfo().isReady() && player2.getPlayerInfo().isReady();
        }
    }
} 