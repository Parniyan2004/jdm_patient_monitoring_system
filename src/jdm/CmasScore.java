package jdm;

/**
 * Stores one CMAS score recorded on a specific date.
 *
 * CMAS goes from 0 (very weak) to 52 (full remission).
 * A higher score means the patient is doing better.
 */
public class CmasScore {

    private String date;
    private int score;
    private String category;

    public CmasScore(String date, int score, String category) {
        this.date = date;
        this.score = score;
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public int getScore() {
        return score;
    }

    public String getCategory() {
        return category;
    }
}
