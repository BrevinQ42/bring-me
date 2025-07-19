package com.aragoza_mejilla_que.final_project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class PhotoGridAdapter extends RealmRecyclerViewAdapter<Photo, PhotoGridAdapter.PhotoGridViewHolder> {

    private Context context;
    private File cacheDir;

    public PhotoGridAdapter(Context context, OrderedRealmCollection<Photo> data) {
        super(data, true);
        this.context = context;
        this.cacheDir = context.getExternalCacheDir();
    }

    @NonNull
    @Override
    public PhotoGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_grid_item, parent, false);
        return new PhotoGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoGridViewHolder holder, int position) {
        Photo photo = getItem(position);

        if (photo == null) {
            return;
        }

        if (photo.getImagePath() != null && !photo.getImagePath().isEmpty()) {
            File imgFile = new File(cacheDir, photo.getImagePath());

            if (imgFile.exists()) {
                Picasso.get()
                        .load(imgFile)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(holder.gridPhotoImageView);
            } else {
                Picasso.get()
                        .load(photo.getImagePath())
                        .into(holder.gridPhotoImageView);
            }
        } else {
            holder.gridPhotoImageView.setImageResource(R.mipmap.ic_launcher);
        }

        holder.gridPhotoImageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewImageScreen.class);
            intent.putExtra("photoID", photo.getPhotoID());
            context.startActivity(intent);
        });
    }

    public static class PhotoGridViewHolder extends RecyclerView.ViewHolder {
        ImageView gridPhotoImageView;

        public PhotoGridViewHolder(@NonNull View itemView) {
            super(itemView);
            gridPhotoImageView = itemView.findViewById(R.id.gridPhotoImageView);
        }
    }
}