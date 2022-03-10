package in.thelosergeek.pdfwriter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button save, print;
    EditText name,address,news,prescription,symptoms;
    CheckBox morning, noon, night;
    Spinner dose1,dose2, dose3;
    ArrayAdapter<String> adapter;

    DatabaseClass databaseClass;
    SQLiteDatabase sqLiteDatabase;
    Date date = new Date();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat timepatternFormat = new SimpleDateFormat("hh:mm a");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save = findViewById(R.id.btn_save);
        print = findViewById(R.id.btn_print);
        name = findViewById(R.id.name);
        news = findViewById(R.id.on);
        prescription = findViewById(R.id.prescription);
        symptoms = findViewById(R.id.symptoms);
        address = findViewById(R.id.address);

        morning = (CheckBox) findViewById(R.id.morning);
        noon = (CheckBox) findViewById(R.id.noon);
        night = (CheckBox) findViewById(R.id.night);

        //get the spinner from the xml.
        dose1 = findViewById(R.id.dose1);
        dose2 = findViewById(R.id.dose2);
        dose3 = findViewById(R.id.dose3);

        //create a list of items for the spinner.
        String[] doses = new String[]{"1", "2", "3"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doses);
        //set the spinners adapter to the previously created one.
        dose1.setAdapter(adapter);
        dose2.setAdapter(adapter);
        dose3.setAdapter(adapter);

        databaseClass = new DatabaseClass(this);
        sqLiteDatabase = databaseClass.getWritableDatabase();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = String.valueOf(name.getText());
                String userAddress = String.valueOf(address.getText());
                String userSymptoms = String.valueOf(symptoms.getText());
                String userPrescription = String.valueOf(prescription.getText());
                String morningString = "morning";
                String noonString = "noon";
                String nightString = "night";
                databaseClass.insert(userName,userAddress,date.getTime(),userSymptoms,userPrescription);

                Toast.makeText(getApplicationContext(),"Saved in Storage", Toast.LENGTH_SHORT).show();

                printInvoice();
                name.setText("");
                address.setText("");
                symptoms.setText("");
                prescription.setText("");

            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RetrievePreviousPrescription.class);
                startActivity(intent);
            }
        });

    }

    private void printInvoice() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        String[] columns = {"invoiceNo","Name","address","date","symptoms","prescription"};
        Cursor cursor = sqLiteDatabase.query("myTable",columns,null,null,null,null,null);
        cursor.move(cursor.getCount());

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1000,900,1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(80);
        canvas.drawText("Hospital Name",250,80,paint);

        paint.setTextSize(30);
        canvas.drawText("Dr. XYZ", 30, 150,paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Invoice No: ",canvas.getWidth()-70,150,paint);
        canvas.drawText(String.valueOf(cursor.getInt(0)),canvas.getWidth()-40,150,paint);

        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(Color.BLACK);
        canvas.drawText("Date:",30,200, paint);
        canvas.drawText(simpleDateFormat.format(cursor.getLong(3)),120,200,paint);

        canvas.drawText("Time:",750,200,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(timepatternFormat.format(cursor.getLong(3)),canvas.getWidth()-40,200,paint);
        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(Color.BLACK);
        canvas.drawText("Patient Name:",30,350,paint);

        canvas.drawText("Patient ID:",620,350,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(Color.BLACK);
        canvas.drawText(cursor.getString(1),30,380,paint);
        canvas.drawText(cursor.getString(2),620,380,paint);
        paint.setTextAlign(Paint.Align.RIGHT);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        canvas.drawText("Medicine Name:",30,450,paint);
        canvas.drawText(cursor.getString(4),30,480,paint);

        canvas.drawText("Slot No.:",620,450,paint);
        canvas.drawText(cursor.getString(5),620,480,paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Dose Times:",200,550,paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        if (morning.isChecked()) {
            canvas.drawText("Morning:",200,580,paint);
            canvas.drawText(dose1.getSelectedItem().toString(), 240, 580, paint);
        }
        if (noon.isChecked()) {
            canvas.drawText("Noon:",200,620,paint);
            canvas.drawText(dose2.getSelectedItem().toString(), 240, 620, paint);
        }
        if (night.isChecked()) {
            canvas.drawText("Night:",200,660,paint);
            canvas.drawText(dose3.getSelectedItem().toString(), 240, 660, paint);
        }

        paint.setTextAlign(Paint.Align.RIGHT);


        canvas.drawText("STAY HEALTHY",900,800,paint);


        pdfDocument.finishPage(page);

        File file = new File(this.getExternalFilesDir("/"),cursor.getString(1)+"-prescription.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfDocument.close();


    }
}