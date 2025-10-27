package com.example.aifoodtracker.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

// FragmentStatePagerAdapter -> FragmentStateAdapter로 변경
public class ReportPagerAdapter extends FragmentStateAdapter {

    // Fragment를 담는 리스트는 동일
    private final ArrayList<Fragment> items = new ArrayList<>();

    // 생성자 방식이 약간 다름 (FragmentManager 대신 FragmentActivity를 받음)
    public ReportPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Fragment를 추가하는 메서드
    public void addItem(Fragment item) {
        items.add(item);
    }

    // getItem -> createFragment로 이름 변경 및 역할 동일
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return items.get(position);
    }

    // getCount -> getItemCount로 이름 변경 및 역할 동일
    @Override
    public int getItemCount() {
        return items.size();
    }
}