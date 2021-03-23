package com.example.medicationtracker;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;

public class InsertSignature {
    public static void insert_signature(Workbook workbook, CreationHelper helper, Drawing patriarch, byte[] bytes, int row, int col){

        int my_picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);

        ClientAnchor anchor = helper.createClientAnchor();

        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 1);
        anchor.setRow2(row + 1);

        patriarch.createPicture(anchor, my_picture_id);
    }
}
