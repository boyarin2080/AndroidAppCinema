package com.example.onlinecinema;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FilmAdapter adapter;
    private List<Film> filmList;
    private SearchView searchView;
    private TextView emptyText;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        filmList = new ArrayList<>();
        adapter = new FilmAdapter(this.getLayoutInflater(), filmList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        emptyText = findViewById(R.id.emptyText);
        searchView = findViewById(R.id.searcharea);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String q = query.trim();
                if (q.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.enter_film_name,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                searchView.clearFocus();
                // Network access is not allowed on the main thread, so the search runs in the background.
                executor.execute(() -> {
                    try {
                        String encoded = Encoder.encode(q);
                        List<Map<String, Object>> parsed = Parser.parse(encoded);
                        List<List<String>> ready = Preparer.prepare(parsed);

                        ArrayList<Film> films = new ArrayList<>();
                        for (List<String> row : ready) {
                            if (row != null && row.size() >= 3) {
                                films.add(new Film(row.get(0), row.get(1), row.get(2)));
                            }
                        }

                        mainHandler.post(() -> {
                            if (isFinishing() || isDestroyed()) {
                                return;
                            }
                            filmList.clear();
                            filmList.addAll(films);
                            adapter.notifyDataSetChanged();
                            emptyText.setVisibility(films.isEmpty() ? View.VISIBLE : View.GONE);
                            if (films.isEmpty()) {
                                Toast.makeText(MainActivity.this,
                                        R.string.nothing_found, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        String message = e.getMessage() != null ? e.getMessage() : e.toString();
                        showError(getString(R.string.error_prefix, message));
                        e.printStackTrace();
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void showError(String message) {
        mainHandler.post(() -> {
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
