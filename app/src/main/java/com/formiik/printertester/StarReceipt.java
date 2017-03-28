package com.formiik.printertester;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import com.formiik.printertester.StarRasterDocument.*;
/**
 * Created by jonathan on 06/07/16.
 */
public class StarReceipt {

    private static final int    printableArea   = 576; // Default for 2 inch receipt

    private static final byte[] boldOn          = new byte[] {0x1b, 0x45};
    private static final byte[] boldOff         = new byte[] {0x1b, 0x46};

    private static final byte[] alignmentLeft   = new byte[] {0x1b, 0x1d, 0x61, 0x00};
    private static final byte[] alignmentCenter = new byte[] {0x1b, 0x1d, 0x61, 0x01};
    private static final byte[] alignmentRight  = new byte[] {0x1b, 0x1d, 0x61, 0x02};

    private static final byte[] height0         = new byte[] {0x1b, 0x68, 0x00};
    private static final byte[] height1         = new byte[] {0x1b, 0x68, 0x01};
    private static final byte[] height2         = new byte[] {0x1b, 0x68, 0x02};

    private static final byte[] width0          = new byte[] {0x1b, 0x57, 0x00};
    private static final byte[] width1          = new byte[] {0x1b, 0x57, 0x01};
    private static final byte[] width2          = new byte[] {0x1b, 0x57, 0x02};
    private static final byte[] width3          = new byte[] {0x1b, 0x57, 0x03};

    private static final byte[] lineFeed = createCp1252("\n");


