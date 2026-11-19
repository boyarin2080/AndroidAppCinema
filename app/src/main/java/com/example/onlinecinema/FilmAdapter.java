package com.example.onlinecinema;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * RecyclerView adapter for movie search results.
 */
public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    static class FilmViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;

        FilmViewHolder(View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.poster);
            title = itemView.findViewById(R.id.title);
        }
    }

    private final List<Film> films;
    private final LayoutInflater inflater;

    public FilmAdapter(LayoutInflater inflater, List<Film> films) {
        this.inflater = inflater;
        this.films = films;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FilmViewHolder(inflater.inflate(R.layout.film_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = films.get(position);
        holder.title.setText(film.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(film.getPoster())
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.poster_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PlayActivity.class);
            intent.putExtra("title", film.getTitle());
            intent.putExtra("posterUrl", film.getPoster());
            intent.putExtra("id", film.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return films.size();
    }
}
