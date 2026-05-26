package com.example.myaudioplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;

    // Avoid static list — use ViewModel in a larger app
    public static ArrayList<MusicFiles> musicFiles = new ArrayList<>();

    static boolean shuffleBoolean = false, repeatBoolean = false;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private static final String TAG = "MainActivity";

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

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);

        checkPermission();
    }

    // Re-check when user returns from Settings
    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermission()) {
            loadAudio();
        }
    }

    private boolean hasPermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermission() {
        if (!hasPermission()) {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_AUDIO
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
        }
        // loadAudio() is called from onResume() so no else branch needed
    }

    private void loadAudio() {
        musicFiles = getAllAudio(this);
        setupViewPager();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                // onResume will handle loading
            } else {
                // Check if "never ask again" was selected
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        this, permissions[0]);

                if (!showRationale) {
                    // User selected "Don't ask again" — guide them to Settings
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Required")
                            .setMessage("Audio permission is required to play music. "
                                    + "Please enable it in App Settings.")
                            .setPositiveButton("Open Settings", (dialog, which) -> {
                                Intent intent = new Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) ->
                                    Toast.makeText(this, "Permission Denied!",
                                            Toast.LENGTH_LONG).show())
                            .show();
                } else {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // ---------------- VIEWPAGER ----------------
    private void setupViewPager() {
        // Avoid re-attaching if already set up
        if (viewPager.getAdapter() != null) return;

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Songs" : "Albums")
        ).attach();
    }

    // ---------------- ADAPTER ----------------
    public static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Let FragmentStateAdapter manage fragment lifecycle — don't cache instances
            return position == 0 ? new SongsFragment() : new AlbumFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    // ---------------- GET AUDIO ----------------
    public static ArrayList<MusicFiles> getAllAudio(Context context) {

        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST
        };

        // Use try-with-resources to guarantee cursor is always closed
        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                null,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        )) {
            if (cursor != null) {
                int idCol       = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int albumCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int titleCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int artistCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);

                while (cursor.moveToNext()) {
                    long   id       = cursor.getLong(idCol);
                    String album    = cursor.getString(albumCol);
                    String title    = cursor.getString(titleCol);
                    long   duration = cursor.getLong(durationCol);
                    String artist   = cursor.getString(artistCol);

                    // Build a proper content URI instead of using the raw file path
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                    // Converted 'id' to String via String.valueOf() to feed the constructor safely
                    MusicFiles music = new MusicFiles(contentUri.toString(), title, artist, album, duration, String.valueOf(id));

                    Log.d(TAG, "Audio: " + title + " | " + contentUri);
                    tempAudioList.add(music);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying audio files", e);
        }

        return tempAudioList;
    }
}