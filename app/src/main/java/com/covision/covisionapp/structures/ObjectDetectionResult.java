package com.covision.covisionapp.structures;

import com.google.gson.annotations.SerializedName;

public class ObjectDetectionResult {

    @SerializedName("class")
    private String className;

    @SerializedName("box")
    private double[] box;

    @SerializedName("score")
    private double score;

    @SerializedName("type")
    private String type;

    public ObjectDetectionResult(){
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double[] getBox() {
        return box;
    }

    public void setBox(double[] box) {
        this.box = box;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }
}
