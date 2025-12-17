package com.nba.predict;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Uses Weka's RandomForest with 10-fold cross-validation on the full dataset.
 * Produces a ModelResult using our ModelEvaluation wrapper so it can be compared
 * alongside the Smile-based models.
 */
public class WekaRandomForestEvaluator {

    public static ModelResult runRandomForestCV(double[][] features, int[] labels) throws Exception {
        long start = System.currentTimeMillis();

        // Build Weka Instances from our feature matrix.
        Instances data = buildInstances(features, labels);

        // Configure RandomForest (number of trees etc.)
        RandomForest rf = new RandomForest();
        // RandomForest extends Bagging; configure via options or use defaults.
        // We rely on Weka's default numIterations (trees) here.

        // 10-fold cross-validation
        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(rf, data, 10, new Random(42));

        long trainingTimeMs = System.currentTimeMillis() - start;

        // Build final model on full data (optional, for later prediction if desired).
        rf.buildClassifier(data);

        // Extract metrics
        int lossIndex = data.classAttribute().indexOfValue("LOSS");
        int winIndex = data.classAttribute().indexOfValue("WIN");

        double accuracy = eval.pctCorrect() / 100.0;

        double precisionWin = eval.precision(winIndex);
        double recallWin = eval.recall(winIndex);
        double f1Win = eval.fMeasure(winIndex);

        double precisionLoss = eval.precision(lossIndex);
        double recallLoss = eval.recall(lossIndex);
        double f1Loss = eval.fMeasure(lossIndex);

        // Confusion matrix from Weka (double[][]) cast to int[][]
        double[][] cm = eval.confusionMatrix();
        int[][] confusionMatrix = new int[2][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                confusionMatrix[i][j] = (int) Math.round(cm[i][j]);
            }
        }

        ModelEvaluation modelEval = new ModelEvaluation(
                accuracy,
                precisionWin, recallWin, f1Win,
                precisionLoss, recallLoss, f1Loss,
                confusionMatrix
        );

        return new ModelResult("Weka RandomForest (10-fold CV)", rf, modelEval, trainingTimeMs);
    }

    private static Instances buildInstances(double[][] features, int[] labels) {
        int n = features.length;
        int d = features[0].length;

        ArrayList<Attribute> attrs = new ArrayList<>(d + 1);
        for (int j = 0; j < d; j++) {
            attrs.add(new Attribute("f" + j));
        }

        ArrayList<String> classValues = new ArrayList<>(Arrays.asList("LOSS", "WIN"));
        Attribute classAttr = new Attribute("class", classValues);
        attrs.add(classAttr);

        Instances data = new Instances("NBA_GAMES", attrs, n);
        data.setClassIndex(d);

        for (int i = 0; i < n; i++) {
            double[] vals = new double[d + 1];
            System.arraycopy(features[i], 0, vals, 0, d);
            vals[d] = labels[i] == 1 ? classAttr.indexOfValue("WIN") : classAttr.indexOfValue("LOSS");
            data.add(new DenseInstance(1.0, vals));
        }

        return data;
    }
}


