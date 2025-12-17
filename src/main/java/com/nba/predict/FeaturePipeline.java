package com.nba.predict;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeaturePipeline {
    private static final LocalDate TRAIN_TEST_SPLIT_DATE = LocalDate.of(2020, 1, 1);
    
    public FeatureData extractFeatures(List<RawGame> games) {
        Map<String, TeamHistory> leagueMemory = new HashMap<>();
        Map<String, LocalDate> lastGameDate = new HashMap<>();
        Map<String, Deque<Boolean>> h2hHistory = new HashMap<>();
        List<double[]> trainFeatures = new ArrayList<>();
        List<Integer> trainLabels = new ArrayList<>();
        List<double[]> testFeatures = new ArrayList<>();
        List<Integer> testLabels = new ArrayList<>();
        
        int skippedGames = 0;
        int processedGames = 0;
        
        for (RawGame game : games) {
            String homeTeamId = game.getTeamIdHome();
            String awayTeamId = game.getTeamIdAway();
            LocalDate gameDate = game.getGameDate();
            
            TeamHistory homeHistory = leagueMemory.computeIfAbsent(homeTeamId, k -> new TeamHistory());
            TeamHistory awayHistory = leagueMemory.computeIfAbsent(awayTeamId, k -> new TeamHistory());
            
            // Compute rest days (team-level schedule features)
            double homeRestDays = lastGameDate.containsKey(homeTeamId)
                    ? ChronoUnit.DAYS.between(lastGameDate.get(homeTeamId), gameDate)
                    : 10.0; // treat first game as well-rested
            double awayRestDays = lastGameDate.containsKey(awayTeamId)
                    ? ChronoUnit.DAYS.between(lastGameDate.get(awayTeamId), gameDate)
                    : 10.0;
            double homeBackToBack = homeRestDays <= 1.0 ? 1.0 : 0.0;
            double awayBackToBack = awayRestDays <= 1.0 ? 1.0 : 0.0;

            // Head-to-head history (home perspective)
            String h2hKey = homeTeamId + "|" + awayTeamId;
            Deque<Boolean> h2hDeque = h2hHistory.computeIfAbsent(h2hKey, k -> new LinkedList<>());
            double h2hWinRate = 0.0;
            if (!h2hDeque.isEmpty()) {
                int wins = 0;
                for (Boolean w : h2hDeque) {
                    if (w) wins++;
                }
                h2hWinRate = (double) wins / h2hDeque.size();
            }

            // Skip if teams don't have enough history (but still update memories below)
            if (!homeHistory.isReady() || !awayHistory.isReady()) {
                skippedGames++;
                // Still update history for future games
                homeHistory.recordMatch(
                    game.getPtsHome(),
                    game.getPtsAway(),
                    game.isHomeWin(),
                    game.getFgPctHome(),
                    game.getRebHome(),
                    game.getAstHome(),
                    game.getTovHome()
                );
                awayHistory.recordMatch(
                    game.getPtsAway(),
                    game.getPtsHome(),
                    !game.isHomeWin(),
                    game.getFgPctAway(),
                    game.getRebAway(),
                    game.getAstAway(),
                    game.getTovAway()
                );

                // Update rest date and head-to-head after game
                lastGameDate.put(homeTeamId, gameDate);
                lastGameDate.put(awayTeamId, gameDate);
                // Update H2H deque (limit to last 10 meetings)
                h2hDeque.addLast(game.isHomeWin());
                if (h2hDeque.size() > 10) {
                    h2hDeque.removeFirst();
                }
                continue;
            }
            
            // Extract features BEFORE updating history (time-travel safe)
            // 0-11: existing rolling window stats
            // 12-13: rolling point differential
            // 14-15: season-to-date win rate
            // 16-17: season-to-date average point differential
            // 18-19: rest days
            // 20-21: back-to-back flags
            // 22-23: current win streak
            // 24: head-to-head win rate (home vs away)
            double[] features = new double[25];
            features[0] = homeHistory.getAvgPoints();
            features[1] = awayHistory.getAvgPoints();
            features[2] = homeHistory.getWinRate();
            features[3] = awayHistory.getWinRate();
            features[4] = homeHistory.getAvgFgPct();
            features[5] = awayHistory.getAvgFgPct();
            features[6] = homeHistory.getAvgReb();
            features[7] = awayHistory.getAvgReb();
            features[8] = homeHistory.getAvgAst();
            features[9] = awayHistory.getAvgAst();
            features[10] = homeHistory.getAvgTov();
            features[11] = awayHistory.getAvgTov();
            // New rolling point differential features
            features[12] = homeHistory.getAvgPointDiffWindow();
            features[13] = awayHistory.getAvgPointDiffWindow();
            // Season-to-date win rate
            features[14] = homeHistory.getSeasonWinRate();
            features[15] = awayHistory.getSeasonWinRate();
            // Season-to-date average point differential
            features[16] = homeHistory.getSeasonAvgPointDiff();
            features[17] = awayHistory.getSeasonAvgPointDiff();
            // Rest days
            features[18] = homeRestDays;
            features[19] = awayRestDays;
            // Back-to-back indicators
            features[20] = homeBackToBack;
            features[21] = awayBackToBack;
            // Current win streak
            features[22] = homeHistory.getCurrentWinStreak();
            features[23] = awayHistory.getCurrentWinStreak();
            // Head-to-head win rate (home perspective)
            features[24] = h2hWinRate;
            
            int label = game.isHomeWin() ? 1 : 0; // 1 = WIN, 0 = LOSS
            
            // Add to train or test based on date
            if (game.getGameDate().isBefore(TRAIN_TEST_SPLIT_DATE)) {
                trainFeatures.add(features);
                trainLabels.add(label);
            } else {
                testFeatures.add(features);
                testLabels.add(label);
            }
            
            processedGames++;
            
            // NOW update history after features are extracted
            homeHistory.recordMatch(
                game.getPtsHome(),
                game.getPtsAway(),
                game.isHomeWin(),
                game.getFgPctHome(),
                game.getRebHome(),
                game.getAstHome(),
                game.getTovHome()
            );
            awayHistory.recordMatch(
                game.getPtsAway(),
                game.getPtsHome(),
                !game.isHomeWin(),
                game.getFgPctAway(),
                game.getRebAway(),
                game.getAstAway(),
                game.getTovAway()
            );

            // Update rest date and head-to-head after game
            lastGameDate.put(homeTeamId, gameDate);
            lastGameDate.put(awayTeamId, gameDate);
            h2hDeque.addLast(game.isHomeWin());
            if (h2hDeque.size() > 10) {
                h2hDeque.removeFirst();
            }
        }
        
        System.out.println("Feature extraction complete:");
        System.out.println("  Processed games: " + processedGames);
        System.out.println("  Skipped games (insufficient history): " + skippedGames);
        System.out.println("  Training examples: " + trainFeatures.size());
        System.out.println("  Test examples: " + testFeatures.size());
        
        return new FeatureData(
            trainFeatures.toArray(new double[0][]),
            trainLabels.stream().mapToInt(i -> i).toArray(),
            testFeatures.toArray(new double[0][]),
            testLabels.stream().mapToInt(i -> i).toArray()
        );
    }
    
    public static class FeatureData {
        private final double[][] trainFeatures;
        private final int[] trainLabels;
        private final double[][] testFeatures;
        private final int[] testLabels;
        
        public FeatureData(double[][] trainFeatures, int[] trainLabels,
                          double[][] testFeatures, int[] testLabels) {
            this.trainFeatures = trainFeatures;
            this.trainLabels = trainLabels;
            this.testFeatures = testFeatures;
            this.testLabels = testLabels;
        }
        
        public double[][] getTrainFeatures() {
            return trainFeatures;
        }
        
        public int[] getTrainLabels() {
            return trainLabels;
        }
        
        public double[][] getTestFeatures() {
            return testFeatures;
        }
        
        public int[] getTestLabels() {
            return testLabels;
        }
    }
}
