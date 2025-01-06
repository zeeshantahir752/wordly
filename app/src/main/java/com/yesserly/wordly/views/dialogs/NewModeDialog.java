package com.yesserly.wordly.views.dialogs;

import static com.yesserly.wordly.utils.Config.TIMES;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.yesserly.wordly.R;
import com.yesserly.wordly.databinding.DialogNewModeBinding;
import com.yesserly.wordly.models.pojo.Language;
import com.yesserly.wordly.models.pojo.Mode;

public class NewModeDialog extends AppCompatDialogFragment {
    private static final String TAG = "NewModeDialog";

    public interface ModeCreationListener {

        void createNewMode(Mode mode);

    }

    /***********************************************************************************************
     * *********************************** Declarations
     */
    private Mode mode = null;
    private int[] LETTERS;
    private final Language[] LANGUAGES;
    private ModeCreationListener listener;
    private DialogNewModeBinding mBinding;

    /***********************************************************************************************
     * *********************************** Constructor
     */
    public NewModeDialog(Language[] LANGUAGES) {
        this.LANGUAGES = LANGUAGES;
    }

    /***********************************************************************************************
     * *********************************** Methods
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (ModeCreationListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement Callback interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Create Default Mode
        mode = new Mode(0, 0, LANGUAGES[0]);
        LETTERS = LANGUAGES[0].getLettersNumber().clone();

        //Create Dialog Container
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        //Set ViewBinding
        mBinding = DialogNewModeBinding.inflate(inflater, null, false);

        //Init UI
        if (LANGUAGES.length > 1) {
            mBinding.languages.setVisibility(View.VISIBLE);
            InitSpinner();
        } else mBinding.languages.setVisibility(View.GONE);
        updateLettersText(0);
        updateTimeText(0);

        //Set Listeners
        mBinding.close.setOnClickListener(v -> dismiss());
        mBinding.addTime.setOnClickListener(v -> addTime());
        mBinding.subtractTime.setOnClickListener(v -> subtractTime());
        mBinding.addLetter.setOnClickListener(v -> addLetter());
        mBinding.subtractLetter.setOnClickListener(v -> subtractLetter());
        mBinding.createMode.setOnClickListener(v -> {
            listener.createNewMode(mode);
            dismiss();
        });

        //Set Dialog
        Dialog dialog = builder.setView(mBinding.getRoot()).create();

        //Make Transparent Background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    private void subtractTime() {
        int currentTime = mode.getTime();
        currentTime--;
        mode.setTime(currentTime);
        updateTimeText(currentTime);

        //Enable/Disable
        mBinding.subtractTime.setEnabled(currentTime != 0);
        mBinding.addTime.setEnabled(currentTime != TIMES.length - 1);
    }

    private void addTime() {
        int currentTime = mode.getTime();
        currentTime++;
        mode.setTime(currentTime);
        updateTimeText(currentTime);

        //Enable/Disable
        mBinding.subtractTime.setEnabled(currentTime != 0);
        mBinding.addTime.setEnabled(currentTime != TIMES.length - 1);
    }

    private void updateTimeText(int current) {
        //Update UI
        if (current <= TIMES.length - 1 && current >= 0)
            mBinding.timeSet.setText(getString(R.string.mode_hours, TIMES[current]));
    }

    private void subtractLetter() {
        int currentLetters = mode.getLetters();
        if (currentLetters > 0) {
            currentLetters--;
            mode.setLetters(currentLetters);
            updateLettersText(currentLetters);
        }
    }

    private void addLetter() {
        int currentLetters = mode.getLetters();
        if (currentLetters < LETTERS.length - 1) {
            currentLetters++;
            mode.setLetters(currentLetters);
            updateLettersText(currentLetters);
        }
    }

    private void updateLettersText(int current) {
        //Update UI
        if (current <= LETTERS.length - 1 && current >= 0)
            mBinding.lettersSet.setText(getString(R.string.mode_letters, LETTERS[current]));
    }

    private void InitSpinner() {
        //Get List of Languages Names
        String[] languagesNames = new String[LANGUAGES.length];
        for (int i = 0; i < LANGUAGES.length; i++) {
            languagesNames[i] = LANGUAGES[i].getName();
        }

        final int[] pos = {0};
        mBinding.languages.setText(languagesNames[pos[0]]);

        //Listener
        mBinding.languages.setOnClickListener(v -> {
            pos[0]++;
            if (pos[0] == LANGUAGES.length) pos[0] = 0;

            LETTERS = null;
            LETTERS = LANGUAGES[pos[0]].getLettersNumber();
            mode.setLetters(0);
            mode.setLanguage(LANGUAGES[pos[0]]);
            updateLettersText(0);
            ((TextView) v).setText(languagesNames[pos[0]]);
        });
    }
}
