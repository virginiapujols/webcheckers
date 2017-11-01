package com.webcheckers.model;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SpaceTest {
    @Test
    public void testSetColor() throws Exception{
        String _color = "black";
        Piece _piece = mock(Piece.class);
        int _cellIdx = 3;
        Space test = new Space(_color, _piece, _cellIdx);
        assertEquals(_color, test.getColor());
    }

    @Test
    public void testSetPiece() throws Exception{
        String _color = "black";
        Piece _piece = mock(Piece.class);
        int _cellIdx = 3;
        Space test = new Space(_color, _piece, _cellIdx);
        assertEquals(_piece, test.getPiece());
    }

    @Test
    public void testSetCellIdx() throws Exception{
        String _color = "black";
        Piece _piece = mock(Piece.class);
        int _cellIdx = 3;
        Space test = new Space(_color, _piece, _cellIdx);
        assertEquals(_cellIdx, test.getCellIdx());
    }

    @Test
    public void testIsValid() throws Exception {
        String _color = "black";
        Piece _piece = mock(Piece.class);
        int _cellIdx = 3;
        Space test = new Space(_color, _piece, _cellIdx);
        assertFalse(test.isValid());

        test = new Space(_color, null, _cellIdx);
        assertTrue(test.isValid());
    }
}