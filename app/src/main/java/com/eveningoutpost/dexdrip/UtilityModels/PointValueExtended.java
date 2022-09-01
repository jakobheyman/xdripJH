package com.eveningoutpost.dexdrip.UtilityModels;

/**
 * Created by Emma Black on 11/15/14.
 */

public class PointValueExtended extends HPointValue {

    public static final int BloodTest = 1;
    public static final int AdjustableDose = 2;

    public PointValueExtended(double x, float y, long offset, String note_param) {
        super(x, y, offset);
        note = note_param;
    }

    public PointValueExtended(double x, float y, long offset, float filtered) {
        super(x, y, offset);
        calculatedFilteredValue = filtered;
    }
    public PointValueExtended(double x, float y, long offset) {
        super(x, y, offset);
        calculatedFilteredValue = -1;
    }

    public PointValueExtended setType(final int type) {
        this.type = type;
        return this;
    }

    public PointValueExtended setUUID(final String uuid) {
        this.uuid = uuid;
        return this;
    }

    public float calculatedFilteredValue;
    public String note;
    public int type = 0;
    public String uuid;
    public long real_timestamp = 0;
}
