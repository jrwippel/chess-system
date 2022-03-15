package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();


    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public boolean getCheck() {
        return check;
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i=0; i<board.getRows(); i++) {
            for (int j=0; j<board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    private void nextTurn(){
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.BLACK;
    }


    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placepiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove (ChessPosition sourcePosition, ChessPosition targetPosition){

        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if (testCheck(currentPlayer)){
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check!");
        }
        check = (testCheck(opponent(currentPlayer))) ? true : false;

        if (!testCheckMate(currentPlayer)){
            checkMate = true;
        }else {
            nextTurn();
        }
        return (ChessPiece) capturedPiece;
    }

    private Piece makeMove(Position source, Position target){
        Piece p = board.removePiece(source);
        Piece capturedPiece = board.removePiece(target);
        board.placepiece(p, target);

        if (capturedPiece != null){
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece){
        Piece p = board.removePiece(target);        // remove a peça destino
        board.placepiece(p, source);                // devolve a peça para a origem
        if (capturedPiece != null){                 // verifica se houve peça capturada
            board.placepiece(capturedPiece, target);// nesse caso, devolve a peça capturada para o destino
            capturedPieces.remove(capturedPiece);   // remove a peça da lista de peças capturadas
            piecesOnTheBoard.add(capturedPiece);    // adiciona a peça na lista de peças do tabuleiro

        }

    }

    private void validateSourcePosition(Position position){
        if (!board.thereIsAPiece(position)){
            throw new ChessException("There is no piece on source position");
        }
        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()){
            throw new ChessException("The chosen piece is not yours");
        }

        if (!board.piece(position).isThereAnyPossibleMove()){
            throw new ChessException("There is no possible moves for the chosen piece");
        }
    }

    private void validateTargetPosition(Position source, Position target){
        if(!board.piece(source).possibleMove(target)){
            throw new ChessException("The chosen piece cant move to target position");
        }
    }

    private Color opponent (Color color){
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        // se a cor for branca, retorna cor preta, senão retorna o branco
    }

    private ChessPiece king (Color color){
        //procurar na minha lista um rei com a cor do parametro.
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor()==color).collect(Collectors.toList());

        for (Piece p:list){
            if (p instanceof King){
                return (ChessPiece)p;
            }
        }
        throw new IllegalStateException("There is no "+ color +" king on the board");
    }
    private boolean testCheck(Color color){
        // Percorre o tabuleiro para verificar se existe alguma peça de rei em check
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor()== opponent(color)).collect(Collectors.toList());

        for (Piece piece: opponentPieces){
            boolean[][] mat = piece.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]){
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(Color color){
        if (!testCheck(color)){
            return false;
        }
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor()== opponent(color)).collect(Collectors.toList());

        for (Piece p : opponentPieces){
            boolean[][] mat = p.possibleMoves();
            for (int i=0;i<board.getRows();i++){
                for (int j=0;j<board.getColumns();j++){
                    if (mat[i][j]){
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i,j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if (!testCheck){
                            return false;
                        }
                    }
                }
            }
        }
        return true;

    }

    private void initialSetup(){
        placeNewPiece('h', 7, new Rook(board, Color.WHITE));
        placeNewPiece('d', 1, new Rook(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE));

        placeNewPiece('b', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 8, new King(board, Color.BLACK));
    }
}
