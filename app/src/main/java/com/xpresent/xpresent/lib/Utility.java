package com.xpresent.xpresent.lib;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.xpresent.xpresent.R;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 28.05.20 13:54
 */
public class Utility {
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static boolean isPhoneValid(String phone) {
        final String PHONE_VERIFICATION = "^[0-9\\-]{6,10}$";
        if(phone.length() < 10) return false;
        return phone.matches(PHONE_VERIFICATION);
    }

    public static String formatDate(String date, String fromFormat, String toFormat){
        try {
            String pattern = (fromFormat.equals("")) ? "yyyy-MM-dd hh:mm:ss" : fromFormat;
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            Date newDate = format.parse(date);
            String patternTo = (toFormat.equals("")) ? "dd MMM, yyyy" : toFormat;
            format = new SimpleDateFormat(patternTo);
            return format.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sha512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getTagColor(String tagColor){
        int color;
        switch (tagColor){
            case "29b7ea":
                color = R.color.colorTagBlue;
                break;
            case "fbad18":
                color = R.color.colorTagYellow;
                break;
            case "8e24aa":
                color = R.color.colorTagViolet;
                break;
            case "000000":
                color = R.color.colorTagBlack;
                break;
            default:
            case "ed1c24":
                color = R.color.colorPrimary;
                break;
        }
        return color;
    }

}

