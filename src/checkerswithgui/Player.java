package checkerswithgui;

import checkerswithgui.PlayerBoard;
import java.util.ArrayList;

public class Player {

    private static int color; // 0 - white; 1 - black
    private static char[][] board;
    private static int searchDepth; // denotes difficulty
    private static String name;

    public Player() {
    }

    public Player(int c, int d, String n) {
        board = PlayerBoard.board;
        color = c;
        searchDepth = d;
        name = n;
    }

    public char[][] think() { // for machine
        Engine AI = new Engine(board, searchDepth);
        board = AI.returnMove();
        return board;
    }

    public ArrayList<Move> legalMoves() {
        Engine ref = new Engine(board, 0);
        return Engine.findMoves();
    }

    public char[][] doMove(Move m) { // for humans
        ArrayList<int[]> targets = m.getMove();
        for (int i = 0; i < targets.size() - 1; i++) {
            int or = targets.get(i)[0];
            int oc = targets.get(i)[1];
            int tr = targets.get(i + 1)[0];
            int tc = targets.get(i + 1)[1];

            if (board[or][oc] == 'X' && tr == 0) { // piece promotion
                board[or][oc] = 'K';
                m.setPromotion(true);
            }

            board[tr][tc] = board[or][oc];
            if (m.isJump()) {
                board[(or + tr) / 2][(oc + tc) / 2] = ' ';
            }
            board[or][oc] = ' ';
        }
        return board;
    }

    public static String getName() {
        return name;
    }
}