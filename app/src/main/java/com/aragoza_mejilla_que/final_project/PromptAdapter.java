package com.aragoza_mejilla_que.final_project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.util.Log;

public class PromptAdapter extends RealmRecyclerViewAdapter<Prompt, PromptAdapter.ArchiveViewHolder> {

    private Context context;
    private File cacheDir;
    private static final String TAG = "PromptAdapter";

    public PromptAdapter(Context context, RealmResults<Prompt> data) {
        super(data, true);
        this.context = context;
        this.cacheDir = context.getExternalCacheDir();
    }

    @NonNull
    @Override
    public ArchiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prompt_layout, parent, false);
        return new ArchiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchiveViewHolder holder, int position) {
        final Prompt prompt = getItem(position);

        if (prompt == null) {
            Log.e(TAG, "Prompt object at position " + position + " is null or invalid.");
            return;
        }

        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
        String formattedDate = monthDayFormat.format(prompt.getDate());

        holder.promptName.setText(formattedDate + ", " + prompt.getText() + "!");

        if (prompt.getImagePath() != null && !prompt.getImagePath().isEmpty()) {
            File promptImageFile = new File(cacheDir, prompt.getImagePath());
            Log.d(TAG, "Prompt image path: " + promptImageFile.getAbsolutePath());

            if (promptImageFile.exists()) {
                Picasso.get()
                        .load(promptImageFile)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.promptImage);
                Log.d(TAG, "Loaded prompt image from cache: " + promptImageFile.getName());
            } else {
                holder.promptImage.setImageResource(R.mipmap.ic_launcher);
                Log.w(TAG, "Prompt image NOT found in cache for path: " + promptImageFile.getAbsolutePath() + ". Using default.");
            }
        } else {
            holder.promptImage.setImageResource(R.mipmap.ic_launcher);
            Log.d(TAG, "Prompt image path is null or empty. Using default.");
        }

        // Add OnClickListener to the promptImage
        holder.promptImage.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArchivedPhotos.class);
            intent.putExtra("promptID", prompt.getPromptID());
            context.startActivity(intent);
        });
    }

    public static class ArchiveViewHolder extends RecyclerView.ViewHolder {
        ImageView promptImage;
        TextView promptName;

        public ArchiveViewHolder(@NonNull View itemView) {
            super(itemView);
            promptImage = itemView.findViewById(R.id.promptImage);
            promptName = itemView.findViewById(R.id.promptName);
        }
    }
}