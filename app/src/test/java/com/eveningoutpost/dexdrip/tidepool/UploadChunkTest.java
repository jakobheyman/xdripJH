package com.eveningoutpost.dexdrip.tidepool;

import static com.google.common.truth.Truth.assertWithMessage;

import com.eveningoutpost.dexdrip.Models.APStatus;
import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.RobolectricTestWithConfig;
import com.eveningoutpost.dexdrip.UtilityModels.Constants;
import com.eveningoutpost.dexdrip.profileeditor.BasalProfile;
import com.eveningoutpost.dexdrip.profileeditor.BasalRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TimeZone;

import lombok.val;

public class UploadChunkTest extends RobolectricTestWithConfig {

    private static final boolean D = false;
    private TimeZone oldTimeZone;

    private static final String testData =
            "9910|100|1651220547452\n" +
                    "9909|0|1651220244810\n" +
                    "9908|100|1651218415019\n" +
                    "9907|170|1651217546352\n" +
                    "9906|100|1651216946525\n" +
                    "9905|250|1651215145326\n" +
                    "9904|220|1651214846325\n" +
                    "9903|170|1651214546891\n" +
                    "9902|250|1651213047141\n" +
                    "9901|100|1651212445893\n" +
                    "9900|0|1651212146333\n" +
                    "9899|100|1651209146287\n" +
                    "9898|0|1651208846579\n" +
                    "9897|100|1651207646029\n" +
                    "9896|150|1651207346247\n" +
                    "9895|100|1651206146494\n" +
                    "9894|0|1651205546466\n" +
                    "9893|100|1651205245758\n" +
                    "9892|0|1651203445507\n" +
                    "9891|100|1651202246248\n" +
                    "9890|150|1651201945609\n" +
                    "9889|250|1651200445268\n" +
                    "9888|100|1651200146859\n" +
                    "9887|250|1651198345785\n" +
                    "9886|100|1651197745824\n" +
                    "9885|250|1651197146052\n" +
                    "9884|0|1651196556019\n" +
                    "9883|100|1651196244771\n" +
                    "9882|0|1651194445320\n" +
                    "9881|100|1651193845919\n" +
                    "9880|0|1651192345783\n" +
                    "9879|100|1651191145393\n" +
                    "9878|250|1651189949027\n" +
                    "9877|200|1651189665832\n" +
                    "9876|0|1651189046027\n" +
                    "9875|100|1651186945832\n" +
                    "9874|0|1651186046464\n" +
                    "9873|250|1651185449628\n" +
                    "9872|100|1651183953434\n" +
                    "9871|160|1651183643506\n" +
                    "9870|100|1651182444488\n" +
                    "9869|250|1651181845006\n" +
                    "9868|100|1651181245994\n" +
                    "9867|0|1651180647010\n" +
                    "9866|100|1651180046117\n" +
                    "9865|0|1651179080193\n" +
                    "9864|250|1651178849894\n" +
                    "9863|100|1651178460508\n" +
                    "9862|190|1651177130790\n" +
                    "9861|160|1651176493998\n" +
                    "9860|110|1651176236669\n" +
                    "9859|250|1651174652881\n" +
                    "9858|100|1651174043158\n" +
                    "9857|250|1651172243658\n" +
                    "9856|200|1651171942863\n" +
                    "9855|130|1651171642970\n" +
                    "9854|100|1651167143146\n" +
                    "9853|0|1651165644615\n" +
                    "9852|100|1651164742959\n" +
                    "9851|90|1651164487653\n" +
                    "9850|0|1651164442495\n" +
                    "9849|100|1651160843605\n" +
                    "9848|0|1651160544765\n" +
                    "9847|250|1651159644202\n" +
                    "9846|100|1651158382458\n" +
                    "9845|250|1651157246322\n" +
                    "9844|100|1651156645085\n" +
                    "9843|0|1651155749441\n" +
                    "9842|100|1651154843636\n" +
                    "9841|220|1651154552203\n" +
                    "9840|180|1651154251350\n" +
                    "9839|100|1651153045378\n" +
                    "9838|0|1651152745114\n" +
                    "9837|100|1651151844241\n" +
                    "9836|140|1651151545484\n" +
                    "9835|100|1651149743169\n" +
                    "9834|250|1651149294987\n" +
                    "9833|100|1651148843878\n" +
                    "9832|250|1651147943099\n" +
                    "9831|240|1651147644399\n" +
                    "9830|100|1651147044092\n" +
                    "9829|210|1651146745123\n" +
                    "9828|160|1651146442995\n" +
                    "9827|100|1651144950675\n" +
                    "9826|0|1651143748415\n" +
                    "9825|100|1651143147879\n" +
                    "9824|250|1651138949263\n" +
                    "9823|100|1651138497708\n" +
                    "9822|250|1651136844568\n" +
                    "9821|240|1651136547373\n" +
                    "9820|100|1651133844397\n" +
                    "9819|0|1651133243704\n" +
                    "9818|100|1651131443808\n" +
                    "9817|160|1651130843261\n" +
                    "9816|100|1651128750702\n" +
                    "9815|0|1651126943601\n" +
                    "9814|100|1651125742744\n" +
                    "9813|190|1651124542897\n" +
                    "9812|250|1651123050322\n" +
                    "9811|100|1651122142887\n" +
                    "9810|180|1651121542893\n" +
                    "9809|170|1651120943851\n" +
                    "9808|100|1651115842747\n" +
                    "9807|150|1651115249235\n" +
                    "9806|100|1651114043717\n" +
                    "9805|0|1651113143746\n" +
                    "9804|100|1651109243465\n" +
                    "9803|250|1651108641097\n" +
                    "9802|220|1651108045076\n" +
                    "9801|100|1651106849758\n" +
                    "9800|0|1651105943968\n" +
                    "9799|100|1651103542296\n" +
                    "9798|0|1651102342687\n" +
                    "9797|100|1651101442972\n" +
                    "9796|0|1651099642530\n" +
                    "9795|100|1651098444476\n" +
                    "9794|0|1651095742522\n" +
                    "9793|100|1651093944364\n" +
                    "9792|0|1651093641898\n" +
                    "9791|250|1651093284165\n" +
                    "9790|100|1651092744462\n" +
                    "9789|250|1651091244409\n" +
                    "9788|220|1651090643781\n" +
                    "9787|180|1651090346854\n" +
                    "9786|100|1651087640374\n" +
                    "9785|0|1651087351834\n" +
                    "9784|250|1651085840040\n" +
                    "9783|210|1651085555804\n" +
                    "9782|170|1651085249217\n" +
                    "9781|100|1651083141444\n" +
                    "9780|0|1651080444597\n" +
                    "9779|100|1651077456815\n" +
                    "9778|0|1651073889349\n" +
                    "9777|100|1651073547749\n" +
                    "9776|250|1651071469443\n" +
                    "9775|100|1651070841693\n" +
                    "9774|250|1651069104501\n" +
                    "9773|100|1651068443840\n" +
                    "9772|0|1651067542100\n" +
                    "9771|100|1651066640464\n" +
                    "9770|0|1651064242079\n" +
                    "9769|100|1651063041121\n" +
                    "9768|0|1651062743766\n" +
                    "9767|160|1651062388231\n" +
                    "9766|100|1651061242662\n" +
                    "9765|0|1651060642446\n" +
                    "9764|100|1651057940765\n" +
                    "9763|250|1651057352329\n" +
                    "9762|100|1651057040936\n" +
                    "9761|250|1651055240437\n" +
                    "9760|0|1651054043142\n" +
                    "9759|100|1651052240290\n" +
                    "9758|90|1651051973021\n" +
                    "9757|0|1651051940704\n" +
                    "9756|100|1651051346439\n" +
                    "9755|250|1651049539475\n" +
                    "9754|100|1651047742045\n" +
                    "9753|0|1651047650304\n" +
                    "9752|250|1651047144670\n" +
                    "9751|100|1651045643595\n" +
                    "9750|160|1651045039895\n" +
                    "9749|100|1651042339427\n" +
                    "9748|0|1651042039465\n" +
                    "9747|100|1651040239295\n" +
                    "9746|160|1651039650418\n" +
                    "9745|100|1651038139930\n" +
                    "9744|180|1651037840272\n" +
                    "9743|100|1651035139875\n" +
                    "9742|200|1651034537687\n" +
                    "9741|100|1651033638884\n" +
                    "9740|200|1651033339228\n" +
                    "9739|100|1651032738661\n" +
                    "9738|210|1651032138733\n" +
                    "9737|100|1651031239824\n" +
                    "9736|180|1651030640359\n" +
                    "9735|100|1651029444333\n" +
                    "9734|0|1651028841220\n" +
                    "9733|100|1651026142845\n" +
                    "9732|180|1651025247780\n" +
                    "9731|250|1651023739622\n" +
                    "9730|100|1651023139891\n" +
                    "9729|0|1651022539004\n" +
                    "9728|100|1651021039069\n" +
                    "9727|210|1651020139302\n" +
                    "9726|0|1651019238660\n" +
                    "9725|100|1651017482367\n" +
                    "9724|250|1651015939184\n" +
                    "9723|100|1651015339347\n" +
                    "9722|250|1651013540326\n" +
                    "9721|100|1651012939721\n" +
                    "9720|0|1651012037107\n" +
                    "9719|100|1651011445922\n" +
                    "9718|0|1651011144944\n" +
                    "9717|100|1651010546491\n" +
                    "9716|0|1651009046480\n" +
                    "9715|100|1651007843467\n" +
                    "9714|250|1651007241805\n" +
                    "9713|100|1651006341057\n" +
                    "9712|250|1651005749327\n" +
                    "9711|100|1651005135745\n";


