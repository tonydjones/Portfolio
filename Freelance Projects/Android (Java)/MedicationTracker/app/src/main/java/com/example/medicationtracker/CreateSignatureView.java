package com.example.medicationtracker;

import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import static com.example.medicationtracker.MainActivity.signature_size;
import static com.example.medicationtracker.MainActivity.unweighted_params;
import static com.example.medicationtracker.MainActivity.weighted_params;

public class CreateSignatureView {

    public static SignatureView create_signature_view(MainActivity Activity){
        ScrollView scroll = Activity.scroll;
        SignatureView signature = new SignatureView(Activity, null);
        signature.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!scroll.getLayoutParams().equals(unweighted_params) && signature.getHeight() > signature_size){
                    scroll.setLayoutParams(unweighted_params);
                }
                else if (!scroll.getLayoutParams().equals(weighted_params) && signature.getHeight() < signature_size){
                    scroll.setLayoutParams(weighted_params);
                }
                signature.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, signature_size));
                signature.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        if (scroll.getLayoutParams().equals(weighted_params)){
            signature.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, signature_size, 1));
        }

        return signature;
    }
}
