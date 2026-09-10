package com.safespace.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

final class CounsellorView extends LinearLayout {
    private final Activity activity;
    private final ScreenNavigator navigator;
    private final boolean dark;
    private final int fg;
    private final int sub;
    private final int card;

    CounsellorView(Activity activity, ScreenNavigator navigator) {
        super(activity);
        this.activity = activity;
        this.navigator = navigator;
        dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        fg = dark ? Color.WHITE : 0xFF122B62;
        sub = dark ? 0xFFD4DAEC : 0xFF586689;
        card = dark ? 0xFF2B3B61 : Color.WHITE;
        setOrientation(VERTICAL);
        setPadding(dp(16), dp(30), dp(16), dp(16));
        setBackgroundColor(dark ? 0xFF111A33 : 0xFFF4F2FF);
        build();
    }

    private void build() {
        LinearLayout head = new LinearLayout(activity); head.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = tv("‹", 32, fg, true); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> navigator.goBack());
        head.addView(back, new LayoutParams(dp(44), dp(44)));
        LinearLayout titles = new LinearLayout(activity); titles.setOrientation(VERTICAL);
        titles.addView(tv("Counsellor support", 22, fg, true));
        titles.addView(tv("Choose the kind of support you want today", 11, sub, false));
        head.addView(titles, new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        addView(head, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        ScrollView sv = new ScrollView(activity); sv.setVerticalScrollBarEnabled(false);
        LinearLayout body = new LinearLayout(activity); body.setOrientation(VERTICAL); body.setPadding(0, dp(10), 0, dp(24));
        sv.addView(body);
        addView(sv, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        body.addView(infoCard("Find a Counsellor", "Browse counsellors by support area, language and availability.", "Browse"), mp(0,0,0,10));
        body.addView(infoCard("Book a Session", "Pick a convenient date and time for a private support session.", "Choose time"), mp(0,0,0,10));
        body.addView(infoCard("Chat Now", "Start a confidential prototype chat with the counsellor support team.", "Start chat"), mp(0,0,0,10));
        body.addView(infoCard("Request Callback", "Leave a callback request and a counsellor can follow up.", "Request"), mp(0,0,0,16));

        TextView note = tv("For immediate danger or urgent help, use the Support section and contact a trusted adult or local emergency service.", 11, sub, false);
        note.setPadding(dp(14), dp(12), dp(14), dp(12)); note.setBackground(round(dark ? 0xFF22304E : 0xFFEAE6FF, 16));
        body.addView(note, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private View infoCard(String title, String desc, String action) {
        LinearLayout c = new LinearLayout(activity); c.setOrientation(VERTICAL); c.setPadding(dp(16), dp(14), dp(16), dp(14)); c.setElevation(dp(2)); c.setBackground(round(card, 18));
        c.addView(tv(title, 16, fg, true));
        TextView d = tv(desc, 12, sub, false); LayoutParams dp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); dp.topMargin=dp(6); c.addView(d, dp);
        TextView a = tv(action + "  →", 12, Color.WHITE, true); a.setGravity(Gravity.CENTER); a.setBackground(round(0xFF7D4EE8, 16));
        a.setOnClickListener(v -> Toast.makeText(activity, action + " selected", Toast.LENGTH_SHORT).show());
        LayoutParams ap = new LayoutParams(dp(118), dp(36)); ap.topMargin=dp(12); c.addView(a, ap);
        return c;
    }

    private TextView tv(String s, float size, int color, boolean bold) { TextView t=new TextView(activity); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setTypeface(Typeface.create("sans-serif-rounded", bold?Typeface.BOLD:Typeface.NORMAL)); t.setIncludeFontPadding(false); return t; }
    private GradientDrawable round(int color, int r){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(r)); return g; }
    private LayoutParams mp(int l,int t,int r,int b){ LayoutParams p=new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private int dp(float v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
