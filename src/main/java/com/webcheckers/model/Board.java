package com.webcheckers.model;

import sun.security.util.AuthResources_ja;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A class that represents a game board.  This class enforces move logic and tracks piece locoations
 */
public class Board implements Iterable<Row>{
    //Class Attributes
    private List<Row> rows;
    private boolean didMove = false; //whether or not the user has moved this turn
    private boolean didJump = false; //whether or not the user has jumped this turn


    /**
     * This is the constructor for the "Board" class.
     */
    public Board() {
        rows = new ArrayList<>();
        for(int i=0; i<=7; i++) {
            rows.add(new Row(i));
        }
    }
   
    /**
     * This function updates the board after moves
     * @return the updated board
     */
    public Board updateBoard() {
        Board board = new Board();
        return board;
    }

    /**
     * Accessor for rows attribute
     * @return rows The Rows on the board
     */
    public List<Row> getRows() {
        return this.rows;
    }

    /**
     * Mutator for rows attribute
     * @param _rows The rows on the board
     */
    public void setRows(List<Row> _rows) {
        this.rows = _rows;
    }

    /**
     * Accessor for didMove attribute
     * @return didMove Whether or not the user has moved
     */
    public boolean didMove() {
        return didMove;
    }

    /**
     * Mutator for didMove attribute
     * @param _didMove Whether or not the user has moved
     */
    public void setDidMove(boolean _didMove) {
        this.didMove = _didMove;
    }

    /**
     * Accessor for didJump attribute
     * @return didJump Whether or not the user has jumped
     */
    public boolean getDidJump() {
        return didJump;
    }

    /**
     * Mutator for didJump attribute
     * @param _didJump Whether or not the user has jumped
     */
    public void setDidJump(boolean _didJump) {
        this.didJump = _didJump;
    }

    /**
     * Method to take a piece from the starting location in a move and place it at the ending location
     * @param _move A Data Object showing the starting & ending locations of a Piece when it is moved
     */
    public void movePiece(Move _move) {
        //Get starting and ending spaces & piece (using coordinates)
        Space startSpace = getSpaceByCoordinate(_move.getStart());
        Space endSpace = getSpaceByCoordinate(_move.getEnd());
        Piece piece = startSpace.getPiece();

        //Empty the starting space, put the piece in the end space, & signify that the user has moved
        startSpace.setPiece(null);
        endSpace.setPiece(piece);
        didMove = true;

        //If the move is a jump, set didJump to true, move the piece to it's new space, and delete the captured piece
        if(_move.getRowsMoved() == 2) {
            didJump = true;
            Coordinate jumpedCoordinate = _move.getJumpedCoordinate();
            Space jumpedSpace = getSpaceByCoordinate(jumpedCoordinate);
            jumpedSpace.removeCapturedPiece();
        }

        if(reachedLastRow(_move.getEnd(), piece)) {
            piece.makeKing();
        }
    }

