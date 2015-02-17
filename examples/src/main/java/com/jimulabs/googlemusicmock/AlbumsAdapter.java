package com.jimulabs.googlemusicmock;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by lintonye on 15-02-16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumVH> {
    private final List<Album> mAlbums;

    public AlbumsAdapter(List<Album> albums) {
        mAlbums = Collections.unmodifiableList(albums);
    }

    @Override
    public AlbumVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlbumVH(parent);
    }

    @Override
    public void onBindViewHolder(AlbumVH holder, int position) {
        Album album = mAlbums.get(position);
        Context context = holder.albumArt.getContext();
        Picasso.with(context).load(album.albumArtUrl).into(holder.albumArt);
    }

    @Override
    public int getItemCount() {
        return mAlbums.size();
    }

    static class AlbumVH extends RecyclerView.ViewHolder {
        @InjectView(R.id.album_art)
        ImageView albumArt;
        public AlbumVH(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
