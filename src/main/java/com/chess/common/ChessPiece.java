package com.chess.common;

import java.io.Serializable;

public class ChessPiece implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum PieceType {
        PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING
    }
    
    public enum PieceColor {
        WHITE, BLACK
    }
    
    private PieceType type;
    private PieceColor color;
    private boolean hasMoved;
    
    public ChessPiece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
        this.hasMoved = false;
    }
    
    public PieceType getType() {
        return type;
    }
    
    public void setType(PieceType type) {
        this.type = type;
    }
    
    public PieceColor getColor() {
        return color;
    }
    
    public void setColor(PieceColor color) {
        this.color = color;
    }
    
    public boolean hasMoved() {
        return hasMoved;
    }
    
    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
    
    @Override
    public String toString() {
        String pieceSymbol;
        switch (type) {
            case PAWN:
                pieceSymbol = "P";
                break;
            case ROOK:
                pieceSymbol = "R";
                break;
            case KNIGHT:
                pieceSymbol = "N";
                break;
            case BISHOP:
                pieceSymbol = "B";
                break;
            case QUEEN:
                pieceSymbol = "Q";
                break;
            case KING:
                pieceSymbol = "K";
                break;
            default:
                pieceSymbol = "?";
        }
        
        return color == PieceColor.WHITE ? pieceSymbol : pieceSymbol.toLowerCase();
    }
} 