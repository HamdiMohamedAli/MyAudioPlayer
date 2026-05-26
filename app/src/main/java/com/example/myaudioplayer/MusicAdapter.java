package com.example.myaudioplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private final Context mContext;
    private final ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {

        MusicFiles file = mFiles.get(position);

        holder.file_name.setText(file.getTitle());

        // FIXED: Now safely fetches modern Bitmaps from content URIs
        Bitmap imageBitmap = getAlbumArt(file.getPath());

        if (imageBitmap != null) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(imageBitmap)
                    .into(holder.album_art);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.test)
                    .into(holder.album_art);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, PlayerActivity.class);
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        });

        holder.menuMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(mContext, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.delete) {
                    deleteFile(position, v);
                    return true;
                }
                return false;
            });
        });
    }

    private void deleteFile(int position, View v) {
        Uri contentUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId())
        );

        try {
            int rowsDeleted = mContext.getContentResolver().delete(contentUri, null, null);

            if (rowsDeleted > 0) {
                mFiles.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mFiles.size());
                Snackbar.make(v, "File Deleted!", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(v, "File can't be deleted!", Snackbar.LENGTH_LONG).show();
            }
        } catch (SecurityException securityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (securityException instanceof RecoverableSecurityException) {
                    RecoverableSecurityException recoverableSecurityException =
                            (RecoverableSecurityException) securityException;
                    IntentSender intentSender = recoverableSecurityException
                            .getUserAction()
                            .getActionIntent()
                            .getIntentSender();
                    try {
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).startIntentSenderForResult(
                                    intentSender, 123, null, 0, 0, 0
                            );
                        }
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Snackbar.make(v, "Permission Denied! File can't be deleted.", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFiles != null ? mFiles.size() : 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView file_name;
        ImageView album_art, menuMore;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }

    // FIXED: High performance extraction using ContentResolver thumbnail hooks
    private Bitmap getAlbumArt(String uriString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Uri trackUri = Uri.parse(uriString);
                return mContext.getContentResolver().loadThumbnail(
                        trackUri,
                        new Size(200, 200),
                        null
                );
            } catch (Exception e) {
                return null;
            }
        } else {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(mContext, Uri.parse(uriString));
                byte[] art = retriever.getEmbeddedPicture();
                if (art != null) {
                    return BitmapFactory.decodeByteArray(art, 0, art.length);
                }
            } catch (Exception ignored) {
            } finally {
                try { retriever.release(); } catch (Exception ignored) {}
            }
            return null;
        }
    }
}