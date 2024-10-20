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

/**
 * option选项适配器，用于在RecyclerView中添加单选选项
 * Powered by ChatGPT3.5
 */
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
        Log.d(TAG, "onBindViewHolder position: " + position);
        PermissionOption option = options.get(position);
        holder.bind(option);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: current: " + options.size());
        return options.size();
    }

    public static class BuildPermissionOptionViewHolder extends RecyclerView.ViewHolder {

        private static final String TAG = "BuildPermissionOptionViewHolder";
        private final CheckBox checkBox;
        private final TextView permissionText;
        private final TextView permissionDescText;

        BuildPermissionOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            permissionText = itemView.findViewById(R.id.permission_text);
            permissionDescText = itemView.findViewById(R.id.permission_desc_text);
        }

        void bind(final PermissionOption option) {
            Log.d(TAG, "bind option: " + option.getPermission());
            checkBox.setChecked(option.isSelected());
            permissionText.setText(option.getPermission().split("permission.")[1]);
            permissionDescText.setText(option.getPermissionDesc());

            itemView.setOnClickListener(view -> {
                option.setSelected(!option.isSelected());
                checkBox.setChecked(option.isSelected());
            });
        }
    }

}
