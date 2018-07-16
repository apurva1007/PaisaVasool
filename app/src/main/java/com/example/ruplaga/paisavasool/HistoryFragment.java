package com.example.ruplaga.paisavasool;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public HistoryFragment() {
        // Required empty public constructor
    }

    private OnFragmentInteractionListener mListener;

    //basic history fragment view
    ListView listView;
    TextView sort, filter, noTransactionMessage;
    LinearLayout bottomLayout;
    ActionMode mActionMode;
    FloatingActionButton exportData;

    //bottom sheet dialog views
    BottomSheetDialog mBottomSheetDialog;
    Button typeButton, modeButton, categoryButton, dateRangeButton;
    CheckBox cFood, cOther, cTransportation, cHealth, cLeisure, cExpense, cIncome, cSalary, cCashback, cMoneyTransfer, cAccount, cPaytm, cCash;
    LinearLayout typeLayout, modeLayout, categoryLayout, dateRangeLayout;
    EditText fromDate, toDate;
    DatePickerDialog.OnDateSetListener mDateSetListener1, mDateSetListener2;
    Button amountLowToHigh, amountHighToLow, recentFirst, oldestFirst;
    Button clearAction, cancelAction, applyAction;

    //database variables
    DatabaseReference databaseExpenses = null;
    DatabaseReference databaseBalances = null;
    //variables
    int year, month, day;
    Long longDateFrom, longDateTo;
    String dateString;
    File myExternalFile;
    ExpenseList customAdapter;
    Balance balance;
    List<Transaction> transactionList = new ArrayList<>();
    List<Transaction> newTransactionList = new ArrayList<>();
    private String filename = "transaction" + new Date().getTime() + ".csv";
    private String filepath = "/PaisaVasool";
    int[] color = {R.color.red, R.color.green};

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

        }
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        listView = view.findViewById(R.id.historyList);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(position);
                view.setSelected(true);
                MyActionModeCallback callback = new MyActionModeCallback(position, view);
                mActionMode = HistoryFragment.this.getActivity().startActionMode(callback);
                mActionMode.setTitle("Options");
                return true;
            }
        });
        bottomLayout = view.findViewById(R.id.bottom_layout);
