package com.example.aifoodtracker.adapter;

import android.content.Context;
import android.content.Intent; // Intent import 추가
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // ImageButton import 추가
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher; // Launcher import 추가
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.aifoodtracker.EditFoodActivity; // EditFoodActivity import 추가
import com.example.aifoodtracker.R;
import com.example.aifoodtracker.domain.FoodEntry;
import java.util.ArrayList;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<FoodEntry> mealList;
    private final ActivityResultLauncher<Intent> editFoodLauncher; // ⭐️ Launcher 멤버 변수 추가

    // ⭐️ 생성자 수정: ActivityResultLauncher를 받도록 변경
    public MealAdapter(Context context, ArrayList<FoodEntry> mealList, ActivityResultLauncher<Intent> editFoodLauncher) {
        this.context = context;
        this.mealList = mealList;
        this.editFoodLauncher = editFoodLauncher; // 전달받은 Launcher 저장
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodEntry entry = mealList.get(position);

        // 데이터 설정 (기존과 동일)
        holder.tvFoodName.setText(entry.getFoodName());
        holder.tvTimestamp.setText(entry.getFormattedTime());
        holder.tvNutritionSummary.setText(entry.getNutritionSummary());

        // 이미지 로딩 (기존과 동일)
        if (entry.getImageUri() != null && !entry.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(entry.getImageUri()))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivFoodImage);
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // ⭐️ 수정 버튼 클릭 리스너 설정 ⭐️
        holder.btnEditMeal.setOnClickListener(v -> {
            // EditFoodActivity를 시작할 Intent 생성
            Intent intent = new Intent(context, EditFoodActivity.class);
            // 수정할 FoodEntry 객체와 리스트에서의 위치(position)를 Intent에 담아 전달
            intent.putExtra("food_entry_to_edit", entry);
            intent.putExtra("food_entry_position", holder.getAdapterPosition()); // 현재 아이템의 정확한 위치

            // ⭐️ MainActivity로부터 전달받은 Launcher를 사용해서 Activity 시작 ⭐️
            editFoodLauncher.launch(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    // ⭐️ ViewHolder 수정: btnEditMeal 추가 ⭐️
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName;
        TextView tvTimestamp;
        TextView tvNutritionSummary;
        ImageButton btnEditMeal; // 수정 버튼 변수 추가

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.iv_food_image);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvNutritionSummary = itemView.findViewById(R.id.tv_nutrition_summary);
            btnEditMeal = itemView.findViewById(R.id.btn_edit_meal); // 수정 버튼 ID 연결
        }
    }
}

