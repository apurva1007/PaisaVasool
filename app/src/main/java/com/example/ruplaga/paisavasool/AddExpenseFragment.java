package com.example.ruplaga.paisavasool;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddExpenseFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    String selectedCategory, mode, notes, dateString;
    float amount;
    int year, month, day;
    long date;

    Balance balance;

    View view;
    TextView mDisplayDate, amountView;
    RadioButton radioButton1, radioButton2, radioButton3;
    EditText notesView;
    DatePickerDialog.OnDateSetListener mDateSetListener;
    DatabaseReference databaseExpenses = null;
    DatabaseReference databaseReferenceBalance;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    String userID;

    public AddExpenseFragment() {
        // Required empty public constructor
    }

    public static AddExpenseFragment newInstance(String param1, String param2) {
        AddExpenseFragment fragment = new AddExpenseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_add_expense, container, false);
        amountView = view.findViewById(R.id.expenseAmount);
        radioButton1 = view.findViewById(R.id.radioButton1);
        radioButton2 = view.findViewById(R.id.radioButton2);
        radioButton3 = view.findViewById(R.id.radioButton3);
        notesView = view.findViewById(R.id.expenseNotes);

        Button save = (Button) view.findViewById(R.id.save);
        save.setOnClickListener(this);

        Button cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        databaseExpenses = mFirebaseDatabase.getReference();
        databaseReferenceBalance = mFirebaseDatabase.getReference().child(userID).child("balances");


        ListView categoryList = view.findViewById(R.id.checkable_list);
        String[] items = {"Other", "Food and Drinks", "Health", "Leisure", "Transportation"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.categorylayout, items);

        categoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        categoryList.setAdapter(adapter);
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = ((TextView) view).getText().toString();
                System.out.println("Selected items: " + selectedCategory);
            }
        });

        mDisplayDate = (TextView) view.findViewById(R.id.transactionDate);
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        cal.set(year,month,day);
        date = cal.getTimeInMillis();

        dateString = year + "-" + month + "-" + day;
        mDisplayDate.setText(dateString);

        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        mDateSetListener,
                        year,month,day);
                // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Calendar cal = Calendar.getInstance();
                cal.set(year,month,day);
                date = cal.getTimeInMillis();

                dateString = year + "-" + month + "-" + day;
                mDisplayDate.setText(dateString);
            }
        };


        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                onSaveClicked(v);
                break;
                
            case R.id.cancel:
                OnCancelClicked(v);
                break;
        }
    }

    private void OnCancelClicked(View v) {
        getFragmentManager().popBackStack();
    }

    private void onSaveClicked(View v) {
        amount = Float.parseFloat(amountView.getText().toString());

        if(radioButton1.isChecked()){
            mode = radioButton1.getText().toString();
            databaseExpenses.child(userID).child("balances").child("bankAccount").setValue(balance.bankAccount - amount);
        }else if(radioButton2.isChecked()){
            mode = radioButton2.getText().toString();
            databaseExpenses.child(userID).child("balances").child("paytm").setValue(balance.paytm - amount);
        }else if(radioButton3.isChecked()){
            mode = radioButton3.getText().toString();
            databaseExpenses.child(userID).child("balances").child("cash").setValue(balance.cash - amount);
        }

        notes = notesView.getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        String id = databaseExpenses.push().getKey();
        Transaction newExpense = new Transaction(id,TransactionType.Expense,amount,mode,selectedCategory,date,notes);

        databaseExpenses.child(userID).child("transactions").child(id).setValue(newExpense);

        Toast.makeText(getActivity(), "Expense Added", Toast.LENGTH_SHORT).show();

     //   System.out.println("Data Captured: " + expenseAmount + " , " + selectedCategory + " , " + mode + " , " + date + "," + expenseNotes);

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();

        System.out.println("ONSTARTTTTTTTTTT");

        databaseReferenceBalance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                balance = dataSnapshot.getValue(Balance.class);

                System.out.println("hghhgggggggggggggggggg");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
