import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
/**
 * PA #2 -- Battleship
 *
 * Implement a single-player Battleship game on a 10x10 board.
 *
 * Startup input (one line from stdin):
 *   N MODE FILE_NAME
 *   - N        : number of bombs (positive integer)
 *   - MODE     : d/D (Debug) or r/R (Release)
 *   - FILE_NAME: board file path (may contain spaces)
 *
 *
 * Submit this file as: Battleship.java
 * - Public class name must be exactly "Battleship"
 * - No Korean comments allowed
 * - Must compile cleanly: javac Battleship.java
 */

public class Battleship {
	

    private static final int BOARD_SIZE = 10;
    private static final long RANDOM_SEED =
        Long.parseLong(System.getProperty("seed", "2026"));

    // Board state
    private char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
    private int score=0;
    
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try{
            // parse startup line
            StartupConfig config=parseStartupLine(reader);

            // create instance and initialize board
            Battleship game= new Battleship();
            game.initializeBoard(config.fileName);

            // run game
            game.play(config.bombs, config.mode, reader);

        } catch (BombInputException e) {
            System.out.println("BombInputException");
        } catch (ModeInputException e) {
            System.out.println("ModeInputException");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    //parsing
    private static StartupConfig parseStartupLine(BufferedReader reader)
            throws IOException, BombInputException, ModeInputException {

        String line= reader.readLine();
        if(line==null || line.trim().isEmpty()){
            throw new BombInputException();
        }
         String[] parts = line.trim().split("\\s+", 3);

        int N;
        try {
            N = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new BombInputException();
        }
        if(N <= 0) throw new BombInputException();

        if(parts.length < 2) throw new ModeInputException();
        String modeStr=parts[1];
        Mode mode;
        if(modeStr.equals("d") || modeStr.equals("D")){
            mode= Mode.DEBUG;
        } else if(modeStr.equals("r") || modeStr.equals("R")) {
            mode= Mode.RELEASE;
        } else{
            throw new ModeInputException();
        }

        // Parse FILE_NAME (everything after mode)
        String fileName = (parts.length >= 3) ? parts[2] : "";

        return new StartupConfig(N, mode, fileName);
    }
 
    private void clearBoard(){
    	for(int i=0; i < BOARD_SIZE; i++)
            for(int j=0; j< BOARD_SIZE; j++)
                board[i][j]=' ';
    }
    //Board Initialization


    private void initializeBoard(String fileName) throws IOException{
        // Fill board with empty sea
        clearBoard();
        File file = new File(fileName);
        if(file.exists()){
            loadBoardFromFile(file);
        }else{
            generateRandomBoard(new Random(RANDOM_SEED));
        }
    }

    private void loadBoardFromFile(File file)throws IOException{
        try (BufferedReader br=new BufferedReader(new java.io.FileReader(file))){
            int row=0;
            String line;
            while((line= br.readLine())!=null && row<BOARD_SIZE){
                for(int col=0; col<line.length()&&col< BOARD_SIZE; col++){
                    board[row][col]=line.charAt(col);
                }
                row++;
            }
        }
    }    
    // random board generation

    private void generateRandomBoard(Random rng){
         Ship[] ships ={
            new AircraftCarrier(),
            new BattleshipShip(),new BattleshipShip(),
            new Submarine(), new Submarine(),
            new Destroyer(),
            new PatrolBoat(), new PatrolBoat(), new PatrolBoat(),new PatrolBoat()
        };

        for (Ship ship :ships){
            boolean placed=false;
            while(!placed){
                boolean horizontal=rng.nextBoolean();
                int row=rng.nextInt(10);
                int col=rng.nextInt(10);
                if (canPlace(row, col, ship.size,horizontal)){
                    placeShip(ship, row, col, horizontal);
                    placed=true;
                }
            }
        }
    }
    private boolean canPlace(int row, int col, int size, boolean horizontal) {
        //check bounds
        if(horizontal&&col+size> BOARD_SIZE) return false;
        if(!horizontal&&row+size > BOARD_SIZE) return false;

        //overlap& adjacency 
        for(int i=0; i<size; i++){
            int r= horizontal ?row: row+i;
            int c= horizontal ?col+i: col;

            for(int dr = -1; dr <= 1; dr++){
                for(int dc = -1; dc <= 1; dc++){
                    int nr=r+dr;
                    int nc=c+dc;
                    if(nr >=0&&nr < BOARD_SIZE&& nc>=0 && nc < BOARD_SIZE) {
                        if(board[nr][nc]!=' ') return false;
                    }
                }
            }
        }
        return true;
    }
    private void placeShip(Ship ship, int row, int col, boolean horizontal) {
        for (int i= 0; i < ship.size; i++) {
            int r= horizontal?row:row+i;
            int c= horizontal?col+i:col;
            board[r][c]=ship.type;
        }
    }
    //Game
    private void play(int bombs, Mode mode, BufferedReader reader) throws IOException {
        int used = 0;
        while(used< bombs){
            if (mode== Mode.DEBUG){
                printBoard();
            }
            
            String inp=reader.readLine();
            if(inp==null) break;
            inp=inp.trim();

            if(inp.length()<2) {
                System.out.println("Try again");
                continue;
            }

            char colChar=Character.toUpperCase(inp.charAt(0));
            if (colChar<'A'||colChar>'J') {
                System.out.println("Try again");
                continue;
            }
            int row;
            try{
                row=Integer.parseInt(inp.substring(1));
            }catch (NumberFormatException e) {
                System.out.println("Try again");
                continue;
            }
            if (row<1 || row>10) {
                System.out.println("Try again");
                continue;
            }
            int coln=colChar - 'A';
            char cell=board[row - 1][coln];

            if (cell=='X'||Character.isLowerCase(cell)) {
                System.out.println("Try again");
                continue;
            }
            used++;
            shoot(row-1,coln,cell);
        }
        printBoard();
        System.out.println("Score " + score);
    }

    private void shoot(int row, int col, char cell) {
        if (cell==' '){
            System.out.println("Miss");
            board[row][col]='X';
        } else{
            System.out.println("Hit " + cell);
            switch (cell) {
                case 'A':score+= 6; break;
                case 'B':score+= 4; break;
                case 'S':score+= 3; break;
                case 'D':score+= 3; break;
                case 'P':score+= 2; break;
            }
            board[row][col]=Character.toLowerCase(cell);
        }
    }
    //Board Display
    private void printBoard(){

        //matching the second provided pdf's format
        System.out.println("     A  B  C  D  E  F  G  H  I  J");
        System.out.println("     -  -  -  -  -  -  -  -  -  -");

        for(int i=0; i<BOARD_SIZE; i++){

            StringBuilder sb=new StringBuilder();
            if(i+1<10)
                sb.append(i+1).append("  | ");
            else
                sb.append(i+1).append(" | ");

            for(int j=0; j< BOARD_SIZE; j++){

                char c=board[i][j];

                if(Character.isLowerCase(c)) {
                    sb.append("X").append(c).append(" ");
                }
                else if(c=='X'){
                    sb.append("X  ");
                }
                else if(c==' '){
                    sb.append("   ");
                }
                else{
                    sb.append(c).append("  ");
                }
            }

            System.out.println(sb.toString().stripTrailing());
        }
    }

    //Inner Types
    private enum Mode{ DEBUG, RELEASE }
    private static class StartupConfig {
        final int    bombs;
        final Mode   mode;
        final String fileName;

        StartupConfig(int bombs, Mode mode, String fileName) {
            this.bombs    = bombs;
            this.mode     = mode;
            this.fileName = fileName;
        }
    }
    //Ship Hierarchy
    private abstract static class Ship {
        final char type;
        final int  size;

        Ship(char type, int size) {
            this.type = type;
            this.size = size;
        }
    }
    private static final class AircraftCarrier extends Ship {
        AircraftCarrier() { super('A', 6); }
    }
    private static final class BattleshipShip extends Ship {
        BattleshipShip() { super('B', 4); }
    }
    private static final class Submarine extends Ship {
        Submarine() { super('S', 3); }
    }

    private static final class Destroyer extends Ship {
        Destroyer() { super('D', 3); }
    }

    private static final class PatrolBoat extends Ship {
        PatrolBoat() { super('P', 2); }
    }
    // Exceptions
    private static class BombInputException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private static class ModeInputException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
