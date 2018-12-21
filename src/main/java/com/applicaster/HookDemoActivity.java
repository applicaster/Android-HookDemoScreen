package com.applicaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.Switch;

import com.applicaster.activities.base.APBaseFragmentActivity;
import com.applicaster.cleengloginplugin.R;
import com.applicaster.hook_screen.HookScreen;
import com.applicaster.hook_screen.HookScreenListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.applicaster.hook_screen.HookScreenManager.ACTIVITY_HOOK_COMPLETED;
import static com.applicaster.hook_screen.HookScreenManager.ACTIVITY_HOOK_RESULT_CODE;
import static com.applicaster.hook_screen.HookScreenManager.HOOK_PROPS_EXTRA;

public class HookDemoActivity extends APBaseFragmentActivity implements HookScreen {

    HookScreenListener hookListener;
    Map<String, Object> hookProps = new HashMap<>();
    HashMap<String, String> hookScreen = new HashMap<>();

    Switch switchFlow;
    Switch switchDismiss;
    Switch switchRecurringow;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hookProps = convertHookMap(getIntent().getStringExtra(HOOK_PROPS_EXTRA));

        setContentView(R.layout.hook_layout);

        Button buttonFail = this.findViewById(R.id.fail_button);
        Button buttonDismiss = this.findViewById(R.id.dismiss_button);
        Button buttonSuccess = this.findViewById(R.id.success_button);

        buttonFail.setOnClickListener((g) -> hookListener.hookFailed(hookProps));
        buttonDismiss.setOnClickListener((g) -> this.hookDismissed());
        buttonSuccess.setOnClickListener((g) -> sendActivityResult(this, hookProps, ACTIVITY_HOOK_COMPLETED));

    }

    private Map<String,Object> convertHookMap(String hookString) {
        return new Gson().fromJson(hookString, new TypeToken<Map<String, Object>>() {}.getType());
    }

    private void sendActivityResult(Activity activity, Map<String,Object> hookProps, int hookResult) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(HOOK_PROPS_EXTRA, new Gson().toJson(hookProps));
        activity.setResult(hookResult, returnIntent);
        activity.finish();
    }

    private void startActivityForResult(Intent intent, Context context, Map<String,?> hookProps) {
        if (hookProps != null) {
            intent.putExtra(HOOK_PROPS_EXTRA, new Gson().toJson(hookProps));
        }
        ((Activity) context).startActivityForResult(intent, ACTIVITY_HOOK_RESULT_CODE);
    }

    @Override
    public boolean isFlowBlocker() {
        return switchFlow.isChecked();
    }

    @Override
    public boolean shouldPresent() {
        return true;
    }

    @Override
    public boolean isRecurringHook() {
        return true;
    }

    @Override
    public void hookDismissed() {
        sendActivityResult(this, hookProps, ACTIVITY_HOOK_COMPLETED);
    }

    @Override
    public void executeHook(@NotNull Context context, @NotNull HookScreenListener hookListener, @Nullable Map<String, ?> hookProps) {
        this.hookListener = hookListener;
        Intent intent = new Intent(context, this.getClass());
        startActivityForResult(intent, context, hookProps);
    }

    @NotNull
    @Override
    public HookScreenListener getListener() {
        return hookListener;
    }

    @NotNull
    @Override
    public HashMap<String, String> getHook() {
        return hookScreen;
    }

    @Override
    public void setHook(@NotNull HashMap<String, String> hookScreen) {
        this.hookScreen = hookScreen;
    }
}