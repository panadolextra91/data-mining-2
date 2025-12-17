package com.nba.predict;

import java.util.ArrayList;
import java.util.List;

/**
 * ModelTrainer now acts as a thin wrapper around Weka's RandomForest,
 * evaluated via 10-fold cross-validation on the full feature matrix.
 */
public class ModelTrainer {

    public List<ModelResult> trainAllModels(double[][] trainFeatures, int[] trainLabels,
                                            double[][] testFeatures, int[] testLabels) {
        List<ModelResult> results = new ArrayList<>();
        try {
            // Single model: Weka RandomForest with 10-fold CV
            results.add(WekaRandomForestEvaluator.runRandomForestCV(trainFeatures, trainLabels));
        } catch (Exception e) {
            System.err.println("Weka RandomForest CV failed: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
}
