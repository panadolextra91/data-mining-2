package com.nba.predict;

public class ModelResult {
    private final String modelName;
    private final Object model; // Smile models have different types
    private final ModelEvaluation evaluation;
    private final long trainingTimeMs;
    
    public ModelResult(String modelName, Object model, ModelEvaluation evaluation, long trainingTimeMs) {
        this.modelName = modelName;
        this.model = model;
        this.evaluation = evaluation;
        this.trainingTimeMs = trainingTimeMs;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public Object getModel() {
        return model;
    }
    
    public ModelEvaluation getEvaluation() {
        return evaluation;
    }
    
    public long getTrainingTimeMs() {
        return trainingTimeMs;
    }
    
    public double getAccuracy() {
        return evaluation.getAccuracy();
    }
}
