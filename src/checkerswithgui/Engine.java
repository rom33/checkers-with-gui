package checkerswithgui;

import java.util.ArrayList;

/**
 *
 * @author Jacob Yu this program aims to create a checkers playing computer
 * through the use of an alpha-beta pruning search tree !! note that a jump =
 * forced move in this variation !!
 *
 */
public class Engine {

    public static char[][] board;
    private static int GDEPTH; // 9 - very hard; 8 - hard; 5 - medium; 3 - easy
    private static boolean jumpsPossible = false;

    public Engine() {
    } // default constructor to keep Evaluation.java happy

    public Engine(char[][] b, int depth) {
        board = b;
        GDEPTH = depth;
    }

    public char[][] returnMove() { // this makeMove makes a move and then returns the board state
        Move m = ab(GDEPTH, 999999, -999999, null, 0);
        makeMove(m);
        return board;
    }

    public static Move ab(int depth, int beta, int alpha, Move move, int player) {
        /*
         * an implementation of the alpha beta pruning algorithm
         * this aims to select the best move based on exploring the tree of permutations following it
         * this algorithm assumes both sides plays perfect moves from the computer's perspective
         * the best move is decided through a rating system defined via Class Eval
         */

        ArrayList<Move> list = findMoves();
        int length = list.size();
        Evaluation e = new Evaluation(board);
        if (depth == 0 || length == 0) { // end of tree or no legal moves
            move.setScore(e.rate() * (player * 2 - 1));
            return move;
        }

        // player: 0 = black, 1 = white
        player = 1 - player; // flips player
        for (int i = 0; i < list.size(); i++) {
            makeMove(list.get(i)); // makes the move on the board, all permutations 1 by 1
            flip();
            Move bestMove = ab(depth - 1, beta, alpha, list.get(i), player);
            int val = bestMove.getScore(); // gets the score of the best move
            flip();
            undoMove(list.get(i));

            if (player == 0) { // update alpha and beta values for this path
                if (val <= beta) {
                    beta = val;
                    if (depth == GDEPTH) {
                        move = new Move(bestMove);
                    }
                }
            } else {
                if (val > alpha) {
                    alpha = val;
                    if (depth == GDEPTH) {
                        move = new Move(bestMove);
                    }
                }
            }

            if (alpha >= beta) { // prune the tree
                if (player == 0) { // so the move is returned + eval
                    move.setScore(beta);
                    return move;
                } else { // or if beta >= alpha, the no point continuing either
                    move.setScore(alpha);
                    return move;
                }
            }
        }
        if (player == 0) {
            move.setScore(beta);
            return move;
        } else {
            move.setScore(alpha);
            return move;
        }
    } // =====================================================================================

	// makeMove(), undoMove() & flip() are used in AI
    // =====================================================================================
    private static void makeMove(Move m) {
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
    }

    private static void undoMove(Move m) {
        ArrayList<int[]> targets = m.getMove();
        for (int i = targets.size() - 1; i > 0; i--) {
            int or = targets.get(i)[0];
            int oc = targets.get(i)[1];
            int tr = targets.get(i - 1)[0];
            int tc = targets.get(i - 1)[1];

            if (m.isPromotion()) // undo promotion moves
            {
                board[or][oc] = 'X';
            }
            board[tr][tc] = board[or][oc];
            if (m.isJump()) {
                char cP = ' ';
                switch (targets.get(i)[2]) { // ascii to char conversion
                    case 107:
                        cP = 'k';
                        break;
                    case 111:
                        cP = 'o';
                        break;
                    default:
                        cP = ' ';
                        break;
                }
                board[(or + tr) / 2][(oc + tc) / 2] = cP; // replaces the piece captured
            }
            board[or][oc] = ' ';
        }
    }
	// =====================================================================================

