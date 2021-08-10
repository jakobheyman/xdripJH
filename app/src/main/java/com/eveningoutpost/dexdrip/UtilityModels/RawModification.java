package com.eveningoutpost.dexdrip.UtilityModels;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eveningoutpost.dexdrip.xdrip;
import com.eveningoutpost.dexdrip.Models.JoH;

// Used for modification (multiplication and/or plus/minus) of Libre2 raw values
// Jakob Heyman 2021-05-19

public class RawModification {
    private static SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());

    private static double bgScale() {
        if (prefs.getString("units", "mgdl").equals("mmol")) {
            return Constants.MMOLL_TO_MGDL;
        } else {
            return 1;
        }
    }

    public static double rawmult() {
        double raw_mult = JoH.tolerantParseDouble(prefs.getString("raw_multiplier", "1"), 1d);
        return raw_mult;
    }

    public static double rawadd() {
        double raw_add = JoH.tolerantParseDouble(prefs.getString("raw_addition", "0"), 0d) * bgScale() * 1000 / Constants.LIBRE_MULTIPLIER;
        return raw_add;
    }

    public static int raw_mod(int raw_in) {
        if (raw_in > 0) {
            double rawd = (double) raw_in * rawmult() + rawadd();
            int raw_out = (int) rawd;
            return raw_out;
        } else {
            return raw_in;
        }
    }
}
