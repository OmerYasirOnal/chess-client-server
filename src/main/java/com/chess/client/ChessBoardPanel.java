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
    private int flashCount = 0;  // Flash counter
    
    // Variables for information texts
    private String statusMessage = "";
    private Color statusColor = Color.BLACK;
    private boolean showCoordinates = true;
    private boolean showLastMove = true;
    private Point lastMoveStart = null;
    private Point lastMoveEnd = null;
    
    // Theme colors
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
        
        // Timer for flashing effect - completely reconfigured
        flashTimer = new Timer(300, e -> {
            isFlashing = !isFlashing;
            
            if (isFlashing) {
                flashCount++;
            }
            
            // Stop after 4 state changes (2 full flashes)
            if (flashCount >= 4) {
                ((Timer)e.getSource()).stop();
                isFlashing = false;
                invalidMove = null;
                flashCount = 0;
                clearSelectionAndHighlights();
            }
            
            repaint();
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
        // Debug
        debugUIState("makeMove-beginning");
        
        // Save the last move
        setLastMove(move.getStartRow(), move.getStartCol(), move.getEndRow(), move.getEndCol());
        
        // Apply the move in the model
        boolean moveSuccess = false;
        try {
            chessBoard.makeMove(move);
            moveSuccess = true;
        } catch (Exception e) {
            System.err.println("Move could not be made: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Clear UI state - do this regardless of success or failure
        clearSelectionAndHighlights();
        
        // If move failed, show this
        if (!moveSuccess) {
            invalidMove = new Point(move.getEndRow(), move.getEndCol());
            isFlashing = true;
            flashTimer.restart();
        }
        
        // Force a repaint to ensure UI is updated
        repaint();
        
        // Debug
        debugUIState("makeMove-end");
        
        // Ensure UI refreshes on the Swing thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            repaint();
        });
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
        
        // Draw highlights first, then pieces
        drawValidMoveIndicators(g2d);
        
        // Draw pieces
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = chessBoard.getPiece(row, col);
                if (piece != null) {
                    drawPiece(g2d, piece, row, col);
                }
            }
        }
        
        // Show status message
        if (!statusMessage.isEmpty()) {
            drawStateMessage(g2d, statusMessage, statusColor);
        }
        
        // Check if game is over
        if (chessBoard.isGameOver()) {
            String endMessage = chessBoard.getGameResult();
            drawGameOverOverlay(g2d, endMessage);
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
        
        // Debug output
        debugUIState("handleMouseClick-beginning");
        
        // Check if the board is flipped
        if (boardFlipped) {
            clickRow = BOARD_SIZE - 1 - clickRow;
            clickCol = BOARD_SIZE - 1 - clickCol;
        }
        
        // If there's currently a flashing effect, stop it and clear selection
        if (flashTimer.isRunning()) {
            clearSelectionAndHighlights();
            repaint();
            return;
        }
        
        // If no square is selected and we clicked on our own piece
        ChessPiece clickedPiece = chessBoard.getPiece(clickRow, clickCol);
        
        // If it's not the player's turn, show red flashing
        if (chessBoard.getCurrentTurn() != playerColor) {
            clearSelectionAndHighlights();
            
            invalidMove = new Point(clickRow, clickCol);
            isFlashing = true;
            flashTimer.restart();
            return;
        }
        
        if (selectedRow == -1 && selectedCol == -1) {
            // Check if we selected our own piece
            if (clickedPiece != null && clickedPiece.getColor() == playerColor) {
                selectedRow = clickRow;
                selectedCol = clickCol;
                // Calculate the valid moves for the piece
                calculateValidMoves(clickRow, clickCol);
                repaint();
                
                // Debug output
                debugUIState("handleMouseClick-afterSelection");
            } else if (clickedPiece != null) {
                // Clicked on opponent's piece - show red flashing
                clearSelectionAndHighlights();
                
                invalidMove = new Point(clickRow, clickCol);
                isFlashing = true;
                flashTimer.restart();
            }
        } 
        // If a square is already selected and clicking on another square
        else if (selectedRow != -1 && selectedCol != -1) {
            // If we click on our own piece, change the selection
            if (clickedPiece != null && clickedPiece.getColor() == playerColor) {
                selectedRow = clickRow;
                selectedCol = clickCol;
                // Recalculate valid moves
                calculateValidMoves(clickRow, clickCol);
                repaint();
                
                // Debug output
                debugUIState("handleMouseClick-newPieceSelection");
            } 
            // If we click on a different square, try to make a move
            else {
                // Check if the target square is a valid move
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
                    // Invalid move - show flashing effect
                    clearSelectionAndHighlights();
                    
                    invalidMove = new Point(clickRow, clickCol);
                    isFlashing = true;
                    flashTimer.restart();
                    
                    // Debug output
                    debugUIState("handleMouseClick-invalidMove");
                }
            }
        }
    }
    
    // Helper method to clear selection state and highlights
    private void clearSelectionAndHighlights() {
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        invalidMove = null;
        isFlashing = false;
        flashCount = 0;
        
        if (flashTimer.isRunning()) {
            flashTimer.stop();
        }
        
        // Debug output
        debugUIState("clearSelectionAndHighlights");
        
        repaint();
    }
    
    private void calculateValidMoves(int row, int col) {
        validMoves.clear();
        ChessPiece piece = chessBoard.getPiece(row, col);
        
        if (piece == null || piece.getColor() != playerColor) {
            return;
        }
        
        // Check if king is in check
        boolean isInCheck = chessBoard.isInCheck(playerColor);
        
        // Calculate all potential moves
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
        
        // For each potential move, check if it's legal using isLegalMove
        // Our isLegalMove method now handles special cases like capturing a checking piece
                for (Point move : potentialMoves) {
                    if (isLegalMove(row, col, move.x, move.y, playerColor)) {
                        validMoves.add(move);
            }
        }
    }
    
    private void calculatePawnMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        int direction = (piece.getColor() == ChessPiece.PieceColor.WHITE) ? -1 : 1;
        
        // Forward movement
        if (chessBoard.isValidPosition(row + direction, col) && 
                chessBoard.getPiece(row + direction, col) == null) {
            moves.add(new Point(row + direction, col));
            
            // Can move two squares forward on first move
            if (!piece.hasMoved() && 
                    chessBoard.isValidPosition(row + 2 * direction, col) && 
                    chessBoard.getPiece(row + 2 * direction, col) == null) {
                moves.add(new Point(row + 2 * direction, col));
            }
        }
        
        // Diagonal capture moves
        for (int offset : new int[]{-1, 1}) {
            if (chessBoard.isValidPosition(row + direction, col + offset)) {
                ChessPiece targetPiece = chessBoard.getPiece(row + direction, col + offset);
                if (targetPiece != null && targetPiece.getColor() != piece.getColor()) {
                    moves.add(new Point(row + direction, col + offset));
                } 
                // En passant check
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
        // Rook moves in straight lines (horizontal and vertical)
        calculateStraightMovesInternal(row, col, piece, true, false, moves);
    }
    
    private void calculateBishopMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // Bishop moves diagonally
        calculateStraightMovesInternal(row, col, piece, false, true, moves);
    }
    
    private void calculateQueenMovesInternal(int row, int col, ChessPiece piece, List<Point> moves) {
        // Queen moves both straight and diagonally
        calculateStraightMovesInternal(row, col, piece, true, true, moves);
    }
    
    private void calculateStraightMovesInternal(int row, int col, ChessPiece piece, boolean straightLines, boolean diagonalLines, List<Point> moves) {
        // Straight movements (horizontal and vertical)
        if (straightLines) {
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, down, left, up
            
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
        
        // Diagonal movements
        if (diagonalLines) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}; // Down-right, down-left, up-left, up-right
            
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
        // Knight moves in L-shapes
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
        // King moves one square in any direction
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
                
                // Check for opponent's piece
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    // King's destination square must not be under attack
                    if (canMoveTo(piece.getColor(), newRow, newCol)) {
                        moves.add(new Point(newRow, newCol));
                    }
                }
            }
        }
        
        // Check for castling (simplified)
        if (!piece.hasMoved()) {
            // Short castling
            if (canCastle(row, col, true)) {
                moves.add(new Point(row, col + 2));
            }
            
            // Long castling
            if (canCastle(row, col, false)) {
                moves.add(new Point(row, col - 2));
            }
        }
    }
    
    // Check if the king can safely move to the position
    private boolean canMoveTo(ChessPiece.PieceColor kingColor, int targetRow, int targetCol) {
        // Temporarily store the target piece
        ChessPiece targetPiece = chessBoard.getPiece(targetRow, targetCol);
        
        // If there's an opponent's piece at the target square, we need to remove it temporarily
        if (targetPiece != null) {
            chessBoard.setPiece(targetRow, targetCol, null);
        }
        
        // Check if king is under attack at this position
        boolean isSafe = !isSquareUnderAttack(targetRow, targetCol, kingColor);
        
        // Restore the target square
        if (targetPiece != null) {
            chessBoard.setPiece(targetRow, targetCol, targetPiece);
        }
        
        return isSafe;
    }
    
    // Check if the king of the specified color is under attack
    private boolean isKingUnderAttack(ChessPiece.PieceColor kingColor) {
        // Find the king's position
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
        
        if (kingRow == -1 || kingCol == -1) {
            return false; // King not found (shouldn't happen)
        }
        
        // Check if the king is under attack
        return isSquareUnderAttack(kingRow, kingCol, kingColor);
    }
    
    // Check if a square is under attack for the specified color player
    private boolean isSquareUnderAttack(int targetRow, int targetCol, ChessPiece.PieceColor kingColor) {
        ChessPiece.PieceColor opponentColor = (kingColor == ChessPiece.PieceColor.WHITE) ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
        
        // First check for pawn attacks directly - pawns only attack diagonally
        if (kingColor == ChessPiece.PieceColor.WHITE) {
            // Check for black pawns attacking diagonally (from above)
            for (int colOffset : new int[]{-1, 1}) {
                int newRow = targetRow - 1;
                int newCol = targetCol + colOffset;
                
                if (chessBoard.isValidPosition(newRow, newCol)) {
                    ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                    if (piece != null && piece.getType() == ChessPiece.PieceType.PAWN && 
                        piece.getColor() == ChessPiece.PieceColor.BLACK) {
                        System.out.println("DEBUG: Found attacking BLACK pawn at " + newRow + "," + newCol);
                        return true;
                    }
                }
            }
        } else {
            // Check for white pawns attacking diagonally (from below)
            for (int colOffset : new int[]{-1, 1}) {
                int newRow = targetRow + 1;
                int newCol = targetCol + colOffset;
                
                if (chessBoard.isValidPosition(newRow, newCol)) {
                    ChessPiece piece = chessBoard.getPiece(newRow, newCol);
                    if (piece != null && piece.getType() == ChessPiece.PieceType.PAWN && 
                        piece.getColor() == ChessPiece.PieceColor.WHITE) {
                        System.out.println("DEBUG: Found attacking WHITE pawn at " + newRow + "," + newCol);
                        return true;
                    }
                }
            }
        }
        
        // Check attacks along straight lines (rook and queen)
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
                            return true; // Opponent king is adjacent
                        }
                        if (type == ChessPiece.PieceType.ROOK || type == ChessPiece.PieceType.QUEEN) {
                            return true; // Threatened by rook or queen
                        }
                    }
                    break; // Stop if a piece is in the way
                }
            }
        }
        
        // Check diagonal attacks (bishop and queen - note: pawn is handled separately above)
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
                        if (i == 1 && type == ChessPiece.PieceType.KING) {
                            return true; // Opponent king is diagonally adjacent
                        }
                        // Note: Pawn is now handled separately at the beginning
                        if (type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.QUEEN) {
                            return true; // Threatened by bishop or queen
                        }
                    }
                    break; // Stop if a piece is in the way
                }
            }
        }
        
        // Knight threat check
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
                    return true; // Knight threat
                }
            }
        }
        
        return false; // Square is not under attack
    }
    
    private boolean canCastle(int row, int col, boolean kingSide) {
        // ... existing code ...
        return false; // ... existing code ...
    }
    
    private void drawValidMoveIndicators(Graphics2D g2d) {
        // Draw valid moves
        if (!validMoves.isEmpty() && selectedRow != -1 && selectedCol != -1) {
            for (Point p : validMoves) {
                int drawRow = boardFlipped ? (BOARD_SIZE - 1 - p.x) : p.x;
                int drawCol = boardFlipped ? (BOARD_SIZE - 1 - p.y) : p.y;

                // Check if there's an opponent's piece at the target square
                ChessPiece targetPiece = chessBoard.getPiece(p.x, p.y);
                
                if (targetPiece != null) {
                    // If there's an opponent's piece, draw a frame
                    g2d.setColor(CAPTURE_INDICATOR_COLOR);
                    g2d.drawRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                    g2d.drawRect(drawCol * SQUARE_SIZE + 1, drawRow * SQUARE_SIZE + 1, SQUARE_SIZE - 3, SQUARE_SIZE - 3);
                    g2d.drawRect(drawCol * SQUARE_SIZE + 2, drawRow * SQUARE_SIZE + 2, SQUARE_SIZE - 5, SQUARE_SIZE - 5);
                } else {
                    // For an empty square, draw a larger dot
                    g2d.setColor(VALID_MOVE_INDICATOR_COLOR);
                    int dotSize = 24; // Larger size
                    g2d.fillOval(
                        drawCol * SQUARE_SIZE + (SQUARE_SIZE - dotSize) / 2, 
                        drawRow * SQUARE_SIZE + (SQUARE_SIZE - dotSize) / 2, 
                        dotSize, dotSize);
                }
            }
        }
        
        // Show invalid move (purple square)
        if (invalidMove != null && isFlashing) {
            int drawRow = boardFlipped ? (BOARD_SIZE - 1 - invalidMove.x) : invalidMove.x;
            int drawCol = boardFlipped ? (BOARD_SIZE - 1 - invalidMove.y) : invalidMove.y;
            
            // Purple highlight
            g2d.setColor(new Color(128, 0, 128, 150));
            g2d.fillRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
        
        // Highlight selected square
        if (selectedRow != -1 && selectedCol != -1) {
            int drawRow = boardFlipped ? (BOARD_SIZE - 1 - selectedRow) : selectedRow;
            int drawCol = boardFlipped ? (BOARD_SIZE - 1 - selectedCol) : selectedCol;
            
            g2d.setColor(SELECTED_SQUARE_COLOR);
            g2d.fillRect(drawCol * SQUARE_SIZE, drawRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }
    
    // Draw game over message
    private void drawGameOverOverlay(Graphics2D g2d, String message) {
        // Semi-transparent overlay for the whole board
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE);
        
        // Message box
        int boxWidth = BOARD_SIZE * SQUARE_SIZE - 100;
        int boxHeight = 200;
        int boxX = 50;
        int boxY = (BOARD_SIZE * SQUARE_SIZE - boxHeight) / 2;
        
        // Box with rounded corners
        g2d.setColor(new Color(240, 240, 240, 230));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Border
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        
        // Different colors for different outcomes
        if (message.contains("White wins")) {
            g2d.setColor(new Color(0, 150, 0));
            g2d.drawString("WHITE WINS", boxX + (boxWidth - fm.stringWidth("WHITE WINS")) / 2, boxY + 50);
        } else if (message.contains("Black wins")) {
            g2d.setColor(new Color(150, 0, 0));
            g2d.drawString("BLACK WINS", boxX + (boxWidth - fm.stringWidth("BLACK WINS")) / 2, boxY + 50);
        } else if (message.contains("Stalemate")) {
            g2d.setColor(new Color(150, 100, 0));
            g2d.drawString("DRAW", boxX + (boxWidth - fm.stringWidth("DRAW")) / 2, boxY + 50);
        } else {
            // Other cases
            g2d.setColor(new Color(0, 0, 150));
            g2d.drawString("GAME OVER", boxX + (boxWidth - fm.stringWidth("GAME OVER")) / 2, boxY + 50);
        }
        
        // Message details
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        
        // Split the message into multiple lines
        int yPos = boxY + 80;
        int lineHeight = fm.getHeight();
        String[] lines = message.split("\\. ");
        for (String line : lines) {
            if (!line.endsWith(".")) line += ".";
            g2d.drawString(line, boxX + (boxWidth - fm.stringWidth(line)) / 2, yPos);
            yPos += lineHeight;
        }
    }
    
    // Show status message
    private void drawStateMessage(Graphics2D g2d, String message, Color color) {
        g2d.setColor(new Color(0, 0, 0, 100)); // Slightly transparent background
        g2d.fillRect(0, 0, BOARD_SIZE * SQUARE_SIZE, 30);
        
        g2d.setColor(color);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Center the text
        int textWidth = g2d.getFontMetrics().stringWidth(message);
        int x = (BOARD_SIZE * SQUARE_SIZE - textWidth) / 2;
        
        g2d.drawString(message, x, 22);
    }
    
    // Update last move information
    public void setLastMove(int startRow, int startCol, int endRow, int endCol) {
        this.lastMoveStart = new Point(startRow, startCol);
        this.lastMoveEnd = new Point(endRow, endCol);
        repaint();
    }
    
    private void tryMove(int startRow, int startCol, int endRow, int endCol) {
        // Debug output
        debugUIState("tryMove-beginning");
        
        // Check if it's the player's turn
        if (chessBoard.getCurrentTurn() != playerColor) {
            // If not their turn, show red flashing
            clearSelectionAndHighlights();
            
            invalidMove = new Point(startRow, startCol);
            isFlashing = true;
            flashTimer.restart();
            return;
        }
        
        try {
            // Create the move
            ChessMove move = new ChessMove(startRow, startCol, endRow, endCol);
            ChessPiece piece = chessBoard.getPiece(startRow, startCol);
            
            // First check if the move is legal according to the rules
            if (!isLegalMove(startRow, startCol, endRow, endCol, playerColor)) {
                System.out.println("Invalid move: Does not resolve check situation.");
                
                // Show the invalid move with a purple highlight that will flash
                invalidMove = new Point(endRow, endCol);
                isFlashing = true;
                flashTimer.restart();
                
                // Make sure we clear selection state before returning
                selectedRow = -1;
                selectedCol = -1;
                validMoves.clear();
                
                // Debug output - invalid move
                debugUIState("tryMove-illegalMove");
                
                // Force repaint
                repaint();
                return;
            }
            
            // Check for pawn promotion
            if (piece.getType() == ChessPiece.PieceType.PAWN) {
                if ((endRow == 0 && piece.getColor() == ChessPiece.PieceColor.WHITE) ||
                    (endRow == 7 && piece.getColor() == ChessPiece.PieceColor.BLACK)) {
                    
                    // Show pawn promotion selection
                    String[] options = {"Queen", "Rook", "Bishop", "Knight"};
                    int choice = JOptionPane.showOptionDialog(
                        this,
                        "Which piece would you like to promote to?",
                        "Pawn Promotion",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                    );
                    
                    // Determine promotion type
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
            
            // Make the move on the board
            makeMove(move);
            
            // Debug output - move successful
            debugUIState("tryMove-moveSuccessful");
            
            // Send the move across the network (if online game)
            if (client != null) {
                client.sendMoveMessage(move);
            }
        } catch (Exception e) {
            // If an error occurs while making the move, clear the UI state
            System.err.println("Error while making move: " + e.getMessage());
            e.printStackTrace();  // Print stack trace
            
            clearSelectionAndHighlights();
            
            // Debug output - error
            debugUIState("tryMove-error");
        }
    }
    
    public ChessPiece.PieceColor getPlayerColor() {
        return playerColor;
    }
    
    // Find all pieces checking the king
    private List<Point> findCheckingPieces(ChessPiece.PieceColor kingColor) {
        List<Point> checkingPieces = new ArrayList<>();
        
        // Find the king's position
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
        
        if (kingRow == -1) return checkingPieces; // King not found
        
        ChessPiece.PieceColor opponentColor = (kingColor == ChessPiece.PieceColor.WHITE) ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
        
        // Check attacks along straight lines (horizontal, vertical - rook and queen)
        int[][] straightDirections = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
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
                            checkingPieces.add(new Point(newRow, newCol));
                        }
                    }
                    break; // Stop if a piece is in the way
                }
            }
        }
        
        // Check attacks along diagonals (diagonal - bishop, queen, and pawn)
        int[][] diagonalDirections = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
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
                            // Pawn attack - pawns only check when moving in the correct direction
                            // White pawns check from bottom to top (dir[0] < 0)
                            // Black pawns check from top to bottom (dir[0] > 0)
                            if ((opponentColor == ChessPiece.PieceColor.WHITE && dir[0] < 0) ||
                                (opponentColor == ChessPiece.PieceColor.BLACK && dir[0] > 0)) {
                                checkingPieces.add(new Point(newRow, newCol));
                            }
                        }
                        if (type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.QUEEN) {
                            checkingPieces.add(new Point(newRow, newCol));
                        }
                    }
                    break; // Stop if a piece is in the way
                }
            }
        }
        
        // Knight attack check
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
                    checkingPieces.add(new Point(newRow, newCol));
                }
            }
        }
        
        return checkingPieces;
    }
    
    // Check if the move blocks the check by creating a block between king and checking piece
    private boolean isBlockingCheck(int startRow, int startCol, int endRow, int endCol, ChessPiece.PieceColor kingColor) {
        // Find the king's position
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
        
        if (kingRow == -1) return false; // King not found
        
        // Find the position of the checking piece
        List<Point> checkingPieces = findCheckingPieces(kingColor);
        if (checkingPieces.isEmpty()) return false;
        
        Point checkingPiece = checkingPieces.get(0);
        
        // Only straight line (rook, queen) or diagonal line (bishop, queen) threats can be blocked
        // Knight and pawn threats cannot be blocked, they must be captured directly
        ChessPiece piece = chessBoard.getPiece(checkingPiece.x, checkingPiece.y);
        if (piece == null || piece.getType() == ChessPiece.PieceType.KNIGHT || piece.getType() == ChessPiece.PieceType.PAWN) {
            return false;
        }
        
        // Find the path from checking piece to king
        int rowDir = Integer.compare(kingRow - checkingPiece.x, 0);
        int colDir = Integer.compare(kingCol - checkingPiece.y, 0);
        
        // Check every square on the path
        int row = checkingPiece.x + rowDir;
        int col = checkingPiece.y + colDir;
        
        while (row != kingRow || col != kingCol) {
            if (row == endRow && col == endCol) {
                return true; // Creating a block on the path
            }
            row += rowDir;
            col += colDir;
        }
        
        return false;
    }
    
    // Check if a move is legal (doesn't put the king in danger)
    private boolean isLegalMove(int startRow, int startCol, int endRow, int endCol, ChessPiece.PieceColor color) {
        // Check if it's a valid movement
        ChessPiece piece = chessBoard.getPiece(startRow, startCol);
        ChessPiece capturedPiece = chessBoard.getPiece(endRow, endCol);
        
        // Is the piece color correct?
        if (piece == null || piece.getColor() != color) {
            return false;
        }
        
        // Is the target square within board boundaries?
        if (!chessBoard.isValidPosition(endRow, endCol)) {
            return false;
        }
        
        // Is there one of our own pieces at the target square?
        if (capturedPiece != null && capturedPiece.getColor() == color) {
            return false;
        }
        
        // If there's a check situation and the target square is the checking piece,
        // capturing the checking piece is always a legal move (including pinned pieces)
        if (chessBoard.isInCheck(color)) {
            List<Point> checkingPieces = findCheckingPieces(color);
            for (Point checkingPiece : checkingPieces) {
                if (checkingPiece.x == endRow && checkingPiece.y == endCol) {
                    // This is a move that captures a checking piece
                    // Make the move temporarily for testing
                    ChessPiece originalPiece = piece;
                    ChessPiece originalCapturedPiece = capturedPiece;
                    
                    // Move the piece temporarily
                    chessBoard.setPiece(endRow, endCol, piece);
                    chessBoard.setPiece(startRow, startCol, null);
                    
                    // Check if our king is under attack after this move
                    boolean kingInCheck = isKingUnderAttack(color);
                    
                    // Restore pieces to original positions
                    chessBoard.setPiece(startRow, startCol, originalPiece);
                    chessBoard.setPiece(endRow, endCol, originalCapturedPiece);
                    
                    // If our king is not in danger after the move, it's a legal move
                    return !kingInCheck;
                }
            }
        }
        
        // Make the move temporarily for testing
        // Save original pieces
        ChessPiece originalPiece = piece;
        ChessPiece originalCapturedPiece = capturedPiece;
        
        // Move the piece temporarily
        chessBoard.setPiece(endRow, endCol, piece);
        chessBoard.setPiece(startRow, startCol, null);
        
        // Special case: En passant
        ChessPiece enPassantCapturedPiece = null;
        int enPassantRow = -1, enPassantCol = -1;
        
        if (piece.getType() == ChessPiece.PieceType.PAWN && 
            startCol != endCol && 
            capturedPiece == null) {
            
            // This could be an en passant capture
            enPassantRow = startRow;
            enPassantCol = endCol;
            enPassantCapturedPiece = chessBoard.getPiece(enPassantRow, enPassantCol);
            
            if (enPassantCapturedPiece != null && 
                enPassantCapturedPiece.getType() == ChessPiece.PieceType.PAWN &&
                chessBoard.wasLastMoveDoublePawnPush() &&
                chessBoard.getLastPawnMoveRow() == startRow &&
                chessBoard.getLastPawnMoveCol() == endCol) {
                
                // Temporarily remove the pawn captured by en passant
                chessBoard.setPiece(enPassantRow, enPassantCol, null);
            } else {
                enPassantCapturedPiece = null;
            }
        }
        
        // Check if our king is under attack after this move
        boolean kingInCheck = isKingUnderAttack(color);
        
        // Restore pieces to original positions
        chessBoard.setPiece(startRow, startCol, originalPiece);
        chessBoard.setPiece(endRow, endCol, originalCapturedPiece);
        
        // Restore en passant piece
        if (enPassantCapturedPiece != null) {
            chessBoard.setPiece(enPassantRow, enPassantCol, enPassantCapturedPiece);
        }
        
        // If our king is in danger after the move, it's not a legal move
        return !kingInCheck;
    }
    
    // Helper function for debugging
    private void debugUIState(String context) {
        System.out.println("==== DEBUG (" + context + ") ====");
        System.out.println("selectedRow: " + selectedRow);
        System.out.println("selectedCol: " + selectedCol);
        System.out.println("validMoves: " + validMoves.size());
        System.out.println("invalidMove: " + invalidMove);
        System.out.println("isFlashing: " + isFlashing);
        System.out.println("flashTimer running: " + flashTimer.isRunning());
        System.out.println("flashCount: " + flashCount);
        System.out.println("==============");
    }
} 