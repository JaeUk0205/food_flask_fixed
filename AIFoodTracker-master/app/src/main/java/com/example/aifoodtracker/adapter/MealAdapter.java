package com.example.aifoodtracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aifoodtracker.R;
import java.util.ArrayList;

// RecyclerView 어댑터는 항상 RecyclerView.Adapter를 상속받아 만듭니다.
public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private Context context;
    private ArrayList<String> mealList; // 데이터 목록 (지금은 간단히 문자열 리스트)

    // 생성자: MainActivity에서 데이터 목록을 받아옵니다.
    public MealAdapter(Context context, ArrayList<String> mealList) {
        this.context = context;
        this.mealList = mealList;
    }

    // 1. ViewHolder 만들기 (아이템 한 칸을 새로 만들어야 할 때 호출됨)
    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_meal.xml 디자인을 가져와서 실제 View 객체로 만듭니다.
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    // 2. ViewHolder에 데이터 채우기 (만들어진 아이템 칸에 데이터를 표시할 때 호출됨)
    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        // mealList에서 position에 해당하는 데이터를 가져옵니다. (예: "식사 1")
        String mealTitle = mealList.get(position);
        // ViewHolder의 TextView에 데이터를 설정합니다.
        holder.tvMealTitle.setText(mealTitle);
    }

    // 3. 전체 아이템 개수 알려주기
    @Override
    public int getItemCount() {
        return mealList.size();
    }


    // ViewHolder 클래스: 아이템 한 칸의 View들을 보관하는 상자
    public static class MealViewHolder extends RecyclerView.ViewHolder {
        // item_meal.xml에 있는 UI 요소들을 변수로 선언
        public TextView tvMealTitle;
        public Button btnMealDetail;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            // 변수와 실제 UI 요소를 ID로 연결
            tvMealTitle = itemView.findViewById(R.id.tv_meal_title);
            btnMealDetail = itemView.findViewById(R.id.btn_meal_detail);
        }
    }
}