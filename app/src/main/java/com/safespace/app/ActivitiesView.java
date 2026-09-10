package com.safespace.app;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Screen 13: the playful mindful activities catalogue. */
final class ActivitiesView extends PastelScreenView {
    private final FrameLayout tabsHost;
    private final LinearLayout activitiesHost;
    private int selectedTab;

    private static final String[][] ACTIVITIES = {
            {"♣", "Gratitude Game", "Find 5 good things today", "0"},
            {"▦", "Memory Game", "Train your mind", "1"},
            {"♥", "Positive Affirmations", "Build a kinder mindset", "2"},
            {"♠", "Nature Walk", "Explore the world around you", "3"},
            {"✎", "Art Therapy", "Express without words", "4"}
    };

    ActivitiesView(Activity activity, ScreenNavigator navigator) {
        super(activity, navigator, -1);
        setContentDescription("Mindful Activities screen");

        content.addView(header("Mindful Activities", "Fun ways to feel better", true,
                "⇧", "Share an activity"), marginParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(73), 0, 0, 0, dp(7)));

        tabsHost = new FrameLayout(activity);
        content.addView(tabsHost, marginParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(45), dp(5), 0, dp(5), dp(15)));

        activitiesHost = new LinearLayout(activity);
        activitiesHost.setOrientation(LinearLayout.VERTICAL);
        content.addView(activitiesHost, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        renderTabs();
        renderActivities();
    }

    private void renderTabs() {
        tabsHost.removeAllViews();
        LinearLayout tabs = segmentedControl(new String[]{"All", "Games", "Exercises"},
                selectedTab, view -> selectTab((Integer) view.getTag()));
        tabsHost.addView(tabs, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void selectTab(int tab) {
        if (tab == selectedTab) {
            return;
        }
        selectedTab = tab;
        renderTabs();
        renderActivities();
        String name = tab == 0 ? "All activities" : tab == 1 ? "Games" : "Exercises";
        announceForAccessibility(name + " selected");
    }

    private void renderActivities() {
        activitiesHost.removeAllViews();
        for (int i = 0; i < ACTIVITIES.length; i++) {
            if ((selectedTab == 1 && i > 1) || (selectedTab == 2 && i < 2)) {
                continue;
            }
            String[] item = ACTIVITIES[i];
            int palette = Integer.parseInt(item[3]);
            activitiesHost.addView(activityCard(item[0], item[1], item[2], palette),
                    marginParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(76),
                            0, 0, 0, dp(10)));
        }
    }

    private View activityCard(String glyph, String title, String subtitle, int palette) {
        int[] fills = {0xFFDDF4E8, 0xFFFFE9D9, 0xFFF1DFFF, 0xFFDFF2E5, 0xFFE0EAFF};
        int[] colors = {0xFF319569, 0xFFE27A32, 0xFF9B48D1, 0xFF3B9D65, 0xFF4777D6};

        LinearLayout card = bottomRowCard(glyph, fills[palette], colors[palette],
                title, subtitle, null, "Opening " + title);

        // The tiny corner sparkle gives these cards the illustrated feel of the reference.
        TextView sparkle = text("✦", 12, 0x66B67BE8, true);
        sparkle.setGravity(Gravity.CENTER);
        card.addView(sparkle, 0, new LinearLayout.LayoutParams(dp(16), dp(28)));
        return card;
    }
}
