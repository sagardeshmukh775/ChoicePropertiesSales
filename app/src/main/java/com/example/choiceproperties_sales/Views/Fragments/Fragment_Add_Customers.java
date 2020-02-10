package com.example.choiceproperties_sales.Views.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.choiceproperties_sales.CallBack.CallBack;
import com.example.choiceproperties_sales.Constant.Constant;
import com.example.choiceproperties_sales.Exception.ExceptionUtil;
import com.example.choiceproperties_sales.Models.Customer;
import com.example.choiceproperties_sales.R;
import com.example.choiceproperties_sales.Views.Activities.CropImageActivity;
import com.example.choiceproperties_sales.Views.Activities.ImagePickerActivity;
import com.example.choiceproperties_sales.Views.dialog.ProgressDialogClass;
import com.example.choiceproperties_sales.repository.UserRepository;
import com.example.choiceproperties_sales.repository.impl.UserRepositoryImpl;
import com.example.choiceproperties_sales.service.ImageCompressionService;
import com.example.choiceproperties_sales.service.impl.ImageCompressionServiceImp;
import com.example.choiceproperties_sales.utilities.FileUtils;
import com.example.choiceproperties_sales.utilities.Utility;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class Fragment_Add_Customers extends Fragment implements View.OnClickListener {

    EditText inputName, inputMobile, inputNote, inputAddress, inputDateTime, inputDiscussion;
    Button btnAdd;
    ImageView imgCustomer, imgAttachment;
    private DatePickerDialog mDatePickerDialog;
    String fdate;
    int mHour;
    int mMinute;
    String image;
    private List<Uri> fileDoneList;
    private Uri filePath;

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_CROP_IMAGE = 2342;
    private static final int REQUEST_PICK_IMAGE = 1002;

    ProgressDialogClass progressDialogClass;
    UserRepository userRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_customers, container, false);

        userRepository = new UserRepositoryImpl(getActivity());
//        leedRepository = new LeedRepositoryImpl();
        progressDialogClass = new ProgressDialogClass(getActivity());

        fileDoneList = new ArrayList<>();

        inputName = (EditText) view.findViewById(R.id.username);
        inputMobile = (EditText) view.findViewById(R.id.mobilenumber);
        inputNote = (EditText) view.findViewById(R.id.note);
        inputAddress = (EditText) view.findViewById(R.id.address);
        inputDateTime = (EditText) view.findViewById(R.id.date_time);
        inputDiscussion = (EditText) view.findViewById(R.id.discussion);
        btnAdd = (Button) view.findViewById(R.id.add_button);

        imgCustomer = (ImageView) view.findViewById(R.id.iv_customerImage);
        imgAttachment = (ImageView) view.findViewById(R.id.attachment);

        btnAdd.setOnClickListener(this);
        imgCustomer.setOnClickListener(this);
        imgAttachment.setOnClickListener(this);
        setDateTimeField();
        inputDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerDialog.show();
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {

        try {

            if (v == btnAdd) {
                progressDialogClass.showDialog(this.getString(R.string.loading), this.getString(R.string.PLEASE_WAIT));
                Customer customer = fillUserModel();
                CreateCustomer(customer);
            }
            else if (v == imgCustomer){

                pickImage();
            }


        } catch (Exception e) {
        }

    }

    public void pickImage() {
        startActivityForResult(new Intent(getActivity(), ImagePickerActivity.class), REQUEST_PICK_IMAGE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (data != null) {
                switch (requestCode) {
                    case REQUEST_PICK_IMAGE:

                        Uri imagePath = Uri.parse(data.getStringExtra("image_path"));

                        String str = imagePath.toString();
                        String whatyouaresearching = str.substring(0, str.lastIndexOf("/"));
                        image = whatyouaresearching.substring(whatyouaresearching.lastIndexOf("/") + 1, whatyouaresearching.length());
                        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                        File file = new File(root, image);

                        filePath = Uri.fromFile(file);
                        setImage(filePath);
                        break;
                }
            }
        } else {

            System.out.println("Failed to load image");
        }

    }

    private void setImage(Uri imagePath) {

        imgCustomer.setImageURI(imagePath);
    }

    private void CreateCustomer(Customer customer) {
        userRepository.createCustomer(customer, new CallBack() {
            @Override
            public void onSuccess(Object object) {
                Toast.makeText(getContext(), "Customer Added Successfully", Toast.LENGTH_SHORT).show();
                inputName.setText("");
                inputMobile.setText("");
                inputAddress.setText("");
                inputNote.setText("");
                inputDateTime.setText("");
                inputDiscussion.setText("");
                progressDialogClass.dismissDialog();
            }

            @Override
            public void onError(Object object) {

            }
        });

    }


    private Customer fillUserModel() {
        Customer customer = new Customer();
        customer.setName(inputName.getText().toString());
        customer.setAddress(inputAddress.getText().toString());
        customer.setMobile(inputMobile.getText().toString());
        customer.setAttendedBy(inputNote.getText().toString());
        customer.setDateTime(inputDateTime.getText().toString());
        customer.setDiscussion(inputDiscussion.getText().toString());
        customer.setStatus(Constant.STATUS_REQUEST_VISITED);
        customer.setCustomerId(Constant.CUSTOMERS_TABLE_REF.push().getKey());

        return customer;
    }


    private void setDateTimeField() {
        Calendar newCalendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
                final Date startDate = newDate.getTime();
                fdate = sd.format(startDate);

                timePicker();
            }

            private void timePicker() {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                mHour = hourOfDay;
                                mMinute = minute;

                                inputDateTime.setText(fdate + " " + hourOfDay + ":" + minute);
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}