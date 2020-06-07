package com.gmail.korex006.mylaundry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class Utils {

    public static void showToastMessage(Context context, String message) {
        // takes a String parameter and uses the string as the content of a toast message
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        View view = toast.getView();

        //Gets the actual oval background of the toast and sets the colour filter
        view.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);

        //Gets the Textview from the toast so it can be editted
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);

        toast.show();
    }

    public static void updateDateText(Calendar date, TextView textView) {
        String myFormat = "EEE, dd-MMM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        // edittext.setText(sdf.format(myCalendar.getTime()));
        textView.setText(sdf.format(date.getTime()));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String formatDate(Calendar date) {
        //format the date to format used in Database
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(date.getTime());
    }

    public static boolean validateEmail(String s) {
        String regex = "(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        return s.toLowerCase().matches(regex);
    }

    public static int calcNumofDeliveryDays(Calendar pickUpDate) {

        // Set Delivery date to 3 days later
        int numofDays = 3;
        int dayofWeek = pickUpDate.get(Calendar.DAY_OF_WEEK);
        switch (dayofWeek) {
            case 7:
                numofDays = 4; // Saturday
            case 6:
                numofDays = 5; // Friday deliver on Wednesday
            case 5:
                numofDays = 5; // Thursday deliver on Tuesday
            default:
                numofDays = 3;

        }
        return numofDays;
    }

}
