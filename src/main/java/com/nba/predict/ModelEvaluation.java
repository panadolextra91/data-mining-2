package com.nba.predict;

public class ModelEvaluation {
    private final double accuracy;
    private final double precisionWin;
    private final double recallWin;
    private final double f1Win;
    private final double precisionLoss;
    private final double recallLoss;
    private final double f1Loss;
    private final int[][] confusionMatrix; // [actual][predicted]
    
    public ModelEvaluation(double accuracy, double precisionWin, double recallWin, double f1Win,
                          double precisionLoss, double recallLoss, double f1Loss,
                          int[][] confusionMatrix) {
        this.accuracy = accuracy;
        this.precisionWin = precisionWin;
        this.recallWin = recallWin;
        this.f1Win = f1Win;
        this.precisionLoss = precisionLoss;
        this.recallLoss = recallLoss;
        this.f1Loss = f1Loss;
        this.confusionMatrix = confusionMatrix;
    }
    
    public double getAccuracy() {
        return accuracy;
    }
    
    public double getPrecisionWin() {
        return precisionWin;
    }
    
    public double getRecallWin() {
        return recallWin;
    }
    
    public double getF1Win() {
        return f1Win;
    }
    
    public double getPrecisionLoss() {
        return precisionLoss;
    }
    
    public double getRecallLoss() {
        return recallLoss;
    }
    
    public double getF1Loss() {
        return f1Loss;
    }
    
    public int[][] getConfusionMatrix() {
        return confusionMatrix;
    }
    
    @Override
    public String toString() {
        return String.format("Accuracy: %.4f, Precision(WIN): %.4f, Recall(WIN): %.4f, F1(WIN): %.4f",
            accuracy, precisionWin, recallWin, f1Win);
    }
}

