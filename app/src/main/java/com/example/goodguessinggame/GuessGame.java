package com.example.goodguessinggame;

import java.util.Random;

/**
 * Manages the core game logic, state, difficulty configurations, and scoring
 * for the number guessing game.
 */
public class GuessGame {

    /**
     * Enum representing game difficulty settings, encapsulating the maximum random
     * range, allowed attempts, and round timer duration.
     */
    public enum Difficulty {
        EASY(50, 10, 30),
        MEDIUM(100, 7, 20),
        HARD(200, 5, 10);

        private final int maxRandom;
        private final int defaultAttempts;
        private final int seconds;

        /**
         * Enum constructor to set difficulty parameters.
         *
         * @param maxRandom       Upper bound (inclusive) for random target generation.
         * @param defaultAttempts Initial number of guesses allowed.
         * @param seconds         Duration in seconds allowed for the round timer.
         */
        Difficulty(int maxRandom, int defaultAttempts, int seconds) {
            this.maxRandom = maxRandom;
            this.defaultAttempts = defaultAttempts;
            this.seconds = seconds;
        }

        /**
         * Generates a random target integer between 1 and maxRandom (inclusive).
         *
         * @param rand Random instance used for generation.
         * @return Target number for the player to guess.
         */
        public int generateTargetNumber(Random rand) {
            return rand.nextInt(this.maxRandom) + 1;
        }

        public int getAttempts() {
            return this.defaultAttempts;
        }

        public int getMaxRandom() {
            return maxRandom;
        }

        public int getSeconds() {
            return seconds;
        }
    }

    private int targetNumber;
    private int remainingAttempts;
    private boolean isGameOver;
    private Difficulty currentDifficulty;
    private Random rand;
    private int totalScore = 0; // Cumulative score maintained across rounds

    /**
     * Initializes a new GuessGame instance using the specified difficulty.
     *
     * @param difficulty Initial difficulty level.
     */
    public GuessGame(Difficulty difficulty) {
        rand = new Random();
        resetGame(difficulty);
    }

    /**
     * Resets round variables (target number, remaining attempts, status)
     * based on the selected difficulty while retaining the cumulative total score.
     *
     * @param difficulty Selected difficulty level for the round.
     */
    public void resetGame(Difficulty difficulty) {
        isGameOver = false;
        currentDifficulty = difficulty;
        targetNumber = currentDifficulty.generateTargetNumber(rand);
        remainingAttempts = currentDifficulty.getAttempts();
    }

    /**
     * Evaluates the user's guess against the target number, decrements remaining attempts,
     * and checks for game-over conditions.
     *
     * @param userGuess Integer value guessed by the player.
     * @return Evaluation result: "Win", "Lose", "Higher", "Lower", or "Game Over".
     */
    public String makeGuess(int userGuess) {
        if (isGameOver) return "Game Over";

        remainingAttempts--;

        if (userGuess == targetNumber) {
            isGameOver = true;
            return "Win";
        } else if (remainingAttempts <= 0) {
            isGameOver = true;
            return "Lose";
        } else if (userGuess > targetNumber) {
            return "Lower";
        } else {
            return "Higher";
        }
    }

    /**
     * Calculates bonus points awarded for a winning round based on remaining
     * attempts and seconds, adding them to the cumulative total score.
     * Formula: Score = (Remaining Attempts * 10) + (Remaining Seconds * 5)
     *
     * @param remainingSeconds Seconds remaining on the round timer upon winning.
     * @return Points earned during this round.
     */
    public int addWinScore(int remainingSeconds) {
        int roundScore = (remainingAttempts * 10) + (remainingSeconds * 5);
        totalScore += roundScore;
        return roundScore;
    }

    /**
     * Marks the current round as over due to a time-out event.
     */
    public void timeout() {
        isGameOver = true;
    }

    // Getters for inspecting game state
    public int getTargetNumber() { return targetNumber; }
    public int getRemainingAttempts() { return remainingAttempts; }
    public boolean isGameOver() { return isGameOver; }
    public Difficulty getCurrentDifficulty() { return currentDifficulty; }
    public int getTotalScore() { return totalScore; }
}