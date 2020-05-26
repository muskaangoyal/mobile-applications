//package com.example.imagelabeler;
//
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.widget.ImageView;
//
//import java.util.LinkedList;
//
//

//Tried to create another class for cleanliness purposes but it kept failing and it was not drawing correctly.


//public class drawClass {
//    LinkedList<Rect> rectanglesList;
//    LinkedList<Integer> categoriesList;
//    LinkedList<String> labelsList;
//    // -------------------------------------------- //
//    //   AndroidGraphics - PAINT and StackOverflow
//    // -------------------------------------------- //
//
//    private void draw(Rect rect,
//                      int classificationCategory,
//                      String label,
//                      Canvas canvas,
//                      ImageView imageView,
//                      LinkedList<Rect> rectList,
//                      LinkedList<Integer> catList,
//                      LinkedList<String> labList) {
//        rectanglesList = rectList;
//        categoriesList = catList;
//        labelsList = labList;
//
//        Paint strokePaint = new Paint();
//        strokePaint.setStyle(Paint.Style.STROKE);
//        strokePaint.setColor(Color.RED);
//        strokePaint.setStrokeWidth(10);
//        canvas.drawRect(rect, strokePaint);
//        Paint text = new Paint();
//        text.setStyle(Paint.Style.FILL);
//        text.setColor(Color.RED);
//        text.setTextSize(100);
//        if ((rect.top - 100) <= 0) {
//            canvas.drawText(label, rect.left, rect.bottom + 100, text);
//        } else {
//            canvas.drawText(label, rect.left, rect.top - 50, text);
//        }
//        imageView.postInvalidate();
//    }
//    public void labelBuffer(String label) {
//        this.labelsList.add(label);
//        startDraw();
//    }
//
//    public void rectanglesBuffer(Rect rect) {
//        this.rectanglesList.add(rect);
//    }
//
//    public void categoriesBuffer(int category) {
//        this.categoriesList.ad
//
//        d(category);
//    }
//    public void startDraw() {
//        draw(rectanglesList.remove(), categoriesList.remove(), labelsList.remove());
//    }
//}
