package com.xpresent.xpresent.ui.certificates;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.xpresent.xpresent.R;
import com.xpresent.xpresent.lib.Utility;
import com.xpresent.xpresent.model.Item;

import com.google.zxing.WriterException;
import com.xpresent.xpresent.requests.ServerConnector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidmads.library.qrgenearator.QRGEncoder;

public class CertificateItemFragment extends Fragment{
    private Context Activity;
    private View view;
    private String sessionKey;
    private Item item;

    CertificateItemFragment(Item item){
        this.item = item;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Activity = getActivity();
        view = inflater.inflate(R.layout.fragment_certificate_item, container, false);
        TextView toolbarTitle = view.findViewById(R.id.Title);
        String impName = item.getParam("impression_name");
        toolbarTitle.setText(impName);
        TextView impressionName = view.findViewById(R.id.impressionOfferName);
        impressionName.setText(impName);

        ImageView btnBack = view.findViewById(R.id.backBtn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        // get app key from storage
        SharedPreferences settings = Activity.getSharedPreferences("xp_client", Context.MODE_PRIVATE);
        sessionKey = settings.getString("sessionKey", "");
        showCertificate();
        return view;
    }

    private void showCertificate(){
        TextView certNumberTxt = view.findViewById(R.id.certNumber);
        TextView certCodeTxt = view.findViewById(R.id.certCode);
        TextView datetimeTxt = view.findViewById(R.id.datetime);
        TextView humanNameTxt = view.findViewById(R.id.human_name);
        TextView durationNameTxt = view.findViewById(R.id.duration_name);
        certNumberTxt.setText(item.getParam("cert_number"));
        certCodeTxt.setText(item.getParam("secret_code"));
        String serviceDate = Utility.formatDate(item.getParam("closed_date_redeemed"), "yyyy-MM-dd hh:mm", "hh:mm dd MMM, yyyy");
        datetimeTxt.setText(serviceDate);
        humanNameTxt.setText(item.getParam("human_name"));
        durationNameTxt.setText(item.getParam("duration_name"));
        QRGEncoder qrgEncoder = new QRGEncoder(item.getParam("cert_id"), null, "TEXT_TYPE", 1000);

        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.encodeAsBitmap();
            ImageView sertQR = view.findViewById(R.id.certQR);
            // Setting Bitmap to ImageView
            sertQR.setImageBitmap(bitmap);
            int certClosed = Integer.parseInt(item.getParam("closed"));
            String partnerId = item.getParam("partner_id");
            String offerId = item.getParam("offer_id");
            // If certificate was activated
            if(certClosed == 1){
                getPartnerById(partnerId, offerId);
            }
        } catch (WriterException e) {
            Log.v("qr_error", e.toString());
        }

    }

    private void getPartnerById(String partnerId, String offerId){
        HashMap<String,String> mapPost = new HashMap<>();
        mapPost.put("action", "getpartner");
        mapPost.put("session_key", sessionKey);
        mapPost.put("partner_id", partnerId);
        mapPost.put("offer_id", offerId);

        ServerConnector Connector = new ServerConnector(Activity, new ServerConnector.AsyncResponse() {
            // here we get result output from POST request
            @Override
            public void processFinish(boolean success, String output) {
                if(success)
                    showPartnerInfo(output);
                else Toast.makeText(Activity, output, Toast.LENGTH_LONG).show();
            }
        }, false);
        Connector.execute(mapPost);
    }

    private void showPartnerInfo(String output){
        try {
            JSONObject jsonResult = new JSONObject(output);
            boolean status = jsonResult.getBoolean("status");
            if (status) {
                JSONObject partner = jsonResult.getJSONObject("partner");
                item.setParam("partner_id", partner.getString("partner_id"));
                item.setParam("name", partner.getString("name"));
                item.setParam("email", partner.getString("email"));
                item.setParam("phone", partner.getString("phone"));
                item.setParam("activation_phone", partner.getString("activation_phone"));
                item.setParam("address_holding", partner.getString("address_holding"));
                item.setParam("send_request_client", partner.getString("send_request_client"));
                item.setParam("send_request_text_client", partner.getString("send_request_text_client"));
                item.setParam("where", partner.getString("where"));
                item.setParam("constraints", partner.getString("constraints"));

                // Partner info
                if(!item.getParam("partner_id").equals("")) {
                    TextView partnerName = view.findViewById(R.id.partnerName);
                    TextView partnerPhone = view.findViewById(R.id.partnerPhone);
                    TextView partnerEmail = view.findViewById(R.id.partnerEmail);
                    String partnerTitle = getResources().getString(R.string.organizer) + " «" + item.getParam("name")+"»";
                    partnerName.setText(partnerTitle);
                    String partnPhone = item.getParam("activation_phone").equals("") ? item.getParam("phone") : item.getParam("activation_phone");
                    partnerPhone.setText(partnPhone);
                    partnerEmail.setText(item.getParam("email"));
                }
                else{
                    LinearLayout partner_block = view.findViewById(R.id.partner_block);
                    partner_block.setVisibility(View.GONE);
                }
                String mimeType = "text/html";
                String encoding = "base64";
                // HTML impression address
                String impAddress = item.getParam("where");
                if(!impAddress.equals("")){
                    WebView wvImpAddress = view.findViewById(R.id.webViewImpressionAddress);
                    WebSettings AddressSettings = wvImpAddress.getSettings();
                    AddressSettings.setJavaScriptEnabled(true);
                    String addressText = Html.fromHtml(impAddress).toString();
                    String addressHtmlText = addressText.replaceAll("height=\\d{3}", "height=200");
                    String encodedAddressHtmlText = Base64.encodeToString(addressHtmlText.getBytes(), Base64.NO_PADDING);
                    wvImpAddress.loadData(encodedAddressHtmlText, mimeType, encoding);
                }
                else{
                    LinearLayout place_block = view.findViewById(R.id.place_block);
                    place_block.setVisibility(View.GONE);
                }
                // HTML additional info
                String additionalInfo = item.getParam("constraints");
                if(!additionalInfo.equals("")) {
                    WebView wvAddInfo = view.findViewById(R.id.webViewAdditionalInfo);
                    WebSettings InfoSettings = wvAddInfo.getSettings();
                    InfoSettings.setJavaScriptEnabled(true);
                    String additionalInfoHtml = Html.fromHtml(additionalInfo).toString();
                    String encodedAddInfoHtml = Base64.encodeToString(additionalInfoHtml.getBytes(), Base64.NO_PADDING);
                    wvAddInfo.loadData(encodedAddInfoHtml, mimeType, encoding);
                }
                else{
                    LinearLayout add_info_block = view.findViewById(R.id.add_info_block);
                    add_info_block.setVisibility(View.GONE);
                }
            }
        } catch (
        JSONException e){
            Toast.makeText(Activity, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}