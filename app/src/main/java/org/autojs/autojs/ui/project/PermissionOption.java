package org.autojs.autojs.ui.project;

public class PermissionOption {
    private final String permission;
    private final String permissionDesc;
    private boolean isSelected;

    public PermissionOption(String permission, String permissionDesc, boolean isSelected) {
        this.permission = permission;
        this.permissionDesc = permissionDesc;
        this.isSelected = isSelected;
    }

    public String getPermission() {
        return permission;
    }
    public String getPermissionDesc() {
        return permissionDesc;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}