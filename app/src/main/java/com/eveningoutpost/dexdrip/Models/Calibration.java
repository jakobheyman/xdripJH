package com.eveningoutpost.dexdrip.Models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.eveningoutpost.dexdrip.GcmActivity;
import com.eveningoutpost.dexdrip.Home;
import com.eveningoutpost.dexdrip.ImportedLibraries.dexcom.records.CalRecord;
import com.eveningoutpost.dexdrip.ImportedLibraries.dexcom.records.CalSubrecord;
import com.eveningoutpost.dexdrip.Models.UserError.Log;
import com.eveningoutpost.dexdrip.Services.Ob1G5CollectionService;
import com.eveningoutpost.dexdrip.UtilityModels.BgSendQueue;
import com.eveningoutpost.dexdrip.UtilityModels.CalibrationSendQueue;
import com.eveningoutpost.dexdrip.UtilityModels.CollectionServiceStarter;
import com.eveningoutpost.dexdrip.UtilityModels.Constants;
import com.eveningoutpost.dexdrip.UtilityModels.Notifications;
import com.eveningoutpost.dexdrip.UtilityModels.Pref;
import com.eveningoutpost.dexdrip.calibrations.CalibrationAbstract;
import com.eveningoutpost.dexdrip.calibrations.NativeCalibrationPipe;
import com.eveningoutpost.dexdrip.calibrations.PluggableCalibration;
import com.eveningoutpost.dexdrip.utils.DexCollectionType;
import com.eveningoutpost.dexdrip.xdrip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.eveningoutpost.dexdrip.Models.BgReading.isDataSuitableForDoubleCalibration;
import static com.eveningoutpost.dexdrip.calibrations.PluggableCalibration.newFingerStickData;


class DexParameters extends SlopeParameters {
    DexParameters() {
        LOW_SLOPE_1 = 0.75;
        LOW_SLOPE_2 = 0.70;
        HIGH_SLOPE_1 = 1.5;
        HIGH_SLOPE_2 = 1.6;
        DEFAULT_LOW_SLOPE_LOW = 0.75;
        DEFAULT_LOW_SLOPE_HIGH = 0.70;
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1.5;
        DEFAULT_HIGH_SLOPE_LOW = 1.4;
    }

}


class DexOldSchoolParameters extends SlopeParameters {
    /*
    Previous defaults up until 20th March 2017
     */
    DexOldSchoolParameters() {
        LOW_SLOPE_1 = 0.95;
        LOW_SLOPE_2 = 0.85;
        HIGH_SLOPE_1 = 1.3;
        HIGH_SLOPE_2 = 1.4;
        DEFAULT_LOW_SLOPE_LOW = 1.08;
        DEFAULT_LOW_SLOPE_HIGH = 1.15;
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1.3;
        DEFAULT_HIGH_SLOPE_LOW = 1.2;
    }

}

class DexParametersAdrian extends SlopeParameters {

    /*
    * Other default vlaues and thresholds that can be only activated in settings, when in engineering mode.
    * promoted to be the regular defaults 20th March 2017
    * */

    DexParametersAdrian() {
        LOW_SLOPE_1 = 0.75;
        LOW_SLOPE_2 = 0.70;
        HIGH_SLOPE_1 = 1.3;
        HIGH_SLOPE_2 = 1.4;
        DEFAULT_LOW_SLOPE_LOW = 0.75;
        DEFAULT_LOW_SLOPE_HIGH = 0.70;
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1.3;
        DEFAULT_HIGH_SLOPE_LOW = 1.2;
    }

}

class LiParameters extends SlopeParameters {
    LiParameters() {
        LOW_SLOPE_1 = 1;
        LOW_SLOPE_2 = 1;
        HIGH_SLOPE_1 = 1;
        HIGH_SLOPE_2 = 1;
        DEFAULT_LOW_SLOPE_LOW = 1;
        DEFAULT_LOW_SLOPE_HIGH = 1;
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1;
        DEFAULT_HIGH_SLOPE_LOW = 1;
    }
}

/* Alternate Li Parameters which don't use a fixed slope */
class LiParametersNonFixed extends SlopeParameters {
    LiParametersNonFixed() {
        LOW_SLOPE_1 = 0.55;
        LOW_SLOPE_2 = 0.50;
        HIGH_SLOPE_1 = 1.5;
        HIGH_SLOPE_2 = 1.6;
        DEFAULT_LOW_SLOPE_LOW = 0.55;
        DEFAULT_LOW_SLOPE_HIGH = 0.50;
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1.5;
        DEFAULT_HIGH_SLOPE_LOW = 1.4;
    }

}

class Li2AppParameters extends SlopeParameters {
    Li2AppParameters() {
        LOW_SLOPE_1 = 1;
        LOW_SLOPE_2 = 1;
        HIGH_SLOPE_1 = 1;
        HIGH_SLOPE_2 = 1;
        DEFAULT_LOW_SLOPE_LOW = 1;
        DEFAULT_LOW_SLOPE_HIGH = 1;
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1;
        DEFAULT_HIGH_SLOPE_LOW = 1;
    }

    @Override
    public double restrictIntercept(double intercept) {
        return Math.min(Math.max(intercept, -40), 20);
    }
}

class TestParameters extends SlopeParameters {
    TestParameters() {
        LOW_SLOPE_1 = 0.85; //0.95
        LOW_SLOPE_2 = 0.80; //0.85
        HIGH_SLOPE_1 = 1.3;
        HIGH_SLOPE_2 = 1.4;
        DEFAULT_LOW_SLOPE_LOW = 0.9; //1.08
        DEFAULT_LOW_SLOPE_HIGH = 0.95; //1.15
        DEFAULT_SLOPE = 1;
        DEFAULT_HIGH_SLOPE_HIGH = 1.3;
        DEFAULT_HIGH_SLOPE_LOW = 1.2;
    }
}

/**
 * Created by Emma Black on 10/29/14.
 */
@Table(name = "Calibration", id = BaseColumns._ID)
public class Calibration extends Model {
    private final static String TAG = Calibration.class.getSimpleName();
    private final static double note_only_marker = 0.000001d;

    @Expose
    @Column(name = "timestamp", index = true)
    public long timestamp;

    @Expose
    @Column(name = "sensor_age_at_time_of_estimation")
    public double sensor_age_at_time_of_estimation;

    @Column(name = "sensor", index = true)
    public Sensor sensor;

    @Expose
    @Column(name = "bg")
    public double bg;

    @Expose
    @Column(name = "raw_value")
    public double raw_value;
//
//    @Expose
//    @Column(name = "filtered_value")
//    public double filtered_value;

    @Expose
    @Column(name = "adjusted_raw_value")
    public double adjusted_raw_value;

    @Expose
    @Column(name = "sensor_confidence")
    public double sensor_confidence;

    @Expose
    @Column(name = "slope_confidence")
    public double slope_confidence;

    @Expose
    @Column(name = "raw_timestamp")
    public long raw_timestamp;

