package com.chess.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.chess.common.ChessBoard;
import com.chess.common.ChessMove;
import com.chess.common.ChessPiece;

public class ChessBoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 60;
    private static final int BOARD_SIZE = 8;
    
    private final ChessClient client;
    private final ChessBoard chessBoard;
    
    private ChessPiece.PieceColor playerColor;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private final Image[][] pieceImages = new Image[2][6]; // [color][type]
    
    private boolean boardFlipped = false;
    
    private List<Point> validMoves = new ArrayList<>();
    private Point invalidMove = null;
    private Timer flashTimer;
    private boolean isFlashing = false;
    
    // Bilgilendirme metinleri için değişkenler
    private String statusMessage = "";
    private Color statusColor = Color.BLACK;
    private boolean showCoordinates = true;
    private boolean showLastMove = true;
    private Point lastMoveStart = null;
    private Point lastMoveEnd = null;
    
    // Tema için renkler
    private final Color LIGHT_SQUARE_COLOR = new Color(240, 217, 181);
    private final Color DARK_SQUARE_COLOR = new Color(181, 136, 99);
    private final Color SELECTED_SQUARE_COLOR = new Color(255, 255, 0, 128);
    private final Color VALID_MOVE_INDICATOR_COLOR = new Color(0, 255, 0, 150);
    private final Color CAPTURE_INDICATOR_COLOR = new Color(255, 0, 0, 150);
    private final Color CHECK_INDICATOR_COLOR = new Color(255, 0, 0, 100);
    private final Color LAST_MOVE_COLOR = new Color(0, 0, 255, 80);
    
    public ChessBoardPanel(ChessClient client) {
        this.client = client;
        this.chessBoard = new ChessBoard();
        this.playerColor = ChessPiece.PieceColor.WHITE; // Default to white
        
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));
        
        loadPieceImages();
        
        // Timer for flashing effect
        flashTimer = new Timer(500, e -> {
            isFlashing = !isFlashing;
            repaint();
            
            // Stop after second flash
            if (((Timer)e.getSource()).getActionListeners()[0].hashCode() % 3 == 0) {
                ((Timer)e.getSource()).stop();
                isFlashing = false;
                invalidMove = null;
                repaint();
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
        
        // Add informative tooltips
        setToolTipText("Click on the board to make moves.");
    }
    
    private void loadPieceImages() {
        try {
            // Load piece images
            String[] colors = {"white", "black"};
            String[] types = {"pawn", "rook", "knight", "bishop", "queen", "king"};
            
            for (int colorIdx = 0; colorIdx < 2; colorIdx++) {
                for (int typeIdx = 0; typeIdx < 6; typeIdx++) {
                    String imagePath = "/pieces/" + colors[colorIdx] + "_" + types[typeIdx] + ".png";
                    pieceImages[colorIdx][typeIdx] = loadImage(imagePath);
                }
            }
        } catch (Exception e) {
            System.err.println("Image loading failed: " + e.getMessage());
        }
    }
    
    private Image loadImage(String path) {
        // This method loads images from the resource folder
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        if (icon.getIconWidth() <= 0) {
            System.err.println("Image loading failed: " + path);
            return null;
        }
        return icon.getImage();
    }
    
    public void setPlayerColor(ChessPiece.PieceColor playerColor) {
        this.playerColor = playerColor;
        
        // Flip the board if playing as black
        boardFlipped = (playerColor == ChessPiece.PieceColor.BLACK);
        
        repaint();
    }
    
    public void makeMove(ChessMove move) {
        // Save the last move
        setLastMove(move.getStartRow(), move.getStartCol(), move.getEndRow(), move.getEndCol());
        
        chessBoard.makeMove(move);
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        repaint();
    }
    
    public void makeRemoteMove(ChessMove move) {
        // Apply moves from remote player
        makeMove(move);
    }
    
    public void resetBoard() {
        // Reset board to initial state
        chessBoard.reset();
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        lastMoveStart = null;
        lastMoveEnd = null;
        statusMessage = "";
        invalidMove = null;
        repaint();
    }
    
    public void setLocked(boolean locked) {
        // Lock or unlock the board to control interaction
        setEnabled(!locked);
        repaint();
    }
    
    public ChessBoard getBoard() {
        return chessBoard;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the chess board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int drawRow = boardFlipped ? (BOARD_SIZE - 1 - row) : row;
                int drawCol = boardFlipped ? (BOARD_SIZE - 1 - col) : col;
                
                boolean isLightSquare = (row + col) % 2 == 1;
                g2d.setColor(isLightSquare ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR);
                g2d.fillRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                
                if (showCoordinates) {
                    g2d.setColor(isLightSquare ? DARK_SQUARE_COLOR : LIGHT_SQUARE_COLOR);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    
                    // Horizontal (a-h) coordinates - bottom edge
                    if (drawRow == BOARD_SIZE - 1) {
                        char file = (char) ('a' + (boardFlipped ? (BOARD_SIZE - 1 - col) : col));
                        g2d.drawString(String.valueOf(file), 
                                drawCol * SQUARE_SIZE + SQUARE_SIZE - 12, 
                                drawRow * SQUARE_SIZE + SQUARE_SIZE - 5);
                    }
                    
                    // Vertical (1-8) coordinates - left edge
                    if (drawCol == 0) {
                        int rank = boardFlipped ? (row + 1) : (BOARD_SIZE - row);
                        g2d.drawString(String.valueOf(rank), 
                                drawCol * SQUARE_SIZE + 3, 
                                drawRow * SQUARE_SIZE + 15);
                    }
                }
            }
        }
        
        // Highlight the squares of the last move
        if (showLastMove && lastMoveStart != null && lastMoveEnd != null) {
            int startRow = boardFlipped ? (BOARD_SIZE - 1 - lastMoveStart.x) : lastMoveStart.x;
            int startCol = boardFlipped ? (BOARD_SIZE - 1 - lastMoveStart.y) : lastMoveStart.y;
            int endRow = boardFlipped ? (BOARD_SIZE - 1 - lastMoveEnd.x) : lastMoveEnd.x;
            int endCol = boardFlipped ? (BOARD_SIZE - 1 - lastMoveEnd.y) : lastMoveEnd.y;
            
            g2d.setColor(LAST_MOVE_COLOR);
            g2d.fillRect(startCol * SQUARE_SIZE, startRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            g2d.fillRect(endCol * SQUARE_SIZE, endRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
        
        // Highlight the square of checked king
        if (chessBoard.isInCheck(chessBoard.getCurrentTurn())) {
            // Find the king position
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    ChessPiece piece = chessBoard.getPiece(row, col);
                    if (piece != null && 
                        piece.getType() == ChessPiece.PieceType.KING && 
                        piece.getColor() == chessBoard.getCurrentTurn()) {
                        
                        int drawRow = boardFlipped ? (BOARD_SIZE - 1 - row) : row;
                        int drawCol = boardFlipped ? (BOARD_SIZE - 1 - col) : col;
                        
                        g2d.setColor(CHECK_INDICATOR_COLOR);
                        g2d.fillRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                    }
                }
            }
        }
        
        // Seçili kareyi vurgula
        if (selectedRow != -1 && selectedCol != -1) {
            g2d.setColor(SELECTED_SQUARE_COLOR); // Sarı vurgu
            
            int drawRow = boardFlipped ? (BOARD_SIZE - 1 - selectedRow) : selectedRow;
            int drawCol = boardFlipped ? (BOARD_SIZE - 1 - selectedCol) : selectedCol;
            
            g2d.fillRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
        
        // Geçerli hamle göstergelerini çiz
        drawValidMoveIndicators(g2d);
        
        // Geçersiz hamle efekti (kırmızı yanıp sönme)
        if (invalidMove != null && isFlashing) {
            g2d.setColor(new Color(255, 0, 0, 150)); // Kırmızı yarı şeffaf
            
            int drawRow = boardFlipped ? (BOARD_SIZE - 1 - invalidMove.x) : invalidMove.x;
            int drawCol = boardFlipped ? (BOARD_SIZE - 1 - invalidMove.y) : invalidMove.y;
            
            g2d.fillRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
        
        // Taşları çiz
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = chessBoard.getPiece(row, col);
                if (piece != null) {
                    drawPiece(g2d, piece, row, col);
                }
            }
        }
        
        // Oyun durumu bilgisini göster
        if (chessBoard.isGameOver()) {
            drawGameOverOverlay(g2d, chessBoard.getGameResult());
        } else if (!statusMessage.isEmpty()) {
            drawStateMessage(g2d, statusMessage, statusColor);
        }
    }
    
    private void drawPiece(Graphics2D g2d, ChessPiece piece, int row, int col) {
        int colorIdx = (piece.getColor() == ChessPiece.PieceColor.WHITE) ? 0 : 1;
        int typeIdx;
        
        switch (piece.getType()) {
            case PAWN:
                typeIdx = 0;
                break;
            case ROOK:
                typeIdx = 1;
                break;
            case KNIGHT:
                typeIdx = 2;
                break;
            case BISHOP:
                typeIdx = 3;
                break;
            case QUEEN:
                typeIdx = 4;
                break;
            case KING:
                typeIdx = 5;
                break;
            default:
                return;
        }
        
        Image pieceImage = pieceImages[colorIdx][typeIdx];
        if (pieceImage != null) {
            int drawRow = boardFlipped ? (BOARD_SIZE - 1 - row) : row;
            int drawCol = boardFlipped ? (BOARD_SIZE - 1 - col) : col;
            
            g2d.drawImage(pieceImage, 
                    drawCol * SQUARE_SIZE, 
                    drawRow * SQUARE_SIZE, 
                    SQUARE_SIZE, 
                    SQUARE_SIZE, 
                    null);
        }
    }
    
    private void handleMouseClick(MouseEvent e) {
        int clickCol = e.getX() / SQUARE_SIZE;
        int clickRow = e.getY() / SQUARE_SIZE;
        
        // Tahtanın çevrilmiş olup olmadığını kontrol et
        if (boardFlipped) {
            clickRow = BOARD_SIZE - 1 - clickRow;
            clickCol = BOARD_SIZE - 1 - clickCol;
        }
        
        // Eğer şu anda yanıp sönme efekti varsa, bunu durdur
        if (flashTimer.isRunning()) {
            flashTimer.stop();
            isFlashing = false;
            invalidMove = null;
            repaint();
            return;
        }
        
        // Geçerli bir kare seçili değilse ve tıklanan karede kendi taşımız varsa
        ChessPiece clickedPiece = chessBoard.getPiece(clickRow, clickCol);
        if (selectedRow == -1 && selectedCol == -1) {
            if (clickedPiece != null && clickedPiece.getColor() == playerColor 
                    && chessBoard.getCurrentTurn() == playerColor) {
                selectedRow = clickRow;
                selectedCol = clickCol;
                // Taşın gidebileceği yerleri belirle
                calculateValidMoves(clickRow, clickCol);
                repaint();
            }
        } 
        // Bir kare seçiliyse ve farklı bir kareye tıklandıysa
        else if (selectedRow != -1 && selectedCol != -1) {
            // Eğer kendi taşımıza tıklandıysa, seçimi değiştir
            if (clickedPiece != null && clickedPiece.getColor() == playerColor) {
                selectedRow = clickRow;
                selectedCol = clickCol;
                // Taşın gidebileceği yerleri yeniden belirle
                calculateValidMoves(clickRow, clickCol);
                repaint();
            } 
            // Farklı bir kareye tıklandıysa, hamle yapmayı dene
            else {
                // Gidilen karenin geçerli bir hamle olup olmadığını kontrol et
                boolean isValidMove = false;
                Point targetMove = null;
                
                for (Point p : validMoves) {
                    if (p.x == clickRow && p.y == clickCol) {
                        isValidMove = true;
                        targetMove = p;
                        break;
                    }
                }
                
                if (isValidMove) {
                    tryMove(selectedRow, selectedCol, targetMove.x, targetMove.y);
                } else {
                    // Geçersiz hamle - kırmızı yanıp sönme efekti göster
                    invalidMove = new Point(clickRow, clickCol);
                    isFlashing = true;
                    flashTimer.restart();
                }
            }
        }
    }
    
    private void calculateValidMoves(int row, int col) {
        validMoves.clear();
        ChessPiece piece = chessBoard.getPiece(row, col);
        
        if (piece == null || piece.getColor() != playerColor) {
            return;
        }
        
        // Şah çekilip çekilmediğini kontrol et
        boolean isInCheck = chessBoard.isInCheck(playerColor);
        Point checkingPieceLocation = null;
        
        // Eğer şah çekiliyorsa, şah çeken taşın konumunu bul
        if (isInCheck) {
            checkingPieceLocation = findCheckingPiece(playerColor);
        }
        
        // Burada satranç kurallarına göre taşın gidebileceği yerler belirlenir
        // Bu basit implementasyonda, taşın tipine göre gidebileceği kareleri hesaplıyoruz
        List<Point> potentialMoves = new ArrayList<>();
        
        switch (piece.getType()) {
            case PAWN:
                calculatePawnMovesInternal(row, col, piece, potentialMoves);
                break;
            case ROOK:
                calculateRookMovesInternal(row, col, piece, potentialMoves);
                break;
            case KNIGHT:
                calculateKnightMovesInternal(row, col, piece, potentialMoves);
                break;
            case BISHOP:
                calculateBishopMovesInternal(row, col, piece, potentialMoves);
                break;
            case QUEEN:
                calculateQueenMovesInternal(row, col, piece, potentialMoves);
                break;
            case KING:
                calculateKingMovesInternal(row, col, piece, potentialMoves);
                break;
        }
        
        // Şah çekiliyorsa ve oynanan taş şah değilse, sadece şahı kurtaracak hamlelere izin ver
        if (isInCheck && piece.getType() != ChessPiece.PieceType.KING) {
            for (Point move : potentialMoves) {
                // 1. Şah çeken taşı alma hamlesi
                if (checkingPieceLocation != null && 
                    move.x == checkingPieceLocation.x && 
                    move.y == checkingPieceLocation.y) {
                    
                    // Eğer şah çeken taş korunuyorsa bu taşı alamayız
                    if (!isPieceProtected(checkingPieceLocation.x, checkingPieceLocation.y)) {
                        validMoves.add(move);
                    }
                }
                // 2. Şah ile şah çeken taş arasına girme hamlesi
                else if (isBlockingCheck(row, col, move.x, move.y, playerColor)) {
                    validMoves.add(move);
                }
            }
        } else {
            // Normal durumda, hamlenin şahı tehlikeye atıp atmadığını kontrol et
            for (Point move : potentialMoves) {
                if (isLegalMove(row, col, move.x, move.y, playerColor)) {
                    validMoves.add(move);
                }
            }
        }
    }
    
    // Şah çeken taşın konumunu bul
    private Point findCheckingPiece(ChessPiece.PieceColor kingColor) {
        // Önce şahın yerini bul
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = chessBoard.getPiece(row, col);
                if (piece != null && 
                    piece.getType() == ChessPiece.PieceType.KING && 
                    piece.getColor() == kingColor) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        
        if (kingRow == -1) return null; // Şah bulunamadı
        
        ChessPiece.PieceColor opponentColor = (kingColor == ChessPiece.PieceColor.WHITE) ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
        
        // Düz hatlar (yatay, dikey)
        int[][] straightDirections = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        // Çapraz hatlar
        int[][] diagonalDirections = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        
        // Düz hatlar üzerinde kale ve vezir kontrolü
        for (int[] dir : straightDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = kingRow + i * dir[0];
                int newCol = kingCol + i * dir[1];
                
                if (!chessBoard.isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (type == ChessPiece.PieceType.ROOK || type == ChessPiece.PieceType.QUEEN) {
                            return new Point(newRow, newCol);
                        }
                    }
                    break; // Başka bir taş varsa o yöne devam etme
                }
            }
        }
        
        // Çapraz hatlar üzerinde fil ve vezir kontrolü
        for (int[] dir : diagonalDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = kingRow + i * dir[0];
                int newCol = kingCol + i * dir[1];
                
                if (!chessBoard.isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (i == 1 && type == ChessPiece.PieceType.PAWN) {
                            // Piyon tehdidi - beyaz şah için alt çaprazlarda, siyah şah için üst çaprazlarda
                            if ((kingColor == ChessPiece.PieceColor.WHITE && dir[0] > 0) ||
                                (kingColor == ChessPiece.PieceColor.BLACK && dir[0] < 0)) {
                                return new Point(newRow, newCol);
                            }
                        }
                        if (type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.QUEEN) {
                            return new Point(newRow, newCol);
                        }
                    }
                    break; // Başka bir taş varsa o yöne devam etme
                }
            }
        }
        
        // At tehdidi kontrolü
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        
        for (int[] move : knightMoves) {
            int newRow = kingRow + move[0];
            int newCol = kingCol + move[1];
            
            if (chessBoard.isValidPosition(newRow, newCol)) {
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null && piece.getColor() == opponentColor && 
                    piece.getType() == ChessPiece.PieceType.KNIGHT) {
                    return new Point(newRow, newCol);
                }
            }
        }
        
        return null; // Şah çeken taş bulunamadı
    }
    
    // Taşın korunup korunmadığını kontrol et
    private boolean isPieceProtected(int row, int col) {
        ChessPiece targetPiece = chessBoard.getPiece(row, col);
        if (targetPiece == null) return false;
        
        ChessPiece.PieceColor pieceColor = targetPiece.getColor();
        ChessPiece.PieceColor opponentColor = (pieceColor == ChessPiece.PieceColor.WHITE) ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
        
        // Geçici olarak bu taşı kaldır
        chessBoard.setPiece(row, col, null);
        
        // Bu konuma rakip taş konulduğunda, kendi taşlarımızdan biri tarafından alınabilir mi kontrol et
        boolean isProtected = false;
        
        // Düz hatlar (yatay, dikey)
        int[][] straightDirections = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        // Düz hatlar üzerinde kale ve vezir koruması
        for (int[] dir : straightDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = row + i * dir[0];
                int newCol = col + i * dir[1];
                
                if (!chessBoard.isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == pieceColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (type == ChessPiece.PieceType.ROOK || type == ChessPiece.PieceType.QUEEN ||
                            (i == 1 && type == ChessPiece.PieceType.KING)) {
                            isProtected = true;
                            break;
                        }
                    }
                    break; // Başka bir taş varsa o yöne devam etme
                }
            }
            if (isProtected) break;
        }
        
        if (!isProtected) {
            // Çapraz hatlar
            int[][] diagonalDirections = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
            
            // Çapraz hatlar üzerinde fil ve vezir koruması
            for (int[] dir : diagonalDirections) {
                for (int i = 1; i < BOARD_SIZE; i++) {
                    int newRow = row + i * dir[0];
                    int newCol = col + i * dir[1];
                    
                    if (!chessBoard.isValidPosition(newRow, newCol)) break;
                    
                    ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                    if (piece != null) {
                        if (piece.getColor() == pieceColor) {
                            ChessPiece.PieceType type = piece.getType();
                            if (i == 1) {
                                if (type == ChessPiece.PieceType.KING) {
                                    isProtected = true;
                                    break;
                                }
                                // Piyon koruması
                                if (type == ChessPiece.PieceType.PAWN) {
                                    if ((pieceColor == ChessPiece.PieceColor.WHITE && dir[0] < 0) ||
                                        (pieceColor == ChessPiece.PieceColor.BLACK && dir[0] > 0)) {
                                        isProtected = true;
                                        break;
                                    }
                                }
                            }
                            if (type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.QUEEN) {
                                isProtected = true;
                                break;
                            }
                        }
                        break; // Başka bir taş varsa o yöne devam etme
                    }
                }
                if (isProtected) break;
            }
        }
        
        if (!isProtected) {
            // At koruması kontrolü
            int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
            };
            
            for (int[] move : knightMoves) {
                int newRow = row + move[0];
                int newCol = col + move[1];
                
                if (chessBoard.isValidPosition(newRow, newCol)) {
                    ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                    if (piece != null && piece.getColor() == pieceColor && 
                        piece.getType() == ChessPiece.PieceType.KNIGHT) {
                        isProtected = true;
                        break;
                    }
                }
            }
        }
        
        // Taşı geri koy
        chessBoard.setPiece(row, col, targetPiece);
        
        return isProtected;
    }
    
    // Hareketin şah ile şah çeken taş arasında blok oluşturup oluşturmadığını kontrol et
    private boolean isBlockingCheck(int startRow, int startCol, int endRow, int endCol, ChessPiece.PieceColor kingColor) {
        // Şahın yerini bul
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = chessBoard.getPiece(row, col);
                if (piece != null && 
                    piece.getType() == ChessPiece.PieceType.KING && 
                    piece.getColor() == kingColor) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        
        if (kingRow == -1) return false; // Şah bulunamadı
        
        // Şah çeken taşın yerini bul
        Point checkingPiece = findCheckingPiece(kingColor);
        if (checkingPiece == null) return false;
        
        // Sadece düz çizgi (kale, vezir) veya çapraz çizgi (fil, vezir) tehditleri bloklanabilir
        // At ve piyon tehditleri bloklanamaz, doğrudan alınmalıdır
        ChessPiece piece = chessBoard.getPiece(checkingPiece.x, checkingPiece.y);
        if (piece.getType() == ChessPiece.PieceType.KNIGHT || piece.getType() == ChessPiece.PieceType.PAWN) {
            return false;
        }
        
        // Şah çeken taştan şaha doğru olan yolu bul
        int rowDir = Integer.compare(kingRow - checkingPiece.x, 0);
        int colDir = Integer.compare(kingCol - checkingPiece.y, 0);
        
        // Yol üzerindeki her kareyi kontrol et
        int row = checkingPiece.x + rowDir;
        int col = checkingPiece.y + colDir;
        
        while (row != kingRow || col != kingCol) {
            if (row == endRow && col == endCol) {
                return true; // Yol üzerinde bir blok oluşuyor
            }
            row += rowDir;
            col += colDir;
        }
        
        return false;
    }
    
    // Hamlenin yasal olup olmadığını kontrol et (şahı tehlikeye atıyor mu)
    private boolean isLegalMove(int startRow, int startCol, int endRow, int endCol, ChessPiece.PieceColor color) {
        ChessPiece piece = chessBoard.getPiece(startRow, startCol);
        ChessPiece capturedPiece = chessBoard.getPiece(endRow, endCol);
        
        // Geçici taşları saklayalım
        ChessPiece tempCapturedPiece = capturedPiece;
        
        // En passant kontrolü
        boolean isEnPassant = false;
        ChessPiece tempEnPassantPiece = null;
        
        if (piece.getType() == ChessPiece.PieceType.PAWN && 
            endCol != startCol && 
            capturedPiece == null) {
            
            // Çapraz hareket edilen yerde taş yok ama sütun değişmiş, en passant olabilir
            if (chessBoard.wasLastMoveDoublePawnPush() && 
                startRow == chessBoard.getLastPawnMoveRow() && 
                endCol == chessBoard.getLastPawnMoveCol()) {
                
                tempEnPassantPiece = chessBoard.getPiece(startRow, endCol);
                chessBoard.setPiece(startRow, endCol, null); // Geçerken alınan piyonu geçici olarak kaldır
                isEnPassant = true;
            }
        }
        
        // Rok kontrolü
        boolean isCastling = false;
        ChessPiece rook = null;
        int rookStartCol = -1;
        int rookEndCol = -1;
        
        if (piece.getType() == ChessPiece.PieceType.KING && Math.abs(startCol - endCol) > 1) {
            isCastling = true;
            
            // Kısa rok (sağa)
            if (endCol > startCol) {
                rookStartCol = 7;
                rookEndCol = 5;
            } 
            // Uzun rok (sola)
            else {
                rookStartCol = 0;
                rookEndCol = 3;
            }
            
            rook = chessBoard.getPiece(startRow, rookStartCol);
            
            // Kaleyi geçici olarak hareket ettir
            chessBoard.setPiece(startRow, rookEndCol, rook);
            chessBoard.setPiece(startRow, rookStartCol, null);
        }
        
        // Hamleyi geçici olarak yap
        chessBoard.setPiece(endRow, endCol, piece);
        chessBoard.setPiece(startRow, startCol, null);
        
        // Şah tehdit altında mı kontrol et
        boolean isLegal = !isKingUnderAttack(color, -1, -1); // -1, -1 parametreleri: şahın mevcut konumunu bul
        
        // Hamleyi geri al
        chessBoard.setPiece(startRow, startCol, piece);
        chessBoard.setPiece(endRow, endCol, tempCapturedPiece);
        
        // En passant ile alınan taşı geri koy
        if (isEnPassant && tempEnPassantPiece != null) {
            chessBoard.setPiece(startRow, endCol, tempEnPassantPiece);
        }
        
        // Rok hamlesini geri al
        if (isCastling && rook != null) {
            chessBoard.setPiece(startRow, rookStartCol, rook);
            chessBoard.setPiece(startRow, rookEndCol, null);
        }
        
        return isLegal;
    }
    
    private void calculatePawnMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        int direction = (piece.getColor() == ChessPiece.PieceColor.WHITE) ? -1 : 1;
        
        // İleri hareket
        if (chessBoard.isValidPosition(row + direction, col) && 
                chessBoard.getPiece(row + direction, col) == null) {
            moves.add(new Point(row + direction, col));
            
            // İlk hamlede iki kare ileri gidebilir
            if (!piece.hasMoved() && 
                    chessBoard.isValidPosition(row + 2 * direction, col) && 
                    chessBoard.getPiece(row + 2 * direction, col) == null) {
                moves.add(new Point(row + 2 * direction, col));
            }
        }
        
        // Çapraz yeme hareketleri
        for (int offset : new int[]{-1, 1}) {
            if (chessBoard.isValidPosition(row + direction, col + offset)) {
                ChessPiece targetPiece = chessBoard.getPiece(row + direction, col + offset);
                if (targetPiece != null && targetPiece.getColor() != piece.getColor()) {
                    moves.add(new Point(row + direction, col + offset));
                } 
                // En passant kontrolü
                else if (targetPiece == null && 
                        chessBoard.wasLastMoveDoublePawnPush() && 
                        chessBoard.getLastPawnMoveRow() == row && 
                        chessBoard.getLastPawnMoveCol() == col + offset) {
                    moves.add(new Point(row + direction, col + offset));
                }
            }
        }
    }
    
    private void calculateRookMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // Kale düz hareket eder (yatay ve dikey)
        calculateStraightMovesInternal(row, col, piece, true, false, moves);
    }
    
    private void calculateBishopMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // Fil çapraz hareket eder
        calculateStraightMovesInternal(row, col, piece, false, true, moves);
    }
    
    private void calculateQueenMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // Vezir hem düz hem çapraz hareket eder
        calculateStraightMovesInternal(row, col, piece, true, true, moves);
    }
    
    private void calculateStraightMovesInternal(int row, int col, ChessPiece piece, boolean straightLines, boolean diagonalLines, List<Point> moves) {
        // Düz hareketler (yatay ve dikey)
        if (straightLines) {
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Sağ, aşağı, sol, yukarı
            
            for (int[] dir : directions) {
                for (int i = 1; i < BOARD_SIZE; i++) {
                    int newRow = row + i * dir[0];
                    int newCol = col + i * dir[1];
                    
                    if (!chessBoard.isValidPosition(newRow, newCol)) {
                        break;
                    }
                    
                    ChessPiece targetPiece = chessBoard.getPiece(newRow, newCol);
                    if (targetPiece == null) {
                        moves.add(new Point(newRow, newCol));
                    } else {
                        if (targetPiece.getColor() != piece.getColor()) {
                            moves.add(new Point(newRow, newCol));
                        }
                        break;
                    }
                }
            }
        }
        
        // Çapraz hareketler
        if (diagonalLines) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}; // Sağ-aşağı, sol-aşağı, sol-yukarı, sağ-yukarı
            
            for (int[] dir : directions) {
                for (int i = 1; i < BOARD_SIZE; i++) {
                    int newRow = row + i * dir[0];
                    int newCol = col + i * dir[1];
                    
                    if (!chessBoard.isValidPosition(newRow, newCol)) {
                        break;
                    }
                    
                    ChessPiece targetPiece = chessBoard.getPiece(newRow, newCol);
                    if (targetPiece == null) {
                        moves.add(new Point(newRow, newCol));
                    } else {
                        if (targetPiece.getColor() != piece.getColor()) {
                            moves.add(new Point(newRow, newCol));
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private void calculateKnightMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // At L şeklinde hareket eder
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            
            if (chessBoard.isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = chessBoard.getPiece(newRow, newCol);
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    moves.add(new Point(newRow, newCol));
                }
            }
        }
    }
    
    private void calculateKingMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // Şah her yöne bir kare hareket eder
        int[][] kingMoves = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };
        
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            
            if (chessBoard.isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = chessBoard.getPiece(newRow, newCol);
                
                // Rakip taş kontrolü
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    // Şahın gideceği kare tehdit altında olmamalı
                    if (canMoveTo(piece.getColor(), newRow, newCol)) {
                        moves.add(new Point(newRow, newCol));
                    }
                }
            }
        }
        
        // Rok hamlesi için kontrol (simplifiye edilmiş)
        if (!piece.hasMoved()) {
            // Kısa rok
            if (canCastle(row, col, true)) {
                moves.add(new Point(row, col + 2));
            }
            
            // Uzun rok
            if (canCastle(row, col, false)) {
                moves.add(new Point(row, col - 2));
            }
        }
    }
    
    // Şahın gideceği konumun güvenli olup olmadığını kontrol et
    private boolean canMoveTo(ChessPiece.PieceColor kingColor, int targetRow, int targetCol) {
        // Hedef konumdaki taşı geçici olarak sakla
        ChessPiece targetPiece = chessBoard.getPiece(targetRow, targetCol);
        
        // Eğer hedef karede rakip taş varsa, geçici olarak kaldırmalıyız
        if (targetPiece != null) {
            chessBoard.setPiece(targetRow, targetCol, null);
        }
        
        // Bu konumda şah tehdit altında mı kontrol et
        boolean isSafe = !isSquareUnderAttack(targetRow, targetCol, kingColor);
        
        // Hedef kareyi eski haline getir
        if (targetPiece != null) {
            chessBoard.setPiece(targetRow, targetCol, targetPiece);
        }
        
        return isSafe;
    }
    
    // Belirli bir konumdaki şahın tehdit altında olup olmadığını kontrol et
    private boolean isKingUnderAttack(ChessPiece.PieceColor kingColor, int kingRow, int kingCol) {
        // Eğer koordinatlar belirtilmemişse, şahın mevcut konumunu bul
        if (kingRow == -1 || kingCol == -1) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    ChessPiece piece = chessBoard.getPiece(row, col);
                    if (piece != null && 
                        piece.getType() == ChessPiece.PieceType.KING && 
                        piece.getColor() == kingColor) {
                        kingRow = row;
                        kingCol = col;
                        break;
                    }
                }
                if (kingRow != -1) break;
            }
        }
        
        if (kingRow == -1) return false; // Şah bulunamadı
        
        // Belirtilen konumdaki karenin tehdit altında olup olmadığını kontrol et
        return isSquareUnderAttack(kingRow, kingCol, kingColor);
    }
    
    // Bir karenin belirtilen renkteki oyuncu için tehdit altında olup olmadığını kontrol eder
    private boolean isSquareUnderAttack(int targetRow, int targetCol, ChessPiece.PieceColor kingColor) {
        ChessPiece.PieceColor opponentColor = (kingColor == ChessPiece.PieceColor.WHITE) ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
        
        // Düz hatlar boyunca saldırı (kale ve vezir)
        int[][] straightDirections = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] dir : straightDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = targetRow + i * dir[0];
                int newCol = targetCol + i * dir[1];
                
                if (!chessBoard.isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (i == 1 && type == ChessPiece.PieceType.KING) {
                            return true; // Rakip şah komşu karede
                        }
                        if (type == ChessPiece.PieceType.ROOK || type == ChessPiece.PieceType.QUEEN) {
                            return true; // Kale veya vezir tarafından tehdit
                        }
                    }
                    break; // Başka bir taş varsa o yöne devam etme
                }
            }
        }
        
        // Çapraz hatlar boyunca saldırı (fil, vezir ve piyon)
        int[][] diagonalDirections = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
        for (int[] dir : diagonalDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = targetRow + i * dir[0];
                int newCol = targetCol + i * dir[1];
                
                if (!chessBoard.isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (i == 1) {
                            if (type == ChessPiece.PieceType.KING) {
                                return true; // Rakip şah çaprazda
                            }
                            // Piyon tehdidi - beyaz şah için alt çaprazlarda, siyah şah için üst çaprazlarda
                            if (type == ChessPiece.PieceType.PAWN) {
                                if ((kingColor == ChessPiece.PieceColor.WHITE && dir[0] > 0) ||
                                    (kingColor == ChessPiece.PieceColor.BLACK && dir[0] < 0)) {
                                    return true; // Piyon tehdidi
                                }
                            }
                        }
                        if (type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.QUEEN) {
                            return true; // Fil veya vezir tehdidi
                        }
                    }
                    break; // Başka bir taş varsa o yöne devam etme
                }
            }
        }
        
        // At tehdidi kontrolü
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        
        for (int[] move : knightMoves) {
            int newRow = targetRow + move[0];
            int newCol = targetCol + move[1];
            
            if (chessBoard.isValidPosition(newRow, newCol)) {
                ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                if (piece != null && piece.getColor() == opponentColor && 
                    piece.getType() == ChessPiece.PieceType.KNIGHT) {
                    return true; // At tehdidi
                }
            }
        }
        
        return false; // Kare tehdit altında değil
    }
    
    private boolean canCastle(int row, int col, boolean kingSide) {
        // ... existing code ...
        return false; // ... existing code ...
    }
    
    private void drawValidMoveIndicators(Graphics2D g2d) {
        if (!validMoves.isEmpty()) {
            for (Point p : validMoves) {
                int drawRow = boardFlipped ? (BOARD_SIZE - 1 - p.x) : p.x;
                int drawCol = boardFlipped ? (BOARD_SIZE - 1 - p.y) : p.y;

                // Hedef karede rakip taş var mı kontrol et
                ChessPiece targetPiece = chessBoard.getPiece(p.x, p.y);
                
                if (targetPiece != null) {
                    // Rakip taş varsa, çerçeve çiz
                    g2d.setColor(CAPTURE_INDICATOR_COLOR);
                    g2d.drawRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    g2d.drawRect(drawCol * SQUARE_SIZE + 1, drawRow * SQUARE_SIZE + 1, SQUARE_SIZE - 3, SQUARE_SIZE - 3);
                    g2d.drawRect(drawCol * SQUARE_SIZE + 2, drawRow * SQUARE_SIZE + 2, SQUARE_SIZE - 5, SQUARE_SIZE - 5);
                } else {
                    // Boş kare için daha büyük bir nokta çiz
                    g2d.setColor(VALID_MOVE_INDICATOR_COLOR);
                    int dotSize = 24; // Daha büyük boyut
                    g2d.fillOval(
                        drawCol * SQUARE_SIZE + (SQUARE_SIZE - dotSize) / 2, 
                        drawRow * SQUARE_SIZE + (SQUARE_SIZE - dotSize) / 2, 
                        dotSize, dotSize);
                }
            }
        }
    }
    
    // Oyun sonu mesajını göster
    private void drawGameOverOverlay(Graphics2D g2d, String message) {
        int width = getWidth();
        int height = getHeight();
        
        // Yarı saydam arka plan
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, width, height);
        
        // Oyun sonucu mesajı için kutu
        int boxWidth = 400;
        int boxHeight = 150;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;
        
        // Kutu arka planı
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Kutu kenarı
        g2d.setColor(new Color(0, 0, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Mesaj metni
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Zafer, yenilgi veya beraberlik göstergesine göre renk ayarla
        if (message.contains("kazandı")) {
            // Kazanan oyuncu adını al
            String winner = message.substring(0, message.indexOf(" "));
            if (playerColor != null) {
                if ((playerColor == ChessPiece.PieceColor.WHITE && winner.equals(chessBoard.getWhitePlayerName())) ||
                    (playerColor == ChessPiece.PieceColor.BLACK && winner.equals(chessBoard.getBlackPlayerName()))) {
                    // Kazanma durumu
                    g2d.setColor(new Color(0, 150, 0));
                    g2d.drawString("ZAFER!", boxX + (boxWidth - fm.stringWidth("ZAFER!")) / 2, boxY + 50);
                } else {
                    // Kaybetme durumu
                    g2d.setColor(new Color(150, 0, 0));
                    g2d.drawString("MAĞLUBİYET", boxX + (boxWidth - fm.stringWidth("MAĞLUBİYET")) / 2, boxY + 50);
                }
            } else {
                g2d.setColor(new Color(0, 0, 150));
                g2d.drawString("OYUN SONA ERDİ", boxX + (boxWidth - fm.stringWidth("OYUN SONA ERDİ")) / 2, boxY + 50);
            }
        } else if (message.contains("berabere") || message.contains("draw")) {
            // Beraberlik durumu
            g2d.setColor(new Color(150, 100, 0));
            g2d.drawString("BERABERLİK", boxX + (boxWidth - fm.stringWidth("BERABERLİK")) / 2, boxY + 50);
        } else {
            // Diğer durumlar
            g2d.setColor(new Color(0, 0, 150));
            g2d.drawString("OYUN SONA ERDİ", boxX + (boxWidth - fm.stringWidth("OYUN SONA ERDİ")) / 2, boxY + 50);
        }
        
        // Mesaj detayları
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        
        // Mesajı birden fazla satıra böl
        int yPos = boxY + 80;
        int lineHeight = fm.getHeight();
        String[] lines = message.split("\\. ");
        for (String line : lines) {
            if (!line.endsWith(".")) line += ".";
            g2d.drawString(line, boxX + (boxWidth - fm.stringWidth(line)) / 2, yPos);
            yPos += lineHeight;
        }
    }
    
    // Durum mesajını göster
    private void drawStateMessage(Graphics2D g2d, String message, Color color) {
        g2d.setColor(new Color(0, 0, 0, 100)); // Hafif saydam arka plan
        g2d.fillRect(0, 0, BOARD_SIZE * SQUARE_SIZE, 30);
        
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Metni ortala
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        int x = (BOARD_SIZE * SQUARE_SIZE - textWidth) / 2;
        
        g2d.drawString(message, x, 22);
    }
    
    // Son hamle bilgisini güncelle
    public void setLastMove(int startRow, int startCol, int endRow, int endCol) {
        this.lastMoveStart = new Point(startRow, startCol);
        this.lastMoveEnd = new Point(endRow, endCol);
        repaint();
    }
    
    private void tryMove(int startRow, int startCol, int endRow, int endCol) {
        // Hamleyi oluştur
        ChessMove move = new ChessMove(startRow, startCol, endRow, endCol);
        ChessPiece piece = chessBoard.getPiece(startRow, startCol);
        
        // Piyon terfisi kontrolü
        if (piece.getType() == ChessPiece.PieceType.PAWN) {
            if ((endRow == 0 && piece.getColor() == ChessPiece.PieceColor.WHITE) ||
                (endRow == 7 && piece.getColor() == ChessPiece.PieceColor.BLACK)) {
                
                // Piyon terfisi seçimini göster
                String[] options = {"Vezir", "Kale", "Fil", "At"};
                int choice = JOptionPane.showOptionDialog(
                    this,
                    "Hangi taşa terfi etmek istersiniz?",
                    "Piyon Terfisi",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
                );
                
                // Terfi tipini belirle
                ChessPiece.PieceType promotionType;
                switch (choice) {
                    case 1:
                        promotionType = ChessPiece.PieceType.ROOK;
                        break;
                    case 2:
                        promotionType = ChessPiece.PieceType.BISHOP;
                        break;
                    case 3:
                        promotionType = ChessPiece.PieceType.KNIGHT;
                        break;
                    case 0:
                    default:
                        promotionType = ChessPiece.PieceType.QUEEN;
                        break;
                }
                
                move.setPromotion(true);
                move.setPromotionType(promotionType);
            }
        }
        
        // Hamleyi tahtada yap
        makeMove(move);
        
        // Hamleyi ağ üzerinden gönder (eğer online oyunsa)
        if (client != null) {
            client.sendMoveMessage(move);
        }
        
        // Seçimi sıfırla
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        repaint();
    }
} 