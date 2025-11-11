package com.example.smartairapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class OnboardingDialogFragment extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_onboarding, null);
        builder.setView(view);

        TextView textNotice = view.findViewById(R.id.text_notice);
        Button getStartedButton = view.findViewById(R.id.button_get_started);

        String role = "User";
        String uid = "";
        if (getArguments() != null) {
            role = getArguments().getString("role", "User");
            uid = getArguments().getString("uid", "");
        }

        // set message based on role
        switch (role) {
            case "Parent":
                textNotice.setText(R.string.parent_welcome);
                break;
            case "Child":
                textNotice.setText(R.string.child_welcome);
                break;
            case "Provider":
                textNotice.setText(R.string.provider_welcome);
                break;
            default:
                textNotice.setText("Welcome! Let’s get started.");
        }

        final String roleFinal = role;

        getStartedButton.setOnClickListener(v -> {
            if (getActivity() != null) {

                Log.d("OnboardingDialog", "roleFinal='" + roleFinal + "'");

                Intent intent;
                switch (roleFinal) {
                    case "Parent":
                        intent = new Intent(getActivity(), ParentHomeActivity.class);
                        break;
                    case "Provider":
                        intent = new Intent(getActivity(), ProviderHomeActivity.class);
                        break;
                    case "Child":
                        intent = new Intent(getActivity(), ChildHomeActivity.class);
                        break;
                    default:
                        intent = new Intent(getActivity(), MainActivity.class);
                }
                getActivity().startActivity(intent);
                getActivity().finish();
            }
            dismiss();
        });

        return builder.create();
    }
}
