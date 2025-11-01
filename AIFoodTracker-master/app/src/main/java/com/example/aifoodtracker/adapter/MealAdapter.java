package com.example.aifoodtracker.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // ⭐️ 이 import는 사용하지 않으므로 삭제해도 됨
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
// import androidx.activity.result.ActivityResultLauncher; // ⭐️ 수정 기능 제거
// import android.content.Intent; // ⭐️ 수정 기능 제거
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.aifoodtracker.R;
// import com.example.aifoodtracker.EditFoodActivity; // ⭐️ 수정 기능 제거
import com.example.aifoodtracker.domain.FoodEntry;
import java.util.ArrayList;

// ⭐️ '수정 기능'이 제거된 단순 버전의 어댑터
public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<FoodEntry> mealList;
    // private final ActivityResultLauncher<Intent> editFoodLauncher; // ⭐️ 수정 기능 제거

    // ⭐️ 생성자: (Context, ArrayList) 2개만 받도록 수정
    public MealAdapter(Context context, ArrayList<FoodEntry> mealList) {
        this.context = context;
        this.mealList = mealList;
        // this.editFoodLauncher = editFoodLauncher; // ⭐️ 수정 기능 제거
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

        holder.tvFoodName.setText(entry.getFoodName());
        holder.tvTimestamp.setText(entry.getFormattedTime());
        holder.tvNutritionSummary.setText(entry.getNutritionSummary());

        if (entry.getImageUri() != null && !entry.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(entry.getImageUri()))
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivFoodImage);
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // ⭐️ 수정 버튼 클릭 리스너 제거 ⭐️
        /*
        holder.btn_edit_meal.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditFoodActivity.class);
            intent.putExtra("food_entry", entry);
            intent.putExtra("food_entry_position", holder.getAdapterPosition());
            editFoodLauncher.launch(intent);
        });
        */
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName;
        TextView tvTimestamp;
        TextView tvNutritionSummary;
        // ImageButton btn_edit_meal; // ⭐️ 수정 버튼 제거

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.iv_food_image);
            tvFoodName = itemView.findViewById(R.id.tv_food_name);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvNutritionSummary = itemView.findViewById(R.id.tv_nutrition_summary);
            // btn_edit_meal = itemView.findViewById(R.id.btn_edit_meal); // ⭐️ 수정 버튼 제거
        }
    }
}

