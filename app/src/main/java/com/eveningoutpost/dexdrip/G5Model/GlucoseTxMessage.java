package com.eveningoutpost.dexdrip.G5Model;

import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.UserError;
import com.eveningoutpost.dexdrip.services.G5CollectionService;

/**
 * Created by jamorham on 25/11/2016.
 */

public class GlucoseTxMessage extends BaseMessage {

    private final static String TAG = G5CollectionService.TAG; // meh
    static final byte opcode = 0x30;

    public GlucoseTxMessage() {
        init(opcode, 3);
        UserError.Log.d(TAG, "GlucoseTx dbg: " + JoH.bytesToHex(byteSequence));
    }
}