    @Expose
    @Column(name = "slope")
    public double slope;

    @Expose
    @Column(name = "intercept")
    public double intercept;

    @Expose
    @Column(name = "distance_from_estimate")
    public double distance_from_estimate;

    @Expose
    @Column(name = "estimate_raw_at_time_of_calibration")
    public double estimate_raw_at_time_of_calibration;

    @Expose
    @Column(name = "estimate_bg_at_time_of_calibration")
    public double estimate_bg_at_time_of_calibration;

    @Expose
    @Column(name = "uuid", index = true)
    public String uuid;

    @Expose
    @Column(name = "sensor_uuid", index = true)
    public String sensor_uuid;

    @Expose
    @Column(name = "possible_bad")
    public Boolean possible_bad;

    @Expose
    @Column(name = "check_in")
    public boolean check_in;

    @Expose
    @Column(name = "first_decay")
    public double first_decay;

    @Expose
    @Column(name = "second_decay")
    public double second_decay;

    @Expose
    @Column(name = "first_slope")
    public double first_slope;

    @Expose
    @Column(name = "second_slope")
    public double second_slope;

    @Expose
    @Column(name = "first_intercept")
    public double first_intercept;

    @Expose
    @Column(name = "second_intercept")
    public double second_intercept;

    @Expose
    @Column(name = "first_scale")
    public double first_scale;

    @Expose
    @Column(name = "second_scale")
    public double second_scale;

    public static void initialCalibration(double bg1, double bg2, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = prefs.getString("units", "mgdl");
        if (unit.compareTo("mgdl") != 0) {
            bg1 = bg1 * Constants.MMOLL_TO_MGDL;
            bg2 = bg2 * Constants.MMOLL_TO_MGDL;
        }

        JoH.clearCache();
        final Calibration higherCalibration = new Calibration();
        final Calibration lowerCalibration = new Calibration();
        final Sensor sensor = Sensor.currentSensor();
        final List<BgReading> bgReadings = BgReading.latest_by_size(3);

        // don't allow initial calibration if data would be stale (but still use data for native mode)
            if ((bgReadings == null) || (bgReadings.size() != 3) || !isDataSuitableForDoubleCalibration() ){

            if (Ob1G5CollectionService.usingNativeMode()) {
                JoH.static_toast_long("Sending Blood Tests to Transmitter"); // TODO extract string
                BloodTest.create(JoH.tsl() - (Constants.SECOND_IN_MS * 30), bg1, "Initial Calibration");
                BloodTest.create(JoH.tsl(), bg2, "Initial Calibration");

                if (!Pref.getBooleanDefaultFalse("bluetooth_meter_for_calibrations_auto")) {
                    // blood tests above don't automatically become part of calibration pipe if this setting is unset so do here
                    NativeCalibrationPipe.addCalibration((int) bg1, JoH.tsl() - (Constants.SECOND_IN_MS * 30));
                    NativeCalibrationPipe.addCalibration((int) bg2, JoH.tsl());
                }

            } else {
                UserError.Log.wtf(TAG, "Did not find 3 readings for initial calibration - aborting");
                JoH.static_toast_long("Not enough recent sensor data! - cancelling!");
            }
            return;
        }


        BgReading bgReading1 = bgReadings.get(0);
        BgReading bgReading2 = bgReadings.get(1);

        if (!SensorSanity.isRawValueSane(bgReading1.raw_data) || (!SensorSanity.isRawValueSane(bgReading2.raw_data))) {
            final String msg = "Sensor raw data is outside sane range! Cannot calibrate: " + bgReading1.raw_data + " " + bgReading2.raw_data;
            UserError.Log.wtf(TAG, msg);
            JoH.static_toast_long(msg);
            return;
        }

        clear_all_existing_calibrations();

        BgReading highBgReading;
        BgReading lowBgReading;
        double higher_bg = Math.max(bg1, bg2);
        double lower_bg = Math.min(bg1, bg2);

        // TODO This should be reworked in the future as it doesn't really make sense
        if (bgReading1.raw_data > bgReading2.raw_data) {
            highBgReading = bgReading1;
            lowBgReading = bgReading2;
        } else {
            highBgReading = bgReading2;
            lowBgReading = bgReading1;
        }

        higherCalibration.bg = higher_bg;
        higherCalibration.slope = 1;
        higherCalibration.intercept = higher_bg;
        higherCalibration.sensor = sensor;
        higherCalibration.estimate_raw_at_time_of_calibration = highBgReading.age_adjusted_raw_value;
        higherCalibration.adjusted_raw_value = highBgReading.age_adjusted_raw_value;
        higherCalibration.raw_value = highBgReading.raw_data;
        higherCalibration.raw_timestamp = highBgReading.timestamp;
        higherCalibration.save();

        highBgReading.calculated_value = higher_bg;
        highBgReading.calibration_flag = true;
        highBgReading.calibration = higherCalibration;
        highBgReading.save();
        higherCalibration.save();

        lowerCalibration.bg = lower_bg;
        lowerCalibration.slope = 1;
        lowerCalibration.intercept = lower_bg;
        lowerCalibration.sensor = sensor;
        lowerCalibration.estimate_raw_at_time_of_calibration = lowBgReading.age_adjusted_raw_value;
        lowerCalibration.adjusted_raw_value = lowBgReading.age_adjusted_raw_value;
        lowerCalibration.raw_value = lowBgReading.raw_data;
        lowerCalibration.raw_timestamp = lowBgReading.timestamp;
        lowerCalibration.save();

        lowBgReading.calculated_value = lower_bg;
        lowBgReading.calibration_flag = true;
        lowBgReading.calibration = lowerCalibration;
        lowBgReading.save();
        lowerCalibration.save();

        JoH.clearCache();
        highBgReading.find_new_curve();
        highBgReading.find_new_raw_curve();
        lowBgReading.find_new_curve();
        lowBgReading.find_new_raw_curve();

        JoH.clearCache();


        NativeCalibrationPipe.addCalibration((int) bg1, JoH.tsl() - (Constants.SECOND_IN_MS * 30));
        NativeCalibrationPipe.addCalibration((int) bg2, JoH.tsl());


        final List<Calibration> calibrations = new ArrayList<Calibration>();
        calibrations.add(lowerCalibration);
        calibrations.add(higherCalibration);

        for (Calibration calibration : calibrations) {
            calibration.timestamp = new Date().getTime();
            calibration.sensor_uuid = sensor.uuid;
            calibration.slope_confidence = .5;
            calibration.distance_from_estimate = 0;
            calibration.check_in = false;
            calibration.sensor_confidence = ((-0.0018 * calibration.bg * calibration.bg) + (0.6657 * calibration.bg) + 36.7505) / 100;

            calibration.sensor_age_at_time_of_estimation = calibration.timestamp - sensor.started_at;
            calibration.uuid = UUID.randomUUID().toString();
            calibration.save();
            JoH.clearCache();
            calculate_w_l_s(calibration.sensor_age_at_time_of_estimation, (double) calibration.timestamp);
            newFingerStickData();
            CalibrationSendQueue.addToQueue(calibration, context);
        }
        JoH.clearCache();
        if (!Ob1G5CollectionService.usingNativeMode()) {
            adjustRecentBgReadings(5);
        }
        CalibrationRequest.createOffset(lowerCalibration.bg, 35);
        Notifications.staticUpdateNotification();
    }

