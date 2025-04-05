package com.example.fitsync;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProductsPagerAdapter extends FragmentStateAdapter {
    public ProductsPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new BrowseFragment();
        } else {
            return new CartFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}