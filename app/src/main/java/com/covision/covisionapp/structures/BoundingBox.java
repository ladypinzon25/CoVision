package com.covision.covisionapp.structures;

public class BoundingBox {
    public String className;
    public double score;
    public double[] box;

    public BoundingBox(String className, double score, double[] box)
    {
        this.className = className;
        this.score = score;
        this.box = box;
    }
}
