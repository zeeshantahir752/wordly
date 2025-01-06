package com.yesserly.wordly.views.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yesserly.wordly.databinding.DialogNewModeBinding;
import com.yesserly.wordly.databinding.DialogSettingsBinding;

public class SettingsDialog extends AppCompatDialogFragment {
    private static final String TAG = "SettingsDialog";

    public interface SettingsListener {

        void switchNotifications(boolean enabled);

        void rateUs();

        void contactUs();

        void goPrivacyPolicy();
    }

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private final boolean notifications;
    private SettingsListener listener;
    private DialogSettingsBinding mBinding;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    public SettingsDialog(boolean notifications) {
        this.notifications = notifications;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (SettingsListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement Callback interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //Create Dialog Container
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        //Set ViewBinding
        mBinding = DialogSettingsBinding.inflate(inflater, null, false);

        //Init UI
        mBinding.notifications.setChecked(notifications);

        //Init Listeners
        mBinding.close.setOnClickListener((v) -> dismiss());
        mBinding.notifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.switchNotifications(isChecked);
        });
        mBinding.rateUs.setOnClickListener((v) -> listener.rateUs());
        mBinding.contactUs.setOnClickListener((v) -> listener.contactUs());
        mBinding.privacyPolicy.setOnClickListener((v) -> listener.goPrivacyPolicy());

        //Set Dialog
        Dialog dialog = builder.setView(mBinding.getRoot()).create();

        //Make Transparent Background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }
}
