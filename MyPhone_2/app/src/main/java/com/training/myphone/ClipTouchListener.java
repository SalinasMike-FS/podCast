package com.training.myphone;

import android.view.MotionEvent;
import android.view.View;

public class ClipTouchListener implements View.OnTouchListener {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
    public boolean onLongClick(View view) {
        // Drag the clip to the target
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDragAndDrop(null, shadowBuilder, view, 0);
        return true;
    }
}
