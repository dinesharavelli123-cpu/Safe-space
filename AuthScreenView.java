package com.safespace.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

final class AuthScreenView extends ScrollView {
    private static final int NAVY = Color.rgb(16, 38, 95);
    private static final int PURPLE = Color.rgb(123, 79, 233);
    private static final int SUCCESS = Color.rgb(36, 142, 89);
    private static final int ERROR = Color.rgb(186, 45, 76);

    private final Activity activity;
    private final FirebaseAuth auth;
    private final Runnable onAuthenticated;
    private final Runnable continueAfterSuccess = this::dispatchAuthenticationSuccess;
    private boolean signUp;
    private boolean busy;
    private boolean successDispatched;
    private LinearLayout content;
    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private Button primaryButton;
    private TextView messageView;
    private String retainedEmail = "";

    AuthScreenView(Activity activity, FirebaseAuth auth, boolean startWithSignUp,
                   Runnable onAuthenticated) {
        super(activity);
        this.activity = activity;
        this.auth = auth;
        this.onAuthenticated = onAuthenticated;
        this.signUp = startWithSignUp;
        setFillViewport(true);
        setClipToPadding(false);
        setVerticalScrollBarEnabled(false);
        setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFD7ECFF, 0xFFE7F4FF, 0xFFF2F9FF}));
        setOnApplyWindowInsetsListener((view, insets) -> {
            setPadding(0, insets.getSystemWindowInsetTop(), 0,
                    insets.getSystemWindowInsetBottom());
            return insets;
        });
        buildContent();
    }

    private void buildContent() {
        removeAllViews();
        content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(26), dp(24), dp(28));
        addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView logoView = new ImageView(activity);
        BitmapFactory.Options logoOptions = new BitmapFactory.Options();
        logoOptions.inScaled = false;
        logoOptions.inSampleSize = 2;
        Bitmap logo = BitmapFactory.decodeResource(
                getResources(), R.drawable.safe_space_logo, logoOptions);
        logoView.setImageBitmap(logo);
        logoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        logoView.setContentDescription("Safe Space logo");
        add(content, logoView, ViewGroup.LayoutParams.MATCH_PARENT, dp(116), 0, 0, 0, dp(2));

        TextView title = text(signUp ? "Create Your Account" : "Welcome Back", 28, NAVY, true);
        add(content, title, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, dp(6));

        TextView subtitle = text(signUp
                ? "A safer, kinder you starts here."
                : "Good to see you again", 14, 0xC710265F, false);
        add(content, subtitle, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, dp(23));

        if (signUp) {
            nameField = input("Full Name",
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            nameField.setAutofillHints(View.AUTOFILL_HINT_NAME);
            add(content, nameField, ViewGroup.LayoutParams.MATCH_PARENT, dp(56), 0, 0, 0, dp(12));
        } else {
            nameField = null;
        }

        emailField = input("Email", InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailField.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS);
        emailField.setText(retainedEmail);
        add(content, emailField, ViewGroup.LayoutParams.MATCH_PARENT, dp(56), 0, 0, 0, dp(12));

        passwordField = input("Password", InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setAutofillHints(signUp ? "newPassword" : View.AUTOFILL_HINT_PASSWORD);
        passwordField.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordField.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        add(content, passwordField, ViewGroup.LayoutParams.MATCH_PARENT, dp(56), 0, 0, 0,
                signUp ? dp(18) : dp(4));

        if (!signUp) {
            TextView forgot = text("Forgot password?", 13, PURPLE, true);
            forgot.setGravity(Gravity.END);
            forgot.setContentDescription("Send password reset email");
            forgot.setOnClickListener(view -> resetPassword());
            add(content, forgot, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, dp(2), dp(14));
        }

        primaryButton = new Button(activity);
        primaryButton.setAllCaps(false);
        primaryButton.setText(signUp ? "Sign Up" : "Log In");
        primaryButton.setTextColor(Color.WHITE);
        primaryButton.setTextSize(16);
        primaryButton.setTypeface(Typeface.create("sans-serif-rounded", Typeface.BOLD));
        primaryButton.setGravity(Gravity.CENTER);
        primaryButton.setElevation(dp(5));
        primaryButton.setBackground(purpleButtonBackground());
        primaryButton.setOnClickListener(view -> submit());
        add(content, primaryButton, ViewGroup.LayoutParams.MATCH_PARENT, dp(56), 0, 0, 0, dp(8));

        messageView = text("", 13, ERROR, false);
        messageView.setGravity(Gravity.CENTER);
        messageView.setMinHeight(dp(24));
        add(content, messageView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, dp(8));

        TextView divider = text("or continue with", 13, 0x9910265F, false);
        add(content, divider, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, dp(12));

        LinearLayout socialRow = new LinearLayout(activity);
        socialRow.setOrientation(LinearLayout.HORIZONTAL);
        socialRow.setGravity(Gravity.CENTER);
        Button google = socialButton("G", "Continue with Google");
        Button apple = socialButton("●", "Continue with Apple");
        LinearLayout.LayoutParams socialParams = new LinearLayout.LayoutParams(dp(64), dp(52));
        socialParams.setMarginEnd(dp(14));
        socialRow.addView(google, socialParams);
        socialRow.addView(apple, new LinearLayout.LayoutParams(dp(64), dp(52)));
        add(content, socialRow, ViewGroup.LayoutParams.MATCH_PARENT, dp(52), 0, 0, 0, dp(22));

        TextView toggle = text(signUp
                ? "Already have an account?  Log in"
                : "Don't have an account?  Sign up", 14, NAVY, false);
        toggle.setContentDescription(signUp ? "Go to login" : "Go to sign up");
        toggle.setOnClickListener(view -> switchMode(!signUp));
        add(content, toggle, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, dp(12));

        TextView firebaseNote = text("Protected by Firebase Authentication", 11,
                0x8010265F, false);
        add(content, firebaseNote, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0);
    }

    private Button socialButton(String label, String description) {
        Button button = new Button(activity);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(20);
        button.setTextColor(NAVY);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setContentDescription(description);
        button.setBackground(outlinedBackground(Color.WHITE, 0x227B4FE9));
        button.setOnClickListener(view -> showMessage(
                "Use email and password for this prototype.", false));
        return button;
    }

    private EditText input(String hint, int inputType) {
        EditText field = new EditText(activity);
        field.setHint(hint);
        field.setInputType(inputType);
        field.setSingleLine(true);
        field.setTextSize(15);
        field.setTextColor(NAVY);
        field.setHintTextColor(0x8010265F);
        field.setPadding(dp(17), 0, dp(17), 0);
        field.setBackground(outlinedBackground(0xF5FFFFFF, 0x267B4FE9));
        return field;
    }

    private TextView text(String value, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(activity);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER);
        view.setTypeface(Typeface.create("sans-serif-rounded",
                bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private GradientDrawable outlinedBackground(int fill, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(16));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private GradientDrawable purpleButtonBackground() {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF9A65EE, 0xFF7042DD});
        drawable.setCornerRadius(dp(28));
        return drawable;
    }

    private void submit() {
        if (busy) {
            return;
        }
        String name = nameField == null ? "" : nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();
        retainedEmail = email;

        if (signUp && name.length() < 2) {
            showMessage("Please enter your full name.", false);
            nameField.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter a valid email address.", false);
            emailField.requestFocus();
            return;
        }
        if (password.length() < 6) {
            showMessage("Password must contain at least 6 characters.", false);
            passwordField.requestFocus();
            return;
        }

        setBusy(true);
        if (signUp) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(activity, result -> {
                        FirebaseUser user = result.getUser();
                        if (user != null) {
                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();
                            user.updateProfile(profile);
                        }
                        finishSuccess("Account created — you're signed in.");
                    })
                    .addOnFailureListener(activity, this::showFirebaseError);
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(activity,
                            result -> finishSuccess("Welcome back — you're signed in."))
                    .addOnFailureListener(activity, this::showFirebaseError);
        }
    }

    private void resetPassword() {
        String email = emailField.getText().toString().trim();
        retainedEmail = email;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Enter your email first.", false);
            emailField.requestFocus();
            return;
        }
        if (busy) {
            return;
        }
        setBusy(true);
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(activity, result -> {
                    setBusy(false);
                    showMessage("Password reset email sent.", true);
                })
                .addOnFailureListener(activity, this::showFirebaseError);
    }

    private void finishSuccess(String message) {
        busy = false;
        primaryButton.setEnabled(false);
        primaryButton.setAlpha(1f);
        primaryButton.setText("Success  ✓");
        showMessage(message, true);
        passwordField.setText("");
        removeCallbacks(continueAfterSuccess);
        postDelayed(continueAfterSuccess, 550L);
    }

    private void dispatchAuthenticationSuccess() {
        if (successDispatched || !isAttachedToWindow()) {
            return;
        }
        successDispatched = true;
        onAuthenticated.run();
    }

    private void showFirebaseError(Exception error) {
        setBusy(false);
        String message;
        if (error instanceof FirebaseAuthUserCollisionException) {
            message = "An account already exists. Log in instead.";
        } else if (error instanceof FirebaseAuthWeakPasswordException) {
            message = "Choose a stronger password.";
        } else if (error instanceof FirebaseAuthInvalidUserException) {
            message = "No account was found for this email.";
        } else if (error instanceof FirebaseAuthInvalidCredentialsException) {
            message = "Check your email and password.";
        } else if (error instanceof FirebaseNetworkException) {
            message = "No internet connection. Please try again.";
        } else {
            message = error == null || error.getLocalizedMessage() == null
                    ? "Could not continue. Please try again."
                    : error.getLocalizedMessage();
        }
        showMessage(message, false);
    }

    private void setBusy(boolean value) {
        busy = value;
        primaryButton.setEnabled(!value);
        primaryButton.setAlpha(value ? .7f : 1f);
        primaryButton.setText(value ? "Please wait…" : (signUp ? "Sign Up" : "Log In"));
    }

    private void showMessage(String message, boolean success) {
        messageView.setText(message);
        messageView.setTextColor(success ? SUCCESS : ERROR);
        messageView.announceForAccessibility(message);
    }

    private void switchMode(boolean nextSignUp) {
        if (busy || nextSignUp == signUp) {
            return;
        }
        retainedEmail = emailField.getText().toString().trim();
        int width = getWidth() > 0 ? getWidth() : getResources().getDisplayMetrics().widthPixels;
        int direction = nextSignUp ? -1 : 1;
        LinearLayout outgoing = content;
        outgoing.animate().translationX(-direction * width).setDuration(180L)
                .withEndAction(() -> {
                    signUp = nextSignUp;
                    buildContent();
                    content.setTranslationX(direction * width);
                    content.animate().translationX(0f).setDuration(260L).start();
                }).start();
    }

    private void add(LinearLayout parent, View child, int width, int height,
                     int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(left, top, right, bottom);
        parent.addView(child, params);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(continueAfterSuccess);
        super.onDetachedFromWindow();
    }
}