    @Before
    public void setupDb() {
        oldTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        val values = testData.split("\n");
        BasalRepository.clearRates();
        assertWithMessage("db test data state okay").that(values.length).isEqualTo(200);
        APStatus.updateDB();
        APStatus.cleanup(0);
        for (val record : values) {
            val fields = record.split("\\|");
            val aps = new APStatus(Long.parseLong(fields[2]), Integer.parseInt(fields[1]), -1d);
            //System.out.println(aps.toS());
            aps.save();
        }

        val fl = new ArrayList<Double>();
        fl.add(1.0d);
        BasalProfile.save("1", fl);
    }


    @After
    public void tearDown() {
        TimeZone.setDefault(oldTimeZone);
    }

    @Test
    public void getBasalsTest() {
        val res = APStatus.latestForGraph(16000, 0, 2651220547453L);
        assertWithMessage("db initial state okay").that(res.size()).isEqualTo(200);
        val ebas = UploadChunk.getBasals(0, 2651220547453L);

        if (D) {
            for (val b : ebas) {
                System.out.println(b.toS());
            }
        }

        val starttime = 1651207381000L + Constants.HOUR_IN_MS;
        val endtime = 1651211461000L + Constants.HOUR_IN_MS;

        val ebas2 = UploadChunk.getBasals(starttime, endtime);

        if (D) {
            System.out.println(JoH.dateTimeText(starttime));
            System.out.println(JoH.dateTimeText(endtime));
            for (val b : ebas2) {
                System.out.println(b.toS());
            }
        }

        val json = JoH.defaultGsonInstance().toJson(ebas2);
        assertWithMessage("segment matches expected head/tail slice").that(json).isEqualTo("[{\"deliveryType\":\"automated\",\"duration\":1165333,\"rate\":1.0,\"scheduleName\":\"AAPS\",\"clockDriftOffset\":0,\"conversionOffset\":0,\"deviceTime\":\"2022-04-29T05:43:01\",\"time\":\"2022-04-29T05:43:01.0000000Z\",\"timezoneOffset\":0,\"type\":\"basal\",\"origin\":{\"id\":\"fb89faeb-8b51-359b-9935-63b7d33f0dfc\"}},{\"deliveryType\":\"automated\",\"duration\":299560,\"rate\":0.0,\"scheduleName\":\"AAPS\",\"clockDriftOffset\":0,\"conversionOffset\":0,\"deviceTime\":\"2022-04-29T06:02:26\",\"time\":\"2022-04-29T06:02:26.3330000Z\",\"timezoneOffset\":0,\"type\":\"basal\",\"origin\":{\"id\":\"08e1715a-485e-3151-a34a-9f57318149c8\"}},{\"deliveryType\":\"automated\",\"duration\":601248,\"rate\":1.0,\"scheduleName\":\"AAPS\",\"clockDriftOffset\":0,\"conversionOffset\":0,\"deviceTime\":\"2022-04-29T06:07:25\",\"time\":\"2022-04-29T06:07:25.8930000Z\",\"timezoneOffset\":0,\"type\":\"basal\",\"origin\":{\"id\":\"d754c3bb-fea8-3992-b9e9-3c6d9fdbf475\"}},{\"deliveryType\":\"automated\",\"duration\":1499750,\"rate\":2.5,\"scheduleName\":\"AAPS\",\"clockDriftOffset\":0,\"conversionOffset\":0,\"deviceTime\":\"2022-04-29T06:17:27\",\"time\":\"2022-04-29T06:17:27.1410000Z\",\"timezoneOffset\":0,\"type\":\"basal\",\"origin\":{\"id\":\"d9f5fb5a-bd7e-30a1-99f0-2e66c9a10527\"}},{\"deliveryType\":\"automated\",\"duration\":299434,\"rate\":1.7,\"scheduleName\":\"AAPS\",\"clockDriftOffset\":0,\"conversionOffset\":0,\"deviceTime\":\"2022-04-29T06:42:26\",\"time\":\"2022-04-29T06:42:26.8910000Z\",\"timezoneOffset\":0,\"type\":\"basal\",\"origin\":{\"id\":\"ba1b40eb-bba9-35dd-84fd-f7afe5286bc0\"}},{\"deliveryType\":\"automated\",\"duration\":214675,\"rate\":2.2,\"scheduleName\":\"AAPS\",\"clockDriftOffset\":0,\"conversionOffset\":0,\"deviceTime\":\"2022-04-29T06:47:26\",\"time\":\"2022-04-29T06:47:26.3250000Z\",\"timezoneOffset\":0,\"type\":\"basal\",\"origin\":{\"id\":\"2227b260-664f-3e29-8893-db4532cdb0e3\"}}]");

    }
}