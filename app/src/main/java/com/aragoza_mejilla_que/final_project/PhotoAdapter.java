package com.aragoza_mejilla_que.final_project;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private RealmResults<Photo> photoList;
    private Context context;
    private Realm realm;
    private File cacheDir;

    public PhotoAdapter(Context context, RealmResults<Photo> photoList) {
        this.context = context;
        this.photoList = photoList;
        this.realm = Realm.getDefaultInstance();
        this.cacheDir = context.getExternalCacheDir();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        if (photo.getCaption() != null && !photo.getCaption().isEmpty() && !photo.getCaption().equals("null")) {
            holder.postCaptionTextView.setText(photo.getCaption());
            holder.postCaptionTextView.setVisibility(View.VISIBLE);
        } else {
            holder.postCaptionTextView.setVisibility(View.GONE);
        }

        holder.postLikeCountTextView.setText( String.valueOf(photo.getLikeCount() != null ? photo.getLikeCount() : 0) );

        User user = realm.where(User.class).equalTo("userID", photo.getUserID()).findFirst();

        if (user != null) {
            holder.postUsernameTextView.setText(user.getName());
            if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                File cacheDir = context.getExternalCacheDir();
                File userPhoto = new File(cacheDir, user.getUserID() + ".jpeg");

                if (userPhoto.exists())
                {
                    Log.v(TAG, "User photo exists");
                    Picasso.get()
                            .load(userPhoto)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(holder.postUserProfilePicImageView);
                }
                else
                {
                    Log.v(TAG, "User photo does not exist");
                    holder.postUserProfilePicImageView.setImageResource(R.mipmap.ic_launcher);
                }
            } else {
                holder.postUserProfilePicImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            holder.postUsernameTextView.setText("Unknown User");
            holder.postUserProfilePicImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        if (photo.getImagePath() != null && !photo.getImagePath().isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            File imgFile = new File(cacheDir, photo.getImagePath());

            if (imgFile.exists()) {
                Picasso.get()
                        .load(imgFile)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(holder.postImageView);
            } else {
                Picasso.get()
                        .load(photo.getImagePath())
                        .into(holder.postImageView);
            }

            holder.postImageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ViewImageScreen.class);
                intent.putExtra("photoID", photo.getPhotoID());
                context.startActivity(intent);
            });

        } else {
            holder.postImageView.setVisibility(View.GONE);
            holder.postImageView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        closeRealm();
    }

    public void closeRealm() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView postCaptionTextView;
        ImageView postUserProfilePicImageView;
        TextView postUsernameTextView;
        TextView postLikeCountTextView;
        ImageView postHeartIconImageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImage);
            postCaptionTextView = itemView.findViewById(R.id.postCaption);
            postUserProfilePicImageView = itemView.findViewById(R.id.postUserProfilePic);
            postUsernameTextView = itemView.findViewById(R.id.postUsername);
            postLikeCountTextView = itemView.findViewById(R.id.postLikeCount);
            postHeartIconImageView = itemView.findViewById(R.id.postHeartIcon);
        }
    }
}