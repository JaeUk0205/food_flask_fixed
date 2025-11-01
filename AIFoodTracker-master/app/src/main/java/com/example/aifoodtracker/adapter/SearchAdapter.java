package com.example.aifoodtracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aifoodtracker.R;
import com.example.aifoodtracker.domain.FoodResponse;
import com.example.aifoodtracker.domain.NutritionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final Context context;
    private List<FoodResponse> searchResults;
    private OnItemClickListener listener; // ⭐️ 아이템 클릭 리스너 인터페이스

    // ⭐️ 아이템 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(FoodResponse foodResponse);
    }

    // ⭐️ 생성자에 리스너 추가
    public SearchAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.searchResults = new ArrayList<>();
        this.listener = listener;
    }

    // ⭐️ 검색 결과를 업데이트하는 메소드
    public void updateResults(List<FoodResponse> newResults) {
        this.searchResults.clear();
        if (newResults != null) {
            this.searchResults.addAll(newResults);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodResponse entry = searchResults.get(position);
        holder.bind(entry, listener); // ⭐️ 리스너와 함께 데이터 바인딩
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    // ViewHolder 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName;
        TextView tvNutritionSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tv_search_food_name);
            tvNutritionSummary = itemView.findViewById(R.id.tv_search_nutrition_summary);
        }

        // ⭐️ 데이터 바인딩 및 클릭 리스너 설정
        public void bind(final FoodResponse foodResponse, final OnItemClickListener listener) {
            tvFoodName.setText(foodResponse.getFoodName());

            NutritionInfo nutrition = foodResponse.getNutritionInfo();
            if (nutrition != null) {
                String summary = String.format(Locale.KOREA, "칼로리: %.1f kcal, 탄: %.1fg, 단: %.1fg, 지: %.1fg",
                        nutrition.getCalories(),
                        nutrition.getCarbohydrate(),
                        nutrition.getProtein(),
                        nutrition.getFat());
                tvNutritionSummary.setText(summary);
            } else {
                tvNutritionSummary.setText("영양 정보 없음");
            }

            // ⭐️ 항목 클릭 시 리스너 호출
            itemView.setOnClickListener(v -> listener.onItemClick(foodResponse));
        }
    }
}
