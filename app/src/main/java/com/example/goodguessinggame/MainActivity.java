package com.example.goodguessinggame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private GuessGame game;
    private TextView scoreTextView;
    private TextView attemptsTextView;
    private TextView timerTextView;
    private Spinner difficultySpinner;
    private EditText pickEditText;
    private EditText nameEditText;
    private Button submitButton;
    private CountDownTimer countDownTimer;
    private boolean isTimerStarted = false; // Prevents timer from restarting on every keypress
    private int remainingSeconds = 0;      // Tracks remaining seconds to calculate bonus points
    private DBHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind layout XML views to Java variables
        scoreTextView = findViewById(R.id.score);
        attemptsTextView = findViewById(R.id.attempts);
        timerTextView = findViewById(R.id.timer);
        difficultySpinner = findViewById(R.id.spinner);
        pickEditText = findViewById(R.id.pick);
        submitButton = findViewById(R.id.submit);
        nameEditText = findViewById(R.id.name);

        // Instantiate game model with initial EASY difficulty
        game = new GuessGame(GuessGame.Difficulty.EASY);
        db = new DBHelper(this);

        // Populate Difficulty Spinner with Enum values (EASY, MEDIUM, HARD)
        ArrayAdapter<GuessGame.Difficulty> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                GuessGame.Difficulty.values()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        // Trigger new game whenever user picks a different difficulty from spinner
        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GuessGame.Difficulty selectedDifficulty = (GuessGame.Difficulty) parent.getItemAtPosition(position);
                startNewGame(selectedDifficulty);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        pickEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!isTimerStarted && s.length() > 0 && !game.isGameOver()) {
                    isTimerStarted = true;
                    startTimer();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        submitButton.setOnClickListener(v -> handleGuess());
    }

    private void updateDB() {
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            name = "Player";
        }

        int currentScore = game.getTotalScore();
        ArrayList<ModelUser> users = db.genericSelectByUserName(name);

        if (!users.isEmpty()) {

            ModelUser existingUser = users.get(0);
            if (currentScore > existingUser.getScore()) {
                existingUser.setScore(currentScore);
                db.update(existingUser);
                Toast.makeText(this, "New High Score Saved!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // New player: insert record
            ModelUser newUser = new ModelUser(name, currentScore, 0);
            db.insert(newUser);
            Toast.makeText(this, "Score Saved!", Toast.LENGTH_SHORT).show();
        }
    }


    private void startNewGame(GuessGame.Difficulty difficulty) {
        cancelTimer();
        isTimerStarted = false;
        remainingSeconds = difficulty.getSeconds();
        game.resetGame(difficulty);
        updateUiState();
    }


    private void startTimer() {
        cancelTimer(); // Stop any active countdown thread

        long totalMillis = game.getCurrentDifficulty().getSeconds() * 1000L;

        countDownTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText(remainingSeconds + "s");
            }

            @Override
            public void onFinish() {
                remainingSeconds = 0;
                timerTextView.setText("Time: 0s");
                game.timeout();
                showGameOverDialog("Time's Up! ⏰", "You ran out of time! The target number was: " + game.getTargetNumber());
            }
        }.start();
    }


    private void updateUiState() {
        scoreTextView.setText("Score: " + game.getTotalScore());
        attemptsTextView.setText("Attempts Left: " + game.getRemainingAttempts());
        timerTextView.setText("Time: " + game.getCurrentDifficulty().getSeconds() + "s");
        pickEditText.setHint("Pick a number from 1-" + game.getCurrentDifficulty().getMaxRandom());
        pickEditText.setText("");
    }

    private void handleGuess() {
        if (game.isGameOver()) return;

        String input = pickEditText.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int userGuess = Integer.parseInt(input);
        String result = game.makeGuess(userGuess);

        // Update remaining attempts text after guess submission
        attemptsTextView.setText("Attempts: " + game.getRemainingAttempts());

        switch (result) {
            case "Win":
                cancelTimer();
                int earnedPoints = game.addWinScore(remainingSeconds);
                scoreTextView.setText("Score: " + game.getTotalScore());
                updateDB();
                showGameOverDialog("You Won! 🎉", "Earned " + earnedPoints + " points!\nTotal Score: " + game.getTotalScore());
                break;

            case "Lose":
                cancelTimer();
                showGameOverDialog("Game Over ❌", "You ran out of attempts! The number was: " + game.getTargetNumber());
                break;

            case "Higher":
                Toast.makeText(this, "Try Higher!", Toast.LENGTH_SHORT).show();
                break;

            case "Lower":
                Toast.makeText(this, "Try Lower!", Toast.LENGTH_SHORT).show();
                break;
        }

        pickEditText.setText("");
    }


    private void showGameOverDialog(String title, String message) {
        cancelTimer();

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    GuessGame.Difficulty currentDifficulty = (GuessGame.Difficulty) difficultySpinner.getSelectedItem();
                    startNewGame(currentDifficulty);
                })
                .show();
    }


    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}