package com.chess.common;

import java.io.Serializable;

public class ChessMove implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int startRow;
    private int startCol;
    private int endRow;
    private int endCol;
    private ChessPiece piece;
    private ChessPiece capturedPiece;
    private boolean isCastling;
    private boolean isPromotion;
    private ChessPiece.PieceType promotionType;
    
    public ChessMove(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
    }
    
    public ChessMove(int startRow, int startCol, int endRow, int endCol, ChessPiece piece) {
        this(startRow, startCol, endRow, endCol);
        this.piece = piece;
    }
    
    public ChessMove(int startRow, int startCol, int endRow, int endCol, ChessPiece piece, boolean isCastling) {
        this(startRow, startCol, endRow, endCol, piece);
        this.isCastling = isCastling;
    }
    
    public int getStartRow() {
        return startRow;
    }
    
    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
    
    public int getStartCol() {
        return startCol;
    }
    
    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }
    
    public int getEndRow() {
        return endRow;
    }
    
    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }
    
    public int getEndCol() {
        return endCol;
    }
    
    public void setEndCol(int endCol) {
        this.endCol = endCol;
    }
    
    public ChessPiece getPiece() {
        return piece;
    }
    
    public void setPiece(ChessPiece piece) {
        this.piece = piece;
    }
    
    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }
    
    public void setCapturedPiece(ChessPiece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }
    
    public boolean isCastling() {
        return isCastling;
    }
    
    public void setCastling(boolean castling) {
        isCastling = castling;
    }
    
    public boolean isPromotion() {
        return isPromotion;
    }
    
    public void setPromotion(boolean promotion) {
        isPromotion = promotion;
    }
    
    public ChessPiece.PieceType getPromotionType() {
        return promotionType;
    }
    
    public void setPromotionType(ChessPiece.PieceType promotionType) {
        this.promotionType = promotionType;
    }
    
    public void setPromotionPiece(ChessPiece.PieceType pieceType) {
        this.promotionType = pieceType;
        this.isPromotion = true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Column letters: a-h
        char startColChar = (char) ('a' + startCol);
        char endColChar = (char) ('a' + endCol);
        
        // Row numbers: 8-1 (in reverse order)
        int startRowNum = 8 - startRow;
        int endRowNum = 8 - endRow;
        
        sb.append(startColChar).append(startRowNum)
          .append("-").append(endColChar).append(endRowNum);
        
        if (isPromotion && promotionType != null) {
            switch (promotionType) {
                case QUEEN:
                    sb.append("=Q");
                    break;
                case ROOK:
                    sb.append("=R");
                    break;
                case BISHOP:
                    sb.append("=B");
                    break;
                case KNIGHT:
                    sb.append("=N");
                    break;
                default:
                    break;
            }
        }
        
        return sb.toString();
    }
} 