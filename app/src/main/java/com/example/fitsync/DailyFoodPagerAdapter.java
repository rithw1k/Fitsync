package com.example.fitsync;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DailyFoodPagerAdapter extends FragmentStateAdapter {
    public DailyFoodPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new AddFoodFragment();
        } else {
            return new FoodHistoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}