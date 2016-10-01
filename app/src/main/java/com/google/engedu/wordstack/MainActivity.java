package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static int WORD_LENGTH = 3;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Stack<LetterTile> placedTiles = new Stack<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                //if(word.length() == WORD_LENGTH){
                words.add(word);
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);

        View word1LinearLayout = findViewById(R.id.word1);
        //word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        //word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                tile.freeze();
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }
                    placedTiles.push(tile);
                    tile.freeze();
                    return true;
            }
            return false;
        }
    }

    protected boolean onStartGame(View view) {
        //Delete anything from previous game
        LinearLayout word1LinearLayout = (LinearLayout) findViewById(R.id.word1);
        LinearLayout word2LinearLayout = (LinearLayout) findViewById(R.id.word2);
        word1LinearLayout.removeAllViews();
        word2LinearLayout.removeAllViews();
        stackedLayout.clear();

        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");

        int randomInt;
        int randomInt2;
        word1 = "";
        word2 = "";
        //initialize random words
        while(word1.length() != WORD_LENGTH)
        {
            randomInt = random.nextInt(words.size());
            word1 = words.get(randomInt);
        }
        while(word2.length() != WORD_LENGTH)
        {
            randomInt2 = random.nextInt(words.size());
            word2 = words.get(randomInt2);
        }
        WORD_LENGTH++;

        String scrambledWords = "";
        int counterWord1 = 0;
        int counterWord2 = 0;
        while(counterWord1 < word1.length() && counterWord2 < word2.length()) {
            randomInt = random.nextInt(2);
            if (randomInt == 0){
                scrambledWords = scrambledWords + word1.charAt(counterWord1);
                counterWord1++;
            }
            else {
                scrambledWords = scrambledWords + word2.charAt(counterWord2);
                counterWord2++;
            }
        }

        while(counterWord1 < word1.length()){
            scrambledWords = scrambledWords + word1.charAt(counterWord1);
            counterWord1++;
        }

        while(counterWord2 < word2.length()) {
            scrambledWords = scrambledWords + word2.charAt(counterWord2);
            counterWord2++;
        }

        TextView viewMessageBox = (TextView) findViewById(R.id.message_box);
        viewMessageBox.setText(scrambledWords);

        //add tiles for the scrambled word to stack
        for(int i = scrambledWords.length() - 1; i >= 0; i--) {
            LetterTile tileToPush = new LetterTile(this, scrambledWords.charAt(i));
            stackedLayout.push(tileToPush);
        }

        return true;
    }

    protected boolean onUndo(View view) {
        if(!stackedLayout.empty()) {
            if (!placedTiles.empty()) {
                //LetterTile tile = placedTiles.peek();
                //stackedLayout.push(tile);
                LetterTile poppedTile = placedTiles.pop();
                //stackedLayout.push(poppedTile);
                poppedTile.moveToViewGroup(stackedLayout);
                poppedTile.unfreeze();
            }
        }
        return true;
    }
}
