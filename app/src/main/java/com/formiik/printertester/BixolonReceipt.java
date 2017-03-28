package com.formiik.printertester;

/**
 * Created by jonathan on 12/07/16.
 */
public class BixolonReceipt {


    private static String ESCAPE_CHARACTERS = new String(new byte[] {0x1b, 0x7c});

    private static final String normal      = ESCAPE_CHARACTERS + "N";
    private static final String fontA       = ESCAPE_CHARACTERS + "aM";// Font A (12x24)
    private static final String fontB       = ESCAPE_CHARACTERS + "bM";// Font B (9x17)
    private static final String fontC       = ESCAPE_CHARACTERS + "cM";// Font C (9x24)

    private static final String left        = ESCAPE_CHARACTERS + "lA";
    private static final String center      = ESCAPE_CHARACTERS + "cA";
    private static final String right       = ESCAPE_CHARACTERS + "rA";

    private static final String boldOn      = ESCAPE_CHARACTERS + "bC";
    private static final String boldOff     = ESCAPE_CHARACTERS + "!bC";


    private static final String underlineOn     = ESCAPE_CHARACTERS + "uC";
    private static final String underlineOff    = ESCAPE_CHARACTERS + "!uC";

    private static final String reverseVideoOn  = ESCAPE_CHARACTERS + "rvC";
    private static final String reverseVideoOff = ESCAPE_CHARACTERS + "!rvC";

    private static final String singleHandW     = ESCAPE_CHARACTERS + "1C";
    private static final String doubleW         = ESCAPE_CHARACTERS + "2C";
    private static final String doubleH         = ESCAPE_CHARACTERS + "3C";
    private static final String doubleHandW     = ESCAPE_CHARACTERS + "4C";

    private static final String scale1Hor       = ESCAPE_CHARACTERS + "1hC";
    private static final String scale2Hor       = ESCAPE_CHARACTERS + "2hC";
    private static final String scale3Hor       = ESCAPE_CHARACTERS + "3hC";
    private static final String scale4Hor       = ESCAPE_CHARACTERS + "4hC";
    private static final String scale5Hor       = ESCAPE_CHARACTERS + "5hC";
    private static final String scale6Hor       = ESCAPE_CHARACTERS + "6hC";
    private static final String scale7Hor       = ESCAPE_CHARACTERS + "7hC";
    private static final String scale8Hor       = ESCAPE_CHARACTERS + "8hC";

    private static final String scale1Ver       = ESCAPE_CHARACTERS + "1vC";
    private static final String scale2Ver       = ESCAPE_CHARACTERS + "2vC";
    private static final String scale3Ver       = ESCAPE_CHARACTERS + "3vC";
    private static final String scale4Ver       = ESCAPE_CHARACTERS + "4vC";
    private static final String scale5Ver       = ESCAPE_CHARACTERS + "5vC";
    private static final String scale6Ver       = ESCAPE_CHARACTERS + "6vC";
    private static final String scale7Ver       = ESCAPE_CHARACTERS + "7vC";
    private static final String scale8Ver       = ESCAPE_CHARACTERS + "8vC";

    private static final String lineFeed        = ESCAPE_CHARACTERS + "\n";

    public static String getReceipt(){

        String data;

        data =
                        //---------------------------------------------------------------
                fontA + center + boldOn +
                        "Financiamiento Progresemos S.A. de C.V.\n\n" +
                        "SOFOM, E.N.R.\n\n" +
                        "\"Con Progresemos, Progresamos\"\n\n" +

                fontC + left + boldOff +
                        "Sucursal: " +
                        boldOn +
                        "999    XXXXXXXX\n" +
                        boldOff +

                        "Monto original del préstamo (capital): " +
                        boldOn +
                        "$ 9999.99\n" +
                        boldOff +

                        "Recibimos de: " +
                        boldOn +
                        "Juan López Nazario\n" +
                        boldOff +

                        "La cantidad de:" +
                        boldOn +
                        "$1000.00\n" +
                        boldOff +

                        "Tipo de operación: " +
                        boldOn +
                        "Microcrédito\n" +
                        boldOff +

                        "Cuota número: " +
                        boldOn +
                        "del Grupo\n" +
                        boldOff +

                        "Comunidad/Municipio: " +
                        boldOn +
                        "Benito Juárez\n" +
                        boldOff +

                        boldOn +
                        "Pago total / Pago parcial\n" +
                        boldOff + lineFeed +

                boldOn +
                        //---------------------------------------------------------------
                        "Nombre         Integrantes     Grupo               Monto Pago \n\n" +

                boldOff +
                        "XXXXXXXX       XXXXXXXX        XXXXXXX             $999.99.00 \n" +
                        "XXXXXXXX       XXXXXXXX        XXXXXXX             $999.99.00 \n" +
                        "XXXXXXXX       XXXXXXXX        XXXXXXX             $999.99.00 \n" +
                        "XXXXXXXX       XXXXXXXX        XXXXXXX             $999.99.00 \n" +
                        "\n" +

                        "Entregado por: " +
                        boldOn +
                        "Nombre Promotor\n" +
                        boldOff + lineFeed +

                        "Recibido por: " +
                        boldOn +
                        "Tesorero del grupo\n" +
                        boldOff + lineFeed +

                fontB + center+

                        "Financiamiento Progresemos, S.A. de C.V. SOFOM, E.N.R.\n" +
                        "XXX   (Direccion Sucursal)\n" +
                        lineFeed +

                        "Para cualquier queja o reclamación en el servicio, favor de\n" +
                        "comunicarse a: (55)5575 2009\n" +
                        "\n" +
                        "Localización de la Unidad Especializada\n" +
                        "Carretera Picacho-Ajusco Nº 130, Despacho 203,\n" +
                        "Col. Jardines en la Montaña,C.P. 14210, México, D.F.\n" +
                        "Tel. (55)5575 2009 Ext.117\n" +
                        "Titular: Thelma Oralia Jiménez Alcalde\n" +

                        "----------------------------------------------------------------\n\n";
        return data;
    }
}