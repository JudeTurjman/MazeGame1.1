package com.jude.mazegame1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {

    private enum Direction{
        UP, DOWN, LEFT, RIGHT
    }
    private Cell[] [] cells;
    private Cell player, exit;
    private static final int Cols = 7, Rows = 10;
    private static final float WALL_THICKNESS = 4;
    private float cellsSize, hMargin, vMargin;
    private Paint wallPaint, playerPaint, exitPaint;
    private Random random;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        random = new Random();

        createMaze();
    }

    private Cell getNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();

        // left neighbour
        if (cell.col > 0)
            if (!cells[cell.col-1][cell.row].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        // right neighbour
        if (cell.col < Cols-1)
            if (!cells[cell.col+1][cell.row].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        // top neighbour
        if (cell.row > 0)
            if (!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        // bottom neighbour
        if (cell.row < Rows-1)
            if (!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        if (neighbours.size() > 0){
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    private void removeWall(Cell current, Cell next){
        if (current.col == next.col && current.row == next.row+1){
            current.topwall = false;
            next.bottomwall = false;
        }

        if (current.col == next.col && current.row == next.row-1){
            current.bottomwall = false;
            next.topwall = false;
        }

        if (current.col == next.col+1 && current.row == next.row){
            current.leftwall = false;
            next.rightwall = false;
        }

        if (current.col == next.col-1 && current.row == next.row+1){
            current.rightwall = false;
            next.leftwall = false;
        }

    }

    private void createMaze(){
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[Cols] [Rows];

        for(int i=0; i<Cols ; i++){
            for(int j=0; j<Rows ; j++){
                cells [i] [j] = new Cell(i,j);
            }
        }

        player = cells[0][0];
        exit = cells[Cols-1][Rows-1];

        current = cells[0][0];
        current.visited = true;
        do {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else
                current = stack.pop();
        }while (!stack.empty());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GREEN);

        int width = getWidth();
        int height = getHeight();

        if(width/height < Cols/Rows)
            cellsSize = width/(Cols+1);
        else
            cellsSize = height/(Rows+1);

        hMargin = (width-Cols*cellsSize)/2;
        vMargin = (height-Rows*cellsSize)/2;

        canvas.translate(hMargin, vMargin);

        for(int i=0; i<Cols ; i++){
            for(int j=0; j<Rows ; j++){
                if(cells[i][j].topwall)
                    canvas.drawLine(
                            i*cellsSize,
                            j*cellsSize,
                            (i+1)*cellsSize,
                            j*cellsSize,
                            wallPaint);

                if(cells[i][j].leftwall)
                    canvas.drawLine(
                            i*cellsSize,
                            j*cellsSize,
                            i*cellsSize,
                            (j+1)*cellsSize,
                            wallPaint);

                if(cells[i][j].bottomwall)
                    canvas.drawLine(
                            i*cellsSize,
                            (j+1)*cellsSize,
                            (i+1)*cellsSize,
                            (j+1)*cellsSize,
                            wallPaint);

                if(cells[i][j].rightwall)
                    canvas.drawLine(
                            (i+1)*cellsSize,
                            j*cellsSize,
                            (i+1)*cellsSize,
                            (j+1)*cellsSize,
                            wallPaint);
            }
        }

        float margin = cellsSize/10;

        canvas.drawRect(
                player.col*cellsSize+margin,
                player.row*cellsSize+margin,
                (player.col+1)*cellsSize-margin,
                (player.row+1)*cellsSize-margin,
                playerPaint);

        canvas.drawRect(
                exit.col*cellsSize+margin,
                exit.row*cellsSize+margin,
                (exit.col+1)*cellsSize-margin,
                (exit.row+1)*cellsSize-margin,
                exitPaint);
    }

    private void movePlayer(Direction direction){
        switch (direction){
            case UP:
                if (!player.topwall)
                    player = cells[player.col][player.row-1];
                break;
            case DOWN:
                if (!player.bottomwall)
                    player = cells[player.col][player.row+1];
                break;
            case LEFT:
                if (!player.leftwall)
                    player = cells[player.col-1][player.row];
                break;
            case RIGHT:
                if (!player.rightwall)
                    player = cells[player.col+1][player.row];

        }

        checkExit();
        invalidate();
    }

    private void checkExit(){
        if (player == exit)
            createMaze();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            return true;

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = hMargin + (player.col+0.5f)*cellsSize;
            float playerCenterY = vMargin + (player.row+0.5f)*cellsSize;

            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);

            if (absDx > cellsSize || absDy > cellsSize){

                if (absDx > absDy){
                    //move in x-direction
                    if (dx > 0)
                        movePlayer(Direction.RIGHT);
                    else
                        movePlayer(Direction.LEFT);
                }
                else {
                    //move in y-direction
                    if (dy > 0)
                        movePlayer(Direction.DOWN);
                    else
                        movePlayer(Direction.UP);
                }
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    private class Cell{
        boolean topwall = true;
        boolean leftwall = true;
        boolean bottomwall = true;
        boolean rightwall = true;
        visited = false;
        int col, row;
        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}
