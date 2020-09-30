package com.townofwinchester.a01890;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //list of information, from which the app's pages are procedurally generated.
    public List<Info> information;

    //dictionary connecting pages to sections
    public Map<String, String[]> pages;

    //dictionary connecting sections to image identifiers
    public Map<String, String> sections;

    //dictionary connecting sections to subsections
    public Map<String, String[]> subsections;

    //bool for whether or not we are on the home page
    public boolean home;

    //A view that serves as the base. This is what we will add the buttons to, and the scrollview when appropriate.
    public LinearLayout screen;

    //Some layout params we'll use regularly throughout various functions.
    public LinearLayout.LayoutParams row;
    public LinearLayout.LayoutParams info;
    public LinearLayout.LayoutParams item;

    //The ScrollView
    public ScrollView scroll_parent;

    //The View within the ScrollView
    public LinearLayout scroll;

    //Whether or not the text tip line is active. If it is, change this value to true to reinstate the text tip page instead of the phone tip page
    public boolean textline;
    public String tipnumber;

    //which social media button should be used for this screen, will be calculated in onCreate
    public int socialmediabutton;

    //CSV parser to get data later on
    public CsvParser parser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the base layout and get access to the screen for easy additions and removals
        setContentView(R.layout.activity_main);
        screen = findViewById(R.id.screen);

        //Get screen size to determine which social media button we should use later on
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ratio = (float) metrics.heightPixels / (float) metrics.widthPixels;
        float thin = (float) Math.abs(ratio - (4 / 3.0));
        float normal = (float) Math.abs(ratio - (8 / 5.0));
        float wide = (float) Math.abs(ratio - (16 / 9.0));

        if (thin < normal && thin < wide) {
            socialmediabutton = R.drawable.socialmediav1;
        } else if (wide < normal && wide < thin) {
            socialmediabutton = R.drawable.socialmediav3;
        } else {
            socialmediabutton = R.drawable.socialmediav2;
        }

        //The row layout is the width of the screen, has flexible height, and 10 px margins on all sides
        row = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        row.setMargins(10, 10, 10, 10);

        //The info layout aligns text to the right of the parent view, and is only as wide and high as it needs to be. Small vertical margins.
        info = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        info.gravity = Gravity.RIGHT;
        info.setMargins(0, 3, 0, 3);

        //The item layout is for use on items in horizontal linear layouts. Makes the items only as big as they need to be and gives them 10 px spacing
        item = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        item.setMargins(10, 0, 10, 0);

        //Create scrollview and vertical linearlayout for use in pages. These won't be used in the home page.
        scroll_parent = new ScrollView(this);
        scroll_parent.setLayoutParams(row);
        scroll = new LinearLayout(this);
        scroll.setLayoutParams(row);
        scroll.setOrientation(LinearLayout.VERTICAL);

        //Initialize Info list and populate with all of the information contained in the app, choosing which tip line to add to the list based on whether the text line is active
        information = new ArrayList<>();

        //set up CSV parser and settings
        parser = new CsvParser(new CsvParserSettings());

        //populate information array with Info objects
        try {
            InputStream is = getAssets().open("information.csv");
            List<String[]> data = parser.parseAll(new InputStreamReader(is));
            for (int i = 1; i < data.size(); i++) {
                Info info = new Info();
                info.reference = data.get(i)[0];
                info.number = process(data.get(i)[1]);
                info.text = process(data.get(i)[2]);
                info.url = process(data.get(i)[3]);
                info.email = process(data.get(i)[4]);
                info.page = process(data.get(i)[5]);
                info.section = process(data.get(i)[6]);
                info.description = data.get(i)[7];
                information.add(info);
            }
        } catch (IOException e) {
        }

        //use CSV to determine whether textline is active or not.
        try {
            InputStream is2 = getAssets().open("tipline.csv");
            List<String[]> tipline = parser.parseAll(new InputStreamReader(is2));
            if (tipline.get(1)[0].toUpperCase().equals("CALL")) {
                textline = false;
            } else {
                textline = true;
            }
            tipnumber = tipline.get(1)[1];
        } catch (IOException e) {
            e.printStackTrace();
        }

        //use CSV to determine page sections
        try {
            InputStream is3 = getAssets().open("pages.csv");
            List<String[]> pagetsv = parser.parseAll(new InputStreamReader(is3));
            pages = new HashMap<>();
            for (int i = 1; i < pagetsv.size(); i++) {
                pages.put(pagetsv.get(i)[0], process(pagetsv.get(i)[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //use CSV to determine section subsections
        try {
            InputStream is4 = getAssets().open("sections.csv");
            List<String[]> sectiontsv = parser.parseAll(new InputStreamReader(is4));
            sections = new HashMap<>();
            subsections = new HashMap<>();
            for (int i = 1; i < sectiontsv.size(); i++) {
                sections.put(sectiontsv.get(i)[0], sectiontsv.get(i)[1]);
                subsections.put(sectiontsv.get(i)[0], process(sectiontsv.get(i)[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Boot up the home page.
        boot();
    }

    public void boot() {
        //Remove all views from the screen, and scrollview's child view. This will be the home screen.
        wipe("01890");
        home = true;

        //some layout params used for the home page. The rows will stretch vertically to fill the screen and the buttons will stretch horizontally to fill the rows
        LinearLayout.LayoutParams weighted_row = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        LinearLayout.LayoutParams weighted_button = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        weighted_button.setMargins(15, 15, 15, 15);

        //For each main row, generates a horizontal linearlayout and 2 buttons to go into it.
        LinearLayout row1 = new LinearLayout(this);
        Button tips = new Button(this);
        Button coalition = new Button(this);

        //if tip line is active, the tips button will be a different image and send to a different page.
        if (textline) {
            tips.setBackgroundResource(R.drawable.textbutton);
            tips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    texttips();
                }
            });
        } else {
            tips.setBackgroundResource(R.drawable.callbutton);
            tips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tips();
                }
            });
        }
        coalition.setBackgroundResource(socialmediabutton);
        tips.setLayoutParams(weighted_button);
        coalition.setLayoutParams(weighted_button);
        coalition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coalition();
            }
        });
        row1.setLayoutParams(weighted_row);
        screen.addView(row1);
        row1.addView(tips);
        row1.addView(coalition);

        LinearLayout row2 = new LinearLayout(this);
        Button contacts = new Button(this);
        Button emergency = new Button(this);
        contacts.setBackgroundResource(R.drawable.schools);
        emergency.setBackgroundResource(R.drawable.emergency);
        contacts.setLayoutParams(weighted_button);
        emergency.setLayoutParams(weighted_button);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contacts();
            }
        });
        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergency();
            }
        });
        row2.setLayoutParams(weighted_row);
        screen.addView(row2);
        row2.addView(contacts);
        row2.addView(emergency);

        LinearLayout row3 = new LinearLayout(this);
        Button mental = new Button(this);
        Button community = new Button(this);
        mental.setBackgroundResource(R.drawable.mental);
        community.setBackgroundResource(R.drawable.community);
        mental.setLayoutParams(weighted_button);
        community.setLayoutParams(weighted_button);
        mental.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mental();
            }
        });
        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                community();
            }
        });
        row3.setLayoutParams(weighted_row);
        screen.addView(row3);
        row3.addView(mental);
        row3.addView(community);

        LinearLayout row4 = new LinearLayout(this);
        Button articles = new Button(this);
        Button school = new Button(this);
        articles.setBackgroundResource(R.drawable.articles);
        school.setBackgroundResource(R.drawable.schoolresources);
        articles.setLayoutParams(weighted_button);
        school.setLayoutParams(weighted_button);
        articles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                articles();
            }
        });
        school.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                school();
            }
        });
        row4.setLayoutParams(weighted_row);
        screen.addView(row4);
        row4.addView(articles);
        row4.addView(school);

        LinearLayout row5 = new LinearLayout(this);
        Button search = new Button(this);
        Button about = new Button(this);

        //These buttons are generated with default Android assets, so we have to set the title text and color to match the rest of the app.
        search.setText("Search");
        search.setBackgroundColor(0xFFDCE2F4);
        about.setText("About");
        about.setBackgroundColor(0xFFDCE2F4);
        search.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        about.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        search.setLayoutParams(weighted_button);
        about.setLayoutParams(weighted_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about();
            }
        });

        //This row will be shorter than the others, since it uses the row params, so it will only be high enough to contain its contents.
        row5.setLayoutParams(row);
        screen.addView(row5);
        row5.addView(search);
        row5.addView(about);
    }

    public void tips() {

        //clear the screen and set up scrollView base
        wipe("Anonymous Reporting");
        setbase();

        //The textviews displaying the information are manually generated. I could have written functions to generate these, but, I mean, I already wrote them, you know?
        TextView stop = new TextView(this);
        stop.setText("STOP!");
        stop.setTextColor(0xFFFF0000);
        stop.setMaxLines(1);
        stop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 75);
        stop.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        stop.setLayoutParams(row);
        scroll.addView(stop);

        //clickable textview to call 911
        final TextView no1 = new TextView(this);
        no1.setText("911");
        TextView stop2 = new TextView(this);
        stop2.setTextColor(0xFF000000);
        SpannableString string = new SpannableString("If there is an EMERGENCY, call 911");
        string.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                dial(no1);
            }
        }, 26, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        string.setSpan(new ForegroundColorSpan(0xFFFF0000), 26, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stop2.setText(string);
        stop2.setTextAppearance(this, android.R.style.TextAppearance_Large);
        stop2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        stop2.setLayoutParams(row);
        stop2.setMovementMethod(LinkMovementMethod.getInstance());
        scroll.addView(stop2);

        //ImageView will be 1/3 the width of the screen and centered. The imageview will automatically scale the image down to fit.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ratio = ((float) metrics.widthPixels * 1 / 3) / (float) getResources().getDrawable(R.drawable.telephone).getIntrinsicWidth();
        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams IMG = new LinearLayout.LayoutParams((int) (ratio * getResources().getDrawable(R.drawable.telephone).getIntrinsicWidth()), (int) (ratio * getResources().getDrawable(R.drawable.telephone).getIntrinsicHeight()));
        IMG.gravity = Gravity.CENTER;
        image.setLayoutParams(IMG);
        image.setImageDrawable(getResources().getDrawable(R.drawable.telephone));
        scroll.addView(image);

        TextView otherwise = new TextView(this);
        otherwise.setText("Otherwise...");
        otherwise.setTextColor(0xFF000000);
        otherwise.setTextAppearance(this, android.R.style.TextAppearance_Large);
        otherwise.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        otherwise.setLayoutParams(row);
        otherwise.setLayoutParams(row);
        otherwise.setTypeface(null, Typeface.BOLD);
        scroll.addView(otherwise);

        TextView see = new TextView(this);
        see.setTextColor(0xFF000000);
        SpannableString seeSomething = new SpannableString("If you SEE something...");
        seeSomething.setSpan(new ForegroundColorSpan(0xFFFF0000), 7, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        see.setText(seeSomething);
        see.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        see.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        see.setLayoutParams(row);
        scroll.addView(see);

        TextView say = new TextView(this);
        say.setTextColor(0xFF000000);
        SpannableString saySomething = new SpannableString("SAY something...");
        saySomething.setSpan(new ForegroundColorSpan(0xFFFF0000), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        say.setText(saySomething);
        say.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        say.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        say.setLayoutParams(row);
        scroll.addView(say);

        TextView act = new TextView(this);
        act.setTextColor(0xFF000000);
        SpannableString doSomething = new SpannableString("DO something.");
        doSomething.setSpan(new ForegroundColorSpan(0xFFFF0000), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        act.setText(doSomething);
        act.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        act.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        act.setLayoutParams(row);
        scroll.addView(act);

        TextView report = new TextView(this);
        report.setTextColor(0xFF000000);
        SpannableString anonymous = new SpannableString("Report anonymously to the Winchester Police Department");
        anonymous.setSpan(new ForegroundColorSpan(0xFFFF0000), 7, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        report.setText(anonymous);
        report.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        report.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        report.setLayoutParams(row);
        scroll.addView(report);

        //clickable textview to call the tip line
        final TextView no2 = new TextView(this);
        no2.setText(tipnumber);
        TextView call = new TextView(this);
        call.setTextColor(0xFF000000);
        SpannableString tip = new SpannableString("Call " + tipnumber);
        tip.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                dial(no2);
            }
        }, 5, tip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tip.setSpan(new ForegroundColorSpan(0xFFFF0000), 5, tip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        call.setText(tip);
        call.setTextAppearance(this, android.R.style.TextAppearance_Large);
        call.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        call.setLayoutParams(row);
        call.setMovementMethod(LinkMovementMethod.getInstance());
        scroll.addView(call);
    }

    public void texttips() {

        //clear the screen and set up scrollView base
        wipe("Anonymous Reporting");
        setbase();

        //The textviews displaying the information are manually generated. I could have written functions to generate these, but, I mean, I already wrote them, you know?
        TextView stop = new TextView(this);
        stop.setText("STOP!");
        stop.setTextColor(0xFFFF0000);
        stop.setMaxLines(1);
        stop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 75);
        stop.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        stop.setLayoutParams(row);
        scroll.addView(stop);

        //clickable textview to call 911
        final TextView no1 = new TextView(this);
        no1.setText("911");
        TextView stop2 = new TextView(this);
        stop2.setTextColor(0xFF000000);
        SpannableString string = new SpannableString("If there is an EMERGENCY, call 911");
        string.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                dial(no1);
            }
        }, 26, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        string.setSpan(new ForegroundColorSpan(0xFFFF0000), 26, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stop2.setText(string);
        stop2.setTextAppearance(this, android.R.style.TextAppearance_Large);
        stop2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        stop2.setLayoutParams(row);
        stop2.setMovementMethod(LinkMovementMethod.getInstance());
        scroll.addView(stop2);

        //clickable textview to call WPD
        final TextView no2 = new TextView(this);
        no2.setText("781-729-1212");
        TextView police = new TextView(this);
        police.setTextColor(0xFF000000);
        SpannableString wpd = new SpannableString("Or call 781-729-1212");
        wpd.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                dial(no2);
            }
        }, 3, wpd.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        wpd.setSpan(new ForegroundColorSpan(0xFFFF0000), 3, wpd.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        police.setText(wpd);
        police.setTextAppearance(this, android.R.style.TextAppearance_Large);
        police.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        police.setLayoutParams(row);
        police.setMovementMethod(LinkMovementMethod.getInstance());
        scroll.addView(police);

        //ImageView will be 1/3 the width of the screen and centered. The imageview will automatically scale the image down to fit.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ratio = ((float) metrics.widthPixels * 1 / 3) / (float) getResources().getDrawable(R.drawable.text).getIntrinsicWidth();
        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams IMG = new LinearLayout.LayoutParams((int) (ratio * getResources().getDrawable(R.drawable.text).getIntrinsicWidth()), (int) (ratio * getResources().getDrawable(R.drawable.text).getIntrinsicHeight()));
        IMG.gravity = Gravity.CENTER;
        image.setLayoutParams(IMG);
        image.setImageDrawable(getResources().getDrawable(R.drawable.text));
        scroll.addView(image);

        TextView otherwise = new TextView(this);
        otherwise.setText("Otherwise...");
        otherwise.setTextColor(0xFF000000);
        otherwise.setTextAppearance(this, android.R.style.TextAppearance_Large);
        otherwise.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        otherwise.setLayoutParams(row);
        otherwise.setLayoutParams(row);
        otherwise.setTypeface(null, Typeface.BOLD);
        scroll.addView(otherwise);

        TextView see = new TextView(this);
        see.setTextColor(0xFF000000);
        SpannableString seeSomething = new SpannableString("If you SEE something...");
        seeSomething.setSpan(new ForegroundColorSpan(0xFFFF0000), 7, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        see.setText(seeSomething);
        see.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        see.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        see.setLayoutParams(row);
        scroll.addView(see);

        TextView say = new TextView(this);
        say.setTextColor(0xFF000000);
        SpannableString saySomething = new SpannableString("SAY something...");
        saySomething.setSpan(new ForegroundColorSpan(0xFFFF0000), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        say.setText(saySomething);
        say.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        say.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        say.setLayoutParams(row);
        scroll.addView(say);

        TextView act = new TextView(this);
        act.setTextColor(0xFF000000);
        SpannableString doSomething = new SpannableString("DO something.");
        doSomething.setSpan(new ForegroundColorSpan(0xFFFF0000), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        act.setText(doSomething);
        act.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        act.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        act.setLayoutParams(row);
        scroll.addView(act);

        TextView report = new TextView(this);
        report.setTextColor(0xFF000000);
        SpannableString anonymous = new SpannableString("Report anonymously to the Winchester Police Department");
        anonymous.setSpan(new ForegroundColorSpan(0xFFFF0000), 7, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        report.setText(anonymous);
        report.setTextAppearance(this, android.R.style.TextAppearance_Medium);
        report.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        report.setLayoutParams(row);
        scroll.addView(report);

        //Generates an input field and send button. When the send button is clicked, data will be sent to the autotext() function.
        LinearLayout.LayoutParams input = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2);
        LinearLayout bar = new LinearLayout(this);
        final EditText edit = new EditText(this);
        Button send = new Button(this);
        bar.setLayoutParams(row);
        edit.setLayoutParams(input);
        send.setLayoutParams(item);
        scroll.addView(bar);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autotext(edit);
            }
        });
        send.setText("Send");
        edit.setHint("Type your anonymous report here");
        bar.addView(edit);
        bar.addView(send);

        //some extra information about reporting at the bottom of the page
        TextView winchester = new TextView(this);
        winchester.setText("When not using this app, you can report anonymously by entering the word \"Winchester\" before your message and texting it to " + tipnumber + ", or calling the Winchester Police Department at 781-729-1212");
        winchester.setTextColor(0xFF000000);
        winchester.setLayoutParams(row);
        scroll.addView(winchester);

        TextView ex = new TextView(this);
        ex.setText("Example: \"Winchester, there is a party planned at 123 xxxx St this Saturday night, I believe the parents are away.\"");
        ex.setTextColor(0xFF000000);
        ex.setLayoutParams(row);
        scroll.addView(ex);
    }

    public void coalition() {

        //clear the screen and set up scrollView base
        wipe("Winchester Coalition For A Safer Community");
        setbase();

        //Horizontal linearlayout with the coalition image on the left and some informational text on the right
        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.HORIZONTAL);
        display.setLayoutParams(row);
        display.setGravity(Gravity.CENTER);

        TextView coalition = new TextView(this);
        coalition.setText("The Winchester Coalition For A Safer Community, housed within the Winchester Health Department, is a comprehensive community-based organization that works collaboratively with residents, town departments, and agencies to reduce risky behaviors, particularly in the youth community, and to foster healthy life choices through education.");
        coalition.setTextColor(0xFF000000);
        coalition.setLayoutParams(item);

        //ImageView will be 2/5 the width of the screen. The imageview will automatically scale the image down to fit.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ratio = ((float) metrics.widthPixels * 2 / 5) / (float) getResources().getDrawable(R.drawable.coalition).getIntrinsicWidth();
        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams((int) (ratio * getResources().getDrawable(R.drawable.coalition).getIntrinsicWidth()), (int) (ratio * getResources().getDrawable(R.drawable.coalition).getIntrinsicHeight())));
        image.setImageDrawable(getResources().getDrawable(R.drawable.coalition));

        scroll.addView(display);
        display.addView(image);
        display.addView(coalition);

        //horizontal linearlayout of 2 buttons. The first button sends to the Coalition website, the second one sends to the Coalition Facebook page
        LinearLayout row1 = new LinearLayout(this);
        Button site = new Button(this);
        Button facebook = new Button(this);
        site.setText("View Website");
        facebook.setText("Facebook Page");
        LinearLayout.LayoutParams weighted_button = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        site.setLayoutParams(weighted_button);
        facebook.setLayoutParams(weighted_button);
        site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.winchestercoalitionsafercommunity.com"));
                startActivity(intent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Winchester.Coalition.Safer.Community"));
                startActivity(intent);
            }
        });
        row1.setLayoutParams(row);
        scroll.addView(row1);
        row1.addView(site);
        row1.addView(facebook);
    }

    public void contacts() {

        //clear the screen and set up scrollView base
        wipe("Non-Emergency Contacts");
        setbase();

        //Generate sections of the page, to which information can be added.
        if (pages.get("Non-Emergency Contacts") != null) {
            createSections("Non-Emergency Contacts");
        }


        //Populate the screen with the relevant information
        showinfo("Non-Emergency Contacts");

        //a button that will take the user to the winchester town website
        Button town = new Button(this);
        town.setLayoutParams(row);
        town.setText("For additional town information click here");
        town.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.winchester.us/directory.aspx"));
                startActivity(intent);
            }
        });
        scroll.addView(town);
    }

    public void emergency() {

        //clear the screen and set up scrollView base
        wipe("Emergency Contacts");
        setbase();

        if (pages.get("Emergency Contacts") != null) {
            createSections("Emergency Contacts");
        }

        //Populate the screen with the relevant information
        showinfo("Emergency Contacts");
    }

    public void mental() {

        //clear the screen and set up scrollView base
        wipe("Mental Health Resources");
        setbase();

        //Generate sections of the page, to which information can be added.
        if (pages.get("Mental Health") != null) {
            createSections("Mental Health");
        }

        //Populate the screen with the relevant information
        showinfo("Mental Health");

        //a button that will take the user to the WHS transition site
        Button town = new Button(this);
        town.setLayoutParams(row);
        town.setText("More support websites and articles");
        town.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.jfleming89.wixsite.com/whs-transition"));
                startActivity(intent);
            }
        });
        scroll.addView(town);

    }

    public void community() {

        //clear the screen and set up scrollView base
        wipe("Community Connector");
        setbase();

        if (pages.get("Community Connector") != null) {
            createSections("Community Connector");
        }

        //Populate the screen with the relevant information
        showinfo("Community Connector");
    }

    public void articles() {

        //This function (and the button that activates it) takes you directly to the articles of interest on the coalition website
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.winchestercoalitionsafercommunity.com/articles-of-interest"));
        startActivity(intent);
    }

    public void school() {

        //Look, it's the same function as the other procedurally generated pages.
        //If you don't know what these functions do by now, I can't help you.
        wipe("School Resources");
        setbase();

        if (pages.get("School Resources") != null) {
            createSections("School Resources");
        }

        showinfo("School Resources");
    }

    public void about() {

        //clear screen, set scrollview base
        wipe("About The App");
        setbase();

        //horizontal linear layout with app icon on the left and text information on the right
        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.HORIZONTAL);
        display.setLayoutParams(row);
        display.setGravity(Gravity.CENTER);

        TextView coalition = new TextView(this);
        coalition.setText("This app was commissioned by the Winchester Coalition For A Safer Community to serve the community of Winchester, MA by providing helpful resources to the residents. It was originally created by Rosanna Zhang and Qiuyue Liu and was recreated by Tony D Jones.");
        coalition.setTextColor(0xFF000000);
        coalition.setLayoutParams(item);

        //ImageView will be 2/5 the width of the screen. The imageview will automatically scale the image down to fit.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float ratio = ((float) metrics.widthPixels * 2 / 5) / (float) getResources().getDrawable(R.drawable.icon).getIntrinsicWidth();
        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams((int) (ratio * getResources().getDrawable(R.drawable.icon).getIntrinsicWidth()), (int) (ratio * getResources().getDrawable(R.drawable.icon).getIntrinsicHeight())));
        image.setImageDrawable(getResources().getDrawable(R.drawable.icon));

        scroll.addView(display);
        display.addView(image);
        display.addView(coalition);

        //button takes you to coalition website
        Button town = new Button(this);
        town.setLayoutParams(row);
        town.setText("For more information about the coalition, please visit our website");
        town.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.winchestercoalitionsafercommunity.com"));
                startActivity(intent);
            }
        });
        scroll.addView(town);
    }

    public void search() {

        //clear screen, but does not yet add scrollview base. this means every time we do a search we can remove the views from the scrollview without removing the search bar itself.
        wipe("Search");

        //generates and adds search bar to screen. the text input area will take up most of the width, stretching to fill empty space.
        LinearLayout search_bar = new LinearLayout(this);
        final EditText edit = new EditText(this);
        Button search_button = new Button(this);
        LinearLayout.LayoutParams prompt = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        LinearLayout.LayoutParams button = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        search_bar.setLayoutParams(row);
        edit.setLayoutParams(prompt);
        search_button.setLayoutParams(button);
        screen.addView(search_bar);

        //the button will send data from the text input field to the generate() functions
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate(edit);
            }
        });
        search_button.setText("Search");
        edit.setHint("Enter Search Terms");
        search_bar.addView(edit);
        search_bar.addView(search_button);

        //now we add the scrollview to the screen. it will be below the search bar, so when views are added to "scroll" they will show up underneath the search bar
        setbase();
    }

    public void generate(View v) {

        //remove previous search results if present
        scroll.removeAllViews();

        //Get the search words from the text input field
        EditText edit = (EditText) v;
        String input = edit.getText().toString();

        //Split the search string into individual words and add them to an array. both words will have to be in the information in order for that Info to be presented.
        String[] search = input.split(" ");

        //List for results of search
        final List<Info> results = new ArrayList<>();

        //for every Info object, this loop checks all data of the Info to see if the search terms are in the String
        //This allows users to search for the names of organizations, or email addresses, phone numbers, or even a url to pull up that Info.
        for (int i = 0; i < information.size(); i++) {

            //searching boolean keeps track of whether we are still scanning the current Info object for a match.
            boolean searching = true;
            if (match(information.get(i).reference, search)) {
                results.add(information.get(i));
                searching = false;
            }
            //have to check all members of arrays
            if (searching && information.get(i).email != null) {
                for (int j = 0; j < information.get(i).email.length; j++) {
                    if (match(information.get(i).email[j], search)) {
                        results.add(information.get(i));
                        searching = false;
                        break;
                    }
                }
            }
            if (searching && information.get(i).text != null) {
                for (int j = 0; j < information.get(i).text.length; j++) {
                    if (match(information.get(i).text[j], search)) {
                        results.add(information.get(i));
                        searching = false;
                        break;
                    }
                }
            }
            if (searching && information.get(i).number != null) {
                for (int j = 0; j < information.get(i).number.length; j++) {
                    if (match(information.get(i).number[j], search)) {
                        results.add(information.get(i));
                        searching = false;
                        break;
                    }
                }
            }
            if (searching && information.get(i).url != null) {
                for (int j = 0; j < information.get(i).url.length; j++) {
                    if (match(information.get(i).url[j], search)) {
                        results.add(information.get(i));
                        searching = false;
                        break;
                    }
                }
            }
            if (searching && information.get(i).description != null && match(information.get(i).description, search)) {
                results.add(information.get(i));
            }
        }

        //for every matched result...
        for (int i = 0; i < results.size(); i++) {

            //present the information
            makeviews(results.get(i));

            //generate an additional view that, when clicked, will redirect to that information's main page. ie clicking this view for a school will take you to the School Resources page
            TextView page = new TextView(this);
            page.setText("Go to Page");
            page.setTextColor(0xFF0000FF);
            page.setPaintFlags(page.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            page.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            page.setLayoutParams(info);
            scroll.addView(page);
            page.setClickable(true);
            final int finalI = i;

            //clicking the view calls the redirect() function, explained below.
            //but particularly notice that that function only takes the FIRST element of the Info's page array.
            //So if an Info object is matched to more than one page, the FIRST element of the page array is considered its default page.
            //for example, the Partnership for drug free kids shows up on non-emergency contacts and mental health.
            //But its default page is mental health, so that is the first element of its page array.
            page.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirect(results.get(finalI).page[0]);
                }
            });

            //Add an extra layout just to give some more space between entries
            TextView space = new TextView(this);
            space.setLayoutParams(new LinearLayout.LayoutParams(10, 150));
            scroll.addView(space);
        }
    }

    //this function takes a string and determines which function to call, which will load that page
    public void redirect(String page) {
        if (page.equals("tips")) {
            tips();
        } else if (page.equals("Non-Emergency Contacts")) {
            contacts();
        } else if (page.equals("coalition")) {
            coalition();
        } else if (page.equals("Emergency Contacts")) {
            emergency();
        } else if (page.equals("Mental Health")) {
            mental();
        } else if (page.equals("Community Connector")) {
            community();
        } else if (page.equals("School Resources")) {
            school();
        }
    }

    //gives us more control of what the back button does.
    @Override
    public void onBackPressed() {

        //if back is pressed while on the home screen, closes the app. Like pressing the home button.
        if (home) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }

        //if not on the home page, sends back to home page by calling boot()
        else {
            boot();
        }

    }

    //function takes a view, pulls a string from the view, translates the string into a valid phone number (no hyphens and translates letters into corresponding numbers)
    //Then sends the user to the phone app with the number already typed out
    public void dial(View v) {
        TextView text = (TextView) v;
        String number = (String) text.getText();
        StringBuilder final_number = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            String digit = number.substring(i, i + 1);
            if (Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9").contains(digit)) {
                final_number = final_number.append(digit);
            } else if (Arrays.asList("A", "B", "C").contains(digit.toUpperCase())) {
                final_number = final_number.append("2");
            } else if (Arrays.asList("D", "E", "F").contains(digit.toUpperCase())) {
                final_number = final_number.append("3");
            } else if (Arrays.asList("G", "H", "I").contains(digit.toUpperCase())) {
                final_number = final_number.append("4");
            } else if (Arrays.asList("J", "K", "L").contains(digit.toUpperCase())) {
                final_number = final_number.append("5");
            } else if (Arrays.asList("M", "N", "O").contains(digit.toUpperCase())) {
                final_number = final_number.append("6");
            } else if (Arrays.asList("P", "Q", "R", "S").contains(digit.toUpperCase())) {
                final_number = final_number.append("7");
            } else if (Arrays.asList("T", "U", "V").contains(digit.toUpperCase())) {
                final_number = final_number.append("8");
            } else if (Arrays.asList("W", "X", "Y", "Z").contains(digit.toUpperCase())) {
                final_number = final_number.append("9");
            }
        }

        Uri call = Uri.parse("tel:" + final_number.toString());
        Intent intent = new Intent(Intent.ACTION_DIAL, call);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    //function takes a view, pulls a string from the view
    //Then sends the user to the SMS app with the number already the designated recipient
    public void text(View v) {
        TextView number = (TextView) v;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:"));
        intent.putExtra("address", number.getText());
        startActivity(intent);
    }

    //function takes a view, pulls a string from the view
    //Then sends the user to the SMS app with the number already the designated recipient
    public void website(View v) {
        TextView text = (TextView) v;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www." + text.getText()));
        startActivity(intent);
    }

    //function takes a view, pulls a string from the view
    //Then sends the user to the email app with the email address already the designated recipient
    public void email(View v) {
        TextView text = (TextView) v;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{(String) text.getText()});
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //function takes a view, pulls a string from the view
    //Then, if permission is given, automatically sends text to designated number
    public void autotext(View v) {
        EditText edit = (EditText) v;

        //only sends SMS if text has been entered in the input field.
        if (edit.getText().length() > 0) {

            //produce the message
            String message = "Winchester, " + edit.getText().toString();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("smsto:"));
            intent.putExtra("address", tipnumber);
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }

    }

    public boolean match(String source, String[] params) {

        //keep track of how many matches found in the source string
        int matches = 0;

        //for each word in the search list
        for (int i = 0; i < params.length; i++) {

            //translate both strings to uppercase for fair comparison
            if (source.toUpperCase().contains(params[i].toUpperCase())) {

                //if source string contains the search word, increment match counter by one
                matches += 1;
            }
        }

        //if all search terms are found in the source string, this is a matched Info, return true.
        if (matches == params.length) {
            return true;
        } else {
            return false;
        }
    }

    //This function takes the section array of an Info object and finds where on the page (ie which view) that Info should be displayed.
    public LinearLayout find(String[] tags) {

        //if no section specified, return the base linearlayout in the scrollview
        if (tags == null) {
            return scroll;
        }

        //if on the current page we find a view with a tag that matches one of the sections in the Info's section array, return that view.
        for (int i = 0; i < tags.length; i++) {
            if (scroll.findViewWithTag(tags[i]) != null) {
                return scroll.findViewWithTag(tags[i]);
            }
        }

        //if there's no match, the default is the base
        return scroll;
    }

    public void wipe(String title) {

        //no longer on the home screen
        home = false;

        //set title in action bar
        getSupportActionBar().setTitle(title);

        //clear the base views
        screen.removeAllViews();
        scroll_parent.removeAllViews();
        scroll.removeAllViews();
    }

    public void setbase() {

        //Add scrollview to screen and linearlayout to scrollview
        screen.addView(scroll_parent);
        scroll_parent.addView(scroll);
    }

    //removes the extension from a string. for example, this functions would take "x.png" and return "x". Necessary to identify images properly
    public String removeExtension(String name) {
        if (name == null) {
            return null;
        }
        if (name.contains(".")) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return name;
    }

    //this function makes sections on the current page
    public void createSections(String page) {

        for (int i = 0; i < pages.get(page).length; i++) {
            //Generate header for the section.
            TextView text = new TextView(this);
            text.setText(pages.get(page)[i]);
            text.setTextColor(0xFF000000);

            //Make text bigger and center the view. if not, it's a subsection.
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            text.setTextAppearance(this, android.R.style.TextAppearance_Large);

            String imgname = removeExtension(sections.get(pages.get(page)[i]));
            //if no image, add textview to page
            if (imgname == null) {
                scroll.addView(text);
            }

            //if there is an image, make a horizontal linear layout, resize image to a reasonable size, and place image on the left, title on the right, and center them in the row.
            else {
                int img = getApplicationContext().getResources().getIdentifier(imgname, "drawable", getApplicationContext().getPackageName());

                LinearLayout header = new LinearLayout(this);
                header.setLayoutParams(row);
                header.setOrientation(LinearLayout.HORIZONTAL);
                header.setGravity(Gravity.CENTER);
                scroll.addView(header);

                //ImageView will be the same 1.3 times the height of the neighboring textview. The imageview will automatically scale the image down to fit.
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                text.measure(item.width, item.height);
                float ratio = (float) 1.3 * text.getMeasuredHeight() / (float) getResources().getDrawable(img).getIntrinsicHeight();
                ImageView image = new ImageView(this);
                image.setLayoutParams(new LinearLayout.LayoutParams((int) (ratio * getResources().getDrawable(img).getIntrinsicWidth()), (int) (ratio * getResources().getDrawable(img).getIntrinsicHeight())));
                image.setImageDrawable(getResources().getDrawable(img));

                text.setLayoutParams(item);
                header.addView(image);
                header.addView(text);
            }

            //This view is for the actual information to be added. It will be placed directly below the header.
            //It has a tag that will allow the find() function to find and return it so Info can be properly added to the content view.
            LinearLayout content = new LinearLayout(this);
            content.setLayoutParams(row);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setTag(pages.get(page)[i]);
            scroll.addView(content);

            //check to see if this section has subsections, and if so, create them and add to the page
            if (subsections.get(pages.get(page)[i]) != null) {
                createSubsections(pages.get(page)[i]);
            }

        }

    }

    //this function makes sections on the current page
    public void createSubsections(String section) {

        for (int i = 0; i < subsections.get(section).length; i++) {
            //Generate header for the section. medium size and not centered.
            TextView text = new TextView(this);
            text.setText(subsections.get(section)[i]);
            text.setTextColor(0xFF000000);
            text.setTextAppearance(this, android.R.style.TextAppearance_Medium);
            text.setLayoutParams(row);
            scroll.addView(text);

            //This view is for the actual information to be added. It will be placed directly below the header.
            //It has a tag that will allow the find() function to find and return it so Info can be properly added to the content view.
            LinearLayout content = new LinearLayout(this);
            content.setLayoutParams(row);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setTag(subsections.get(section)[i]);
            scroll.addView(content);
        }
    }

    //This function searches the information list and finds all of the Info Objects meant to display on the specified page, based on the Info's activity array.
    public void showinfo(String activity) {
        List<Info> results = new ArrayList<>();

        for (int i = 0; i < information.size(); i++) {
            if (Arrays.asList(information.get(i).page).contains(activity)) {
                results.add(information.get(i));
            }
        }

        //for every Info object found that is meant to display on this page, make the views and add its information to the scrollview.
        for (int i = 0; i < results.size(); i++) {
            makeviews(results.get(i));

            //Add an extra layout just to give some more space between entries
            TextView space = new TextView(this);
            space.setLayoutParams(new LinearLayout.LayoutParams(10, 150));
            find(results.get(i).section).addView(space);
        }

    }

    //This view takes an info object and adds its information to the scrollview
    public void makeviews(Info result) {

        //textview for the reference, added to the proper section per the Info's section array.
        TextView reference = new TextView(this);
        reference.setText(result.reference);
        reference.setTextColor(0xFF000000);
        reference.setLayoutParams(row);
        find(result.section).addView(reference);

        //textview for the description, if not null
        if (result.description != null) {
            final TextView text = new TextView(this);
            text.setText(result.description);
            text.setLayoutParams(row);
            find(result.section).addView(text);
        }

        //textviews for the numbers, if not null. clicking will call dial() and send to phone app
        if (result.number != null) {
            for (int j = 0; j < result.number.length; j++) {
                final TextView number = new TextView(this);
                number.setText(result.number[j]);
                number.setTextColor(0xFF0000FF);
                number.setPaintFlags(number.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                number.setGravity(View.TEXT_ALIGNMENT_VIEW_END);
                number.setLayoutParams(info);
                find(result.section).addView(number);
                number.setClickable(true);
                number.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dial(number);
                    }
                });
            }
        }

        //textview for the text number, if not null. clicking will call text() and send to SMS app
        if (result.text != null) {
            for (int j = 0; j < result.text.length; j++) {
                final TextView text = new TextView(this);
                text.setText(result.text[j]);
                text.setTextColor(0xFF0000FF);
                text.setPaintFlags(text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                text.setLayoutParams(info);
                find(result.section).addView(text);
                text.setClickable(true);
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        text(text);
                    }
                });
            }

        }

        //textview for the email address, if not null. Clicking will call email() and sent to email app
        if (result.email != null) {
            for (int j = 0; j < result.email.length; j++) {
                final TextView email = new TextView(this);
                email.setText(result.email[j]);
                email.setTextColor(0xFF0000FF);
                email.setPaintFlags(email.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                email.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                email.setLayoutParams(info);
                find(result.section).addView(email);
                email.setClickable(true);
                email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        email(email);
                    }
                });
            }

        }

        //textviews for the URLs, if not null. clicking will call website() and send to browser app
        if (result.url != null) {
            for (int j = 0; j < result.url.length; j++) {
                final TextView url = new TextView(this);
                url.setText(result.url[j]);
                url.setTextColor(0xFF0000FF);
                url.setPaintFlags(url.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                url.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                url.setLayoutParams(info);
                find(result.section).addView(url);
                url.setClickable(true);
                url.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        website(url);
                    }
                });
            }
        }
    }

    //this function takes a string and returns an array of strings, splitting the original string by commas and removing space characters if there are any in front or at the end of the strings.
    public String[] process(String input) {
        if (input == null) {
            return null;
        }
        String[] output = input.split(",");
        for (int i = 0; i < output.length; i++) {
            while (output[i].substring(0, 1).equals(" ")) {
                output[i] = output[i].substring(1);
            }
            while (output[i].substring(output[i].length() - 2, output[i].length() - 1).equals(" ")) {
                output[i] = output[i].substring(0, output.length - 2);
            }
        }
        return output;
    }
}