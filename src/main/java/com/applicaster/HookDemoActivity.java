package com.applicaster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Switch;

import com.applicaster.hook_screen.HookScreen;
import com.applicaster.hook_screen.HookScreenListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;


public class HookDemoActivity extends AppCompatActivity implements HookScreen {

    HookScreenListener hookListener;
    Map<String, Object> hookProps = new HashMap<>();
    HashMap<String, String> hookScreen = new HashMap<>();

    Switch switchFlow;
    Switch switchDismiss;
    Switch switchRecurringow;

    String HOOK_PROPS_EXTRA = "hook_props_extra";
    int ACTIVITY_HOOK_FAILED = 1002;
    int ACTIVITY_HOOK_COMPLETED = 1001;
    int ACTIVITY_HOOK_RESULT_CODE = 1000;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hookProps = convertHookMap(getIntent().getStringExtra(HOOK_PROPS_EXTRA));

        setContentView(R.layout.hook_layout);

        Button buttonFail = this.findViewById(R.id.fail_button);
        Button buttonDismiss = this.findViewById(R.id.dismiss_button);
        Button buttonSuccess = this.findViewById(R.id.success_button);

        switchFlow = this.findViewById(R.id.flow_switch);
        switchDismiss = this.findViewById(R.id.dismiss_switch);
        switchRecurringow = this.findViewById(R.id.recurring_switch);

        buttonFail.setOnClickListener((g) -> sendActivityResult(this, hookProps, switchFlow.isChecked()? ACTIVITY_HOOK_FAILED: ACTIVITY_HOOK_COMPLETED));
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
        return switchFlow != null && switchFlow.isChecked();
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
    public void executeHook(Context context, HookScreenListener hookListener, Map<String, ?> hookProps) {
        this.hookListener = hookListener;
        Intent intent = new Intent(context, this.getClass());
        startActivityForResult(intent, context, hookProps);
    }

    @Override
    public HookScreenListener getListener() {
        return hookListener;
    }

    @Override
    public HashMap<String, String> getHook() {
        return hookScreen;
    }

    @Override
    public void setHook(HashMap<String, String> hookScreen) {
        this.hookScreen = hookScreen;
    }
}