    //Create Calibration Checkin Dexcom Bluetooth Share
    public static void create(CalRecord[] calRecords, long addativeOffset, Context context) {
        create(calRecords, context, false, addativeOffset);
    }

    public static void create(CalRecord[] calRecords, Context context) {
        create(calRecords, context, false, 0);
    }

    // Bluetooth Share
    public static void create(CalRecord[] calRecords, Context context, boolean override, long addativeOffset) {
        //TODO: Change calibration.last and other queries to order calibrations by timestamp rather than ID
        Log.i("CALIBRATION-CHECK-IN: ", "Creating Calibration Record");
        Sensor sensor = Sensor.currentSensor();
        CalRecord firstCalRecord = calRecords[0];
        CalRecord secondCalRecord = calRecords[0];
//        CalRecord secondCalRecord = calRecords[calRecords.length - 1];
        //TODO: Figgure out how the ratio between the two is determined
        double calSlope = ((secondCalRecord.getScale() / secondCalRecord.getSlope()) + (3 * firstCalRecord.getScale() / firstCalRecord.getSlope())) * 250;

        double calIntercept = (((secondCalRecord.getScale() * secondCalRecord.getIntercept()) / secondCalRecord.getSlope()) + ((3 * firstCalRecord.getScale() * firstCalRecord.getIntercept()) / firstCalRecord.getSlope())) / -4;
        if (sensor != null) {
            for (int i = 0; i < firstCalRecord.getCalSubrecords().length - 1; i++) {
                if (((firstCalRecord.getCalSubrecords()[i] != null && Calibration.is_new(firstCalRecord.getCalSubrecords()[i], addativeOffset))) || (i == 0 && override)) {
                    CalSubrecord calSubrecord = firstCalRecord.getCalSubrecords()[i];

                    Calibration calibration = new Calibration();
                    calibration.bg = calSubrecord.getCalBGL();
                    calibration.timestamp = calSubrecord.getDateEntered().getTime() + addativeOffset;
                    calibration.raw_timestamp = calibration.timestamp;
                    if (calibration.timestamp > new Date().getTime()) {
                        Log.d(TAG, "ERROR - Calibration timestamp is from the future, wont save!");
                        return;
                    }
                    calibration.raw_value = calSubrecord.getCalRaw() / 1000;
                    calibration.slope = calSlope;
                    calibration.intercept = calIntercept;

                    calibration.sensor_confidence = ((-0.0018 * calibration.bg * calibration.bg) + (0.6657 * calibration.bg) + 36.7505) / 100;
                    if (calibration.sensor_confidence <= 0) {
                        calibration.sensor_confidence = 0;
                    }
                    calibration.slope_confidence = 0.8; //TODO: query backwards to find this value near the timestamp
                    calibration.estimate_raw_at_time_of_calibration = calSubrecord.getCalRaw() / 1000;
                    calibration.sensor = sensor;
                    calibration.sensor_age_at_time_of_estimation = calibration.timestamp - sensor.started_at;
                    calibration.uuid = UUID.randomUUID().toString();
                    calibration.sensor_uuid = sensor.uuid;
                    calibration.check_in = true;

                    calibration.first_decay = firstCalRecord.getDecay();
                    calibration.second_decay = secondCalRecord.getDecay();
                    calibration.first_slope = firstCalRecord.getSlope();
                    calibration.second_slope = secondCalRecord.getSlope();
                    calibration.first_scale = firstCalRecord.getScale();
                    calibration.second_scale = secondCalRecord.getScale();
                    calibration.first_intercept = firstCalRecord.getIntercept();
                    calibration.second_intercept = secondCalRecord.getIntercept();

                    calibration.save();
                    CalibrationSendQueue.addToQueue(calibration, context);
                    Calibration.requestCalibrationIfRangeTooNarrow();
                    newFingerStickData();
                }
            }
            if (firstCalRecord.getCalSubrecords()[0] != null && firstCalRecord.getCalSubrecords()[2] == null) {
                if (Calibration.latest(2).size() == 1) {
                    Calibration.create(calRecords, context, true, 0);
                }
            }
            Notifications.start();
        }
    }

    public static boolean is_new(CalSubrecord calSubrecord, long addativeOffset) {
        Sensor sensor = Sensor.currentSensor();
        Calibration calibration = new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("timestamp <= ?", calSubrecord.getDateEntered().getTime() + addativeOffset + (1000 * 60 * 2))
                .orderBy("timestamp desc")
                .executeSingle();
        if (calibration != null && Math.abs(calibration.timestamp - (calSubrecord.getDateEntered().getTime() + addativeOffset)) < (4 * 60 * 1000)) {
            Log.d("CAL CHECK IN ", "Already have that calibration!");
            return false;
        } else {
            Log.d("CAL CHECK IN ", "Looks like a new calibration!");
            return true;
        }
    }

    public static Calibration getForTimestamp(double timestamp) {
        Sensor sensor = Sensor.currentSensor();
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp <= ?", (timestamp + (30 * 1000))) // 30 seconds padding
                .orderBy("timestamp desc")
                .executeSingle();
    }

    public static Calibration getAfterTimestamp(double timestamp) {
        Sensor sensor = Sensor.currentSensor();
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp >= ?", timestamp)
                .orderBy("timestamp asc")
                .executeSingle();
    }

