package com.chenyou.localvideoplayer.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chenyou.localvideoplayer.pager.BasePager;

public class ReplaceFragment extends Fragment {
    private BasePager mBasePager;

    public ReplaceFragment(BasePager basePager) {
        mBasePager = basePager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mBasePager.rootView;
    }
}
