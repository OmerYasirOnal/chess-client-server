package com.chess.common;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChessBoard implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final int BOARD_SIZE = 8;
    private ChessPiece[][] board;
    private ChessPiece.PieceColor currentTurn;
    private boolean gameOver;
    private String gameResult;
    
    // Oyuncu adları
    private String whitePlayerName;
    private String blackPlayerName;
    
    // For en passant
    private int lastPawnMoveRow = -1;
    private int lastPawnMoveCol = -1;
    private boolean lastMoveWasDoublePawnPush = false;
    
    // For fifty-move rule and threefold repetition
    private int halfMoveClock = 0;
    private List<String> boardPositions = new ArrayList<>();
    
    public ChessBoard() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        currentTurn = ChessPiece.PieceColor.WHITE; // White starts
        gameOver = false;
        whitePlayerName = "White Player";
        blackPlayerName = "Black Player";
        
        initializeBoard();
        
        // Save the initial position
        boardPositions.add(getBoardPositionString());
    }
    
    private void initializeBoard() {
        // Place pieces in their starting positions
        
        // White pieces
        board[7][0] = new ChessPiece(ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.WHITE);
        board[7][1] = new ChessPiece(ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.WHITE);
        board[7][2] = new ChessPiece(ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.WHITE);
        board[7][3] = new ChessPiece(ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.WHITE);
        board[7][4] = new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.WHITE);
        board[7][5] = new ChessPiece(ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.WHITE);
        board[7][6] = new ChessPiece(ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.WHITE);
        board[7][7] = new ChessPiece(ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.WHITE);
        
        // White pawns
        for (int col = 0; col < BOARD_SIZE; col++) {
            board[6][col] = new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        }
        
        // Black pieces
        board[0][0] = new ChessPiece(ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.BLACK);
        board[0][1] = new ChessPiece(ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK);
        board[0][2] = new ChessPiece(ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.BLACK);
        board[0][3] = new ChessPiece(ChessPiece.PieceType.QUEEN, ChessPiece.PieceColor.BLACK);
        board[0][4] = new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK);
        board[0][5] = new ChessPiece(ChessPiece.PieceType.BISHOP, ChessPiece.PieceColor.BLACK);
        board[0][6] = new ChessPiece(ChessPiece.PieceType.KNIGHT, ChessPiece.PieceColor.BLACK);
        board[0][7] = new ChessPiece(ChessPiece.PieceType.ROOK, ChessPiece.PieceColor.BLACK);
        
        // Black pawns
        for (int col = 0; col < BOARD_SIZE; col++) {
            board[1][col] = new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        }
    }
    
    public ChessPiece getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }
    
    public void setPiece(int row, int col, ChessPiece piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
        }
    }
    
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }
    
    public void makeMove(ChessMove move) {
        int startRow = move.getStartRow();
        int startCol = move.getStartCol();
        int endRow = move.getEndRow();
        int endCol = move.getEndCol();
        
        ChessPiece piece = board[startRow][startCol];
        ChessPiece capturedPiece = board[endRow][endCol];
        
        // Hamle doğrulaması: Eğer hamle yapan oyuncu şah çekme durumundaysa, 
        // sadece şahı tehditten kurtaran hamlelere izin verilir
        if (piece != null && isInCheck(piece.getColor())) {
            // En passant durumu için özel kontrol
            boolean isEnPassant = false;
            ChessPiece tempCapturedPiece = null;
            
            if (piece.getType() == ChessPiece.PieceType.PAWN && 
                endCol != startCol && 
                capturedPiece == null) {
                
                // Diagonal move with no piece at target but column changed, could be en passant
                if (lastMoveWasDoublePawnPush && 
                    startRow == lastPawnMoveRow && 
                    endCol == lastPawnMoveCol) {
                    
                    tempCapturedPiece = board[startRow][endCol];
                    board[startRow][endCol] = null; // Temporary removal of captured pawn
                    isEnPassant = true;
                }
            }
            
            // Rok durumu için özel kontrol
            boolean isCastling = false;
            ChessPiece rook = null;
            int rookStartCol = -1;
            int rookEndCol = -1;
            
            if (piece.getType() == ChessPiece.PieceType.KING && Math.abs(startCol - endCol) > 1) {
                isCastling = true;
                
                // Kingside castling
                if (endCol > startCol) {
                    rookStartCol = 7;
                    rookEndCol = 5;
                } 
                // Queenside castling
                else {
                    rookStartCol = 0;
                    rookEndCol = 3;
                }
                
                rook = board[startRow][rookStartCol];
                
                // Temporarily move rook
                board[startRow][rookEndCol] = rook;
                board[startRow][rookStartCol] = null;
            }
            
            // Geçici olarak hamleyi uygula
            board[endRow][endCol] = piece;
            board[startRow][startCol] = null;
            
            // Hamle sonrası şah hala tehdit altında mı kontrol et
            boolean stillInCheck = isInCheck(piece.getColor());
            
            // Hamleyi geri al
            board[startRow][startCol] = piece;
            board[endRow][endCol] = capturedPiece;
            
            // En passant durumunu geri al
            if (isEnPassant && tempCapturedPiece != null) {
                board[startRow][endCol] = tempCapturedPiece;
            }
            
            // Rok durumunu geri al
            if (isCastling && rook != null) {
                board[startRow][rookStartCol] = rook;
                board[startRow][rookEndCol] = null;
            }
            
            // Eğer şah hala tehdit altındaysa, hamle geçersizdir
            if (stillInCheck) {
                return; // Hamleyi uygulamadan çık
            }
        }
        
        // Save the captured piece (if any)
        move.setCapturedPiece(capturedPiece);
        
        // Update counter for 50 move rule
        boolean isPawnMove = piece.getType() == ChessPiece.PieceType.PAWN;
        boolean isCapture = capturedPiece != null;
        
        if (isPawnMove || isCapture) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // En passant check
        boolean isEnPassant = false;
        if (piece.getType() == ChessPiece.PieceType.PAWN && 
            endCol != startCol && 
            board[endRow][endCol] == null) {
            // Diagonal move with no piece at target, could be en passant
            int captureRow = startRow; // The captured pawn is on the original row
            ChessPiece capturedPawn = board[captureRow][endCol];
            
            if (capturedPawn != null && 
                capturedPawn.getType() == ChessPiece.PieceType.PAWN && 
                capturedPawn.getColor() != piece.getColor()) {
                
                // Record the en passant move
                move.setCapturedPiece(capturedPawn);
                board[captureRow][endCol] = null; // Remove the captured pawn
                isCapture = true;
                isEnPassant = true;
                halfMoveClock = 0; // Reset counter for piece capture
            }
        }
        
        // Move the piece to new position
        board[endRow][endCol] = piece;
        board[startRow][startCol] = null;
        
        // Save last move for en passant
        lastMoveWasDoublePawnPush = false;
        if (piece.getType() == ChessPiece.PieceType.PAWN && Math.abs(startRow - endRow) == 2) {
            lastPawnMoveRow = endRow;
            lastPawnMoveCol = endCol;
            lastMoveWasDoublePawnPush = true;
        } else {
            lastPawnMoveRow = -1;
            lastPawnMoveCol = -1;
        }
        
        // Mark the piece as moved
        piece.setHasMoved(true);
        
        // Check for castling move
        if (piece.getType() == ChessPiece.PieceType.KING && Math.abs(startCol - endCol) > 1) {
            // Kingside castling (to the right)
            if (endCol > startCol) {
                ChessPiece rook = board[startRow][7];
                board[startRow][5] = rook;
                board[startRow][7] = null;
                rook.setHasMoved(true);
                move.setCastling(true);
            } 
            // Queenside castling (to the left)
            else {
                ChessPiece rook = board[startRow][0];
                board[startRow][3] = rook;
                board[startRow][0] = null;
                rook.setHasMoved(true);
                move.setCastling(true);
            }
        }
        
        // Check for pawn promotion
        if (piece.getType() == ChessPiece.PieceType.PAWN && (endRow == 0 || endRow == 7)) {
            if (move.isPromotion() && move.getPromotionType() != null) {
                board[endRow][endCol] = new ChessPiece(move.getPromotionType(), piece.getColor());
            } else {
                // Default promotion to queen
                board[endRow][endCol] = new ChessPiece(ChessPiece.PieceType.QUEEN, piece.getColor());
                move.setPromotion(true);
                move.setPromotionType(ChessPiece.PieceType.QUEEN);
            }
        }
        
        // Change turn
        currentTurn = (currentTurn == ChessPiece.PieceColor.WHITE) 
                ? ChessPiece.PieceColor.BLACK 
                : ChessPiece.PieceColor.WHITE;
        
        // Save the new board position
        String newPosition = getBoardPositionString();
        boardPositions.add(newPosition);
        
        // Check game state (checkmate, stalemate, draw)
        checkGameState();
    }
    
    /**
     * Checks if a specified square is under attack for a piece of the specified color.
     * This method is used to determine if a square is threatened by opponent pieces.
     * Especially useful for determining safe squares where the king can move.
     * 
     * For pawn threats, pawns only threaten squares diagonally (not forward):
     * - White pawns threaten squares in the upper-right and upper-left diagonals
     * - Black pawns threaten squares in the lower-right and lower-left diagonals
     * 
     * @param targetRow       Row index of the square to check
     * @param targetCol       Column index of the square to check
     * @param kingColor       Color of the king (the color of the player being checked for threats)
     * @return                True if the square is under attack, false otherwise
     */
    private boolean isSquareUnderAttack(int targetRow, int targetCol, ChessPiece.PieceColor kingColor) {
        ChessPiece.PieceColor opponentColor = (kingColor == ChessPiece.PieceColor.WHITE) ? 
                ChessPiece.PieceColor.BLACK : ChessPiece.PieceColor.WHITE;
        
        // Check for attacks along straight lines (rook and queen)
        int[][] straightDirections = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, down, left, up
        for (int[] dir : straightDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = targetRow + i * dir[0];
                int newCol = targetCol + i * dir[1];
                
                if (!isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = board[newRow][newCol];
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (i == 1 && type == ChessPiece.PieceType.KING) {
                            return true; // Opponent's king is in an adjacent square
                        }
                        if (type == ChessPiece.PieceType.ROOK || type == ChessPiece.PieceType.QUEEN) {
                            return true; // Threatened by rook or queen
                        }
                    }
                    break; // Another piece blocks this line
                }
            }
        }
        
        // Special check for pawn threats
        // Pawns only threaten diagonally
        if (kingColor == ChessPiece.PieceColor.WHITE) {
            // Check for white king - black pawns threaten diagonally upward
            if (isValidPosition(targetRow + 1, targetCol - 1)) {
                ChessPiece piece = board[targetRow + 1][targetCol - 1];
                if (piece != null && piece.getColor() == ChessPiece.PieceColor.BLACK && 
                    piece.getType() == ChessPiece.PieceType.PAWN) {
                    return true; // Black pawn threatening from lower-left diagonal
                }
            }
            if (isValidPosition(targetRow + 1, targetCol + 1)) {
                ChessPiece piece = board[targetRow + 1][targetCol + 1];
                if (piece != null && piece.getColor() == ChessPiece.PieceColor.BLACK && 
                    piece.getType() == ChessPiece.PieceType.PAWN) {
                    return true; // Black pawn threatening from lower-right diagonal
                }
            }
        } else {
            // Check for black king - white pawns threaten diagonally downward
            if (isValidPosition(targetRow - 1, targetCol - 1)) {
                ChessPiece piece = board[targetRow - 1][targetCol - 1];
                if (piece != null && piece.getColor() == ChessPiece.PieceColor.WHITE && 
                    piece.getType() == ChessPiece.PieceType.PAWN) {
                    return true; // White pawn threatening from upper-left diagonal
                }
            }
            if (isValidPosition(targetRow - 1, targetCol + 1)) {
                ChessPiece piece = board[targetRow - 1][targetCol + 1];
                if (piece != null && piece.getColor() == ChessPiece.PieceColor.WHITE && 
                    piece.getType() == ChessPiece.PieceType.PAWN) {
                    return true; // White pawn threatening from upper-right diagonal
                }
            }
        }
        
        // Check for attacks along diagonals (bishop and queen)
        int[][] diagonalDirections = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}}; // Down-right, down-left, up-left, up-right
        for (int[] dir : diagonalDirections) {
            for (int i = 1; i < BOARD_SIZE; i++) {
                int newRow = targetRow + i * dir[0];
                int newCol = targetCol + i * dir[1];
                
                if (!isValidPosition(newRow, newCol)) break;
                
                ChessPiece piece = board[newRow][newCol];
                if (piece != null) {
                    if (piece.getColor() == opponentColor) {
                        ChessPiece.PieceType type = piece.getType();
                        if (i == 1 && type == ChessPiece.PieceType.KING) {
                            return true; // Opponent's king is in diagonal
                        }
                        if (type == ChessPiece.PieceType.BISHOP || type == ChessPiece.PieceType.QUEEN) {
                            return true; // Threatened by bishop or queen
                        }
                    }
                    break; // Another piece blocks this line
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
            
            if (isValidPosition(newRow, newCol)) {
                ChessPiece piece = board[newRow][newCol];
                if (piece != null && piece.getColor() == opponentColor && 
                    piece.getType() == ChessPiece.PieceType.KNIGHT) {
                    return true; // Knight threat
                }
            }
        }
        
        return false; // Square not threatened
    }
    
    /**
     * Checks if the king of the specified color is in check.
     * 
     * @param kingColor The color of the king
     * @return True if the king is in check, false otherwise
     */
    public boolean isInCheck(ChessPiece.PieceColor kingColor) {
        // Find the king's position
        int kingRow = -1, kingCol = -1;
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
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
        
        // Check if king's position is under attack
        return isSquareUnderAttack(kingRow, kingCol, kingColor);
    }
    
    private boolean canCastle(int row, int col, boolean kingSide, ChessPiece.PieceColor color) {
        int rookCol = kingSide ? 7 : 0;
        int direction = kingSide ? 1 : -1;
        
        ChessPiece rook = board[row][rookCol];
        if (rook == null || rook.getType() != ChessPiece.PieceType.ROOK || 
                rook.hasMoved() || rook.getColor() != color) {
            return false;
        }
        
        // Check for empty squares between king and rook
        for (int i = 1; i < Math.abs(col - rookCol); i++) {
            if (board[row][col + i * direction] != null) {
                return false;
            }
        }
        
        // Check if king is in check
        if (isInCheck(color)) {
            return false;
        }
        
        // Check if squares king will pass through are threatened
        for (int i = 0; i <= 2; i++) {
            int testCol = col + i * direction;
            if (isSquareUnderAttack(row, testCol, color)) {
                return false;
            }
        }
        
        return true;
    }
    
    // Check for checkmate
    public boolean isCheckmate(ChessPiece.PieceColor kingColor) {
        // First, check king
        if (!isInCheck(kingColor)) {
            return false;
        }
        
        // Check if any legal move exists
        return !hasLegalMoves(kingColor);
    }
    
    // Check for stalemate
    public boolean isStalemate(ChessPiece.PieceColor kingColor) {
        // If king not in check and cannot move
        return !isInCheck(kingColor) && !hasLegalMoves(kingColor);
    }
    
    // Check if any legal move exists for a player
    private boolean hasLegalMoves(ChessPiece.PieceColor color) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.getColor() == color) {
                    List<Point> moves = calculatePieceMoves(row, col, piece);
                    
                    // Simulate each potential move and check if king is in check
                    for (Point move : moves) {
                        if (isLegalMove(row, col, move.x, move.y, color)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    // Check if a move is legal (puts king in check)
    private boolean isLegalMove(int startRow, int startCol, int endRow, int endCol, ChessPiece.PieceColor color) {
        ChessPiece piece = board[startRow][startCol];
        ChessPiece capturedPiece = board[endRow][endCol];
        
        // If king is trying to capture a piece that is in check, move is illegal
        if (piece.getType() == ChessPiece.PieceType.KING && 
            capturedPiece != null && 
            isSquareUnderAttack(endRow, endCol, color)) {
            return false;
        }
        
        // Temporary piece storage
        ChessPiece tempCapturedPiece = null;
        boolean isEnPassant = false;
        
        // En passant check
        if (piece.getType() == ChessPiece.PieceType.PAWN && 
            endCol != startCol && 
            capturedPiece == null) {
            
            // Diagonal move with no piece at target but column changed, could be en passant
            if (lastMoveWasDoublePawnPush && 
                startRow == lastPawnMoveRow && 
                endCol == lastPawnMoveCol) {
                
                tempCapturedPiece = board[startRow][endCol];
                board[startRow][endCol] = null; // Temporary removal of captured pawn
                isEnPassant = true;
            }
        }
        
        // Rook check
        boolean isCastling = false;
        ChessPiece rook = null;
        int rookStartCol = -1;
        int rookEndCol = -1;
        
        if (piece.getType() == ChessPiece.PieceType.KING && Math.abs(startCol - endCol) > 1) {
            isCastling = true;
            
            // Kingside castling
            if (endCol > startCol) {
                rookStartCol = 7;
                rookEndCol = 5;
            } 
            // Queenside castling
            else {
                rookStartCol = 0;
                rookEndCol = 3;
            }
            
            rook = board[startRow][rookStartCol];
            
            // Temporarily move rook
            board[startRow][rookEndCol] = rook;
            board[startRow][rookStartCol] = null;
        }
        
        // Simulate move temporarily
        board[endRow][endCol] = piece;
        board[startRow][startCol] = null;
        
        // Check if king is in check
        boolean isLegal = !isInCheck(color);
        
        // Undo move
        board[startRow][startCol] = piece;
        board[endRow][endCol] = capturedPiece;
        
        // Return captured piece to board
        if (isEnPassant && tempCapturedPiece != null) {
            board[startRow][endCol] = tempCapturedPiece;
        }
        
        // Undo rook move
        if (isCastling && rook != null) {
            board[startRow][rookStartCol] = rook;
            board[startRow][rookEndCol] = null;
        }
        
        return isLegal;
    }
    
    // 50 move rule check
    public boolean isFiftyMoveRule() {
        return halfMoveClock >= 50;
    }
    
    // Threefold repetition check
    public boolean isThreefoldRepetition() {
        String currentPosition = getBoardPositionString();
        int count = 0;
        
        for (String position : boardPositions) {
            if (position.equals(currentPosition)) {
                count++;
                if (count >= 3) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Generate board position string
    private String getBoardPositionString() {
        StringBuilder sb = new StringBuilder();
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece == null) {
                    sb.append("-");
                } else {
                    char pieceChar = piece.toString().charAt(0);
                    sb.append(piece.getColor() == ChessPiece.PieceColor.WHITE ? 
                            Character.toUpperCase(pieceChar) : Character.toLowerCase(pieceChar));
                }
            }
        }
        
        // Add current turn
        sb.append(currentTurn == ChessPiece.PieceColor.WHITE ? "w" : "b");
        
        return sb.toString();
    }
    
    // Check game state
    private void checkGameState() {
        // Check for checkmate
        if (isInCheck(currentTurn)) {
            if (!hasLegalMoves(currentTurn)) {
                gameOver = true;
                ChessPiece.PieceColor winner = (currentTurn == ChessPiece.PieceColor.WHITE) 
                        ? ChessPiece.PieceColor.BLACK 
                        : ChessPiece.PieceColor.WHITE;
                gameResult = winner.toString() + " player checkmated and won!";
                return;
            }
        }
        // Check for stalemate
        else if (!hasLegalMoves(currentTurn)) {
            gameOver = true;
            gameResult = "Stalemate! Game ended in a draw.";
            return;
        }
        
        // 50 move rule check
        if (isFiftyMoveRule()) {
            gameOver = true;
            gameResult = "50 move rule! Game ended in a draw.";
            return;
        }
        
        // Threefold repetition check
        if (isThreefoldRepetition()) {
            gameOver = true;
            gameResult = "Threefold repetition! Game ended in a draw.";
            return;
        }
        
        // Insufficient material check
        if (hasInsufficientMaterial()) {
            gameOver = true;
            gameResult = "Insufficient material! Game ended in a draw.";
            return;
        }
    }
    
    // Insufficient material check (only kings left or king+bishop/knight vs.)
    private boolean hasInsufficientMaterial() {
        int whiteCount = 0, blackCount = 0;
        boolean whiteHasMinorPiece = false, blackHasMinorPiece = false;
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    if (piece.getColor() == ChessPiece.PieceColor.WHITE) {
                        whiteCount++;
                        if (piece.getType() == ChessPiece.PieceType.BISHOP || 
                            piece.getType() == ChessPiece.PieceType.KNIGHT) {
                            whiteHasMinorPiece = true;
                        }
                    } else {
                        blackCount++;
                        if (piece.getType() == ChessPiece.PieceType.BISHOP || 
                            piece.getType() == ChessPiece.PieceType.KNIGHT) {
                            blackHasMinorPiece = true;
                        }
                    }
                    
                    // Rook, queen, or pawn exists, sufficient material
                    if (piece.getType() == ChessPiece.PieceType.ROOK || 
                        piece.getType() == ChessPiece.PieceType.QUEEN || 
                        piece.getType() == ChessPiece.PieceType.PAWN) {
                        return false;
                    }
                }
            }
        }
        
        // Only kings left, insufficient material
        if (whiteCount == 1 && blackCount == 1) {
            return true;
        }
        
        // King + bishop/knight vs. king situations, insufficient material
        if ((whiteCount == 1 || (whiteCount == 2 && whiteHasMinorPiece)) && 
            (blackCount == 1 || (blackCount == 2 && blackHasMinorPiece))) {
            return true;
        }
        
        return false;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public void updateGameState() {
        checkGameState();
    }
    
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    public String getGameResult() {
        return gameResult;
    }
    
    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
        this.gameOver = true;
    }
    
    public ChessPiece.PieceColor getCurrentTurn() {
        return currentTurn;
    }
    
    public void setCurrentTurn(ChessPiece.PieceColor currentTurn) {
        this.currentTurn = currentTurn;
    }
    
    public boolean isWhiteTurn() {
        return currentTurn == ChessPiece.PieceColor.WHITE;
    }
    
    public boolean wasLastMoveDoublePawnPush() {
        return lastMoveWasDoublePawnPush;
    }
    
    public int getLastPawnMoveRow() {
        return lastPawnMoveRow;
    }
    
    public int getLastPawnMoveCol() {
        return lastPawnMoveCol;
    }
    
    public void reset() {
        // Reset board to starting position
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        currentTurn = ChessPiece.PieceColor.WHITE;
        gameOver = false;
        gameResult = null;
        
        // Reset en passant variables
        lastPawnMoveRow = -1;
        lastPawnMoveCol = -1;
        lastMoveWasDoublePawnPush = false;
        
        // Reset 50 move rule counter
        halfMoveClock = 0;
        
        // Reset board history
        boardPositions.clear();
        
        // Reset pieces to starting positions
        initializeBoard();
        
        // Save starting position
        boardPositions.add(getBoardPositionString());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            sb.append(8 - row).append(" ");
            
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece == null) {
                    sb.append(((row + col) % 2 == 0) ? "□ " : "■ ");
                } else {
                    sb.append(piece.toString()).append(" ");
                }
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Calculates all valid moves for the specified piece.
     *
     * @param row The row position of the piece
     * @param col The column position of the piece
     * @param piece The piece object
     * @return A list of all valid squares the piece can move to
     */
    private List<Point> calculatePieceMoves(int row, int col, ChessPiece piece) {
        List<Point> moves = new ArrayList<>();
        
        switch (piece.getType()) {
            case PAWN:
                calculatePawnMoves(row, col, piece, moves);
                break;
            case ROOK:
                calculateStraightMoves(row, col, piece, true, false, moves);
                break;
            case KNIGHT:
                calculateKnightMoves(row, col, piece, moves);
                break;
            case BISHOP:
                calculateStraightMoves(row, col, piece, false, true, moves);
                break;
            case QUEEN:
                calculateStraightMoves(row, col, piece, true, true, moves);
                break;
            case KING:
                calculateKingMoves(row, col, piece, moves);
                break;
        }
        
        return moves;
    }
    
    /**
     * Calculates possible moves for a pawn.
     * 
     * @param row Row position of the pawn
     * @param col Column position of the pawn
     * @param piece Pawn object
     * @param moves List of moves to add to
     */
    private void calculatePawnMoves(int row, int col, ChessPiece piece, List<Point> moves) {
        int direction = (piece.getColor() == ChessPiece.PieceColor.WHITE) ? -1 : 1;
        
        // Forward movement
        if (isValidPosition(row + direction, col) && board[row + direction][col] == null) {
            moves.add(new Point(row + direction, col));
            
            // Can move two squares on first move
            if (!piece.hasMoved() && 
                isValidPosition(row + 2 * direction, col) && 
                board[row + 2 * direction][col] == null) {
                moves.add(new Point(row + 2 * direction, col));
            }
        }
        
        // Diagonal capture moves
        for (int offset : new int[]{-1, 1}) {
            if (isValidPosition(row + direction, col + offset)) {
                ChessPiece targetPiece = board[row + direction][col + offset];
                if (targetPiece != null && targetPiece.getColor() != piece.getColor()) {
                    moves.add(new Point(row + direction, col + offset));
                } 
                // En passant check
                else if (targetPiece == null && lastMoveWasDoublePawnPush) {
                    // If there's a pawn in the adjacent square that just moved two squares
                    if (row == lastPawnMoveRow && col + offset == lastPawnMoveCol) {
                        ChessPiece pawnToCapture = board[row][col + offset];
                        if (pawnToCapture != null && 
                            pawnToCapture.getType() == ChessPiece.PieceType.PAWN && 
                            pawnToCapture.getColor() != piece.getColor()) {
                            moves.add(new Point(row + direction, col + offset));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Calculates straight and diagonal movements for rook, bishop, and queen.
     * 
     * @param row Row position of the piece
     * @param col Column position of the piece
     * @param piece Piece object
     * @param straightLines Check straight movements
     * @param diagonalLines Check diagonal movements
     * @param moves List of moves to add to
     */
    private void calculateStraightMoves(int row, int col, ChessPiece piece, 
                                     boolean straightLines, boolean diagonalLines, List<Point> moves) {
        // Straight movements (horizontal and vertical)
        if (straightLines) {
            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, down, left, up
            
            for (int[] dir : directions) {
                for (int i = 1; i < BOARD_SIZE; i++) {
                    int newRow = row + i * dir[0];
                    int newCol = col + i * dir[1];
                    
                    if (!isValidPosition(newRow, newCol)) {
                        break;
                    }
                    
                    ChessPiece targetPiece = board[newRow][newCol];
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
                    
                    if (!isValidPosition(newRow, newCol)) {
                        break;
                    }
                    
                    ChessPiece targetPiece = board[newRow][newCol];
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
    
    /**
     * Calculates possible moves for a knight.
     * 
     * @param row Row position of the knight
     * @param col Column position of the knight
     * @param piece Knight object
     * @param moves List of moves to add to
     */
    private void calculateKnightMoves(int row, int col, ChessPiece piece, List<Point> moves) {
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        
        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            
            if (isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = board[newRow][newCol];
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    moves.add(new Point(newRow, newCol));
                }
            }
        }
    }
    
    /**
     * Calculates possible moves for a king.
     * 
     * @param row Row position of the king
     * @param col Column position of the king
     * @param piece King object
     * @param moves List of moves to add to
     */
    private void calculateKingMoves(int row, int col, ChessPiece piece, List<Point> moves) {
        int[][] kingMoves = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };
        
        for (int[] move : kingMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];
            
            if (isValidPosition(newRow, newCol)) {
                ChessPiece targetPiece = board[newRow][newCol];
                if (targetPiece == null || targetPiece.getColor() != piece.getColor()) {
                    // The king cannot move to a square that is under attack
                    if (!isSquareUnderAttack(newRow, newCol, piece.getColor())) {
                        moves.add(new Point(newRow, newCol));
                    }
                }
            }
        }
        
        // Check castling - ensure king is not in check and will not pass through check
        if (!piece.hasMoved() && !isInCheck(piece.getColor())) {
            // Kingside castling
            if (canCastle(row, col, true, piece.getColor())) {
                moves.add(new Point(row, col + 2));
            }
            
            // Queenside castling
            if (canCastle(row, col, false, piece.getColor())) {
                moves.add(new Point(row, col - 2));
            }
        }
    }
    
    public void setWhitePlayerName(String name) {
        this.whitePlayerName = name;
    }
    
    public void setBlackPlayerName(String name) {
        this.blackPlayerName = name;
    }
    
    public String getWhitePlayerName() {
        return whitePlayerName;
    }
    
    public String getBlackPlayerName() {
        return blackPlayerName;
    }
} 