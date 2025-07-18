package com.aragoza_mejilla_que.final_project;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

// the parameterization <type of the RealmObject, ViewHolder type)
public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {

    // THIS DEFINES WHAT VIEWS YOU ARE FILLING IN
    public class ViewHolder extends RecyclerView.ViewHolder {

        // have a field for each one
        TextView username;
        TextView photosPosted;
        Button visitProfileButton;

        ImageView profilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // initialize them from the itemView using standard style
            username = itemView.findViewById(R.id.username);
            photosPosted = itemView.findViewById(R.id.photosPosted);

            visitProfileButton = itemView.findViewById(R.id.visitProfileButton);
            // initialize imageView
            profilePic = itemView.findViewById(R.id.profilePic);
        }
    }


    // IMPORTANT
    // THE CONTAINING ACTIVITY NEEDS TO BE PASSED SO YOU CAN GET THE LayoutInflator(see below)
    UsersScreen activity;

    public UserAdapter(UsersScreen activity, @Nullable OrderedRealmCollection<User> data, boolean autoUpdate) {
        super(data, autoUpdate);

        // THIS IS TYPICALLY THE ACTIVITY YOUR RECYCLERVIEW IS IN
        this.activity = activity;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create the raw view for this ViewHolder
        View v = activity.getLayoutInflater().inflate(R.layout.user_layout, parent, false);  // VERY IMPORTANT TO USE THIS STYLE

        // assign view to the viewholder
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // gives you the data object at the given position
        User u = getItem(position);

        // copy all the values needed to the appropriate views
        holder.username.setText(u.getName());
        holder.photosPosted.setText(u.getPassword());

        holder.visitProfileButton.setOnClickListener(v -> editUser(u));

        // get cache directory
        File cacheDir = activity.getExternalCacheDir();
        File userPhoto = new File(cacheDir, u.getUserID() + ".jpeg");

        if (userPhoto.exists())
        {
            Picasso.get()
                    .load(userPhoto)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(holder.profilePic);
        }
        else
        {
            holder.profilePic.setImageResource(R.mipmap.ic_launcher); // insert default photo
        }
    }

    void editUser(User u)
    {
        activity.edit(u);
    }

    void deleteUser(User u)
    {
        activity.delete(u);
    }
}
