package com.formiik.printertester;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bxl.config.editor.BXLConfigLoader;
import com.starmicronics.stario.PortInfo;
import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private final String                TAG = "MainActivity";

    private Button                      mButton_search,
                                        mButton_print;

    private TextView                    mText_printer;

    private ListView mList_devices;

    private LinearLayout                mLayout_printer;

    private CountDownTimer              mCountDownTimer;

    private ProgressDialog              mProgressDialog;

    private BluetoothAdapter            mBluetoothAdapter;

    private BluetoothReceiver           mBluetoothReceiver;

    private ArrayList<BluetoothDevice>  mPrinters;
    private ArrayList<String>           mPrintersNamesIDs;
    private ArrayAdapter<String>        mPrintersAdapter;
    private static BluetoothDevice      mPrinter;
    private static PortInfo             mStarPrinter;

    public static int                   REQUEST_BLUETOOTH = 1;

    private enum printerBrands          {STAR, BIXOLON, ZEBRA};

    private printerBrands               mCurrentPrinterBrand;

    private String                      mStarStatusPrinter;

    private POSPrinter                  posPrinter;

    private BXLConfigLoader             bxlConfigLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton_search      = (Button) findViewById(R.id.mButton_search);
        mButton_print       = (Button) findViewById(R.id.mButton_print);
        mList_devices       = (ListView) findViewById(R.id.mList_devices);
        mLayout_printer     = (LinearLayout) findViewById(R.id.mLayout_printer);
        mText_printer       = (TextView) findViewById(R.id.mText_printer);

        mButton_search.setOnClickListener(this);
        mButton_print.setOnClickListener(this);

        mList_devices.setOnItemClickListener(this);

        /*
            mPrintersNamesIDs variable para almancenar los nombre de los dispositivos y sus direcciones MAC, se mostrará en mList_devices
         */
        mPrintersNamesIDs = new ArrayList<>();
        mPrintersAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mPrintersNamesIDs);
        mList_devices.setAdapter(mPrintersAdapter);

        mPrinters = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*
            Revisa si el telefono cuenta con bluetooth
         */
        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_compatible)
                    .setMessage(R.string.no_bluetooh)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }


        enableBluetoothAdapter();

        /*
            BroadcastReceiver de Bluetooth para detectar eventos
         */
        mBluetoothReceiver  = new BluetoothReceiver();

        registerReceiver(mBluetoothReceiver,    new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mBluetoothReceiver,    new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(mBluetoothReceiver,    new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(mBluetoothReceiver,    new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(mBluetoothReceiver,    new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));
        registerReceiver(mBluetoothReceiver,    new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST));

        /*
            Contador regresivo para detener la busqueda de dispositivos despues de 25 segundos
         */
        mCountDownTimer =  new CountDownTimer(1000 * 25, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                endDevicesSearch();
                Toast.makeText(getApplicationContext(), R.string.time_search_expired, Toast.LENGTH_SHORT).show();

                if(mPrinters.size() == 0){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), R.string.no_devices, Toast.LENGTH_LONG).show();
                }
            }
        };

        bxlConfigLoader = new BXLConfigLoader(this);
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }
        posPrinter = new POSPrinter(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);

        try {
            posPrinter.close();
        } catch (JposException e) {
            e.printStackTrace();
        }
    }

    public void enableBluetoothAdapter(){
        /*
            Encerder Bluetooth
         */
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

    }
    /*
        BroadcastReceiver de Bluetooth para detectar eventos de inicio y termino de Discovering,
        dispositivos encontrados, dispositivos vinculados, dispositivos conectados y deconcectados
        y peticion de emparejamiento
     */
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null && !mPrinters.contains(device)) {
                    mPrinters.add(device);
                    if (device.getName() != null) {
                        mPrintersNamesIDs.add(device.getName() + " (" + device.getAddress() + ")");
                    }else{
                        mPrintersNamesIDs.add(device.getAddress());
                    }
                    mPrintersAdapter.notifyDataSetChanged();
                }
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(getApplicationContext(), R.string.searching, Toast.LENGTH_SHORT).show();

            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                endDevicesSearch();

            }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    //Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    //Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT).show();
                }

            }else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                String pin = "1234";

                if(mCurrentPrinterBrand == printerBrands.STAR) {
                    pin = "1234";
                }else  if(mCurrentPrinterBrand == printerBrands.BIXOLON) {
                    pin = "0000";
                }

                try {
                    byte[] pinBytes = pin.getBytes();
                    Method m = mPrinter.getClass().getMethod("setPin", byte[].class);
                    m.invoke(mPrinter, pinBytes);
                    mPrinter.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(mPrinter, true);

                    /*BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int pinINT = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                    //the pin in case you need to accept for an specific pin
                    Log.d(TAG, "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",1234));
                    byte[] pinBytes2;
                    pinBytes2 = (""+pinINT).getBytes("UTF-8");
                    device.setPin(pinBytes2);
                    //setPairing confirmation if neeeded
                    device.setPairingConfirmation(true);
                    */
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Esta conectado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
        Detener al busqueda de dispositivos bluetooth
     */
    private void endDevicesSearch(){
        mButton_search.setText(R.string.search);
        mCountDownTimer.cancel();
        mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(mBluetoothAdapter.isEnabled() && requestCode == REQUEST_BLUETOOTH) {
            if(!mBluetoothAdapter.isDiscovering()){
                mButton_search.performClick();
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.mButton_search:

                if(!mBluetoothAdapter.isEnabled()) {
                    enableBluetoothAdapter();
                    break;
                }

                if(!mBluetoothAdapter.isDiscovering()){


                    mLayout_printer.setVisibility(View.GONE);

                    mButton_search.setText(R.string.stop);

                    if(!mPrinters.isEmpty())mPrinters.clear();
                    if(!mPrintersNamesIDs.isEmpty()) mPrintersNamesIDs.clear();
                    if(!mPrintersAdapter.isEmpty())mPrintersAdapter.notifyDataSetChanged();

                    /*
                        Busca que dispositivos ya estaban emparejados al telefono y los agrega a la
                        mPrintersNamesIDs

                        Un dispositivo emparejado no implica que el dispositivo este encendido o conectado
                    */
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    for(BluetoothDevice deviceBonded : pairedDevices){
                        mPrinters.add(deviceBonded);
                        if (deviceBonded.getName() != null) {
                            mPrintersNamesIDs.add(deviceBonded.getName() + " (" + deviceBonded.getAddress() + ")");
                        }else{
                            mPrintersNamesIDs.add(deviceBonded.getAddress());
                        }
                    }

                    mPrintersAdapter.notifyDataSetChanged();

                    mBluetoothAdapter.startDiscovery();
                    mCountDownTimer.start();

                }else {
                    endDevicesSearch();
                    Toast.makeText(getApplicationContext(), R.string.search_sttoped, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.mButton_print:
                /*
                    Revisa a que marca pertenece la impresora y ejecuta el metodo de impresion
                    correspondiente
                 */

                if(mCurrentPrinterBrand == printerBrands.STAR) {
                    new StarPrinterPrintTask().execute();
                }else  if(mCurrentPrinterBrand == printerBrands.BIXOLON) {
                    new BixelonPrinterPrintTask().execute();
                }


                break;
        }
    }

    /*
        Seleccion de un dispositivo encontrado para iniciar emparejamiento/vinculacion
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        switch (parent.getId()){
            case R.id.mList_devices:

                AlertDialog.Builder dialogConnect = new AlertDialog.Builder(this);
                dialogConnect.setTitle(R.string.link_dialog);
                dialogConnect.setMessage(mPrintersNamesIDs.get(position));
                dialogConnect.setCancelable(true);
                dialogConnect.setPositiveButton(R.string.link, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogConnect, int id) {
                        endDevicesSearch();
                        connectToPrinter(position);
                    }
                });
                dialogConnect.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogConnect, int id) {
                        dialogConnect.dismiss();
                    }
                });
                dialogConnect.show();

                break;
        }
    }

    /*
        Método para detectar la marca de la impresora e iniciar emparejamiento
     */
    private void connectToPrinter(int position) {

        mPrinter = mPrinters.get(position);

        if (BluetoothAdapter.checkBluetoothAddress(mPrinter.getAddress())) {

            if(mPrinter.getName() != null && mPrinter.getName().toLowerCase().contains("star")){

                mCurrentPrinterBrand = printerBrands.STAR;
                new PairPrinter().execute(mPrinter);

            }else if(mPrinter.getName() != null && mPrinter.getName().toLowerCase().contains("spp")){

                mCurrentPrinterBrand = printerBrands.BIXOLON;
                new PairPrinter().execute(mPrinter);

            }else{
                Toast.makeText(getApplicationContext(), R.string.no_printer, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*
        Método para emparejar dispositivo
        Si bool es verdadero, el dispositivo no esta emparejado.
     */
    private class PairPrinter extends AsyncTask<BluetoothDevice, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.linking));
        }
        @Override
        protected Boolean doInBackground(BluetoothDevice... params) {
            BluetoothDevice printer = params[0];
            try {
                Boolean bool;

                try {
                    Method method = printer.getClass().getMethod("createBond", (Class[]) null);
                    method.invoke(printer, (Object[]) null);

                    Thread.sleep(2000);

                    bool = (Boolean) method.invoke(printer);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

                if(bool) {
                    return false;
                }

                /*
                if(!bool){

                    if (!(printer.getBondState() == BluetoothDevice.BOND_BONDED)){
                        Log.e("359 boolero", " "+ bool);
                        return false;
                    }
                }else {
                    Log.e("363 boolero", " "+ bool);
                    return false;
                }
                */
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();

            /*
                Si el emparejamiento se llevo a cabo, se muestra el botón para iniciar el proceso
                de impresión
             */
            if (result) {
                mText_printer.setText(mPrinter.getName());

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                }

                mLayout_printer.setVisibility(View.VISIBLE);
            } else {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setNegativeButton(R.string.ok, null);

                dialog.setTitle(R.string.printer + mPrinter.getName());
                dialog.setMessage(R.string.error_connect);
                dialog.setCancelable(false);

                AlertDialog alert = dialog.create();
                alert.show();
            }
        }
    }


    /*
        AsyncTask para iniciar el proceso de impresión con una impresora Star.
     */
    private class StarPrinterPrintTask extends AsyncTask<PortInfo, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.printing));
        }

        @Override
        protected Boolean doInBackground(PortInfo... params) {

            /*
                Verifica que la impresora no este apagada, que tenga papel y que la tapa este cerrada
             */
            if(!starPrinterCheckStatus()){
                return false;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }

            boolean result = true;

            PortInfo    printer =       mStarPrinter;
            Context     context =       getApplicationContext();
            String      portName =      printer.getPortName();
            String      portSettings =  starPrinterGetPortSettings();
            int         source =        R.drawable.progresemos;


             /*
                Obtiene el recibo LineMode
             */
            ArrayList<byte[]> byteListLine = StarReceipt.getReceiptLineMode(); //

            /*
                Obtiene el recibo RasterMode
             */
            //ArrayList<byte[]> byteListRaster = StarReceipt.getReceiptRasterMode(portSettings);

            /*
                Obtiene el recibo la imagen/logo
             */
            ArrayList<byte[]> byteListImage = StarReceipt.getReceiptImage(portSettings, source, getResources());


             /*
                Ejecuta la impresion para cada byteList definido
                Al mandarlo a llamar seguido, se imprime como un solo ticket
             */
            result = starPrinterPrint(context, portName, portSettings, byteListImage);

            if(!result)
                return result;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            result = starPrinterPrint(context, portName, portSettings, byteListLine);
            //result = starPrinterPrint(context, portName, portSettings, byteListRaster);

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();


            if(result)
                mStarStatusPrinter = getString(R.string.finished_print);

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setNegativeButton(R.string.ok, null);

            dialog.setTitle(mPrinter.getName());
            dialog.setMessage(mStarStatusPrinter);
            dialog.setCancelable(false);

            AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    /*
        Metodo para ejecutar la impresion de Star
        Se imprime lo correspondiente a byteList
     */
    private boolean starPrinterPrint(Context context, String portName, String portSettings, ArrayList<byte[]> byteList){
        // sendCommand from SDK
        boolean result = true;
        StarIOPort port = null;
        try {
			/*
			 * using StarIOPort3.1.jar (support USB Port) Android OS Version: upper 2.2
			 */
            port = StarIOPort.getPort(portName, portSettings, 10000, context);
			/*
			 * using StarIOPort.jar Android OS Version: under 2.1 port = StarIOPort.getPort(portName, portSettings, 10000);
			 */
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }

			/*
			 * Using Begin / End Checked Block method When sending large amounts of raster data,
			 * adjust the value in the timeout in the "StarIOPort.getPort" in order to prevent
			 * "timeout" of the "endCheckedBlock method" while a printing.
			 *
			 * If receipt print is success but timeout error occurs(Show message which is "There
			 * was no response of the printer within the timeout period." ), need to change value
			 * of timeout more longer in "StarIOPort.getPort" method.
			 * (e.g.) 10000 -> 30000
			 */
            StarPrinterStatus status = port.beginCheckedBlock();

            if (true == status.offline) {
                mStarStatusPrinter = getString(R.string.printer_offline);
                result = false;
            }

            byte[] commandToSendToPrinter = starPrinterconvertFromListByteArrayTobyteArray(byteList);
            port.writePort(commandToSendToPrinter, 0, commandToSendToPrinter.length);

            port.setEndCheckedBlockTimeoutMillis(30000);// Change the timeout time of endCheckedBlock method.
            status = port.endCheckedBlock();

            if (status.offline == true) {
                mStarStatusPrinter = getString(R.string.printer_offline);
                result = false;
            } else if (status.receiptPaperEmpty == true) {
                mStarStatusPrinter += "\n" + getString(R.string.paper_empty);
                result = false;
            } else if (status.coverOpen == true) {
                mStarStatusPrinter += "\n" + getString(R.string.cover_open);
                result = false;
            }
        } catch (StarIOPortException e) {
            e.printStackTrace();
            mStarStatusPrinter = getString(R.string.error_connect);
            result = true;
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
        return result;
    }

    /*
        Método de apoyo para la impresion Star
     */
    private static byte[] starPrinterconvertFromListByteArrayTobyteArray(List<byte[]> ByteArray) {
        int dataLength = 0;
        for (int i = 0; i < ByteArray.size(); i++) {
            dataLength += ByteArray.get(i).length;
        }

        int distPosition = 0;
        byte[] byteArray = new byte[dataLength];
        for (int i = 0; i < ByteArray.size(); i++) {
            System.arraycopy(ByteArray.get(i), 0, byteArray, distPosition, ByteArray.get(i).length);
            distPosition += ByteArray.get(i).length;
        }

        return byteArray;
    }

    /*
        Método de apoyo para la impresion Star
     */
    private String starPrinterGetPortSettings() {

        String portSettings = "portable";

        if((Build.VERSION.SDK_INT == 14) || (Build.VERSION.SDK_INT == 15) ||(Build.VERSION.SDK_INT == 16)){
            portSettings += ";u";
        }

        portSettings += "";     // No Retry
        //portSettings += ";l"; //Retry

        return portSettings;
    }

    /*
        Método para revisar el estado de al impresora Star
        Detecta si esta apagada, no tiene papel o si la tapa esta abierta
     */
    public boolean starPrinterCheckStatus() {
        //CheckStatus from SDK
        boolean isOK = true;

        Context context = MainActivity.this;

        List<PortInfo> BTPortList;
        ArrayList<PortInfo> arrayDiscovery = new ArrayList<>();;

        try {
            BTPortList = StarIOPort.searchPrinter("BT:");

            for (PortInfo portInfo : BTPortList) {
                arrayDiscovery.add(portInfo);
            }

            for (PortInfo printer : arrayDiscovery) {
                if(printer.getMacAddress().equalsIgnoreCase(mPrinter.getAddress())){
                    mStarPrinter = printer;
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        String portName = mStarPrinter.getPortName();
        String portSettings = starPrinterGetPortSettings();

        StarIOPort port = null;
        try {

            port = StarIOPort.getPort(portName, portSettings, 30000, context);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            StarPrinterStatus status = port.retreiveStatus();

            if (status.offline == true) {

                mStarStatusPrinter = getString(R.string.printer_offline);

                if (status.receiptPaperEmpty == true) {
                    mStarStatusPrinter += "\n" + getString(R.string.paper_empty);
                }

                if (status.coverOpen == true) {
                    mStarStatusPrinter += "\n" + getString(R.string.cover_open);
                }

                isOK = false;
            }

        } catch (StarIOPortException e) {

            mStarStatusPrinter = getString(R.string.error_connect);
            isOK = false;
        } finally {
            if (port != null) {
                try {
                    StarIOPort.releasePort(port);
                } catch (StarIOPortException e) {
                }
            }
        }
        return isOK;
    }

    /*
        Dialogo de espera para los procesos en hilos
     */
    protected void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }


    /*
       AsyncTask para iniciar el proceso de impresión con una impresora Star.
    */
    private class BixelonPrinterPrintTask extends AsyncTask<PortInfo, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.printing));
            mStarStatusPrinter = "";
        }

        @Override
        protected Boolean doInBackground(PortInfo... params) {

            boolean result = true;

            String printerName      = mPrinter.getName();
            String printerAddress   = mPrinter.getAddress();

            /*
                Configuración
             */
            try {
                for (Object entry : bxlConfigLoader.getEntries()) {
                    JposEntry jposEntry = (JposEntry) entry;
                    bxlConfigLoader.removeEntry(jposEntry.getLogicalName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            try {
                bxlConfigLoader.addEntry(printerName,
                        BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER,
                        printerName,
                        BXLConfigLoader.DEVICE_BUS_BLUETOOTH, printerAddress);

                bxlConfigLoader.saveFile();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }


            /*
                Caracteristicas de impresion de imagenes
             */
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put((byte) POSPrinterConst.PTR_S_RECEIPT);
            buffer.put((byte) 60);  //brightness
            buffer.put((byte) 1);   //compress
            buffer.put((byte) 0x00);

            /*
                Obtención de imagen a imprimir
             */
            Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.progresemos));

            /*
                Obtención del texto del recibo
             */
            String data = BixolonReceipt.getReceipt();



            try {
                posPrinter.open(mPrinter.getName());
                posPrinter.claim(0);
                posPrinter.setDeviceEnabled(true);

                /*
                    Método para imprimir una imagen
                 */
                posPrinter.printBitmap(buffer.getInt(0), bitmap, 288, POSPrinterConst.PTR_BM_CENTER);

                /*
                    Método para imprimir texto
                 */
                posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, data);

            } catch (JposException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    posPrinter.close();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();

            if(result)
                mStarStatusPrinter = getString(R.string.finished_print);
            else{
                if(mStarStatusPrinter.equalsIgnoreCase(""))  mStarStatusPrinter = getString(R.string.error_connect);
            }


            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setNegativeButton(R.string.ok, null);

            dialog.setTitle(mPrinter.getName());
            dialog.setMessage(mStarStatusPrinter);
            dialog.setCancelable(false);

            AlertDialog alert = dialog.create();
            alert.show();
        }
    }




}