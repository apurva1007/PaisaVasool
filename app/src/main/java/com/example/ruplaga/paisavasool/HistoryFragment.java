package com.example.ruplaga.paisavasool;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    public HistoryFragment() {
        // Required empty public constructor
    }

    private OnFragmentInteractionListener mListener;
    ListView listView;
    TextView sort, filter;
    LinearLayout account_filter, paytm_filter, cash_filter;
    BottomSheetDialog mBottomSheetDialog;

    String filterby, sortby;

    List<Transaction> transactionList = new ArrayList<>();

    DatabaseReference databaseExpenses = null;

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
        sort = view.findViewById(R.id.sort);
        sort.setOnClickListener(this);

        filter = view.findViewById(R.id.filter);
        filter.setOnClickListener(this);



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        databaseExpenses = FirebaseDatabase.getInstance().getReference().child(userId).child("transactions");
        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        databaseExpenses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();
                for(DataSnapshot expenseSnapshot : dataSnapshot.getChildren()){
                    Transaction expense = expenseSnapshot.getValue(Transaction.class);
                    transactionList.add(expense);
                }

                ExpenseList customAdapter = new ExpenseList(getActivity(), transactionList);
                listView.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter:
                getDataForFilter();
                break;

            case R.id.sort:
                sort();
                break;

            case R.id.bottom_sheet_account:
                filterby = "Account";
                doFilter(filterby);
                break;

            case R.id.bottom_sheet_paytm:
                filterby = "Paytm";
                doFilter(filterby);
                break;

            case R.id.bottom_sheet_cash:
                filterby = "Cash";
                doFilter(filterby);
                break;

        }
    }

    private void doFilter(String filterby) {
        Query query = databaseExpenses.orderByChild("transactionModeOfPayment").equalTo(filterby);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();
                for(DataSnapshot expenseSnapshot : dataSnapshot.getChildren()){
                    Transaction expense = expenseSnapshot.getValue(Transaction.class);
                    transactionList.add(expense);
                }

                ExpenseList customAdapter = new ExpenseList(getActivity(), transactionList);
                listView.setAdapter(customAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBottomSheetDialog.dismiss();

    }

    private void sort() {

    }

    private void getDataForFilter() {
        mBottomSheetDialog = new BottomSheetDialog(getActivity());
        View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_filterby, null);

        account_filter = sheetView.findViewById(R.id.bottom_sheet_account);
        account_filter.setOnClickListener(this);

        paytm_filter = sheetView.findViewById(R.id.bottom_sheet_paytm);
        paytm_filter.setOnClickListener(this);

        cash_filter = sheetView.findViewById(R.id.bottom_sheet_cash);
        cash_filter.setOnClickListener(this);

        mBottomSheetDialog.setContentView(sheetView);
        mBottomSheetDialog.show();

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

            Date date = new Date(transaction.getTransactionDate());
            int year = date.getYear()+1900;
            int month = date.getMonth();
            int day = date.getDay();

            dateView.setText(year + "-" + month + "-" + day);
            categoryView.setText(transaction.getTransactionCategory());
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

}