    /**
     * A method to validate if a move is a legal move or not: illegal moves are prevented from occuring
     * @param _move The move that is being made
     * @return A message with text and type "info" it's a valid move or type "error" if it's an invalid move
     */
    public Message validateMove(Move _move) {
        //Get the spaces and piece involved in the move
        Space startSpace = getSpaceByCoordinate(_move.getStart());
        Space endSpace = getSpaceByCoordinate(_move.getEnd());
        Piece piece = startSpace.getPiece();

        //Check if the user has a jump available, requiring them to take it if one exists
        if(_move.getRowsMoved() == 1 && piece.getColor().equals("RED") && checkForAvailableRedJumps()) {
            return new Message("You have a jump that you must take", "error");
        } else if(_move.getRowsMoved() == 1 && piece.getColor().equals("WHITE") && checkForAvailableWhiteJumps()) {
            return new Message("You have a jump that you must take", "error");
        }

        //If they don't have any available jumps, allow them to make a move and evalute it's validity
        if(!didMove || (didMove && didJump)) { //If the user hasn't already moved OR has only made jumps
            if(endSpace.getPiece() == null) { //If there's already a piece in the ending space
                if(piece.getType().equals("SINGLE")) { //If the piece is a single piece
                    if (piece.getColor().equals(Piece.PIECE_RED)) { //If the piece is red (i.e. if the piece moves "up" on the board)
                        if (_move.getRowsMoved() == 1 && !_move.isMoveUp() && !didJump) { //Ensure that a move is only 1 row "up"
                            return new Message("Valid Move", "info");
                        } else if (_move.getRowsMoved() == 2 && !_move.isMoveUp()) { //Check for a Jump, but it still has to be "up"
                            return validateJump(_move, piece);
                        } else { //Otherwise return an error message
                            String messageText = didJump ? "You cannot make a regular move after jumping!" : "You must move 1 row forward";
                            return new Message(messageText, "error");
                        }
                    } else if (piece.getColor().equals(Piece.PIECE_WHITE)) { //If the piece is white (i.e. the piece moves "down" on the board
                        if (_move.getRowsMoved() == 1 && _move.isMoveUp() && !didJump) { //Ensure that a move is only one row "down"
                            return new Message("Valid Move", "info");
                        } else if (_move.getRowsMoved() == 2 && _move.isMoveUp()) { //Check for a Jump, but it still has to be "down"
                            return validateJump(_move, piece);
                        } else { //Otherwise return an error message
                            String messageText = didJump ? "You cannot make a regular move after jumping!" : "You must move 1 row forward";
                            return new Message(messageText, "error");
                        }
                    }
                } else if(piece.getType().equals("KING")) { //If the piece is a King Piece, it has special move logic
                    if(_move.getRowsMoved() == 1 && !didJump) { //A king piece can move 1 row in any direction
                        return new Message("Valid Move", "info");
                    } else if(_move.getRowsMoved() == 2) { //A king piece can jump 2 rows in any direction, return jump validation
                        return validateJump(_move, piece);
                    } else { //Otherwise return an error message
                        String messageText = didJump ? "You cannot make a regular move after jumping!" : "You must move 1 row forward";
                        return new Message(messageText, "error");
                    }
                }
            } else { //If there's a piece in the ending location, show an error message
                return new Message("A Piece is already in that Square", "error");
            }
        } else { //If they have already moved, show an error message
            return new Message("You have already made a move this turn", "error");
        }

        //catch all that returns a generic error message
        return new Message("Invalid Move", "error");
    }

    /**
     * A method to validate whether or not a jump is legal
     * This method is called by validateMove when it detects that a jump has occured
     * @param _move The move being made
     * @param _jumpingPiece The piece object that is performing the move
     * @return
     */
    public Message validateJump(Move _move, Piece _jumpingPiece) {
        //Get the space & piece of the move
        Coordinate jumpedCoordinate = _move.getJumpedCoordinate();
        Space jumpedSpace = getSpaceByCoordinate(jumpedCoordinate);
        Piece jumpedPiece = jumpedSpace.getPiece();

        if(jumpedPiece != null) { //If the jumped piece isn't null, check that it's the opposing color and return a corresponding message
            if(!jumpedPiece.getColor().equals(_jumpingPiece.getColor())) {
                return new Message("Valid Move", "info");
            } else {
                return new Message("You cannot jump your own piece!", "error");
            }
        } else {
            return new Message("You cannot jump an empty square!", "error");
        }
    }

    /**
     * Method to undo a move that has been completed on a turn that hasn't been submitted yet
     * @param _move The move to be undone
     */
    public void undoMove(Move _move) {
        Space startSpace = getSpaceByCoordinate(_move.getStart());
        Space endSpace = getSpaceByCoordinate(_move.getEnd());
        Piece piece = endSpace.getPiece();

        //Set the piece back in the starting square and empty the ending square.  Indicate that the user now hasn't completed a move this turn
        startSpace.setPiece(piece);
        endSpace.setPiece(null);
        didMove = false;
    }

