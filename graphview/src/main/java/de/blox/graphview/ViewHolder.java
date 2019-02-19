package de.blox.graphview;

import android.support.annotation.NonNull;
import android.view.View;

public abstract class ViewHolder {
    public final View itemView;

    public ViewHolder(@NonNull View itemView) {
        if (itemView == null) {
            throw new IllegalArgumentException("itemView may not be null");
        }
        this.itemView = itemView;
    }
}