package com.nba.predict;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

public class ModelEvaluator {
    
    public void evaluateAndCompare(List<ModelResult> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MODEL EVALUATION RESULTS");
        System.out.println("=".repeat(80));
        
        // Filter out failed models
        List<ModelResult> validResults = results.stream()
            .filter(r -> r.getEvaluation() != null)
            .collect(Collectors.toList());
        
        // Sort by accuracy
        validResults.sort(Comparator.comparingDouble(ModelResult::getAccuracy).reversed());
        
        System.out.println("\nRanking by Accuracy:\n");
        System.out.printf("%-25s | %-12s | %-15s | %-20s%n", 
            "Model", "Accuracy", "Training Time", "Additional Metrics");
        System.out.println("-".repeat(80));
        
        for (int i = 0; i < validResults.size(); i++) {
            ModelResult result = validResults.get(i);
            ModelEvaluation eval = result.getEvaluation();
            
            System.out.printf("%-25s | %-12.4f | %-15d ms | Precision: %.4f, Recall: %.4f%n",
                result.getModelName(),
                result.getAccuracy(),
                result.getTrainingTimeMs(),
                eval.getPrecisionWin(),
                eval.getRecallWin()
            );
        }
        
        // Print confusion matrices
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONFUSION MATRICES");
        System.out.println("=".repeat(80));
        
        for (ModelResult result : validResults) {
            System.out.println("\n" + result.getModelName() + ":");
            int[][] cm = result.getEvaluation().getConfusionMatrix();
            System.out.printf("                Predicted%n");
            System.out.printf("              LOSS   WIN%n");
            System.out.printf("Actual LOSS   %4d   %4d%n", cm[0][0], cm[0][1]);
            System.out.printf("       WIN    %4d   %4d%n", cm[1][0], cm[1][1]);
        }
        
        // Best model analysis
        if (!validResults.isEmpty()) {
            ModelResult bestModel = validResults.get(0);
            System.out.println("\n" + "=".repeat(80));
            System.out.println("BEST MODEL: " + bestModel.getModelName());
            System.out.println("=".repeat(80));
            System.out.printf("Accuracy: %.4f%%%n", bestModel.getAccuracy() * 100);
            System.out.printf("Training Time: %d ms%n", bestModel.getTrainingTimeMs());
            
            ModelEvaluation eval = bestModel.getEvaluation();
            System.out.println("\nDetailed Metrics:");
            System.out.printf("  Precision (WIN): %.4f%n", eval.getPrecisionWin());
            System.out.printf("  Recall (WIN): %.4f%n", eval.getRecallWin());
            System.out.printf("  F1-Score (WIN): %.4f%n", eval.getF1Win());
            System.out.printf("  Precision (LOSS): %.4f%n", eval.getPrecisionLoss());
            System.out.printf("  Recall (LOSS): %.4f%n", eval.getRecallLoss());
            System.out.printf("  F1-Score (LOSS): %.4f%n", eval.getF1Loss());
        }
    }
    
    public void exportResultsToCSV(List<ModelResult> results, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("Model_Name,Accuracy,Training_Time_ms,Precision_WIN,Recall_WIN,F1_WIN,Precision_LOSS,Recall_LOSS,F1_LOSS,CM00,CM01,CM10,CM11\n");
            
            for (ModelResult result : results) {
                if (result.getEvaluation() == null) {
                    continue;
                }
                
                ModelEvaluation eval = result.getEvaluation();
                int[][] cm = eval.getConfusionMatrix();
                int cm00 = cm[0][0];
                int cm01 = cm[0][1];
                int cm10 = cm[1][0];
                int cm11 = cm[1][1];

                writer.append(String.format(Locale.US, "%s,%.6f,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%d,%d,%d,%d%n",
                    result.getModelName(),
                    result.getAccuracy(),
                    result.getTrainingTimeMs(),
                    eval.getPrecisionWin(),
                    eval.getRecallWin(),
                    eval.getF1Win(),
                    eval.getPrecisionLoss(),
                    eval.getRecallLoss(),
                    eval.getF1Loss(),
                    cm00, cm01, cm10, cm11
                ));
            }
        }
        System.out.println("\nResults exported to: " + filename);
    }
}