    /**
     * A method to undo a the capture of a piece (used when a piece is captured and then a move is undone)
     * @param _jumpedSpace The space that was jumped in the move
     * @param _capturedPiece The piece that was captured in the jump
     * @param _stillCapturedCount The count of pieces captured this turn that are still captured (i.e. If 3 were captured and 1 capture was undone, it would be 2)
     */
    public void undoCapture(Space _jumpedSpace, Piece _capturedPiece, int _stillCapturedCount) {
        _jumpedSpace.setPiece(_capturedPiece);

        if(_stillCapturedCount == 0) { //If undoing the move means they haven't captured any pieces, set "didJump" to false
            didJump = false;
        }
    }

    /**
     * A method to get a Space by a given coordinate
     * @param _coord The coordinate that points at Space
     * @return The Space that corresponds to the coordinate
     */
    public Space getSpaceByCoordinate(Coordinate _coord) {
        return rows.get(_coord.getRow()).getSpaces().get(_coord.getCell());
    }

    /**
     * A method to check if Player 1 (Red pieces) has any available jumps
     * @return A boolean that is true if they have a jump they can take
     */
    public boolean checkForAvailableRedJumps() {
        //Loop through each Row & Space, getting the Piece from each space if it exists
        for(Row row: rows) {
            for(Space space : row) {
                Piece piece = space.getPiece();

                //If the piece in a space isn't null & is a red piece, we check it's jumps
                if(piece != null && piece.getColor().equals(Piece.PIECE_RED)) {
                    if (row.getIndex() > 1) { //If the piece isn't in the last 2 rows (i.e. it wouldn't have enough space to jump)
                        if (space.getCellIdx() <= 1) { //If it's in the two left-most columns, it can only jump right, so we only check that direction
                            //Get the piece being jumped & landing space
                            Space squareToJump = getSpaceByCoordinate(new Coordinate(row.getIndex() - 1, space.getCellIdx() + 1));
                            Space landingSquare = getSpaceByCoordinate(new Coordinate(row.getIndex() - 2, space.getCellIdx() + 2));

                            Piece jumpedPiece = squareToJump.getPiece();
                            Piece landingSquarePiece = landingSquare.getPiece();

                            //If it jumps over an opposing piece and onto an empty square, return true
                            if (jumpedPiece != null && jumpedPiece.getColor().equals(Piece.PIECE_WHITE) && landingSquarePiece == null) {
                                return true;
                            }
                        } else if (space.getCellIdx() >= 6) { //If it's in the rightmost columns, it can only jump left, so we only check that direction
                            Space squareToJump = getSpaceByCoordinate(new Coordinate(row.getIndex() - 1, space.getCellIdx() - 1));
                            Space landingSquare = getSpaceByCoordinate(new Coordinate(row.getIndex() - 2, space.getCellIdx() - 2));

                            Piece jumpedPiece = squareToJump.getPiece();
                            Piece landingSquarePiece = landingSquare.getPiece();

                            if (jumpedPiece != null && jumpedPiece.getColor().equals(Piece.PIECE_WHITE) && landingSquarePiece == null) {
                                return true;
                            }
                        } else { //Otherwise it can jump left OR right, so we check both directions
                            Space squareToJumpLeft = getSpaceByCoordinate(new Coordinate(row.getIndex() - 1, space.getCellIdx() - 1));
                            Space landingSquareLeft = getSpaceByCoordinate(new Coordinate(row.getIndex() - 2, space.getCellIdx() - 2));
                            Piece jumpedPieceLeft = squareToJumpLeft.getPiece();
                            Piece landingSquarePieceLeft = landingSquareLeft.getPiece();

                            Space squareToJumpRight = getSpaceByCoordinate(new Coordinate(row.getIndex() - 1, space.getCellIdx() + 1));
                            Space landingSquareRight = getSpaceByCoordinate(new Coordinate(row.getIndex() - 2, space.getCellIdx() + 2));
                            Piece jumpedPieceRight = squareToJumpRight.getPiece();
                            Piece landingSquarePieceRight = landingSquareRight.getPiece();

                            //If either of the possible jumps can be taken, then return true
                            if (jumpedPieceLeft != null && jumpedPieceLeft.getColor().equals(Piece.PIECE_WHITE) && landingSquarePieceLeft == null) {
                                return true;
                            } else if (jumpedPieceRight != null && jumpedPieceRight.getColor().equals(Piece.PIECE_WHITE) && landingSquarePieceRight == null) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        //If no possible jumps exist, return false
        return false;
    }

    /**
     * A method to check if Player 2 (White pieces) has any available jumps
     * @return A boolean that is true if they have a jump they can take
     */
    public boolean checkForAvailableWhiteJumps() {
        //Loop through each Row & Space, getting the Piece from each space if it exists
        for(Row row: rows) {
            for(Space space : row) {
                Piece piece = space.getPiece();

                //If the piece in a space isn't null & is a white piece, we check it's jumps
                if(piece != null && piece.getColor().equals(Piece.PIECE_WHITE)) {
                    if (row.getIndex() < 6) { //If the piece is in the last two rows (i.e. It wouldn't have enough space to jump)
                        if (space.getCellIdx() <= 1) { //If it's the 2 leftmost columns, it can only jump right, so only check that direction
                            Space squareToJump = getSpaceByCoordinate(new Coordinate(row.getIndex() + 1, space.getCellIdx() + 1));
                            Space landingSquare = getSpaceByCoordinate(new Coordinate(row.getIndex() + 2, space.getCellIdx() + 2));

                            Piece jumpedPiece = squareToJump.getPiece();
                            Piece landingSquarePiece = landingSquare.getPiece();

                            if (jumpedPiece != null && jumpedPiece.getColor().equals(Piece.PIECE_RED) && landingSquarePiece == null) {
                                return true;
                            }
                        } else if (space.getCellIdx() >= 6) { //If it's the 2 rightmost columns, it can only jump left, so only check that direction
                            Space squareToJump = getSpaceByCoordinate(new Coordinate(row.getIndex() + 1, space.getCellIdx() - 1));
                            Space landingSquare = getSpaceByCoordinate(new Coordinate(row.getIndex() + 2, space.getCellIdx() - 2));

                            Piece jumpedPiece = squareToJump.getPiece();
                            Piece landingSquarePiece = landingSquare.getPiece();

                            if (jumpedPiece != null && jumpedPiece.getColor().equals(Piece.PIECE_RED) && landingSquarePiece == null) {
                                return true;
                            }
                        } else { //Otherwise it can jump both directions so we have to check each way
                            Space squareToJumpLeft = getSpaceByCoordinate(new Coordinate(row.getIndex() + 1, space.getCellIdx() + 1));
                            Space landingSquareLeft = getSpaceByCoordinate(new Coordinate(row.getIndex() + 2, space.getCellIdx() + 2));
                            Piece jumpedPieceLeft = squareToJumpLeft.getPiece();
                            Piece landingSquarePieceLeft = landingSquareLeft.getPiece();

                            Space squareToJumpRight = getSpaceByCoordinate(new Coordinate(row.getIndex() + 1, space.getCellIdx() - 1));
                            Space landingSquareRight = getSpaceByCoordinate(new Coordinate(row.getIndex() + 2, space.getCellIdx() - 2));
                            Piece jumpedPieceRight = squareToJumpRight.getPiece();
                            Piece landingSquarePieceRight = landingSquareRight.getPiece();

                            if (jumpedPieceLeft != null && jumpedPieceLeft.getColor().equals(Piece.PIECE_RED) && landingSquarePieceLeft == null) {
                                return true;
                            } else if (jumpedPieceRight != null && jumpedPieceRight.getColor().equals(Piece.PIECE_RED) && landingSquarePieceRight == null) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean reachedLastRow(Coordinate endSpace, Piece piece) {
        int rowValue = endSpace.getRow();

        if((rowValue == 0 && piece.getColor().equals(Piece.PIECE_RED)) || (rowValue == 7 && piece.getColor().equals(Piece.PIECE_WHITE))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    @Override
    public void forEach(Consumer<? super Row> action) {

    }

    @Override
    public Spliterator<Row> spliterator() {
        return null;
    }
}
