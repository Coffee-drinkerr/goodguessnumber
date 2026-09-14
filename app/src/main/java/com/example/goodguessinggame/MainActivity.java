package com.example.goodguessinggame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GuessGame game;
    private CountDownTimer timer;
    private int secondsLeft;

    private RadioGroup rgDifficulty;
    private EditText etGuess;
    private TextView tvStatus, tvTimer, tvScore, tvFeedback;
    private Button btnGuess, btnNewGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        btnNewGame.setOnClickListener(v -> startNewGame());
        btnGuess.setOnClickListener(v -> handleGuess());


    }

    private void initViews() {
        rgDifficulty = findViewById(R.id.rgDifficulty);
        etGuess = findViewById(R.id.etGuess);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnGuess = findViewById(R.id.btnGuess);
        btnNewGame = findViewById(R.id.btnNewGame);
    }

    private GuessGame.Difficulty getSelectedDifficulty() {
        int checkedId = rgDifficulty.getCheckedRadioButtonId();
        if (checkedId == R.id.rbHard) {
            return GuessGame.Difficulty.HARD;
        } else if (checkedId == R.id.rbMedium) {
            return GuessGame.Difficulty.MEDIUM;
        } else {
            return GuessGame.Difficulty.EASY;
        }
    }

    private void startNewGame() {
        GuessGame.Difficulty selectedDifficulty = getSelectedDifficulty();

        btnNewGame.setEnabled(false);
        if (game == null) {
            game = new GuessGame(selectedDifficulty);
        } else {
            game.resetGame(selectedDifficulty);
        }

        stopTimer();
        startTimer(selectedDifficulty.getTimeInSeconds());
        updateUI("بدأت لعبة جديدة! خمن الرقم بين 1 و " + selectedDifficulty.getMaxRange());
    }

    private void handleGuess() {
        if (game.isGameOver()) {
            Toast.makeText(this, "الرجاء بدء لعبة جديدة", Toast.LENGTH_SHORT).show();
            return;
        }

        String input = etGuess.getText().toString().trim();
        if (input.isEmpty()) {
            etGuess.setError("أدخل رقماً أولاً");
            return;
        }

        int userGuess = Integer.parseInt(input);
        String result = game.makeGuess(userGuess, secondsLeft);
        etGuess.setText("");

        if (game.isGameOver()) {
            stopTimer();
        }

        updateUI(result);
    }

    private void startTimer(int seconds) {
        secondsLeft = seconds;
        timer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(secondsLeft + " ث");
            }

            @Override
            public void onFinish() {
                secondsLeft = 0;
                tvTimer.setText("0 ث");
                game.timeout();
                stopTimer();
                btnGuess.setEnabled(true);
                updateUI("انتهى الوقت! خسرت الجولة.");
            }
        }.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateUI(String feedbackText) {
        tvFeedback.setText(feedbackText);
        tvStatus.setText(String.valueOf(game.getRemainingAttempts()));
        tvScore.setText(String.valueOf(game.getTotalScore()));
        btnGuess.setEnabled(!game.isGameOver());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}