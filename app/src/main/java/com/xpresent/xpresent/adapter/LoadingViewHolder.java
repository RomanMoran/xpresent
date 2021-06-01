/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 25.05.20 10:01
 *
 */
package com.xpresent.xpresent.adapter;

import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xpresent.xpresent.R;

class LoadingViewHolder extends RecyclerView.ViewHolder{
    ProgressBar progressBar;
    LoadingViewHolder(@NonNull View itemView) {
        super(itemView);
        progressBar = itemView.findViewById(R.id.ItemLoadingProgress);
    }
}