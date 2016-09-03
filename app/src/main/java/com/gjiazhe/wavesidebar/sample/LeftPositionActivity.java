package com.gjiazhe.wavesidebar.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.gjiazhe.wavesidebar.WaveSideBar;

import java.util.ArrayList;

public class LeftPositionActivity extends AppCompatActivity {
    private RecyclerView rvContacts;
    private WaveSideBar sideBar;

    private ArrayList<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_contacts);
        rvContacts = (RecyclerView) findViewById(R.id.rv_contacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(new ContactsAdapter(contacts, R.layout.item_contacts_right));

        sideBar = (WaveSideBar) findViewById(R.id.side_bar);
        sideBar.setPosition(WaveSideBar.POSITION_LEFT);
        sideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String index) {
                for (int i=0; i<contacts.size(); i++) {
                    if (contacts.get(i).getIndex().equals(index)) {
                        ((LinearLayoutManager) rvContacts.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });
    }

    private void initData() {
        contacts.addAll(Contact.getChineseContacts());
    }
}
