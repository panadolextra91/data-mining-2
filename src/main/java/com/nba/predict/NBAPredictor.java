package com.nba.predict;

import java.io.IOException;
import java.util.List;

public class NBAPredictor {
    
    public static void main(String[] args) {
        String dataPath = "nba.sqlite";
        java.io.File sqliteFile = new java.io.File(dataPath);
        if (!sqliteFile.exists()) {
            dataPath = "game.csv"; // fallback to CSV if sqlite not present
        }
        
        if (args.length > 0) {
            dataPath = args[0];
        }
        
        System.out.println("NBA Match Prediction Engine");
        System.out.println("=".repeat(80));
        System.out.println("Loading data from: " + dataPath);
        
        try {
            // Phase 1: Load data
            DataLoader loader = new DataLoader();
            List<RawGame> games = loader.loadGames(dataPath);
            System.out.println("Loaded " + games.size() + " games");
            
            // Phase 2: Extract features
            FeaturePipeline pipeline = new FeaturePipeline();
            FeaturePipeline.FeatureData featureData = pipeline.extractFeatures(games);
            
            double[][] trainFeatures = featureData.getTrainFeatures();
            int[] trainLabels = featureData.getTrainLabels();
            double[][] testFeatures = featureData.getTestFeatures();
            int[] testLabels = featureData.getTestLabels();
            
            // Phase 3: Train multiple models
            System.out.println("\nTraining models...");
            ModelTrainer trainer = new ModelTrainer();
            List<ModelResult> results = trainer.trainAllModels(
                trainFeatures, trainLabels, testFeatures, testLabels);
            
            // Phase 4: Evaluate and compare
            ModelEvaluator evaluator = new ModelEvaluator();
            evaluator.evaluateAndCompare(results);
            
            // Phase 5: Export results
            evaluator.exportResultsToCSV(results, "model_results.csv");
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Training and evaluation complete!");
            System.out.println("=".repeat(80));
            
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during training: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