    public static Calibration getByTimestamp(double timestamp) {//KS
        Sensor sensor = Sensor.currentSensor();
        if(sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("timestamp = ?", timestamp)
                .executeSingle();
    }

    public static Double getConvertedBg(double bg) {
        final String unit = Pref.getString("units", "mgdl");
        if (unit.compareTo("mgdl") != 0) {
            bg = bg * Constants.MMOLL_TO_MGDL;
        }
        if ((bg < 10) || (bg > 1000)) {
            return null;
        }
        return bg;
    }

    // without timeoffset
    public static Calibration create(double bg, Context context) {
        return create(bg, 0, context);
    }

    public static Calibration create(double bg, long timeoffset, Context context) {
        return create(bg, timeoffset, context, false, 0);
    }

    // regular calibration
    // changed from original xDrip+
    public static Calibration create(double bg, long timeoffset, Context context, boolean note_only, long estimatedInterstitialLagSeconds) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String unit = prefs.getString("units", "mgdl");
        final boolean adjustPast = prefs.getBoolean("rewrite_history", false);

        final Double result = getConvertedBg(bg);
        if (result == null) {
            Log.wtf(TAG, "Invalid out of range calibration glucose mg/dl value of: " + bg);
            JoH.static_toast_long("Calibration out of range: " + bg + " mg/dl");
            return null;
        }

        bg = result; // unbox result

        if (!note_only) CalibrationRequest.clearAll();
        final Calibration calibration = new Calibration();
        Sensor sensor = Sensor.currentSensor();

        boolean is_follower = prefs.getString("dex_collection_method", "").equals("Follower");
        if ((sensor == null)
                && (is_follower)) {
            Sensor.create(Math.round(JoH.ts())); // no sensor? no problem, create virtual one for follower
            sensor = Sensor.currentSensor();
        }

        if (sensor != null) {
            BgReading bgReading = null;
            if (timeoffset == 0) {
                bgReading = BgReading.last(is_follower);
            } else {
                // get closest bg reading we can find with a cut off at 15 minutes max time
                bgReading = BgReading.getForPreciseTimestamp(new Date().getTime() - ((timeoffset - estimatedInterstitialLagSeconds) * 1000 ), (15 * 60 * 1000));
            }
            if (bgReading != null) {
                if (SensorSanity.isRawValueSane(bgReading.raw_data, DexCollectionType.getDexCollectionType(), true)) {
                    calibration.sensor = sensor;
                    calibration.bg = bg;
                    calibration.check_in = false;
                    calibration.timestamp = new Date().getTime() - (timeoffset * 1000); //  potential historical bg readings
                    calibration.raw_value = bgReading.raw_data;
                    calibration.adjusted_raw_value = bgReading.age_adjusted_raw_value;
                    calibration.sensor_uuid = sensor.uuid;
                    calibration.slope_confidence = Math.min(Math.max(((4 - Math.abs((bgReading.calculated_value_slope) * 60000)) / 4), 0.01), 1);
                    calibration.raw_timestamp = bgReading.timestamp;
                    calibration.estimate_raw_at_time_of_calibration = bgReading.age_adjusted_raw_value;
                    calibration.distance_from_estimate = Math.abs(calibration.bg - bgReading.calculated_value);
                    if (!note_only) {
                        calibration.sensor_confidence = Math.max(((-0.0018 * bg * bg) + (0.6657 * bg) + 36.7505) / 100, 0.01);
                    } else {
                        calibration.sensor_confidence = 0; // exclude from calibrations but show on graph
                        calibration.slope_confidence = note_only_marker; // this is a bit ugly
                        calibration.slope = 0;
                        calibration.intercept = 0;
                    }
                    calibration.sensor_age_at_time_of_estimation = calibration.timestamp - sensor.started_at;
                    calibration.uuid = UUID.randomUUID().toString();

                    if (!SensorSanity.isRawValueSane(calibration.estimate_raw_at_time_of_calibration, true)) {
                        JoH.static_toast_long("Estimated raw value out of range - cannot calibrate");
                        return null;
                    }

                    calibration.save();

                    if (!note_only) {
                        bgReading.calibration = calibration;
                        bgReading.calibration_flag = true;
                        bgReading.save();
                    }

                    if ((!is_follower) && (!note_only)) {
                        BgSendQueue.handleNewBgReading(bgReading, "update", context);
                        if (adjustPast) {
                            calculate_w_l_s_multiple(calibration.sensor_age_at_time_of_estimation);
                        } else {
                            calculate_w_l_s(calibration.sensor_age_at_time_of_estimation, (double) calibration.timestamp);
                            Log.e(TAG, "Calibrated 1 point");
                        }
                        CalibrationSendQueue.addToQueue(calibration, context);
                        BgReading.pushBgReadingSyncToWatch(bgReading, false);
                        if (adjustPast && !Ob1G5CollectionService.usingNativeMode()) {
                            adjustBgReadings(calibration.sensor_age_at_time_of_estimation);
                        }
                        newFingerStickData();
                    } else {
                        Log.d(TAG, "Follower mode or note so not processing calibration deeply");
                    }
                } else {
                    final String msg = "Sensor data fails sanity test - Cannot Calibrate! raw:" + bgReading.raw_data;
                    UserError.Log.e(TAG, msg);
                    JoH.static_toast_long(msg);
                }
            } else {
                // we couldn't get a reading close enough to the calibration timestamp
                if (!is_follower) {
                    JoH.static_toast(context, "No close enough reading for Calib (15 min)", Toast.LENGTH_LONG);
                }
            }
        } else {
            Log.d("CALIBRATION", "No sensor, cant save!");
        }
        return Calibration.last();
    }

