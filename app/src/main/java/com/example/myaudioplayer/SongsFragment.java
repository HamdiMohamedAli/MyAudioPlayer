package com.example.myaudioplayer;

import static com.example.myaudioplayer.MainActivity.musicFiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongsFragment extends Fragment {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;

    public SongsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_songs,
                container,
                false
        );

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        RecyclerView.VERTICAL,
                        false
                )
        );

        if (musicFiles != null && !musicFiles.isEmpty()) {

            musicAdapter = new MusicAdapter(
                    requireContext(),
                    musicFiles
            );

            recyclerView.setAdapter(musicAdapter);
        }

        return view;
    }
}