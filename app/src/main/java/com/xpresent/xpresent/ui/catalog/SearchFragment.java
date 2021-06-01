/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 22.06.20
 */

package com.xpresent.xpresent.ui.catalog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xpresent.xpresent.R;


public class SearchFragment extends Fragment{
    private Context Activity;
    private EditText searchET;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        Activity = this.getActivity();
        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        RelativeLayout searchBtn = view.findViewById(R.id.searchBlock);
        // set focus on search edit text
        searchET = view.findViewById(R.id.etSearch);
        searchET.requestFocus();

        // press enter on keyboard
        searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( (actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN ))){
                    search();
                }
                return true;
            }
        });

        // search button
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        return view;
    }

    public void search(){
        String search = searchET.getText().toString();
        if(!search.equals("") && search.length() > 2) {
            String title = getResources().getString(R.string.title_search)+": "+search;
            ImpressionsFragment impressionsFragment = new ImpressionsFragment(0, title, search);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment, impressionsFragment).addToBackStack(null);
            fragmentTransaction.commit();
        }
        else{
            String enter_search = getResources().getString(R.string.enter_search);
            Toast.makeText(Activity, enter_search, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // show keyboard
        searchET.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(searchET, 0);
            }
        },200);
    }
}