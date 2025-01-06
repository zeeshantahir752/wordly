package com.yesserly.wordly.views.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yesserly.wordly.R;
import com.yesserly.wordly.databinding.DialogRewindBinding;
import com.yesserly.wordly.databinding.DialogSettingsBinding;

public class RewindDialog extends AppCompatDialogFragment {
    private static final String TAG = "RewindDialog";

    public interface RewindListener {

        void rewind();

        void goBack();

        void openStatistics();

        void dismissed();
    }

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private String word;
    private String definitions;
    private String text;
    private RewindListener listener;
    private DialogRewindBinding mBinding;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    public RewindDialog(String word, String definition) {
        this.word = word;
        this.definitions = definition;
    }

    public RewindDialog(String text) {
        this.text = text;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (RewindListener) getParentFragment();
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
        mBinding = DialogRewindBinding.inflate(inflater, null, false);

        //Init UI
        if (word == null){
            mBinding.rewindRequest.setText(text);
            if (!text.equals(getString(R.string.rewind_question))){
                mBinding.rewindYes.setVisibility(View.GONE);
                mBinding.rewindNo.setVisibility(View.GONE);
                mBinding.rewindDismiss.setVisibility(View.VISIBLE);
                mBinding.rewindDismiss.setOnClickListener(v -> {
                    dismiss();
                    listener.dismissed();
                });

                if (text.equals(getString(R.string.no_rewind_lost)) || text.equals(getString(R.string.no_rewind_won))){
                    mBinding.rewindDismiss.setText(getString(R.string.start_over));
                    mBinding.rewindDismiss.setOnClickListener(v -> {
                        dismiss();
                        listener.goBack();
                    });
                }
            } else {
                mBinding.rewindYes.setVisibility(View.VISIBLE);
                mBinding.rewindNo.setVisibility(View.VISIBLE);
                mBinding.rewindDismiss.setVisibility(View.GONE);
            }
        } else {
            mBinding.rewindText.setText(getString(R.string.last_word) + " " + word);
            mBinding.rewindRequest.setText(definitions);
            mBinding.rewindRequest.setMovementMethod(new ScrollingMovementMethod());
            mBinding.rewindDismiss.setText(R.string.got_it);

            //Visibility
            mBinding.rewindYes.setVisibility(View.GONE);
            mBinding.rewindNo.setVisibility(View.GONE);
            mBinding.rewindDismiss.setVisibility(View.VISIBLE);
            mBinding.rewindDismiss.setOnClickListener(v -> {
                dismiss();
                listener.openStatistics();
            });
        }

        //Listeners
        mBinding.rewindYes.setOnClickListener(v -> {
            dismiss();
            listener.rewind();
        });
        mBinding.rewindNo.setOnClickListener(v -> dismiss());

        //Set Dialog
        Dialog dialog = builder.setView(mBinding.getRoot()).create();

        //Make Transparent Background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
