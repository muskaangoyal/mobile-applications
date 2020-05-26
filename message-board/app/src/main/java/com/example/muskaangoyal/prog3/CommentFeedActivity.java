package com.example.muskaangoyal.prog3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// Displays a list of comments for a particular landmark.
public class CommentFeedActivity extends AppCompatActivity {

    private static final String TAG = CommentFeedActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> mComments = new ArrayList<Comment>();
    // UI elements
    EditText commentInputBox;
    RelativeLayout layout;
    Button sendButton;
    private String username;
    private String landmarkName;
    public HashMap<String, String> commentThread;
    boolean firstCall = true;
    FirebaseDatabase database = FirebaseDatabase.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_feed);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        landmarkName = intent.getStringExtra("locationName");

        // sets the app bar's title
        setTitle(landmarkName + "Posts");

        // hook up UI elements
        layout = (RelativeLayout) findViewById(R.id.comment_layout);
        commentInputBox = (EditText) layout.findViewById(R.id.comment_input_edit_text);
        sendButton = (Button) layout.findViewById(R.id.send_button);
        mRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an onclick for the send button
        setOnClickForSendButton();

        // make some test comment objects that we add to the recycler view
        mComments = new ArrayList<Comment>();

        DatabaseReference landmark = database.getReference(landmarkName);


        ValueEventListener myDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mComments = new ArrayList<Comment>();
                commentThread = (HashMap<String, String>) dataSnapshot.getValue();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Log.e(TAG, "children: " + data);
                    String time = data.getKey().toString();
                    String comment = data.child("comment").getValue(String.class);
                    String user = data.child("user").getValue(String.class);
                    Comment newComment = new Comment(comment, user, new Date(time));
                    mComments.add(newComment);
                }
                    setAdapterAndUpdateData();
                    //firstCall = false;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("0", "cancelled");
            }
        };
        // try changing `someRef` here
        landmark.addValueEventListener(myDataListener);

        // use the comments in mComments to create an adapter. This will populate mRecyclerView
        // with a custom cell (with comment_cell_layout) for each comment in mComments
        setAdapterAndUpdateData();
    }

    private void setOnClickForSendButton() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentInputBox.getText().toString();
                if (TextUtils.isEmpty(comment)) {
                    // don't do anything if nothing was added
                    commentInputBox.requestFocus();
                } else {
                    // clear edit text, post comment
                    commentInputBox.setText("");
                    postNewComment(comment);
                }
            }
        });
    }

    private void setAdapterAndUpdateData() {
        // create a new adapter with the updated mComments array
        // this will "refresh" our recycler view
        mAdapter = new com.example.muskaangoyal.prog3.CommentAdapter(this, mComments);
        mRecyclerView.setAdapter(mAdapter);

        // scroll to the last comment
        if (mComments.size() == 0) {
            mRecyclerView.smoothScrollToPosition(0);
        } else {
            mRecyclerView.smoothScrollToPosition(mComments.size() - 1);
        }
    }

    private void postNewComment(String commentText) {
        Comment newComment = new Comment(commentText, username, new Date());
        mComments.add(newComment);
        DatabaseReference landmark = database.getReference(landmarkName);
        DatabaseReference time = landmark.child(String.valueOf(new Date()));
        time.child("user").setValue(username);
        time.child("comment").setValue(commentText);
        setAdapterAndUpdateData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