//        noTransactionMessage = view.findViewById(R.id.noTransactionText);

        sort = view.findViewById(R.id.sort);
        sort.setOnClickListener(this);

        filter = view.findViewById(R.id.filter);
        filter.setOnClickListener(this);

        exportData = view.findViewById(R.id.exportData);
        exportData.setOnClickListener(this);
        exportData.setImageResource(R.drawable.ic_csv);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            exportData.setEnabled(false);
        }
        else {
            File root = new File(Environment.getExternalStorageDirectory()+filepath);
            if (!root.exists())
                root.mkdirs();
            myExternalFile = new File(root, filename);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseExpenses = FirebaseDatabase.getInstance().getReference().child(userId).child("transactions");
        databaseBalances = FirebaseDatabase.getInstance().getReference().child(userId).child("balances");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        databaseExpenses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();
                newTransactionList.clear();
                for(DataSnapshot expenseSnapshot : dataSnapshot.getChildren()){
                    Transaction expense = expenseSnapshot.getValue(Transaction.class);
                    transactionList.add(expense);
                }

                createCopy();
                System.out.println(newTransactionList.size());

                if(getActivity()!=null){
                    customAdapter = new ExpenseList(getActivity(), transactionList);
                    listView.setAdapter(customAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        if(transactionList.size() == 0) {
//            bottomLayout.setVisibility(View.GONE);
//            exportData.setVisibility(View.GONE);
//            listView.setVisibility(View.GONE);
//            noTransactionMessage.setVisibility(View.VISIBLE);
//        }
//        else{
//            noTransactionMessage.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter:

                openFilter();
                break;

            case R.id.sort:
                openSort();
                break;

            case R.id.buttonType:
                typeLayout.setVisibility(View.VISIBLE);
                modeLayout.setVisibility(View.INVISIBLE);
                categoryLayout.setVisibility(View.INVISIBLE);
                dateRangeLayout.setVisibility(View.INVISIBLE);

                break;

            case R.id.buttonMode:
                typeLayout.setVisibility(View.INVISIBLE);
                modeLayout.setVisibility(View.VISIBLE);
                categoryLayout.setVisibility(View.INVISIBLE);
                dateRangeLayout.setVisibility(View.INVISIBLE);

                break;

            case R.id.buttonCategory:
                typeLayout.setVisibility(View.INVISIBLE);
                modeLayout.setVisibility(View.INVISIBLE);
                categoryLayout.setVisibility(View.VISIBLE);
                dateRangeLayout.setVisibility(View.INVISIBLE);

                break;

            case R.id.buttonDateRange:
                typeLayout.setVisibility(View.INVISIBLE);
                modeLayout.setVisibility(View.INVISIBLE);
                categoryLayout.setVisibility(View.INVISIBLE);
                dateRangeLayout.setVisibility(View.VISIBLE);

                break;

            case R.id.applyAction:
                getFilterData();
                mBottomSheetDialog.dismiss();
                break;

            case R.id.clearAllAction:
                clearAll();
                getFilterData();
                mBottomSheetDialog.dismiss();
                break;

            case R.id.cancelAction:
                mBottomSheetDialog.dismiss();
                break;

            case R.id.exportData:
                exportTxt();
                break;

        }
    }



    private void deleteTransaction(String transactionId) {

        System.out.println("In delete transaction");

        Query query = databaseExpenses.orderByChild("transactionId").equalTo(transactionId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot deleteSnapshot: dataSnapshot.getChildren()) {
                    deleteSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



       // getFilterData();
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }




    public void createCopy(){
        newTransactionList.clear();
        for(int i = 0; i < transactionList.size(); i++){
            newTransactionList.add(i, transactionList.get(i));
        }
    }

    public void exportTxt(){
        if (checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                FileOutputStream fos = new FileOutputStream(myExternalFile, true);
                for(int i = 0; i < newTransactionList.size(); i++){
                    Transaction t = newTransactionList.get(i);
                    String data = t.getTransactionId() + ","  + getStringDate(t) + "," + t.getTransactionType() + "," + t.getTransactionCategory()
                            + "," + t.getTransactionModeOfPayment() + "," + t.getTransactionAmount() + "," + t.getTransactionNotes()+"\n";
                    System.out.println(data);
                    fos.write(data.getBytes());
                }
                fos.close();
                Toast.makeText(getActivity(), "File Exported to : " + myExternalFile, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    private void openSort() {

        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_sortby, null);

        amountHighToLow = sheetView.findViewById(R.id.amountHighToLow);
        amountHighToLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        amountLowToHigh = sheetView.findViewById(R.id.amountLowToHigh);
        amountLowToHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recentFirst = sheetView.findViewById(R.id.recentFirst);
        recentFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        oldestFirst = sheetView.findViewById(R.id.oldestFirst);
        oldestFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();
    }

    public void clearAll() {
        cExpense.setChecked(false);
        cIncome.setChecked(false);
        cAccount.setChecked(false);
        cPaytm.setChecked(false);
        cCash.setChecked(false);
        cOther.setChecked(false);
        cFood.setChecked(false);
        cHealth.setChecked(false);
        cLeisure.setChecked(false);
        cTransportation.setChecked(false);
        cSalary.setChecked(false);
        cCashback.setChecked(false);
        cMoneyTransfer.setChecked(false);
        fromDate.setText(null);
        toDate.setText(null);
    }

    public static Date getZeroTimeDate(Date fecha) {
        Date res = fecha;
        Calendar calendar = Calendar.getInstance();

        calendar.setTime( fecha );
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        res = calendar.getTime();

        return res;
    }

    private void getFilterData() {
        newTransactionList.clear();
        Query query = null;
        query = databaseExpenses;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();

                for(DataSnapshot expenseSnapshot : dataSnapshot.getChildren()){
                    Transaction expense = expenseSnapshot.getValue(Transaction.class);
                    transactionList.add(expense);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //transactionType
        if(!cExpense.isChecked() && !cIncome.isChecked()){

        }
        else{
            if(!cIncome.isChecked()) {
                for (int i = 0; i < transactionList.size(); i++) {
                    if(transactionList.get(i) != null ){
                    if (transactionList.get(i).getTransactionType() == TransactionType.Income) {
                        transactionList.set(i, null);
                    }
                }}
            }
            if(!cExpense.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                    if(transactionList.get(i).getTransactionType() == TransactionType.Expense){
                        transactionList.set(i, null);
                    }
                }}
            }
        }


        //transactionMode
        if(!cAccount.isChecked() && !cPaytm.isChecked() && !cCash.isChecked()){

        }
        else {
            if(!cAccount.isChecked()) {
                for (int i = 0; i < transactionList.size(); i++) {
                    if(transactionList.get(i) != null ){
                    if (transactionList.get(i).getTransactionModeOfPayment().equals("Account")) {
                        transactionList.set(i, null);
                    }
                }}

            }
            if(!cCash.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                    if(transactionList.get(i).getTransactionModeOfPayment().equals("Cash")){
                        transactionList.set(i, null);
                    }
                }}
            }
            if (!cPaytm.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                    if(transactionList.get(i).getTransactionModeOfPayment().equals("Paytm")){
                        transactionList.set(i, null);
                    }
                }}
            }
        }

        if(!cFood.isChecked() && !cHealth.isChecked() && !cLeisure.isChecked() && !cOther.isChecked() && !cTransportation.isChecked() && !cMoneyTransfer.isChecked() && !cCashback.isChecked() && !cSalary.isChecked()){

        }
        else {
            if(!cFood.isChecked()) {
                for (int i = 0; i < transactionList.size(); i++) {
                    if(transactionList.get(i) != null ){
                        if (transactionList.get(i).getTransactionCategory().equals("Food and Drinks")) {
                            transactionList.set(i, null);
                        }
                    }}

            }
            if(!cHealth.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                        if(transactionList.get(i).getTransactionCategory().equals("Health")){
                            transactionList.set(i, null);
                        }
                    }}
            }
            if (!cLeisure.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                        if(transactionList.get(i).getTransactionCategory().equals("Leisure")){
                            transactionList.set(i, null);
                        }
                    }}
            }
            if(!cOther.isChecked()) {
                for (int i = 0; i < transactionList.size(); i++) {
                    if(transactionList.get(i) != null ){
                        if (transactionList.get(i).getTransactionCategory().equals("Other")) {
                            transactionList.set(i, null);
                        }
                    }}

            }
            if(!cTransportation.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                        if(transactionList.get(i).getTransactionCategory().equals("Transportation")){
                            transactionList.set(i, null);
                        }
                    }}
            }
            if (!cMoneyTransfer.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                        if(transactionList.get(i).getTransactionCategory().equals("Money Transfer")){
                            transactionList.set(i, null);
                        }
                    }}
            }
            if(!cCashback.isChecked()) {
                for (int i = 0; i < transactionList.size(); i++) {
                    if(transactionList.get(i) != null ){
                        if (transactionList.get(i).getTransactionCategory().equals("Cashback")) {
                            transactionList.set(i, null);
                        }
                    }}

            }
            if(!cSalary.isChecked()) {
                for(int i = 0; i < transactionList.size(); i++){
                    if(transactionList.get(i) != null ){
                        if(transactionList.get(i).getTransactionModeOfPayment().equals("Salary")){
                            transactionList.set(i, null);
                        }
                    }}
            }
        }

        if(longDateFrom!=null && longDateTo!=null){
            for(int i = 0; i < transactionList.size(); i++){
                if(transactionList.get(i) != null ){
                    Date toDate = getZeroTimeDate(new Date(longDateTo));
                    Date fromDate =getZeroTimeDate( new Date(longDateFrom));
                    Date transactionDate = getZeroTimeDate(new Date(transactionList.get(i).transactionDate));

                    if(fromDate.compareTo(transactionDate)<=0 && transactionDate.compareTo(toDate)<=0 ){

                    }else{
                        transactionList.set(i, null);
                    }

                }}
        }


        newTransactionList.clear();
        for(int i = 0; i < transactionList.size(); i++){
            if(transactionList.get(i) != null ){
                newTransactionList.add(transactionList.get(i));
            }
        }

        ExpenseList customAdapter = new ExpenseList(getActivity(), newTransactionList);
        listView.setAdapter(customAdapter);

        longDateTo=null;
        longDateFrom=null;
        mBottomSheetDialog.dismiss();
    }

    private void openFilter() {
        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_filterby, null);

        typeButton = sheetView.findViewById(R.id.buttonType);
        typeButton.setOnClickListener(this);

        modeButton = sheetView.findViewById(R.id.buttonMode);
        modeButton.setOnClickListener(this);

        categoryButton = sheetView.findViewById(R.id.buttonCategory);
        categoryButton.setOnClickListener(this);

        dateRangeButton = sheetView.findViewById(R.id.buttonDateRange);
        dateRangeButton.setOnClickListener(this);

        cFood = sheetView.findViewById(R.id.foodAndDrinksCheckBox);
        cFood.setOnClickListener(this);

        cOther = sheetView.findViewById(R.id.otherCheckBox);
        cOther.setOnClickListener(this);

        cTransportation = sheetView.findViewById(R.id.transportationCheckBox);
        cTransportation.setOnClickListener(this);

        cHealth = sheetView.findViewById(R.id.healthCheckBox);
        cHealth.setOnClickListener(this);

        cLeisure = sheetView.findViewById(R.id.leisureCheckBox);
        cLeisure.setOnClickListener(this);

        cExpense = sheetView.findViewById(R.id.expenseCheckBox);
        cExpense.setOnClickListener(this);

        cIncome = sheetView.findViewById(R.id.incomeCheckBox);
        cIncome.setOnClickListener(this);

        cSalary = sheetView.findViewById(R.id.salaryCheckBox);
        cSalary.setOnClickListener(this);

        cCashback = sheetView.findViewById(R.id.cashbackCheckBox);
        cCashback.setOnClickListener(this);

        cMoneyTransfer = sheetView.findViewById(R.id.moneyTransferCheckBox);
        cMoneyTransfer.setOnClickListener(this);

        cAccount = sheetView.findViewById(R.id.accountCheckBox);
        cAccount.setOnClickListener(this);

        cCash = sheetView.findViewById(R.id.cashCheckBox);
        cCash.setOnClickListener(this);

        cPaytm = sheetView.findViewById(R.id.paytmCheckBox);
        cPaytm.setOnClickListener(this);

        typeLayout = sheetView.findViewById(R.id.typeLayout);
        typeLayout.setOnClickListener(this);

        modeLayout = sheetView.findViewById(R.id.ModeLayout);
        modeLayout.setOnClickListener(this);

        categoryLayout = sheetView.findViewById(R.id.categoryLayout);
        categoryLayout.setOnClickListener(this);

        dateRangeLayout = sheetView.findViewById(R.id.dateRangeLayout);
        dateRangeLayout.setOnClickListener(this);

        clearAction = sheetView.findViewById(R.id.clearAllAction);
        clearAction.setOnClickListener(this);

        cancelAction = sheetView.findViewById(R.id.cancelAction);
        cancelAction.setOnClickListener(this);

        applyAction = sheetView.findViewById(R.id.applyAction);
        applyAction.setOnClickListener(this);

        fromDate = sheetView.findViewById(R.id.dateFrom);
        fromDate.setOnClickListener(this);

        toDate = sheetView.findViewById(R.id.dateTo);
        toDate.setOnClickListener(this);

        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        mDateSetListener1,
                        year,month,day);
                // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month;
                Calendar cal = Calendar.getInstance();
                cal.set(year,month,day);
                longDateFrom = cal.getTimeInMillis();

                dateString = year + "-" + (month+1) + "-" + day;
                fromDate.setText(dateString);
            }
        };

        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog dialog = new DatePickerDialog(
                        getContext(),
                        mDateSetListener2,
                        year,month,day);
                // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month;
                Calendar cal = Calendar.getInstance();
                cal.set(year,month,day);
                longDateTo = cal.getTimeInMillis();

                dateString = year + "-" + (month+1) + "-" + day;
                toDate.setText(dateString);
            }
        };

        typeLayout.setVisibility(View.VISIBLE);
        modeLayout.setVisibility(View.INVISIBLE);
        categoryLayout.setVisibility(View.INVISIBLE);
        dateRangeLayout.setVisibility(View.INVISIBLE);

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class MyActionModeCallback implements ActionMode.Callback{

        int position;
        View view;

        public MyActionModeCallback(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()){
                case R.id.delete:

                    System.out.println("Customadapter:" +customAdapter.getCount());
                    updateBalance(customAdapter.getItem(position));
                    deleteTransaction(customAdapter.getItem(position).getTransactionId());
                    customAdapter.remove(customAdapter.getItem(position));
                    mode.finish();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            view.setSelected(false);
        }
    }

    private void updateBalance(final Transaction item) {




        databaseBalances.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                float currentAmount = 0;
                final float deletedAmount = item.getTransactionAmount();

                final String mode = item.getTransactionModeOfPayment();
                final TransactionType type = item.getTransactionType();
                String balanceType = "";

                balance = dataSnapshot.getValue(Balance.class);

                switch (mode){
                    case "Cash":
                        balanceType = "cash";
                        currentAmount = balance.cash;
                        break;

                    case "Account":
                        balanceType = "bankAccount";
                        currentAmount = balance.bankAccount;
                        break;

                    case "Paytm":
                        balanceType = "paytm";
                        currentAmount = balance.paytm;
                        break;
                }


              //  System.out.println(currentAmount + " , " + deletedAmount);
                if(type == TransactionType.Expense)
                    databaseBalances.child(balanceType).setValue(currentAmount + deletedAmount);
                else
                    databaseBalances.child(balanceType).setValue(currentAmount - deletedAmount);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        System.out.println(balance.cash);



    }

    class ExpenseList extends ArrayAdapter<Transaction> {

        private Activity context;
        private List<Transaction> transactionList;

        public ExpenseList(Activity context, List<Transaction> transactionList){
            super(context, R.layout.historylayout,transactionList);
            this.context = context;
            this.transactionList = transactionList;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = getLayoutInflater().inflate(R.layout.historylayout,null);

            TextView dateView= view.findViewById(R.id.date);
            TextView categoryView= view.findViewById(R.id.category);
            TextView amountView= view.findViewById(R.id.amount);
            View typeView = view.findViewById(R.id.typeView);

            Transaction transaction = transactionList.get(position);

            String dateString = getStringDate(transaction);

            dateView.setText(dateString);
            categoryView.setText(transaction.getTransactionModeOfPayment());
            amountView.setText(Float.toString(transaction.getTransactionAmount()));

            if (transaction.getTransactionType() == TransactionType.Expense){
                typeView.setBackgroundResource(color[0]);
                amountView.setTextColor(Color.parseColor("#f46e6e"));
            }
            else if(transaction.getTransactionType() == TransactionType.Income){
                typeView.setBackgroundResource(color[1]);
                amountView.setTextColor(Color.parseColor("#a7cb4b"));
            }
            return view;
        }
    }

    @NonNull
    public String getStringDate(Transaction transaction) {
        Date date = new Date(transaction.getTransactionDate());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return year + "-" + (month+1) + "-" + day;
    }

}