    public static List<Calibration> allForSensorInLastFiveDays() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 5)))
                .orderBy("timestamp desc")
                .execute();
    }

    private synchronized static void calculate_w_l_s_multiple(double calib_point_in_time) {
        List<Calibration> calibrations = allForSensorWithWeightX(0, 1, calib_point_in_time);
        for (Calibration calibration : calibrations) {
            calculate_w_l_s(calibration.sensor_age_at_time_of_estimation, (double) calibration.timestamp);
        }
        if (calibrations.size() > 1) {
            Log.e(TAG, "Calibrated " + calibrations.size() + " points");
        } else if (calibrations.size() == 1) {
            Log.e(TAG, "Calibrated 1 point");
        }
    }

    // changed from original xDrip+
    private synchronized static void calculate_w_l_s(double calib_point_in_time, double calib_point_in_time_fulltime) {
        if (Sensor.isActive()) {
            double l = 0;
            double m = 0;
            double n = 0;
            double p = 0;
            double q = 0;
            double w;

            ActiveAndroid.clearCache();
            List<Calibration> calibrations = allForSensorWithWeightX(0, 1, calib_point_in_time);
            
            if (calibrations == null) {
                Log.e(TAG, "Somehow ended up with null calibration list!");
                Home.toaststatic("Somehow ended up with null calibration list!");
                return;
            }

            ActiveAndroid.clearCache();
            if (calibrations.size() == 1) {
                final Calibration calibration = Calibration.getForTimestamp(calib_point_in_time_fulltime);
                ActiveAndroid.clearCache();
                calibration.slope = 1;
                calibration.intercept = calibration.bg - (calibration.raw_value * calibration.slope);
                calibration.save();
                newFingerStickData();
            } else {
                for (Calibration calibration : calibrations) {
                    w = calibration.calculateWeight(calib_point_in_time);
                    l += (w);
                    m += (w * calibration.estimate_raw_at_time_of_calibration);
                    n += (w * calibration.estimate_raw_at_time_of_calibration * calibration.estimate_raw_at_time_of_calibration);
                    p += (w * calibration.bg);
                    q += (w * calibration.estimate_raw_at_time_of_calibration * calibration.bg);
                }

                double d = (l * n) - (m * m);
                final Calibration calibration = Calibration.getForTimestamp(calib_point_in_time_fulltime);
                ActiveAndroid.clearCache();
                calibration.intercept = ((n * p) - (m * q)) / d;
                calibration.slope = ((l * q) - (m * p)) / d;
                Log.d(TAG, "Calibration slope debug: slope:" + calibration.slope + " q:" + q + " m:" + m + " p:" + p + " d:" + d);
                Log.d(TAG, "Calculated Calibration Slope: " + calibration.slope);
                Log.d(TAG, "Calculated Calibration intercept: " + calibration.intercept);

                // sanity check result
                if (Double.isInfinite(calibration.slope)
                        ||(Double.isNaN(calibration.slope))
                        ||(Double.isInfinite(calibration.intercept))
                        ||(Double.isNaN(calibration.intercept))) {
                    calibration.sensor_confidence = 0;
                    calibration.slope_confidence = 0;
                    Home.toaststaticnext("Got invalid impossible slope calibration!");
                    calibration.save(); // Save nulled record, lastValid should protect from bad calibrations
                    newFingerStickData();
                }

                if ((calibration.slope == 0) && (calibration.intercept == 0)) {
                    calibration.sensor_confidence = 0;
                    calibration.slope_confidence = 0;
                    Home.toaststaticnext("Got invalid zero slope calibration!");
                    calibration.save(); // Save nulled record, lastValid should protect from bad calibrations
                    newFingerStickData();
                } else {
                    calibration.save();
                    newFingerStickData();
                }
            }
        } else {
            Log.d(TAG, "NO Current active sensor found!!");
        }
    }

    @NonNull
    private static SlopeParameters getSlopeParameters() {

        if (CollectionServiceStarter.isLibre2App((Context)null)) {
            return new Li2AppParameters();
        }

        if (CollectionServiceStarter.isLimitter()) {
            if (Pref.getBooleanDefaultFalse("use_non_fixed_li_parameters")) {
                return new LiParametersNonFixed();
            } else {
                return new LiParameters();
            }
        }
        // open question about parameters used with LibreAlarm

        if (Pref.getBooleanDefaultFalse("engineering_mode") && Pref.getBooleanDefaultFalse("old_school_calibration_mode")) {
            JoH.static_toast_long("Using old pre-2017 calibration mode!");
            return new DexOldSchoolParameters();
        }

        return new DexParameters();
    }

    // here be dragons.. at time of writing estimate_bg_at_time_of_calibration is never written to and the possible_bad logic below looks backwards but
    // will never fire because the bg_at_time_of_calibration is not set.
    private double slopeOOBHandler(int status) {

        final SlopeParameters sParams = getSlopeParameters();

        // If the last slope was reasonable and reasonably close, use that, otherwise use a slope that may be a little steep, but its best to play it safe when uncertain
        final List<Calibration> calibrations = Calibration.latest(3);
        final Calibration thisCalibration = calibrations.get(0);
        if (status == 0) {
            if (calibrations.size() == 3) {
                if ((Math.abs(thisCalibration.bg - thisCalibration.estimate_bg_at_time_of_calibration) < 30)
                        && (calibrations.get(1).slope != 0)
                        && (calibrations.get(1).possible_bad != null && calibrations.get(1).possible_bad == true)) {
                    return calibrations.get(1).slope;
                } else {
                    return Math.max(((-0.048) * (thisCalibration.sensor_age_at_time_of_estimation / (60000 * 60 * 24))) + 1.1, sParams.getDefaultLowSlopeLow());
                }
            } else if (calibrations.size() == 2) {
                return Math.max(((-0.048) * (thisCalibration.sensor_age_at_time_of_estimation / (60000 * 60 * 24))) + 1.1, sParams.getDefaultLowSlopeHigh());
            }
            return sParams.getDefaultSlope();
        } else {
            if (calibrations.size() == 3) {
                if ((Math.abs(thisCalibration.bg - thisCalibration.estimate_bg_at_time_of_calibration) < 30)
                        && (calibrations.get(1).slope != 0)
                        && (calibrations.get(1).possible_bad != null && calibrations.get(1).possible_bad == true)) {
                    return calibrations.get(1).slope;
                } else {
                    return sParams.getDefaultHighSlopeHigh();
                }
            } else if (calibrations.size() == 2) {
                return sParams.getDefaulHighSlopeLow();
            }
        }
        return sParams.getDefaultSlope();
    }

    private static List<Calibration> calibrations_for_sensor(Sensor sensor) {
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ?", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp desc")
                .execute();
    }

    // changed from original xDrip+
    private double calculateWeight(double calib_point_in_time) {
        // calibration weight decreases linearly with time from calibration point
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());
        double calibw_days = JoH.tolerantParseDouble(prefs.getString("calibration_weight_days", "5"), 5d);
        double calibw_days_init = JoH.tolerantParseDouble(prefs.getString("calibration_weight_days_initial", "1"), 1d);
        double calibw_days_init_trans = JoH.tolerantParseDouble(prefs.getString("calibration_weight_days_initial_transition", "2"), 2d);
        // calib_timespan1 and calib_timespan2 used to enable historical re-calculations with subsequent calibration points added
        double calib_timespan1 = 60000 * 60 * 24 * calibw_days - Math.max(60000 * 60 * 24 * (calibw_days - calibw_days_init) - (60000 * 60 * 24 * (calibw_days - calibw_days_init) * calib_point_in_time / (60000 * 60 * 24 * calibw_days_init_trans)), 0);
        double calib_timespan2 = 60000 * 60 * 24 * calibw_days - Math.max(60000 * 60 * 24 * (calibw_days - calibw_days_init) - (60000 * 60 * 24 * (calibw_days - calibw_days_init) * sensor_age_at_time_of_estimation / (60000 * 60 * 24 * calibw_days_init_trans)), 0);
        double calib_timespan = Math.min(calib_timespan1, calib_timespan2);
        return Math.max(1 - (Math.abs(calib_point_in_time - sensor_age_at_time_of_estimation) / calib_timespan), 0);
    }

    public static void adjustBgReadings(double calib_point_in_time) {
        // pick out all calibrations with weight > 0
        List<Calibration> calibrations = allForSensorWithWeightX(0, 1, calib_point_in_time);
        int recalc_n = 0;
        // if there are bg points beyond the last calibration point in time
        if (BgReading.last().timestamp > calibrations.get(0).timestamp) {
            // use one subsequent calibration point if existing or use the last calibration for all bg points past the last calibration
            Calibration postcalibration = getAfterTimestamp((double) (calibrations.get(0).timestamp + 60000)); 
            if (postcalibration == null) {
                recalc_n = recalc_n + recalcBgReadings(calibrations.get(0).timestamp, JoH.tsl(), calibrations.get(0).slope, calibrations.get(0).slope, calibrations.get(0).intercept, calibrations.get(0).intercept);
            } else {
                recalc_n = recalc_n + recalcBgReadings(calibrations.get(0).timestamp, postcalibration.timestamp, calibrations.get(0).slope, postcalibration.slope, calibrations.get(0).intercept, postcalibration.intercept);
            }
        }
        // go through pair of calibration points for all bg points within recalibrated points
        int caln = calibrations.size();
        for (int i = 1; i < caln; i++) {
            recalc_n = recalc_n + recalcBgReadings(calibrations.get(i).timestamp, calibrations.get(i-1).timestamp, calibrations.get(i).slope, calibrations.get(i-1).slope, calibrations.get(i).intercept, calibrations.get(i-1).intercept);
        }
        // use one prior calibration point if existing or use the first calibration for all initial bg points of the sensor
        Calibration precalibration = getForTimestamp((double) (calibrations.get(caln-1).timestamp - 60000));
        if (precalibration != null) {
            recalc_n = recalc_n + recalcBgReadings(precalibration.timestamp, calibrations.get(caln-1).timestamp, precalibration.slope, calibrations.get(caln-1).slope, precalibration.intercept, calibrations.get(caln-1).intercept);
        } else {
            recalc_n = recalc_n + recalcBgReadings(Sensor.currentSensor().started_at, calibrations.get(caln-1).timestamp, calibrations.get(caln-1).slope, calibrations.get(caln-1).slope, calibrations.get(caln-1).intercept, calibrations.get(caln-1).intercept);
        }
        Log.e(TAG, "Recalculated " + recalc_n + " bg points");
    }

    public static int recalcBgReadings(long time1, long time2, double slope1, double slope2, double intercept1, double intercept2) {
        // pick out bg data based on time1 and time2
        final List<BgReading> bgReadings = BgReading.latestForGraph(25000, time1, time2);
        // convert time1 and time2 to doubles
        double time1d = (double) time1;
        double time2d = (double) time2;
        // go through bg points, calculate slope and intercept (linear interpolation over time), and calculate new bg values
        for (BgReading bgReading : bgReadings) {
            double time_fraction = ((double) bgReading.timestamp - time1d) / (time2d - time1d);
            double slope = slope1 + (time_fraction * (slope2 - slope1));
            double intercept = intercept1 + (time_fraction * (intercept2 - intercept1));
            bgReading.calculated_value = (slope * bgReading.age_adjusted_raw_value) + intercept;
            bgReading.filtered_calculated_value = (slope * bgReading.filtered_data) + intercept;
            bgReading.save();
        }
        return bgReadings.size();
    }

    public synchronized static void reenable(Calibration calibration) {
        // first calculate slope_confidence and sensor_confidence to include the calibration point in allForSensorWithWeightX
        BgReading bgReading = null;
        bgReading = BgReading.getForPreciseTimestamp(calibration.timestamp, (15 * 60 * 1000));
        calibration.slope_confidence = Math.min(Math.max(((4 - Math.abs((bgReading.calculated_value_slope) * 60000)) / 4), 0.01), 1);
        calibration.sensor_confidence = Math.max(((-0.0018 * calibration.bg * calibration.bg) + (0.6657 * calibration.bg) + 36.7505) / 100, 0.01);
        calibration.save();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());
        if (prefs.getBoolean("rewrite_history", false)) {
            // recalibrate calibration points
            calculate_w_l_s_multiple(calibration.sensor_age_at_time_of_estimation);
            // recalculate bg data
            adjustBgReadings(calibration.sensor_age_at_time_of_estimation);
        } else {
            // recalibrate calibration point
            calculate_w_l_s(calibration.sensor_age_at_time_of_estimation, (double) calibration.timestamp);
            Log.e(TAG, "Calibrated 1 point");
        }
    }

    public static void adjustRecentBgReadings(int adjustCount) {
        //TODO: add some handling around calibration overrides as they come out looking a bit funky
        final List<Calibration> calibrations = Calibration.latest(3);
        if (calibrations == null) {
            Log.wtf(TAG, "Calibrations is null in adjustRecentBgReadings");
            return;
        }

        final List<BgReading> bgReadings = BgReading.latestUnCalculated(adjustCount);
        if (bgReadings == null) {
            Log.wtf(TAG, "bgReadings is null in adjustRecentBgReadings");
            return;
        }

        // ongoing calibration
        if (calibrations.size() >= 3) {
            final int denom = bgReadings.size();
            //Calibration latestCalibration = calibrations.get(0);
            try {
                final Calibration latestCalibration = Calibration.lastValid();
                int i = 0;
                for (BgReading bgReading : bgReadings) {
                    if (bgReading.calibration != null) {
                        final double oldYValue = bgReading.calculated_value;
                        final double newYvalue = (bgReading.age_adjusted_raw_value * latestCalibration.slope) + latestCalibration.intercept;
                        final double new_calculated_value = ((newYvalue * (denom - i)) + (oldYValue * (i))) / denom;
                        // if filtered == raw then rewrite them both because this would not happen if filtered data was from real source
                        if (bgReading.filtered_calculated_value == bgReading.calculated_value) {
                            bgReading.filtered_calculated_value = new_calculated_value;
                        }
                        bgReading.calculated_value = new_calculated_value;

                        bgReading.save();
                        BgReading.pushBgReadingSyncToWatch(bgReading, false);
                        i += 1;
                    } else {
                        Log.d(TAG, "History Rewrite: Ignoring BgReading without calibration from: " + JoH.dateTimeText(bgReading.timestamp));
                    }
                }
            } catch (NullPointerException e) {
                Log.wtf(TAG, "Null pointer in AdjustRecentReadings >=3: " + e);
            }
            // initial calibration
        } else if (calibrations.size() == 2) {
            //Calibration latestCalibration = calibrations.get(0);
            try {
                final Calibration latestCalibration = Calibration.lastValid();
                for (BgReading bgReading : bgReadings) {
                    final double newYvalue = (bgReading.age_adjusted_raw_value * latestCalibration.slope) + latestCalibration.intercept;
                    if (bgReading.filtered_calculated_value == bgReading.calculated_value) {
                        bgReading.filtered_calculated_value = newYvalue;
                    }
                    bgReading.calculated_value = newYvalue;
                    BgReading.updateCalculatedValueToWithinMinMax(bgReading);
                    bgReading.save();
                    BgReading.pushBgReadingSyncToWatch(bgReading, false);
                }
            } catch (NullPointerException e) {
                Log.wtf(TAG, "Null pointer in AdjustRecentReadings ==2: " + e);
            }
        }

        try {
            // TODO this method call is probably only needed when we are called for initial calibration, it should probably be moved
            bgReadings.get(0).find_new_raw_curve();
            bgReadings.get(0).find_new_curve();
            BgReading.pushBgReadingSyncToWatch(bgReadings.get(0), false);
        } catch (NullPointerException e) {
            Log.wtf(TAG, "Got null pointer exception in adjustRecentBgReadings");
        }
    }

    public void rawValueOverride(double rawValue, Context context) {
        estimate_raw_at_time_of_calibration = rawValue;
        save();
        final Calibration latestCalibration = Calibration.lastValid();
        calculate_w_l_s(latestCalibration.sensor_age_at_time_of_estimation, (double) latestCalibration.timestamp);
        CalibrationSendQueue.addToQueue(this, context);
    }

    public static void requestCalibrationIfRangeTooNarrow() {
        double max = Calibration.max_recent();
        double min = Calibration.min_recent();
        if ((max - min) < 55) {
            double avg = ((min + max) / 2);
            double dist = max - avg;
            CalibrationRequest.createOffset(avg, dist + 20);
        }
    }

    public static void clear_all_existing_calibrations() {
        CalibrationRequest.clearAll();
        List<Calibration> pastCalibrations = Calibration.allForSensor();
        if (pastCalibrations != null) {
            for (Calibration calibration : pastCalibrations) {
                calibration.slope_confidence = 0;
                calibration.sensor_confidence = 0;
                calibration.save();
                newFingerStickData();
            }
        }
    }

    public static long msSinceLastCalibration() {
        final Calibration calibration = lastValid();
        if (calibration == null) return 86400000000L;
        return JoH.msSince(calibration.timestamp);
    }

    public static void clearLastCalibration() {
        CalibrationRequest.clearAll();
        Log.d(TAG, "Trying to clear last calibration");
        Calibration calibration = Calibration.last();
        if (calibration != null) {
            calibration.invalidate();
            CalibrationSendQueue.addToQueue(calibration, xdrip.getAppContext());
            newFingerStickData();
            // recalculate bg data
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());
            if (prefs.getBoolean("rewrite_history", false)) {
                calculate_w_l_s_multiple(calibration.sensor_age_at_time_of_estimation);
                adjustBgReadings(calibration.sensor_age_at_time_of_estimation);
            }
        }
    }

    public static void clearCalibrationByUUID(String uuid) {
        final Calibration calibration = Calibration.byuuid(uuid);
        if (calibration != null) {
            CalibrationRequest.clearAll();
            Log.d(TAG, "Trying to clear last calibration: " + uuid);
            calibration.invalidate();
            CalibrationSendQueue.addToQueue(calibration, xdrip.getAppContext());
            newFingerStickData();
            // recalculate bg data
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());
            if (prefs.getBoolean("rewrite_history", false)) {
                calculate_w_l_s_multiple(calibration.sensor_age_at_time_of_estimation);
                adjustBgReadings(calibration.sensor_age_at_time_of_estimation);
            }
        } else {
            Log.d(TAG,"Could not find calibration to clear: "+uuid);
        }
    }



    public String toS() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .serializeSpecialFloatingPointValues()
                .create();
        return gson.toJson(this);
    }

    public static Calibration byid(long id) {
        return new Select()
                .from(Calibration.class)
                .where("_ID = ?", id)
                .executeSingle();
    }

    public static Calibration byuuid(String uuid) {
        if (uuid == null) return null;
        return new Select()
                .from(Calibration.class)
                .where("uuid = ?", uuid)
                .orderBy("_ID desc")
                .executeSingle();
    }

    public static void clear_byuuid(String uuid, boolean from_interactive) {
        if (uuid == null) return;
        Calibration calibration = byuuid(uuid);
        if (calibration != null) {
            calibration.invalidate();
            CalibrationSendQueue.addToQueue(calibration, xdrip.getAppContext());
            newFingerStickData();
            if (from_interactive) {
                GcmActivity.clearLastCalibration(uuid);
            }
            // recalculate bg data
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());
            if (prefs.getBoolean("rewrite_history", false)) {
                calculate_w_l_s_multiple(calibration.sensor_age_at_time_of_estimation);
                adjustBgReadings(calibration.sensor_age_at_time_of_estimation);
            }
        }
    }

    public static void upsertFromMaster(Calibration jsonCalibration) {

        if (jsonCalibration == null) {
            Log.wtf(TAG,"Got null calibration from json");
            return;
        }
        try {
            Sensor sensor = Sensor.getByUuid(jsonCalibration.sensor_uuid);
            if (sensor == null) {
                Log.e(TAG, "No sensor found, ignoring cailbration " + jsonCalibration.sensor_uuid);
                return;
            }
            Calibration existingCalibration = byuuid(jsonCalibration.uuid);
            if (existingCalibration == null) {
                Log.d(TAG, "saving new calibration record. sensor uuid =" + jsonCalibration.sensor_uuid + " calibration uuid = " + jsonCalibration.uuid);
                jsonCalibration.sensor = sensor;
                jsonCalibration.save();
            } else {
                Log.d(TAG, "updating existing calibration record: " + jsonCalibration.uuid);
                existingCalibration.sensor = sensor;
                existingCalibration.timestamp = jsonCalibration.timestamp;
                existingCalibration.sensor_age_at_time_of_estimation = jsonCalibration.sensor_age_at_time_of_estimation;
                existingCalibration.bg = jsonCalibration.bg;
                existingCalibration.raw_value = jsonCalibration.raw_value;
                existingCalibration.adjusted_raw_value = jsonCalibration.adjusted_raw_value;
                existingCalibration.sensor_confidence = jsonCalibration.sensor_confidence;
                existingCalibration.slope_confidence = jsonCalibration.slope_confidence;
                existingCalibration.raw_timestamp = jsonCalibration.raw_timestamp;
                existingCalibration.slope = jsonCalibration.slope;
                existingCalibration.intercept = jsonCalibration.intercept;
                existingCalibration.distance_from_estimate = jsonCalibration.distance_from_estimate;
                existingCalibration.estimate_raw_at_time_of_calibration = jsonCalibration.estimate_raw_at_time_of_calibration;
                existingCalibration.estimate_bg_at_time_of_calibration = jsonCalibration.estimate_bg_at_time_of_calibration;
                existingCalibration.uuid = jsonCalibration.uuid;
                existingCalibration.sensor_uuid = jsonCalibration.sensor_uuid;
                existingCalibration.possible_bad = jsonCalibration.possible_bad;
                existingCalibration.check_in = jsonCalibration.check_in;
                existingCalibration.first_decay = jsonCalibration.first_decay;
                existingCalibration.second_decay = jsonCalibration.second_decay;
                existingCalibration.first_slope = jsonCalibration.first_slope;
                existingCalibration.second_slope = jsonCalibration.second_slope;
                existingCalibration.first_intercept = jsonCalibration.first_intercept;
                existingCalibration.second_intercept = jsonCalibration.second_intercept;
                existingCalibration.first_scale = jsonCalibration.first_scale;
                existingCalibration.second_scale = jsonCalibration.second_scale;

                existingCalibration.save();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not save Calibration: " + e.toString());
        }
    }


    //COMMON SCOPES!
    public static Calibration last() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .orderBy("timestamp desc")
                .executeSingle();
    }

    public static Calibration lastValid() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("slope != 0")
                // no limit for intercept
                //.where("intercept <= ?", CalibrationAbstract.getHighestSaneIntercept())
                .orderBy("timestamp desc")
                .executeSingle();
    }

    public static Calibration first() {
        Sensor sensor = Sensor.currentSensor();
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp asc")
                .executeSingle();
    }

    public static double max_recent() {
        Sensor sensor = Sensor.currentSensor();
        Calibration calibration = new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 4)))
                .orderBy("bg desc")
                .executeSingle();
        if (calibration != null) {
            return calibration.bg;
        } else {
            return 120;
        }
    }

    public static double min_recent() {
        Sensor sensor = Sensor.currentSensor();
        Calibration calibration = new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 4)))
                .orderBy("bg asc")
                .executeSingle();
        if (calibration != null) {
            return calibration.bg;
        } else {
            return 100;
        }
    }

    public static List<Calibration> latest(int number) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
    }

    // TODO calls to this method are used for UI features as to whether calibration is needed
    // TODO this might need to updated to ignore invalid intercepts depending on plugin configuration etc
    public static List<Calibration> latestValid(int number) {
        return latestValid(number, JoH.tsl() + Constants.HOUR_IN_MS);
    }

    public static List<Calibration> latestValid(int number, long until) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        // we don't filter invalid intercepts here as they will be filtered in the plugin itself
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("slope != 0")
                .where("timestamp <= ?", until)
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
    }

    public static List<Calibration> latestForGraph(int number, long startTime) {
        return latestForGraph(number, startTime, (long)JoH.ts());
    }

    public static List<Calibration> latestForGraph(int number, long startTime, long endTime) {
        return new Select()
                .from(Calibration.class)
                .where("timestamp >= " + Math.max(startTime, 0))
                .where("timestamp <= " + endTime)
                .where("(slope != 0 or slope_confidence = ?)", note_only_marker)
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
    }

    public static List<Calibration> latestForGraphSensor(int number, long startTime, long endTime) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("timestamp >= " + Math.max(startTime, 0))
                .where("timestamp <= " + endTime)
                .where("(slope != 0 or slope_confidence = ?)", note_only_marker)
                .orderBy("timestamp desc")
                .limit(number)
                .execute();
    }

    public static List<Calibration> allForSensor() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp desc")
                .execute();
    }

    public static List<Calibration> allForSensorInLastFourDays() {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .where("timestamp > ?", (new Date().getTime() - (60000 * 60 * 24 * 4)))
                .orderBy("timestamp desc")
                .execute();
    }

    public static List<Calibration> allForSensorWithWeightX(double min_weight, double max_weight) {
        return allForSensorWithWeightX(min_weight, max_weight, Calibration.lastValid().sensor_age_at_time_of_estimation);
    }

    public static List<Calibration> allForSensorWithWeightX(double min_weight, double max_weight, double calib_point_in_time) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        // pick out all valid calibrations from sensor
        List<Calibration> cal1 = new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp desc")
                .execute();
        // pick out calibrations based on min_weight and max_weight
        List<Calibration> cal2 = new ArrayList<Calibration>();
        for (Calibration cal : cal1) {
            double calib_weight = cal.calculateWeight(calib_point_in_time);
            if (calib_weight > min_weight && calib_weight <= max_weight) {
                cal2.add(cal);
            }
        }
        return cal2;
    }

    public static List<Calibration> allForSensorLimited(int limit) {
        Sensor sensor = Sensor.currentSensor();
        if (sensor == null) {
            return null;
        }
        return new Select()
                .from(Calibration.class)
                .where("Sensor = ? ", sensor.getId())
                .where("slope_confidence != 0")
                .where("sensor_confidence != 0")
                .orderBy("timestamp desc")
                .limit(limit)
                .execute();
    }

    public static List<Calibration> getCalibrationsForSensor(Sensor sensor, int limit) {
        return new Select()
                .from(Calibration.class)
                .where("sensor_uuid = ? ", sensor.uuid)
                 .orderBy("timestamp desc")
                 .limit(limit)
                .execute();
    }

    public static List<Calibration> futureCalibrations() {
        double timestamp = new Date().getTime();
        return new Select()
                .from(Calibration.class)
                .where("timestamp > " + timestamp)
                .orderBy("timestamp desc")
                .execute();
    }

    public boolean isNote() {
        Calibration calibration = this;
        if ((calibration.slope == 0)
                && (calibration.slope_confidence == note_only_marker)
                && (calibration.sensor_confidence == 0)
                && (calibration.intercept == 0)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValid() {
        Calibration calibration = this;
        if ((calibration.slope_confidence != 0)
                && (calibration.sensor_confidence != 0)
                && (calibration.slope != 0)
                && (calibration.intercept != 0)) {
            return true;
        } else {
            return false;
        }
    }

    public void invalidate() {
        this.slope_confidence = 0;
        this.sensor_confidence = 0;
        this.slope = 0;
        this.intercept = 0;
        save();
        PluggableCalibration.invalidateAllCaches();
    }

    public static synchronized void invalidateAllForSensor() {
        final List<Calibration> cals = allForSensorLimited(9999999);
        if (cals != null) {
            for (Calibration cal : cals) {
                cal.invalidate();
            }
            // recalculate bg data
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(xdrip.getAppContext());
            if (prefs.getBoolean("rewrite_history", false)) {
                final List<BgReading> bgReadings = BgReading.latestForGraph(25000, Sensor.currentSensor().started_at, JoH.tsl());
                for (BgReading bgReading : bgReadings) {
                    bgReading.calculated_value = bgReading.age_adjusted_raw_value;
                    bgReading.filtered_calculated_value = bgReading.filtered_data;
                    bgReading.save();
                }
                Log.e(TAG, "Recalculated " + bgReadings.size() + " bg points");
            }
        }
        JoH.clearCache();
        String msg = "Deleted all calibrations for sensor";
        Log.ueh(TAG, msg);
        JoH.static_toast_long(msg);
    }

}

abstract class SlopeParameters {
    protected double LOW_SLOPE_1;
    protected double LOW_SLOPE_2;
    protected double HIGH_SLOPE_1;
    protected double HIGH_SLOPE_2;
    protected double DEFAULT_LOW_SLOPE_LOW;
    protected double DEFAULT_LOW_SLOPE_HIGH;
    protected int DEFAULT_SLOPE;
    protected double DEFAULT_HIGH_SLOPE_HIGH;
    protected double DEFAULT_HIGH_SLOPE_LOW;

    public double getLowSlope1() {
        return LOW_SLOPE_1;
    }

    public double getLowSlope2() {
        return LOW_SLOPE_2;
    }

    public double getHighSlope1() {
        return HIGH_SLOPE_1;
    }

    public double getHighSlope2() {
        return HIGH_SLOPE_2;
    }

    public double getDefaultLowSlopeLow() {
        return DEFAULT_LOW_SLOPE_LOW;
    }

    public double getDefaultLowSlopeHigh() {
        return DEFAULT_LOW_SLOPE_HIGH;
    }

    public int getDefaultSlope() {
        return DEFAULT_SLOPE;
    }

    public double getDefaultHighSlopeHigh() {
        return DEFAULT_HIGH_SLOPE_HIGH;
    }

    public double getDefaulHighSlopeLow() {
        return DEFAULT_HIGH_SLOPE_LOW;
    }

    public double restrictIntercept(double intercept) { return  intercept; }
}
