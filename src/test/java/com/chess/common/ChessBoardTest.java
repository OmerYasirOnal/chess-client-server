package com.chess.common;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ChessBoardTest {
    
    private ChessBoard chessBoard;
    private Method isSquareUnderAttackMethod;
    
    @Before
    public void setUp() throws Exception {
        chessBoard = new ChessBoard();
        
        // private metodları test edebilmek için reflection kullanıyoruz
        isSquareUnderAttackMethod = ChessBoard.class.getDeclaredMethod("isSquareUnderAttack", 
                int.class, int.class, ChessPiece.PieceColor.class);
        isSquareUnderAttackMethod.setAccessible(true);
    }
    
    @Test
    public void testInitialBoard() {
        // Test the initial board setup
        assertEquals(ChessPiece.PieceType.ROOK, chessBoard.getPiece(0, 0).getType());
        assertEquals(ChessPiece.PieceType.KNIGHT, chessBoard.getPiece(0, 1).getType());
        assertEquals(ChessPiece.PieceType.BISHOP, chessBoard.getPiece(0, 2).getType());
        assertEquals(ChessPiece.PieceType.QUEEN, chessBoard.getPiece(0, 3).getType());
        assertEquals(ChessPiece.PieceType.KING, chessBoard.getPiece(0, 4).getType());
        
        // Test colors
        assertEquals(ChessPiece.PieceColor.BLACK, chessBoard.getPiece(0, 0).getColor());
        assertEquals(ChessPiece.PieceColor.WHITE, chessBoard.getPiece(7, 0).getColor());
    }
    
    @Test
    public void testPawnMovement() {
        // Test initial pawn move (two squares)
        ChessMove move = new ChessMove(6, 0, 4, 0);
        chessBoard.makeMove(move);
        
        assertNull(chessBoard.getPiece(6, 0)); // Original position is empty
        assertEquals(ChessPiece.PieceType.PAWN, chessBoard.getPiece(4, 0).getType()); // New position has pawn
        assertEquals(ChessPiece.PieceColor.WHITE, chessBoard.getPiece(4, 0).getColor());
        
        // Test opponent pawn move (two squares)
        move = new ChessMove(1, 1, 3, 1);
        chessBoard.makeMove(move);
        
        assertNull(chessBoard.getPiece(1, 1));
        assertEquals(ChessPiece.PieceType.PAWN, chessBoard.getPiece(3, 1).getType());
        assertEquals(ChessPiece.PieceColor.BLACK, chessBoard.getPiece(3, 1).getColor());
    }
    
    @Test
    public void testKingCannotMoveToSquareProtectedByPawn() throws Exception {
        // Özel bir düzen oluştur - Beyaz şah ve siyah piyon
        chessBoard = new ChessBoard();
        
        // Tahtayı temizle
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chessBoard.setPiece(row, col, null);
            }
        }
        
        // e4 (4,4) konumuna beyaz şah yerleştir
        chessBoard.setPiece(4, 4, new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.WHITE));
        ChessPiece kingPiece = chessBoard.getPiece(4, 4);
        
        // f5 (3,5) konumuna siyah piyon yerleştir
        chessBoard.setPiece(3, 5, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK));
        
        // Piyonlar sadece çapraz olarak tehdit eder
        // Siyah piyon iki kareyi tehdit eder: e6 (2,4) ve g6 (2,6)
        // f6 (2,5) siyah piyon tarafından tehdit edilmez!
        
        // e6 (2,4) karesi siyah piyon tarafından tehdit edilmeli
        Boolean isE6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 4, ChessPiece.PieceColor.WHITE);
        assertTrue("e6 karesi (2,4) siyah piyon tarafından tehdit edilmelidir", isE6UnderAttack);
        
        // g6 (2,6) karesi siyah piyon tarafından tehdit edilmeli
        Boolean isG6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 6, ChessPiece.PieceColor.WHITE);
        assertTrue("g6 karesi (2,6) siyah piyon tarafından tehdit edilmelidir", isG6UnderAttack);
        
        // f6 (2,5) karesi siyah piyon tarafından tehdit edilmemeli
        Boolean isF6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 5, ChessPiece.PieceColor.WHITE);
        assertFalse("f6 karesi (2,5) siyah piyon tarafından tehdit edilmemelidir", isF6UnderAttack);
        
        // Şimdi şahı e6 (2,4) karesine taşıyalım ve şah altında olup olmadığına bakalım
        chessBoard.setPiece(4, 4, null);
        chessBoard.setPiece(2, 4, kingPiece);
        
        assertTrue("Şah e6 karesinde (2,4) tehdit altında olmalıdır", chessBoard.isInCheck(ChessPiece.PieceColor.WHITE));
        
        // Şahı geri taşıyalım ve f6 (2,5) karesine taşıyalım
        chessBoard.setPiece(2, 4, null);
        chessBoard.setPiece(4, 4, kingPiece);
        
        // Şahı f6 karesine taşıyın
        chessBoard.setPiece(4, 4, null);
        chessBoard.setPiece(2, 5, kingPiece);
        
        assertFalse("Şah f6 karesinde (2,5) tehdit altında olmamalıdır", chessBoard.isInCheck(ChessPiece.PieceColor.WHITE));
    }
    
    @Test
    public void testPawnProtectedSquares() throws Exception {
        // Özel bir düzen oluştur - Beyaz şah ve birden fazla siyah piyon
        chessBoard = new ChessBoard();
        
        // Tahtayı temizle
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chessBoard.setPiece(row, col, null);
            }
        }
        
        // Beyaz şahı d5'e (3,3) yerleştir
        ChessPiece whiteKing = new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.WHITE);
        chessBoard.setPiece(3, 3, whiteKing);
        
        // Siyah piyonu e6'ya (2,4) yerleştir - Bu piyon d7 ve f7 karelerini korur
        ChessPiece blackPawn1 = new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        chessBoard.setPiece(2, 4, blackPawn1);
        
        // Siyah piyonu c6'ya (2,2) yerleştir - Bu piyon b7 ve d7 karelerini korur
        ChessPiece blackPawn2 = new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK);
        chessBoard.setPiece(2, 2, blackPawn2);
        
        // d7 (1,3) karesini doğrudan kontrol et
        Boolean isD7UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 1, 3, ChessPiece.PieceColor.WHITE);
        assertTrue("d7 karesi (1,3) her iki siyah piyon tarafından tehdit edilmelidir", isD7UnderAttack);
        
        // Şimdi şahı d6 (2,3) karesine taşıyalım - bu kare her iki piyon tarafından tehdit edilmemeli çünkü
        // piyonlar sadece bir ileri çapraz kareleri tehdit eder
        chessBoard.setPiece(3, 3, null);
        chessBoard.setPiece(2, 3, whiteKing);
        
        // d6 karesini doğrudan kontrol edelim (bir önceki şah hareketi)
        Boolean isD6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 3, ChessPiece.PieceColor.WHITE);
        assertFalse("d6 karesi (2,3) siyah piyonlar tarafından tehdit edilmemelidir", isD6UnderAttack);
        
        assertFalse("Şah d6 karesinde (2,3) tehdit altında olmamalıdır", chessBoard.isInCheck(ChessPiece.PieceColor.WHITE));
        
        // Şahı geri taşı
        chessBoard.setPiece(2, 3, null);
        chessBoard.setPiece(3, 3, whiteKing);
        
        // e5 (3,4) - bu kare piyon tarafından korunmuyor
        chessBoard.setPiece(3, 3, null);
        chessBoard.setPiece(3, 4, whiteKing);
        
        assertFalse("Şah e5 karesinde (3,4) tehdit altında olmamalıdır", chessBoard.isInCheck(ChessPiece.PieceColor.WHITE));
        
        // Şimdi beyaz piyonları ekleyerek daha karmaşık bir durum oluştur
        
        // Beyaz piyon e4'e (4,4) yerleştir - Bu piyon d3 ve f3 karelerini korur
        chessBoard.setPiece(3, 4, null); // önce şahı kaldır
        chessBoard.setPiece(3, 3, whiteKing); // şahı orijinal konumuna geri yerleştir
        
        ChessPiece whitePawn = new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE);
        chessBoard.setPiece(4, 4, whitePawn);
        
        // d3 ve f3 karelerini doğrudan kontrol et
        Boolean isD3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 3, ChessPiece.PieceColor.BLACK);
        Boolean isF3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 5, ChessPiece.PieceColor.BLACK);
        
        assertTrue("d3 karesi (5,3) beyaz piyon tarafından tehdit edilmelidir", isD3UnderAttack);
        assertTrue("f3 karesi (5,5) beyaz piyon tarafından tehdit edilmelidir", isF3UnderAttack);
        
        // Siyah şahı d3'e (5,3) yerleştir
        ChessPiece blackKing = new ChessPiece(ChessPiece.PieceType.KING, ChessPiece.PieceColor.BLACK);
        chessBoard.setPiece(5, 3, blackKing);
        
        assertTrue("Siyah şah d3'te (5,3) tehdit altında olmalıdır", chessBoard.isInCheck(ChessPiece.PieceColor.BLACK));
        
        // Siyah şahı e3'e (5,4) taşı - bu kare beyaz piyonun çapraz alanında değil, bu nedenle tehdit altında olmamalı
        chessBoard.setPiece(5, 3, null);
        chessBoard.setPiece(5, 4, blackKing);
        
        assertFalse("Siyah şah e3'te (5,4) tehdit altında olmamalıdır", chessBoard.isInCheck(ChessPiece.PieceColor.BLACK));
    }
    
    @Test
    public void testIsSquareUnderAttackByPawn() throws Exception {
        chessBoard = new ChessBoard();
        
        // Tahtayı temizle
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chessBoard.setPiece(row, col, null);
            }
        }
        
        // Beyaz piyon d4'e (4, 3) yerleştir
        chessBoard.setPiece(4, 3, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE));
        
        // Beyaz piyon c3 (5,2) ve e3 (5,4) karelerini korumalı
        Boolean c3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 2, ChessPiece.PieceColor.BLACK);
        Boolean e3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 4, ChessPiece.PieceColor.BLACK);
        
        assertTrue("c3 karesi beyaz piyon tarafından korunmalı", c3UnderAttack);
        assertTrue("e3 karesi beyaz piyon tarafından korunmalı", e3UnderAttack);
        
        // d3 (5,3) karesi piyon tarafından korunmamalı (piyon çapraz tehdit eder)
        Boolean d3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 3, ChessPiece.PieceColor.BLACK);
        assertFalse("d3 karesi beyaz piyon tarafından korunmamalı", d3UnderAttack);
        
        // Siyah piyon e5'e (3, 4) yerleştir
        chessBoard.setPiece(3, 4, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK));
        
        // Siyah piyon d6 (2,3) ve f6 (2,5) karelerini korumalı
        Boolean d6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 3, ChessPiece.PieceColor.WHITE);
        Boolean f6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 5, ChessPiece.PieceColor.WHITE);
        
        assertTrue("d6 karesi siyah piyon tarafından korunmalı", d6UnderAttack);
        assertTrue("f6 karesi siyah piyon tarafından korunmalı", f6UnderAttack);
        
        // e6 (2,4) karesi piyon tarafından korunmamalı (piyon çapraz tehdit eder)
        Boolean e6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 4, ChessPiece.PieceColor.WHITE);
        assertFalse("e6 karesi siyah piyon tarafından korunmamalı", e6UnderAttack);
    }
    
    @Test
    public void testPawnDoesNotThreatenForwardSquare() throws Exception {
        chessBoard = new ChessBoard();
        
        // Tahtayı temizle
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chessBoard.setPiece(row, col, null);
            }
        }
        
        // Beyaz piyon e4'e (4,4) yerleştir
        chessBoard.setPiece(4, 4, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.WHITE));
        
        // e3 (5,4) karesi (piyonun önü) tehdit altında olmamalı
        Boolean e3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 4, ChessPiece.PieceColor.BLACK);
        assertFalse("e3 karesi beyaz piyon tarafından tehdit edilmemeli", e3UnderAttack);
        
        // d3 (5,3) karesi (piyonun sol çaprazı) tehdit altında olmalı
        Boolean d3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 3, ChessPiece.PieceColor.BLACK);
        assertTrue("d3 karesi beyaz piyon tarafından tehdit edilmeli", d3UnderAttack);
        
        // f3 (5,5) karesi (piyonun sağ çaprazı) tehdit altında olmalı
        Boolean f3UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 5, 5, ChessPiece.PieceColor.BLACK);
        assertTrue("f3 karesi beyaz piyon tarafından tehdit edilmeli", f3UnderAttack);
        
        // Siyah piyon e5'e (3,4) yerleştir
        chessBoard.setPiece(3, 4, new ChessPiece(ChessPiece.PieceType.PAWN, ChessPiece.PieceColor.BLACK));
        
        // e6 (2,4) karesi (piyonun önü) tehdit altında olmamalı
        Boolean e6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 4, ChessPiece.PieceColor.WHITE);
        assertFalse("e6 karesi siyah piyon tarafından tehdit edilmemeli", e6UnderAttack);
        
        // d6 (2,3) karesi (piyonun sol çaprazı) tehdit altında olmalı
        Boolean d6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 3, ChessPiece.PieceColor.WHITE);
        assertTrue("d6 karesi siyah piyon tarafından tehdit edilmeli", d6UnderAttack);
        
        // f6 (2,5) karesi (piyonun sağ çaprazı) tehdit altında olmalı
        Boolean f6UnderAttack = (Boolean) isSquareUnderAttackMethod.invoke(chessBoard, 2, 5, ChessPiece.PieceColor.WHITE);
        assertTrue("f6 karesi siyah piyon tarafından tehdit edilmeli", f6UnderAttack);
    }
} 