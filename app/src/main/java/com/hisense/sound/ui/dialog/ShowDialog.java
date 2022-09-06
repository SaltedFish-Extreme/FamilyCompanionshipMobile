package com.hisense.sound.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hisense.sound.R;

/**
 * 仿IOS对话框Dialog
 */
public class ShowDialog {
    public static void showSelectDialog(Context context, String title, String msgSub, final View.OnClickListener listener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ProcessDialog)
                .setCancelable(false).create();
        if (((Activity) context).isFinishing()) {
            return;
        }
        alertDialog.show();
        alertDialog.setContentView(R.layout.dialog_alert);
        TextView tvTitle = alertDialog.getWindow().findViewById(R.id.tv_edit_dialog_title);
        TextView tvSubMsg = alertDialog.getWindow().findViewById(R.id.tv_select_dialog_submsg);
        tvTitle.setText(title);
        tvSubMsg.setText(msgSub);
        if (TextUtils.isEmpty(msgSub)) {
            tvSubMsg.setVisibility(View.GONE);
        }
        alertDialog.getWindow().findViewById(R.id.tv_select_dialog_cancel).setOnClickListener(
                view -> alertDialog.dismiss());
        alertDialog.getWindow().findViewById(
                R.id.tv_select_dialog_ok).setOnClickListener(view -> {
            listener.onClick(view);
            alertDialog.dismiss();
        });
    }

    public static void showConfirmDialog(Context context, String title, String message,
                                         final View.OnClickListener listener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ProcessDialog)
                .setCancelable(false).create();
        if (((Activity) context).isFinishing()) {
            return;
        }
        alertDialog.show();
        alertDialog.setContentView(R.layout.dialog_confirm);
        TextView tvTitle = alertDialog.getWindow().findViewById(
                R.id.tv_confirm_dialog_title);
        TextView tvMsg = alertDialog.getWindow().findViewById(
                R.id.tv_confirm_dialog_msg);
        tvTitle.setText(title);
        tvMsg.setText(message);
        alertDialog.getWindow().findViewById(R.id.bt_confirm_dialog_ok).setOnClickListener(
                view -> {
                    if (listener != null) {
                        listener.onClick(view);
                    }
                    alertDialog.dismiss();
                });
        alertDialog.getWindow().findViewById(R.id.bt_confirm_dialog_cancel).setOnClickListener(
                view -> alertDialog.dismiss());
    }

    public static void showSelectDialog(Context context, String title, String message,
                                        String msgSub, final View.OnClickListener listener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ProcessDialog)
                .setCancelable(false).create();
        if (((Activity) context).isFinishing()) {
            return;
        }
        alertDialog.show();
        alertDialog.setContentView(R.layout.dialog_alert);
        TextView tvTitle = alertDialog.getWindow().findViewById(
                R.id.tv_edit_dialog_title);
        TextView tvMsg = alertDialog.getWindow().findViewById(R.id.tv_select_dialog_msg);
        TextView tvSubMsg = alertDialog.getWindow().findViewById(
                R.id.tv_select_dialog_submsg);
        tvTitle.setText(title);
        tvMsg.setText(message);
        tvSubMsg.setText(msgSub);
        if (TextUtils.isEmpty(message)) {
            tvMsg.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(msgSub)) {
            tvSubMsg.setVisibility(View.GONE);
        }
        alertDialog.getWindow().findViewById(R.id.tv_select_dialog_cancel).setOnClickListener(
                view -> alertDialog.dismiss());
        alertDialog.getWindow().findViewById(R.id.tv_select_dialog_ok).setOnClickListener(
                view -> {
                    listener.onClick(view);
                    alertDialog.dismiss();
                });
    }

    public static void showSelectDialog(Context context, String title, String message, String msgSub,
                                        final View.OnClickListener enter,
                                        final View.OnClickListener cancel) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.ProcessDialog)
                .setCancelable(false).create();
        if (((Activity) context).isFinishing()) {
            return;
        }
        alertDialog.show();
        alertDialog.setContentView(R.layout.dialog_alert);
        TextView tvTitle = alertDialog.getWindow().findViewById(
                R.id.tv_edit_dialog_title);
        TextView tvMsg = alertDialog.getWindow().findViewById(R.id.tv_select_dialog_msg);
        TextView tvSubMsg = alertDialog.getWindow().findViewById(
                R.id.tv_select_dialog_submsg);
        tvTitle.setText(title);
        tvMsg.setText(message);
        tvSubMsg.setText(msgSub);
        if (TextUtils.isEmpty(message)) {
            tvMsg.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(msgSub)) {
            tvSubMsg.setVisibility(View.GONE);
        }
        alertDialog.getWindow().findViewById(R.id.tv_select_dialog_ok).setOnClickListener(
                view -> {
                    enter.onClick(view);
                    alertDialog.dismiss();
                });
        alertDialog.getWindow().findViewById(R.id.tv_select_dialog_cancel).setOnClickListener(
                view -> {
                    cancel.onClick(view);
                    alertDialog.dismiss();
                });
    }
}