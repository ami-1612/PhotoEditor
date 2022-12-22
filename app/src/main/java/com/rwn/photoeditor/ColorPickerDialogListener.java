package com.rwn.photoeditor;

public interface ColorPickerDialogListener {
    void onColorSelected(int dialogid, int color);

    void onDialogDismissed(int dialogid);
}
