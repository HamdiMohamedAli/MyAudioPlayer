package com.example.myaudioplayer;

import static com.example.myaudioplayer.MainActivity.musicFiles;
import static com.example.myaudioplayer.MainActivity.repeatBoolean;
import static com.example.myaudioplayer.MainActivity.shuffleBoolean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;

    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.mContainer),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        initViews();
        getIntentMethod();

        playPauseBtn.setOnClickListener(v -> playPauseBtnClicked());
        nextBtn.setOnClickListener(v -> nextBtnClicked());
        prevBtn.setOnClickListener(v -> prevBtnClicked());
        backBtn.setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    duration_played.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        };

        shuffleBtn.setOnClickListener(v -> {
            if (shuffleBoolean) {
                shuffleBoolean = false;
                shuffleBtn.setImageResource(R.drawable.shuffle_off);
            } else {
                shuffleBoolean = true;
                shuffleBtn.setImageResource(R.drawable.shuffle_on);
            }
        });

        repeatBtn.setOnClickListener(v -> {
            if (repeatBoolean) {
                repeatBoolean = false;
                repeatBtn.setImageResource(R.drawable.repeat_off);
            } else {
                repeatBoolean = true;
                repeatBtn.setImageResource(R.drawable.repeat_on);
            }
        });
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        listSongs = musicFiles;

        if (listSongs != null && position != -1) {
            uri = Uri.parse(listSongs.get(position).getPath());
            playMedia(uri);
        }
    }

    private void playMedia(Uri trackUri) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                playPauseBtn.setImageResource(R.drawable.pause);
                mp.start();
                mp.setOnCompletionListener(PlayerActivity.this);

                song_name.setText(listSongs.get(position).getTitle());
                artist_name.setText(listSongs.get(position).getArtist());
                seekBar.setMax(mp.getDuration() / 1000);
                metaData(trackUri);

                handler.removeCallbacks(updateSeekBarRunnable);
                handler.post(updateSeekBarRunnable);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(PlayerActivity.this, "Playback Error", Toast.LENGTH_SHORT).show();
                mp.reset();
                return true;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not readable", Toast.LENGTH_SHORT).show();
        }
    }

    private void playPauseBtnClicked() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.play);
                mediaPlayer.pause();
            } else {
                playPauseBtn.setImageResource(R.drawable.pause);
                mediaPlayer.start();
                handler.post(updateSeekBarRunnable);
            }
        }
    }

    private void nextBtnClicked() {
        if (listSongs != null && listSongs.size() > 0) {
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = (position + 1) % listSongs.size();
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            playMedia(uri);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void prevBtnClicked() {
        if (listSongs != null && listSongs.size() > 0) {
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = (position - 1) < 0 ? listSongs.size() - 1 : (position - 1);
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            playMedia(uri);
        }
    }

    private void metaData(Uri trackUri) {
        // FIXED: Extracted via Scoped Storage compatible routines first
        Bitmap bitmap = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                bitmap = getContentResolver().loadThumbnail(trackUri, new Size(600, 600), null);
            } catch (Exception ignored) {}
        }

        // Standard legacy extraction fallback
        if (bitmap == null) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(getApplicationContext(), trackUri);
                byte[] art = retriever.getEmbeddedPicture();
                if (art != null) {
                    bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                }
                retriever.release();
            } catch (Exception ignored) {}
        }

        // FIXED: Safe up-cast avoids constraint type runtime crashes
        View gradient = findViewById(R.id.image_view_gradient);
        View mContainer = findViewById(R.id.mContainer);

        if (bitmap != null) {
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(palette -> {
                Palette.Swatch swatch = (palette != null) ? palette.getDominantSwatch() : null;
                if (swatch != null) {
                    GradientDrawable gradientDrawable = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{swatch.getRgb(), 0x00000000});
                    gradient.setBackground(gradientDrawable);

                    GradientDrawable gradientDrawableBg = new GradientDrawable(
                            GradientDrawable.Orientation.BOTTOM_TOP,
                            new int[]{swatch.getRgb(), swatch.getRgb()});
                    mContainer.setBackground(gradientDrawableBg);

                    song_name.setTextColor(swatch.getTitleTextColor());
                    artist_name.setTextColor(swatch.getTitleTextColor());
                } else {
                    setDefaultGradients(gradient, mContainer);
                }
            });
        } else {
            Glide.with(this).load(R.drawable.test).into(cover_art);
            setDefaultGradients(gradient, mContainer);
        }
    }

    private void setDefaultGradients(View gradient, View mContainer) {
        gradient.setBackgroundResource(R.drawable.gradient_bg);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xff121212, 0x00000000});
        gradient.setBackground(gradientDrawable);

        GradientDrawable gradientDrawableBg = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{0xff121212, 0xff121212});
        mContainer.setBackground(gradientDrawableBg);

        song_name.setTextColor(Color.WHITE);
        artist_name.setTextColor(Color.LTGRAY);
    }

    private String formattedTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds);
    }

    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause_btn);
        seekBar = findViewById(R.id.seekBar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            handler.post(updateSeekBarRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                imageView.startAnimation(animIn);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationStart(Animation animation) {}
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
    }
}