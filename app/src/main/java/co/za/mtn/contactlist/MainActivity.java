package co.za.mtn.contactlist;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    //initialize variable
    RecyclerView recyclerView;
    ArrayList<ContactModel> arrayList = new ArrayList<ContactModel>();
    MainAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign variable
        recyclerView = findViewById(R.id.recycler_view);

        //Handling clicks
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Share(arrayList.get(position).name, arrayList.get(position).number);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        //Check permission
        checkPermission();
    }

    //Share function
    public void Share(String name, String number){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, name);
        intent.putExtra(Intent.EXTRA_TEXT, name + " " + number);
        startActivity(Intent.createChooser(intent, "Share using"));
    }

    private void checkPermission() {
        //check condition
        if (ContextCompat.checkSelfPermission(MainActivity.this
                 , Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED){
            //When permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this
            ,new String[]{Manifest.permission.READ_CONTACTS},100);
        }else {
            //When permission is granted
            //create method
            getContactList();
        }
    }


    private void getContactList() {
        //Initialize Uri Uri
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //Sort by Ascending
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC";
        //Initialize Cursor
        Cursor cursor = getContentResolver().query(
                uri,null,null,null,sort
        );
        //check condition
        if (cursor.getCount() > 0){
            //When count is greater than 0
            //use while loop
            while (cursor.moveToNext()){
                //Cursor move to next
                //Get contact id
                String id = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts._ID
                ));
                //Get Contact Name
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                ));
                //Initialize Phone uri
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //Initialize Selection
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                        +" =?";
                //Initialize Phone cursor
                Cursor phoneCursor = getContentResolver().query(
                        uriPhone,null,selection
                        ,new String[]{id},null
                );
                //Check Condition
                if (phoneCursor.moveToNext()){
                    //When phone cursor move to next
                    String number =phoneCursor.getString(phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    ));
                    //Initialize Contact model
                    ContactModel model = new ContactModel();
                    //Set name
                    model.setName(name);
                    //Set number
                    model.setNumber(number);
                    //Add model in array list
                    arrayList.add(model);
                    //Close phone cursor
                    phoneCursor.close();
                }
            }
            //Close cursor
            cursor.close();
        }
        //Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Initialize adapter
        adapter = new MainAdapter(this,arrayList);
        //Set adapter
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check condition
        if (requestCode == 100 && grantResults.length > 0 && grantResults [0]
                == PackageManager.PERMISSION_GRANTED){
            //When permission granted
            //Call method
            getContactList();
        }else {
            //When permission is denied
            //Display Toast
            Toast.makeText(MainActivity.this,"Permission Denied."
                    ,Toast.LENGTH_SHORT).show();
            //Call check permission method
            checkPermission();
        }
    }
}