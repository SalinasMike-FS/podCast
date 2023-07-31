package com.training.myphone;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class EditAudioActivity extends AppCompatActivity {

    private FrameLayout audioClipContainer;
    private LinearLayout dummyClipsLayout;
    private TextView trash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_audio);

        // Get the selected dummy clip title from MainActivity
        String selectedClipTitle = getIntent().getStringExtra("selectedClipTitle");

        boolean isEditedClip = getIntent().getBooleanExtra("isEditedClip", false);
        // Now you can perform the audio editing for the selected clip
        // ...

        // For example, you can display the title of the selected clip in a TextView
        EditText txtSelectedClipTitle = findViewById(R.id.txtSelectedClipTitle);
        txtSelectedClipTitle.setText(selectedClipTitle);

        audioClipContainer = findViewById(R.id.audioClipContainer);
        dummyClipsLayout = findViewById(R.id.dummyClipsLayout);
        trash = findViewById(R.id.btnTrash);

        if (isEditedClip) {
            Button btnSave = findViewById(R.id.btnSave);
            btnSave.setVisibility(View.GONE);
        }

        // Set drag listeners
        audioClipContainer.setOnDragListener(new AudioDragListener());
        trash.setOnDragListener(new TrashDragListener());
    }


    public void onSaveButtonClick(View view) {
        // Check if the edited clip title is not empty
        EditText txtSelectedClipTitle = findViewById(R.id.txtSelectedClipTitle);
        String editedClipTitle = txtSelectedClipTitle.getText().toString().trim();
        if (editedClipTitle.isEmpty()) {
            Toast.makeText(this, "Please enter a title for the edited clip.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new dummy clip title to indicate that it is an edited clip
        String editedDummyClipTitle = "Edited: " + editedClipTitle;

        // Add the edited dummy clip title to the list of dummy clips in MainActivity

        MainActivity.addDummyClipTitle(editedDummyClipTitle);

        // Display a toast to indicate that the clip was saved
        Toast.makeText(this, "Edited clip saved as a new dummy clip.", Toast.LENGTH_SHORT).show();
        this.finish();
    }










    private class AudioDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View view, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String draggedClipTitle = item.getText().toString();
                    TextView draggedView = new TextView(EditAudioActivity.this);
                    draggedView.setText(draggedClipTitle);
                    draggedView.setBackgroundColor(Color.LTGRAY);
                    draggedView.setTextColor(Color.BLACK);
                    draggedView.setPadding(16, 8, 16, 8);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 8, 0, 0);
                    draggedView.setLayoutParams(params);
                    audioClipContainer.addView(draggedView);
                    draggedView.setOnLongClickListener(new ClipLongClickListener());
                    draggedView.setOnTouchListener(new ClipTouchListener());
                    Toast.makeText(EditAudioActivity.this, "Dummy clip added", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    }

    private class TrashDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View view, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DROP:
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    String draggedClipTitle = item.getText().toString();
                    TextView draggedView = new TextView(EditAudioActivity.this);
                    draggedView.setText(draggedClipTitle);
                    draggedView.setBackgroundColor(Color.LTGRAY);
                    draggedView.setTextColor(Color.BLACK);
                    draggedView.setPadding(16, 8, 16, 8);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 8, 0, 0);
                    draggedView.setLayoutParams(params);
                    dummyClipsLayout.addView(draggedView);
                    draggedView.setOnLongClickListener(new ClipLongClickListener());
                    draggedView.setOnTouchListener(null);
                    Toast.makeText(EditAudioActivity.this, "Dummy clip deleted", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    }

    public static class ClipLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            ClipData.Item item = new ClipData.Item(((TextView) view).getText());
            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDragAndDrop(data, shadowBuilder, view, 0);
            return true;
        }
    }

    public static class ClipTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData.Item item = new ClipData.Item(((TextView) view).getText());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDragAndDrop(data, shadowBuilder, view, 0);
                return true;
            }
            return false;
        }
    }
}
