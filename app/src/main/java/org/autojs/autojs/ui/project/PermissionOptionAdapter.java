package org.autojs.autojs.ui.project;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.autojs.autojs.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PermissionOptionAdapter extends RecyclerView.Adapter<PermissionOptionAdapter.BuildPermissionOptionViewHolder> {
    private static final String TAG = "PermissionOptionAdapter";

    private final List<PermissionOption> options;

    public PermissionOptionAdapter(List<PermissionOption> options) {
        this.options = options;
        Log.d(TAG, "PermissionOptionAdapter: options size: " + this.options.size());
    }

    @NonNull
    @Override
    public BuildPermissionOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_permission_option, parent, false);
        return new BuildPermissionOptionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildPermissionOptionViewHolder holder, int position) {
        PermissionOption option = options.get(position);

        holder.checkBox.setChecked(option.isSelected());
//        holder.permissionText.setText(option.getPermission().split("permission.")[1]);
        holder.permissionText.setText(option.getPermission());
        holder.permissionDescText.setText(option.getPermissionDesc());

        holder.itemView.setOnClickListener(v -> {
            option.setSelected(!option.isSelected());
            holder.checkBox.setChecked(option.isSelected());
            notifyItemChanged(position); // 刷新当前项
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: current: " + options.size());
        return options.size();
    }

    // 根据item总数计算并设置RecyclerView的总高度
    public void calculateAndSetRecyclerViewHeight(RecyclerView recyclerView) {
        int totalHeight = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View itemView = onCreateViewHolder(recyclerView, getItemViewType(i)).itemView;
            itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += itemView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = totalHeight + (recyclerView.getPaddingTop() + recyclerView.getPaddingBottom());
        recyclerView.setLayoutParams(params);
    }

    public static class BuildPermissionOptionViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;
        private final TextView permissionText;
        private final TextView permissionDescText;

        BuildPermissionOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            permissionText = itemView.findViewById(R.id.permission_text);
            permissionDescText = itemView.findViewById(R.id.permission_desc_text);
        }
    }
}
