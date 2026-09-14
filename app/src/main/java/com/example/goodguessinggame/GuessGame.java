package com.example.goodguessinggame;

import java.util.Random;

public class GuessGame {

    public enum Difficulty {
        EASY(50, 10, 30),
        MEDIUM(100, 7, 20),
        HARD(200, 5, 10);

        private final int maxRange;
        private final int attempts;
        private final int timeInSeconds;

        Difficulty(int maxRange, int attempts, int timeInSeconds) {
            this.maxRange = maxRange;
            this.attempts = attempts;
            this.timeInSeconds = timeInSeconds;
        }

        public int getMaxRange() { return maxRange; }
        public int getAttempts() { return attempts; }
        public int getTimeInSeconds() { return timeInSeconds; }
    }

    private int targetNumber;
    private int remainingAttempts;
    private boolean isGameOver;
    private Difficulty currentDifficulty;
    private int totalScore = 0;

    public GuessGame(Difficulty difficulty) {
        resetGame(difficulty);
    }

    public void resetGame(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        this.remainingAttempts = difficulty.getAttempts();
        this.isGameOver = false;
        this.targetNumber = new Random().nextInt(difficulty.getMaxRange()) + 1;
    }

    public String makeGuess(int userGuess, int remainingSeconds) {
        if (isGameOver) {
            return "اللعبة انتهت بالفعل!";
        }

        remainingAttempts--;

        if (userGuess == targetNumber) {
            isGameOver = true;
            calculateScore(remainingSeconds);
            return "إجابة صحيحة! أحسنت.";
        } else if (remainingAttempts <= 0) {
            isGameOver = true;
            return "انتهت المحاولات! الرقم الصحيح كان: " + targetNumber;
        } else if (userGuess < targetNumber) {
            return "الرقم المطلوب أكبر من " + userGuess;
        } else {
            return "الرقم المطلوب أصغر من " + userGuess;
        }
    }

    public void timeout() {
        this.isGameOver = true;
    }

    private void calculateScore(int remainingSeconds) {
        int roundScore = (remainingAttempts * 10) + (remainingSeconds * 5);
        totalScore += roundScore;
    }

    public int getRemainingAttempts() { return remainingAttempts; }
    public boolean isGameOver() { return isGameOver; }
    public Difficulty getCurrentDifficulty() { return currentDifficulty; }
    public int getTotalScore() { return totalScore; }
}
