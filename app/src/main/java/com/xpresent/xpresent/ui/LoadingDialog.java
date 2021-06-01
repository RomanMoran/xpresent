/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 16.06.20 12:34
 */

package com.xpresent.xpresent.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.xpresent.xpresent.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context, boolean dark) {
        super(context);
        int theme = dark ? R.layout.dialog_loading_black : R.layout.dialog_loading;
        View view = LayoutInflater.from(context).inflate(theme, null);
        setTitle(null);
        setCancelable(true);
        setOnCancelListener(null);
        setContentView(view);
    }
}