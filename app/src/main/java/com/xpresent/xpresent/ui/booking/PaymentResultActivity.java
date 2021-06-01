package com.xpresent.xpresent.ui.booking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.xpresent.xpresent.MainActivity;
import com.xpresent.xpresent.R;
import com.xpresent.xpresent.ui.catalog.ImpressionsFragment;
import com.xpresent.xpresent.ui.certificates.CertificatesFragment;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        //TextView toolbar = findViewById(R.id.Title);
        //toolbar.setText("Заказ оформлен");
        TextView resultPayment = findViewById(R.id.payment_result);
        Bundle extras = getIntent().getExtras();
        int paymentId = extras.getInt("paymentId");
        int orderId = extras.getInt("orderId");
        String successfulPayment;
        if(paymentId == 1){
            successfulPayment = "Ваш заказ №"+orderId+" принят в обработку. В ближайшее время мы свяжемся с вами.";
        }
        else{
            successfulPayment = "Ваш заказ №"+orderId+" успешно оплачен. ";
        }

        resultPayment.setText(successfulPayment);

        MaterialButton myCertificates = findViewById(R.id.btnMyCertificates);
        myCertificates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
