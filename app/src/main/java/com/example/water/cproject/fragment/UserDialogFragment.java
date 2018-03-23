package com.example.water.cproject.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.water.cproject.DBResolver;
import com.example.water.cproject.MainActivity;
import com.example.water.cproject.R;

import java.util.List;

/**
 * Created by watering on 18. 3. 23.
 */

public class UserDialogFragment extends DialogFragment {
    private MainActivity mainActivity;
    private AlertDialog.Builder builder;
    private DBResolver resolver;
    private LayoutInflater inflater;
    private UserListener listener;
    private List<String> listsCode;
    private int position;

    public interface UserListener {
        void onWorkComplete();
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mainActivity = (MainActivity)getActivity();
        builder = new AlertDialog.Builder(mainActivity);
        inflater = mainActivity.getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_delete, (ViewGroup) getView());
        TextView textView = view.findViewById(R.id.textView_dialog_delete);
        textView.setText(R.string.delete_code);

        this.resolver = mainActivity.resolver;
        listsCode = resolver.getCodes(mainActivity.getToday());
        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String code = listsCode.get(position);
                resolver.deleteInfoMachine(code);
                resolver.deleteCode(code);
                listener.onWorkComplete();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    public static UserDialogFragment newInstance(int position, UserListener listener) {
        UserDialogFragment fragment = new UserDialogFragment();
        fragment.position = position;
        fragment.listener = listener;

        return fragment;
    }
}
