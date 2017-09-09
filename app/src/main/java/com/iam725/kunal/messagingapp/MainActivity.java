package com.iam725.kunal.messagingapp;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int result = 0;
    Uri contactUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        renderContact(null);
    }

    public void renderContact(Uri uri) {

        TextView contact_name = (TextView) findViewById(R.id.contact_name);
        TextView contact_phone = (TextView) findViewById(R.id.contact_phone);
        ImageView contact_photo = (ImageView) findViewById(R.id.contact_photo);

        if (uri == null) {
            contact_name.setText(R.string.select_a_contact);
            contact_phone.setText("");
            contact_photo.setImageBitmap(null);
        }
        else {
            contact_name.setText(getDisplayName(uri));
            contact_phone.setText(getMobileNumber(uri));
            contact_photo.setImageBitmap(getPhoto(uri));
        }
    }

    public void onUpdateContact(View view) {

        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),result);
    }

    public void onSendMessage(View view) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                getMobileNumber(contactUri),
                null,
                "I'm Cool",
                null,
                null
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == result) {
            if (resultCode == RESULT_OK) {
                contactUri = data.getData();
                renderContact(contactUri);
            }
        }
    }

    private String getDisplayName (Uri uri) {
        String displayName = null;

        Cursor cursor = getContentResolver().query(uri,null,null,null,null);

        if (cursor.moveToFirst()) {
            displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();
        return displayName;
    }

    private String getMobileNumber(Uri uri) {
        String phoneNumber = null;
        Cursor contactCursor = getContentResolver().query(uri,new String[] {ContactsContract.Contacts._ID},null,null,null);
        String id = null;
        if (contactCursor.moveToFirst()) {
            id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        contactCursor.close();

        Cursor phoneCursor = getContentResolver().query (
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",/* = AND "
                +ContactsContract.CommonDataKinds.Phone.TYPE + "="
                +ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,*/
                new String[] { id },
                null
        );
        if (phoneCursor.moveToFirst()) {
            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER)
            );
        }
        phoneCursor.close();
        return phoneNumber;
    }

    private Bitmap getPhoto (Uri uri) {
        Bitmap photo = null;
        String id = null;
        Cursor contactCursor = getContentResolver().query(
                uri,new String[] {ContactsContract.Contacts._ID},null,null,null
        );
        if (contactCursor.moveToFirst()) {
            id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
        contactCursor.close();

        try {
            InputStream inputStream =
                    ContactsContract.Contacts.openContactPhotoInputStream(
                            getContentResolver(),
                            ContentUris.withAppendedId(
                                    ContactsContract.Contacts.CONTENT_URI,
                                    new Long(id).longValue()
                            )
                    );
            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }
            if (photo != null) {
                inputStream.close();
            }
        } catch (IOException iox) {}
        return photo;
    }
}
