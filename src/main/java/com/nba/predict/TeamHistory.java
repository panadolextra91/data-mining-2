package com.nba.predict;

import java.util.ArrayDeque;
import java.util.Deque;

public class TeamHistory {
    private static final int WINDOW_SIZE = 5;
    
    private final Deque<Integer> recentPoints;
    private final Deque<Integer> recentPointDiff;
    private final Deque<Boolean> recentWins;
    private final Deque<Double> recentFgPct;
    private final Deque<Double> recentReb;
    private final Deque<Double> recentAst;
    private final Deque<Double> recentTov;

    // Season-to-date aggregates (up to current game, not windowed)
    private int seasonGames;
    private int seasonWins;
    private int seasonPointsFor;
    private int seasonPointsAgainst;
    private int currentWinStreak;
    
    public TeamHistory() {
        this.recentPoints = new ArrayDeque<>();
        this.recentPointDiff = new ArrayDeque<>();
        this.recentWins = new ArrayDeque<>();
        this.recentFgPct = new ArrayDeque<>();
        this.recentReb = new ArrayDeque<>();
        this.recentAst = new ArrayDeque<>();
        this.recentTov = new ArrayDeque<>();
    }
    
    public void recordMatch(int pointsScored, int pointsAllowed, boolean won,
                            double fgPct, double reb, double ast, double tov) {
        recentPoints.addLast(pointsScored);
        recentPointDiff.addLast(pointsScored - pointsAllowed);
        recentWins.addLast(won);
        recentFgPct.addLast(fgPct);
        recentReb.addLast(reb);
        recentAst.addLast(ast);
        recentTov.addLast(tov);
        
        if (recentPoints.size() > WINDOW_SIZE) {
            recentPoints.removeFirst();
            recentPointDiff.removeFirst();
            recentWins.removeFirst();
            recentFgPct.removeFirst();
            recentReb.removeFirst();
            recentAst.removeFirst();
            recentTov.removeFirst();
        }

        // Update season aggregates
        seasonGames++;
        if (won) {
            seasonWins++;
            currentWinStreak++;
        } else {
            currentWinStreak = 0;
        }
        seasonPointsFor += pointsScored;
        seasonPointsAgainst += pointsAllowed;
    }
    
    public double getAvgPoints() {
        if (recentPoints.isEmpty()) {
            return 0.0;
        }
        return recentPoints.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
    
    public double getWinRate() {
        if (recentWins.isEmpty()) {
            return 0.0;
        }
        long wins = recentWins.stream().mapToLong(w -> w ? 1 : 0).sum();
        return (double) wins / recentWins.size();
    }

    public double getAvgPointDiffWindow() {
        if (recentPointDiff.isEmpty()) {
            return 0.0;
        }
        return recentPointDiff.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
    
    public double getAvgFgPct() {
        if (recentFgPct.isEmpty()) {
            return 0.0;
        }
        return recentFgPct.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    public double getAvgReb() {
        if (recentReb.isEmpty()) {
            return 0.0;
        }
        return recentReb.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    public double getAvgAst() {
        if (recentAst.isEmpty()) {
            return 0.0;
        }
        return recentAst.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    public double getAvgTov() {
        if (recentTov.isEmpty()) {
            return 0.0;
        }
        return recentTov.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    public boolean isReady() {
        return recentPoints.size() >= WINDOW_SIZE;
    }
    
    public int getGameCount() {
        return recentPoints.size();
    }

    public double getSeasonWinRate() {
        if (seasonGames == 0) {
            return 0.0;
        }
        return (double) seasonWins / seasonGames;
    }

    public double getSeasonAvgPointDiff() {
        if (seasonGames == 0) {
            return 0.0;
        }
        return (double) (seasonPointsFor - seasonPointsAgainst) / seasonGames;
    }

    public int getCurrentWinStreak() {
        return currentWinStreak;
    }
}

