package com.example.ruplaga.paisavasool;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddIncomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddIncomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddIncomeFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    String selectedCategory, mode, dateString, notes;
    float amount;
    int year, month, day;
    Long date;

    View view;
    TextView mDisplayDate, amountView;
    RadioButton radioButton1, radioButton2, radioButton3;
    EditText notesView;
    DatePickerDialog.OnDateSetListener mDateSetListener;

    DatabaseReference databaseReference = null;
    DatabaseReference databaseReferenceBalance = null;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    String userID;

    Balance balance;

    public AddIncomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddIncomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddIncomeFragment newInstance(String param1, String param2) {
        AddIncomeFragment fragment = new AddIncomeFragment();
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
        view = inflater.inflate(R.layout.fragment_add_income, container, false);
        amountView = view.findViewById(R.id.amount);
        radioButton1 = view.findViewById(R.id.radioButton1);
        radioButton2 = view.findViewById(R.id.radioButton2);
        radioButton3 = view.findViewById(R.id.radioButton3);
        notesView = view.findViewById(R.id.notes);

        Button save = (Button) view.findViewById(R.id.save);
        save.setOnClickListener(this);

        Button cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        databaseReference = mFirebaseDatabase.getReference();
        databaseReferenceBalance = mFirebaseDatabase.getReference().child(userID).child("balances");

        System.out.println("ONCReateeeeeeeeeeeeeeee");

        ListView categoryList = view.findViewById(R.id.checkable_list);
        String[] items = {"Salary", "Cashback", "Money Transfer"};
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

        dateString = year + "-" + (month+1) + "-" + day;
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
                month = month;
                Calendar cal = Calendar.getInstance();
                cal.set(year,month,day);
                date = cal.getTimeInMillis();

                dateString = year + "-" + (month+1) + "-" + day;
                mDisplayDate.setText(dateString);
            }
        };

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
            databaseReference.child(userID).child("balances").child("bankAccount").setValue(balance.bankAccount + amount);
        }else if(radioButton2.isChecked()){
            mode = radioButton2.getText().toString();
            databaseReference.child(userID).child("balances").child("paytm").setValue(balance.paytm + amount);
        }else if(radioButton3.isChecked()){
            mode = radioButton3.getText().toString();
            databaseReference.child(userID).child("balances").child("cash").setValue(balance.cash + amount);
        }

        notes = notesView.getText().toString();

        String id = databaseReference.push().getKey();
        Transaction newIncome = new Transaction(id,TransactionType.Income,amount,mode,selectedCategory,date,notes);
        databaseReference.child(userID).child("transactions").child(id).setValue(newIncome);



        Toast.makeText(getActivity(), "Income Added", Toast.LENGTH_SHORT).show();

       // System.out.println("Data Captured: " + amount + " , " + selectedCategory + " , " + mode + " , " + date + " , " + notes);

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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
