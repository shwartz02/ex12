package com.example.roomexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.example.roomexample.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AppDB db;
    private ListView lvPosts;
    private List<String> posts;
    private List<Post> dbPosts;
    private ArrayAdapter<String> adapter;
    private PostDao postDao;
    private SampleViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SampleViewModel.class);

        // Observe foo changes
        viewModel.getFoo().observe(this, foo -> {
            if (foo != null) {
                getSupportActionBar().setTitle(foo);
            }
        });

        db = Room.databaseBuilder(getApplicationContext(), AppDB.class, "FooDB")
                .allowMainThreadQueries()
                .build();
        postDao = db.postDao();
        handlePosts();

        binding.btnAdd.setOnClickListener(view -> {
            // Update the title with current date/time
            String currentDateTime = java.text.DateFormat.getDateTimeInstance().format(new Date());
            viewModel.getFoo().setValue(currentDateTime);
            
            // Original post creation logic
            Intent intent = new Intent(this, FormActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    private void handlePosts() {
        lvPosts = binding.lvPosts;
        posts = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, posts);
        loadPosts();
        lvPosts.setAdapter(adapter);

        lvPosts.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(this, FormActivity.class);
            intent.putExtra("id", dbPosts.get(i).getId());
            startActivity(intent);
        });

        lvPosts.setOnItemLongClickListener((adapterView, view, i, l) -> {
            posts.remove(i);
            Post post = dbPosts.remove(i);
            postDao.delete(post);
            adapter.notifyDataSetChanged();
            return true;
        });
    }

    private void loadPosts() {
        posts.clear();
        dbPosts = postDao.index();
        for (Post post : dbPosts) {
            posts.add(post.getId() + "," + post.getContent());
        }
        adapter.notifyDataSetChanged();
    }
}