    public static ArrayList<byte[]> getReceiptLineMode(){

        ArrayList<byte[]> list = new ArrayList<byte[]>();

        list.add(new byte[] { 0x1b, 0x1d, 0x74, 0x20 }); // Code Page #1252 (Windows Latin-1)

        // list.add("[If loaded.. Logo1 goes here]\r\n".getBytes());
        // list.add(new byte[]{0x1b, 0x1c, 0x70, 0x01, 0x00, '\r', '\n'}); //Stored Logo Printing

        // Notice that we use a unicode representation because that is
        // how Java expresses these bytes as double byte unicode

        //list.add(new byte[] { 0x06, 0x09, 0x1b, 0x57, 0x01, 0x01 }); // Character expansion
        //list.add(new byte[] { 0x1b, 0x68, 0x00, 0x00 }); // Cancel Character Expansion

        list.add(alignmentCenter);

        list.add(height0);
        list.add(width0);
        list.add(boldOn);
        //list.add(createCp1252("--------------------------------\r\n"));
        list.add(createCp1252("Financiamiento Progresemos\n"));
        list.add(createCp1252("S.A. de C.V., SOFOM, E.N.R.\n"));
        list.add(createCp1252("\"Con Progresemos, Progresamos\"\n\n"));

        list.add(boldOff);

        list.add(alignmentLeft);

        //list.add(createCp1252("------------------------------\r\n"));

        list.add(createCp1252("Sucursal:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("999 XXXXXXXX\r\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Monto original del préstamo:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("$ 9999.99\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Recibimos de:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("Juan López Nazario\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("La cantidad de:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("$ 1000.00(MN)\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Tipo de operación:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("Microcrédito\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Cuota número:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("del Grupo\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Comunidad/Municipio:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("Benito Juárez\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Pago total / Pago parcial\n"));
        list.add(lineFeed);

        list.add(alignmentCenter); // Alignment
        list.add(new byte[] { 0x1b, 0x44, 0x02, 0x10, 0x22, 0x00 }); // Set horizontal tab

        //list.add(createCp1252("--------------------------------\n"));

        list.add(boldOn);
        list.add(createCp1252("Nombre Integrantes Grupo  Monto\n\n"));
        list.add(boldOff);

        //list.add(createCp1252("--------------------------------\r\n"));

        list.add(createCp1252("XXXXXX XXXXXXXX    XXXXX $999.99\n"));
        list.add(createCp1252("XXXXXX XXXXXXXX    XXXXX $999.99\n"));
        list.add(createCp1252("XXXXXX XXXXXXXX    XXXXX $999.99\n"));
        list.add(createCp1252("XXXXXX XXXXXXXX    XXXXX $999.99\n"));

        list.add(lineFeed);

        list.add(alignmentLeft);

        list.add(createCp1252("Entregado por:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("Nombre Promotor\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(createCp1252("Recibido por:\n"));

        list.add(alignmentRight);
        list.add(boldOn);
        list.add(createCp1252("Tesorero del grupo\n"));
        list.add(boldOff);
        list.add(alignmentLeft);

        list.add(lineFeed);

        list.add(alignmentCenter);

        //list.add(createCp1252("------------------------------"));
        list.add(createCp1252("Financiamiento Progresemos, S.A."));
        list.add(createCp1252("de C.V. SOFOM, E.N.R.\n"));
        list.add(createCp1252("(Direccion Sucursal)\n"));
        list.add(lineFeed);

        //list.add(createCp1252("------------------------------"));
        list.add(createCp1252("En caso de queja del servicio,\n"));
        list.add(createCp1252("favor de comunicarse a:\n"));
        list.add(createCp1252("(55)5575 2009\n"));
        list.add(lineFeed);

        //list.add(createCp1252("------------------------------"));
        list.add(createCp1252("Localización de la Unidad\n"));
        list.add(createCp1252("Especializada\n"));
        list.add(lineFeed);

        //list.add(createCp1252("------------------------------"));
        list.add(createCp1252("Carretera Picacho-Ajusco Nº 130,\n"));
        list.add(createCp1252("Despacho 203,Col. Jardines en la\n"));
        list.add(createCp1252("Montaña,C.P. 14210, México, D.F.\n"));
        list.add(lineFeed);

        list.add(createCp1252("Tel. (55)5575 2009 Ext. 117\n"));
        list.add(lineFeed);

        list.add(createCp1252("Titular:\n"));
        list.add(createCp1252("Thelma Oralia Jiménez Alcalde\n"));
        list.add(lineFeed);
        // 1D barcode example
        //list.add(new byte[] { 0x1b, 0x1d, 0x61, 0x01 });
        //list.add(new byte[] { 0x1b, 0x62, 0x06, 0x02, 0x02 });

        //list.add(createCp1252(" 12ab34cd56\u001e\r\n"));

        list.add(new byte[] { 0x1b, 0x64, 0x02 }); // Cut
        //list.add(new byte[]{0x07}); // Kick cash drawer

        return list;

    }

    private static byte[] createCp1252(String inputText) {
        byte[] byteBuffer = null;

        try {
            byteBuffer = inputText.getBytes("Windows-1252");
        } catch (UnsupportedEncodingException e) {
            byteBuffer = inputText.getBytes();
        }

        return byteBuffer;
    }


    public static ArrayList<byte[]> getReceiptRasterMode(String portSettings){
        ArrayList<byte[]> list = new ArrayList<byte[]>();


        RasterCommand rasterType = RasterCommand.Standard;
        if (portSettings.toUpperCase(Locale.US).contains("PORTABLE")) {
            rasterType = RasterCommand.Graphics;
        }


        StarRasterDocument rasterDoc = new StarRasterDocument(RasSpeed.Medium, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, RasTopMargin.Standard, 0, 0, 0);

        if (rasterType == RasterCommand.Standard) {
            list.add(rasterDoc.BeginDocumentCommandData());
        }

        String textToPrint = (
                "COMERCIAL DE ALIMENTOS\r\n" +
                        "       CARREFOUR LTDA.\r\n");
        list.add(createRasterCommand(textToPrint, 13, 0, rasterType));

        textToPrint = (
                "Avenida Moyses Roysen,\r\n" +
                        "S/N Vila Guilherme\r\n" +
                        "Cep: 02049-010–SaoPaulo–SP\n" +
                        "---------------------------------------------\r\n" +
                        "MM/DD/YYYY HH:MM:SS\n" +
                        "CCF:133939 COO:227808\n" +
                        "---------------------------------------------\r\n" +
                        "CUPOM FISCAL\n" +
                        "---------------------------------------------\r\n" +
                        "01 CAFÉ DO PONTO TRAD A\n" +
                        "                              1un F1 8,15)\n" +
                        "02 CAFÉ DO PONTO TRAD A\n" +
                        "                              1un F1 8,15)\n" +
                        "03 CAFÉ DO PONTO TRAD A\n"+
                        "---------------------------------------------\r\n" +
                        "TOTAL  R$                        27,23\n" +
                        "DINHEIROv                     29,00\n\n" +
                        "TROCO R$                          1,77\r\n" +
                        "Valor dos Tributos R$2,15(7,90%)\n" +
                        "OBRIGADO PERA PREFERENCIA.\r\n" +
                        "VOLTE SEMPRE!\n" +
                        "SAC 0800 724 2822\n");

        list.add(createRasterCommand(textToPrint, 13, 0, rasterType));

        if (rasterType == RasterCommand.Standard) {
            list.add(rasterDoc.EndDocumentCommandData());
            list.add(new byte[] { 0x07 }); // Kick cash drawer
        } else {
            list.add(new byte[] { 0x1b, 0x64, 0x02 }); // Cut
        }

        return list;
    }

    private static byte[] createRasterCommand(String printText, int textSize, int bold, RasterCommand rasterType) {
        byte[] command;

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        Typeface typeface;

        try {
            typeface = Typeface.create(Typeface.SERIF, bold);
        } catch (Exception e) {
            typeface = Typeface.create(Typeface.DEFAULT, bold);
        }

        paint.setTypeface(typeface);
        paint.setTextSize(textSize * 2);
        paint.setLinearText(true);

        TextPaint textpaint = new TextPaint(paint);
        textpaint.setLinearText(true);
        android.text.StaticLayout staticLayout = new StaticLayout(printText, textpaint, printableArea, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int height = staticLayout.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(staticLayout.getWidth(), height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.WHITE);
        c.translate(0, 0);
        staticLayout.draw(c);

        StarBitmap starbitmap = new StarBitmap(bitmap, false, printableArea);

        if (rasterType == RasterCommand.Standard) {
            command = starbitmap.getImageRasterDataForPrinting_Standard(true);
        } else {
            command = starbitmap.getImageRasterDataForPrinting_graphic(true);
        }

        return command;
    }


    public static ArrayList<byte[]> getReceiptImage(String portSettings, int source, Resources res){

        RasterCommand rasterType = RasterCommand.Standard;
        if (portSettings.toUpperCase(Locale.US).contains("PORTABLE")) {
            rasterType = RasterCommand.Graphics;
        }

        int paperWidth = 384;
        boolean compressionEnable = true;

        ArrayList<byte[]> commands = new ArrayList<byte[]>();

        StarRasterDocument rasterDoc = new StarRasterDocument(RasSpeed.Full, RasPageEndMode.FeedAndFullCut, RasPageEndMode.FeedAndFullCut, RasTopMargin.Standard, 0, 0, 0);
        Bitmap bm = BitmapFactory.decodeResource(res, source);
        StarBitmap starbitmap = new StarBitmap(bm, false, paperWidth);

        if (rasterType == RasterCommand.Standard) {
            commands.add(rasterDoc.BeginDocumentCommandData());

            commands.add(starbitmap.getImageRasterDataForPrinting_Standard(compressionEnable));
            commands.add(rasterDoc.EndDocumentCommandData());
        } else {
            commands.add(starbitmap.getImageRasterDataForPrinting_graphic(compressionEnable));
            commands.add(new byte[] { 0x1b, 0x64, 0x02 }); // Feed to cutter position
        }

        return commands;
    }

    public enum RasterCommand {
        Standard, Graphics
    };


}
