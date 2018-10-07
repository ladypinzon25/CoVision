package com.covision.covisionapp.structures;

import java.util.ArrayList;

public class ObjectDetectionResult {
    public String resultText;
    public ArrayList<BoundingBox> boxes;

    public ObjectDetectionResult(){
        this.resultText = "";
        boxes = new ArrayList<>();
    }

    public void addText(String text)
    {
        resultText = text;
    }

    public void addBox(BoundingBox newBox){
        boxes.add(newBox);
    }
}