    public static void flip() { // used by evaluation
        for (int i = 0; i < 4; i++) {
            char[] tempr = board[i]; // flips the ranks
            board[i] = board[7 - i];
            board[7 - i] = tempr;
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) { // flips the files
                if (Character.isUpperCase(board[i][j])) // switches the colour of pieces (used for AI)
                {
                    board[i][j] = Character.toLowerCase(board[i][j]);
                } else {
                    board[i][j] = Character.toUpperCase(board[i][j]);
                }
                if (Character.isUpperCase(board[i][7 - j])) {
                    board[i][7 - j] = Character.toLowerCase(board[i][7 - j]);
                } else {
                    board[i][7 - j] = Character.toUpperCase(board[i][7 - j]);
                }

                char tempf = board[i][j];
                board[i][j] = board[i][7 - j];
                board[i][7 - j] = tempf;
            }
        }

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 'x') {
                    board[i][j] = 'o';
                } else if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                }
            }
        }
    }
	// =====================================================================================

    public static ArrayList<Move> findMoves() {
        ArrayList<Move> allMoves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                switch (board[i][j]) {
                    case 'X': // pawns
                        allMoves.addAll(checkX(i, j));
                        break;
                    case 'K': // kings
                        allMoves.addAll(checkK(i, j));
                }
            }
        }
        if (jumpsPossible) { // enforces jumping
            // this also helps improve performance by limiting the number of possible moves
            ArrayList<Move> tempMoves = new ArrayList<>();
            int len = allMoves.size();
            for (int i = 0; i < len; i++) {
                if (allMoves.get(i).isJump()) {
                    tempMoves.add(allMoves.get(i));
                }
            }
            allMoves = tempMoves;
        }
        jumpsPossible = false;
        return allMoves;
    }
	// =====================================================================================

    public static ArrayList<Move> checkK(int r, int c) { // check kings
        ArrayList<Move> moves = new ArrayList<>();

        // king jumping moves
        if (!immediateJumps(r, c, 'K').isEmpty()) { // jump moves take priority
            jumpsPossible = true;
            moves.addAll(getJumpMoves(r, c, new Move(r, c, 0), new ArrayList<>(), 'K'));
            return moves;
        }

        // normal king moves
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                try {
                    if (board[r + j][c + i] == ' ') {
                        Move tempMove = new Move(r, c, 0);
                        tempMove.addDestination((r + j), (c + i), 0);
                        moves.add(tempMove);
                    }
                } catch (Exception e) {
                }
            }
        }
        return moves;
    }

    public static ArrayList<Move> checkX(int r, int c) { // check normal pieces 
        ArrayList<Move> moves = new ArrayList<>();

        // jumping moves
        if (!immediateJumps(r, c, 'X').isEmpty()) { // jump moves take priority
            jumpsPossible = true;
            moves.addAll(getJumpMoves(r, c, new Move(r, c, 0), new ArrayList<>(), 'X'));
            return moves;
        }

        // normal moves
        for (int i = -1; i <= 1; i += 2) {
            try {
                if (board[r - 1][c + i] == ' ') {
                    Move tempMove = new Move(r, c, 0);
                    tempMove.addDestination((r - 1), (c + i), 0);
                    moves.add(tempMove);
                }
            } catch (Exception e) {
            }
        }
        return moves;
    }
	// =====================================================================================

    /*
     * recursively find all jumping paths of a piece
     * this thing took me hours - 27/Sep/2015 never4get
     */
    public static ArrayList<Move> getJumpMoves(int r, int c, Move jump, ArrayList<Move> paths, char piece) {
        ArrayList<String> immediateJumps = immediateJumps(r, c, piece);

        if (immediateJumps.isEmpty()) { // end of current jump, load jump to list
            Move temp = new Move(jump); // real copy of the object - not a reference
            paths.add(temp);
            jump.removeLastDestination(); // !! important !!
            return null;
        }

        for (int i = 0; i < immediateJumps.size(); i++) { // loops through paths at the crossing
            String target = immediateJumps.get(i);
            int destr = Character.getNumericValue(target.charAt(0));
            int destc = Character.getNumericValue(target.charAt(1));
            jump.addDestination(destr, destc, board[(r + destr) / 2][(c + destc) / 2]);

            // temporary move to avoid overflow error with king (can go forwards and back)
            // i refrained from using jump as its properties changes throughout the loop and so cannot be undone
            Move tempMove = new Move(r, c, 0);
            tempMove.addDestination(destr, destc, board[(r + destr) / 2][(c + destc) / 2]);
            makeMove(tempMove);
            getJumpMoves(destr, destc, jump, paths, piece); // moves onto the next level, starting from the new square
            undoMove(tempMove);
        }
        jump.removeLastDestination(); // !! important !!
        return paths;
    }

    // helper function for getJumpMoves(), finds all possible jumps
    public static ArrayList<String> immediateJumps(int r, int c, char piece) {
        ArrayList<String> temp = new ArrayList<>();
        if (piece == 'X') // pawn jumps
        {
            for (int i = -1; i <= 1; i += 2) {
                try {
                    if (Character.isLowerCase(board[r - 1][c + i]) && board[r - 2][c + i * 2] == ' ') {
                        temp.add("" + (r - 2) + (c + i * 2));
                    }
                } catch (Exception e) {
                }
            }
        } else if (piece == 'K') // king jumps
        {
            for (int i = -1; i <= 1; i += 2) {
                for (int j = -1; j <= 1; j += 2) {
                    try {
                        if (Character.isLowerCase(board[r + j][c + i]) && board[r + j * 2][c + i * 2] == ' ') {
                            temp.add("" + (r + j * 2) + (c + i * 2));
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return temp;
    }
    // =====================================================================================
}